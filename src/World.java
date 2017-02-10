import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

//Got to change all the references influence makes to an agent to the allyid instead.

public class World {
	public static final int[][] directions = {{1,0}, {1,1}, {0,1}, {-1,1}, {-1,0}, {-1,-1}, {0,-1}, {1,-1}};
	public static final int X_NUM_CELLS = 100;
	public static final int Y_NUM_CELLS = 100;
	public static final int PIXELS_PER_CELL = 5;
	public static final int AREA_WIDTH = X_NUM_CELLS * PIXELS_PER_CELL;
	public static final int AREA_HEIGHT = Y_NUM_CELLS * PIXELS_PER_CELL;
	public static final int NUM_AGENTS = 10;
	
	public Random rand = new Random();

	public ArrayList<ArrayList<Cell>> cells = new ArrayList<ArrayList<Cell>>();
	public ArrayList<ArrayList<Cell>> openCells = new ArrayList<ArrayList<Cell>>();
	public ArrayList<Agent> agentObjects;

	public World() {
		agentObjects = new ArrayList<Agent>();

		SimplexNoise simp = new SimplexNoise(349292351);
		for (int i = 0; i < X_NUM_CELLS; i++) {
			cells.add(i, new ArrayList<Cell>());
			for (int j = 0; j < Y_NUM_CELLS; j++) {
				cells.get(i).add(new Cell(new int[] {i, j}, Math.max(0,(-0.5 + simp.noise(i, j))/2)));
			}
		}
		
		generateAgents();
	}
	
	private void generateAgents() {
		while (agentObjects.size() < NUM_AGENTS) {
			Cell newAgentCell = cells.get(rand.nextInt(X_NUM_CELLS)).get(rand.nextInt(Y_NUM_CELLS));
			
			boolean checking = true;
			int numCellsTried = 0;
			
			check:
			while (checking) {
				for (Agent agent : agentObjects) {
					for (Child child : agent.children) {
						if (child.cell == newAgentCell | newAgentCell.influencePerTurn < 0.25 / 100000) {
							newAgentCell = cells.get(rand.nextInt(X_NUM_CELLS)).get(rand.nextInt(Y_NUM_CELLS));
							checking = true;
							numCellsTried++;
							continue check;
						} else if (numCellsTried > 2 * X_NUM_CELLS * Y_NUM_CELLS) {
							break;
						}
					}
				}
				checking = false;
			}
			
			makeAgent(newAgentCell);
		}
	}

	private void makeAgent(Cell initCell) {
		Agent newAgent = new Agent(initCell);
		agentObjects.add(newAgent);
		for (ArrayList<Cell> row : cells) {
			for (Cell cell : row) {
				if (newAgent.children.get(0).cell != cell) {
					cell.influence.put(newAgent, (double) 0);
				}
			}
		}
	}

	private void deleteAgent(Agent agent) {
		for (ArrayList<Cell> row : cells) {
			for (Cell cell : row) {
				cell.influence.remove(agent);
			}
		}
		agentObjects.remove(agent);
		agent = null;
	}

