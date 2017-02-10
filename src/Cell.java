import java.util.HashMap;

public class Cell {
	public HashMap<Agent, Double> influence = new HashMap<Agent, Double>();
	public double influencePerTurn;
	public boolean needsUpdate = false;
	
	public int[] position;

	public Cell(int[] position, double influencePerTurn) {
		this.position = position;
		this.influencePerTurn = influencePerTurn;
	}
}