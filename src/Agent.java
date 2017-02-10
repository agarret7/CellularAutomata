import java.util.ArrayList;
import java.util.Random;

public class Agent {
	Random rand = new Random();
	public float[] color = {rand.nextFloat(), rand.nextFloat(), rand.nextFloat()};
	
	ArrayList<Child> children = new ArrayList<Child>();
	
	public Agent(Cell initialCell) {
		children.add(new Child(initialCell));
		initialCell.influence.put(this, 0.5);
	}
}

/*
* General Structure:
*   - 2D grid of cells with a set of resource gains per turn.
*   - Randomly seed single allied cells with 10*baseline influence.
*   - Each allied cell or "agent" has an identical neural network for each of its cells.
*   - The set of weights of these neural networks in bit form are the "DNA" of the agent.
*   - The fitness function for this agent is the number of controlled cells.
*   - If the agent dies, two agents will be probabilistically selected (weighted by fitness) to generate
*     a new agent. The agent will inherit a crossover of "genes", which are segments of the weights split
*     into random lengths between a minimum and maximum value, with mutationRate = 1/maxFitness random mutations.
* 
* Graphics:
*   - Color owned cells on a gradient from white to a random color linearly related to influence.
*   - Draw a partially transparent gray dot that's linearly related to the influence gain per turn.
*   
* Cell:
* 	Attributes:
* 	  - Influence (has a cap at a certain high value)
*     - Ownership (must have influence of one player greater than baseline)
* 	  - Resource gain per turn
* 	Update:
*     - Add everyone's influence to their influence pools on the cell.
*     - If the cell is owned, in addition to incoming influence, add influence generated by the cell.
*     - Take the lowest player's influence, and for every player with influence greater than ours,
*       subtract our influence from their influence, and set our influence to zero.
*     - If the player with the greatest influence has influence greater than baseline, the cell is theirs.
*     - Else the square becomes neutral.
*     - Decay the owning player's influence by decayRate.
*     
* Allied Cell:
* 	Attributes:
*     - Neural Network that has the following inputs:
*     Inputs:
*       - Adjacent tiles enemy influence (0 if baseline, and linearly related)
*       - Adjacent tiles friendly influence (0 if baseline, and linearly related)
*       - Adjacent tiles resource gain per turn
*       - This cell's current influence (0 if baseline, and linearly related)
*     Outputs:
*       - Proportional influence flow (8 directions + 1 keep influence = 9 outputs)
*         influenceFlow[n] = (flowNeuronOutput[n] / sum(flowNeuronOutput)) * totalInfluence
*/