	public void update() {
		
		for (Agent agent : agentObjects) {
			//System.out.println(agent.children.size());
			for (Child child : agent.children) {
				ArrayList<Double> input = new ArrayList<Double>();
	
				input.add(child.cell.influence.get(agent));
	
				for (int i = 0; i < directions.length; i++) {
					Cell targetCell = cells.get(Math.floorMod(child.cell.position[0] + directions[i][0], X_NUM_CELLS))
										   .get(Math.floorMod(child.cell.position[1] + directions[i][1], Y_NUM_CELLS));
					double alliedInfluence = 0;
					double enemyInfluence = 0;
	
					for (Agent targetAgent : targetCell.influence.keySet()) {
						if (agent == targetAgent) {
							alliedInfluence += targetCell.influence.get(targetAgent);
						} else {
							enemyInfluence += targetCell.influence.get(targetAgent);
						}
					}
	
					double influencePerTurn = targetCell.influencePerTurn;
	
					input.add(alliedInfluence);
					input.add(enemyInfluence);
					input.add(influencePerTurn);
				}
	
				ArrayList<Double> behavior = child.NN.forward(input);
	
				double totalFlowActivity = behavior.stream().mapToDouble(Double::doubleValue).sum(); // Replace this with a vector that has sums.
				
				ArrayList<Double> pushValues = new ArrayList<Double>();
				
				for (int i = 0; i < directions.length; i++) {
					pushValues.add((behavior.get(i) / totalFlowActivity) * child.cell.influence.get(agent));
				}
				
				for (int i = 0; i < directions.length; i++) {
					pushInfluence(agent, child, directions[i], pushValues.get(i));
				}
	
				double newInfluence = child.cell.influence.get(agent) + child.cell.influencePerTurn;
				child.cell.influence.put(agent, newInfluence);
			}
		}

		for (ArrayList<Cell> row : cells) {
			for (Cell cell : row) {
				if (cell.needsUpdate) {
					// For every cell, if our cell needs updating (got pushed influence)...
					// Find the max value of the cell's influence.
					double maxValue = Collections.max(cell.influence.values());
					// Copy the influence, and remove the max value.
					List<Double> copyInfluence = new ArrayList<Double>(cell.influence.values());
					copyInfluence.remove(maxValue);
					
					// Find the second largest value, if it exists. If not, second largest is 0.
					// (Largest is guaranteed to be found because loop runs only if agent pushed
					// influence to this target cell.)
					double secondMaxValue;
					try {
						secondMaxValue = Collections.max(copyInfluence);
					} catch (Exception e) {
						secondMaxValue = 0;
					}

					boolean maxFound = false;

					for (Agent agent : cell.influence.keySet()) {
						if (cell.influence.get(agent) == maxValue) {
							// Let the first max found be the only max. (I know this is cheating.)
							// Subtract second largest influence.
							cell.influence.put(agent, maxValue - secondMaxValue);
							maxFound = true;
						} else if (maxFound) {
							// Everyone else now has zero influence.
							cell.influence.put(agent, (double) 0);
						}
					}
					
					for (Agent agent : cell.influence.keySet()) {
						if (cell.influence.get(agent) >= 0.1) {
							boolean cellAlreadyInAgent = false;
							for (Child child : agent.children) {
								if (child.cell == cell) {
									cellAlreadyInAgent = true;
									break;
								}
							}
							
							if (!cellAlreadyInAgent) {
								agent.children.add(new Child(cell));
							}
						}
					}
					
					cell.needsUpdate = false;
				}
			}
		}
		
		ArrayList<Agent> removeAgent = new ArrayList<Agent>();

		for (Agent agent : agentObjects) {
			ArrayList<Child> removeChildren = new ArrayList<Child>();
			for (Child child : agent.children) {
				if (child.cell.influence.get(agent) < 0.1) {
					removeChildren.add(child);
				}
			}
			
			for (Child child : removeChildren) {
				agent.children.remove(child);
				child = null;
			}
			
			if (agent.children.isEmpty()) {
				removeAgent.add(agent);
			}
		}
		
		for (Agent agent : removeAgent) {
			deleteAgent(agent);
		}
		
		if (agentObjects.size() == 1) {
			deleteAgent(agentObjects.get(0));
			generateAgents();
		}
	}

	private void pushInfluence(Agent givingAgent, Child givingChild, int[] direction, double amount) {
		Cell givingCell = givingChild.cell;
		Cell receivingCell = cells.get(Math.floorMod(givingCell.position[0] + direction[0], X_NUM_CELLS))
								  .get(Math.floorMod(givingCell.position[1] + direction[1], Y_NUM_CELLS));

		double givingCellInitInfluence = givingCell.influence.get(givingAgent);
		double receivingCellInitInfluence = receivingCell.influence.get(givingAgent);

		givingCell.influence.put(givingAgent, givingCellInitInfluence - amount);
		receivingCell.influence.put(givingAgent, receivingCellInitInfluence + amount);

		givingChild.cell.needsUpdate = true;
		receivingCell.needsUpdate = true;
	}
}