import java.util.ArrayList;

public class Child {
	public NeuralNetwork NN;
	public Cell cell;
	
	public Child(Cell cell) {
		this.cell = cell;
		NN = new NeuralNetwork(new int[] {1,2,3});
	}
}

class NeuralNetwork {
	private int inputSize, hiddenSize, outputSize;
	
	public NeuralNetwork(int[] hyperParams) {
		inputSize = hyperParams[0];
		hiddenSize = hyperParams[1];
		outputSize = hyperParams[2];
	}
	
	private double nonlinear(double x) {
		 x = Math.min(Math.max(x, NonlinearSample.MIN_X), NonlinearSample.MAX_X); // Clips x into domain of samples.
		 x -= NonlinearSample.MIN_X; // Shifts x to align minX value with 0.
		 x = Math.round(x / NonlinearSample.RANGE); // Rounds to nearest index value.
		 
		 return NonlinearSample.values[(int) x];
	}
	
	public ArrayList<Double> forward(ArrayList<Double> input) {
		/*
		 * Forward:
		 * dim(input) = inputSize
		 * dim(w1) =  (inputSize, hiddenSize)
		 * dim(w2) = (hiddenSize, outputSize)
		 * dim(output) = outputSize
		 * 
		 * z1 = matrixMultiply(w1, input);
		 * z2 = nonlinear(z1);
		 * z3 = matrixMultiply(w2, z2);
		 * output = nonlinear(z3);
		 * 
		 * return output;
		 */
		
		ArrayList<Double> sampleOutput = new ArrayList<Double>();
		for (int n = 0; n < 9; n++) {
			sampleOutput.add((double)1);
		}

		return sampleOutput;
	}
	
	static class NonlinearSample {
		public static final double MIN_X = -10;
		public static final double MAX_X = 10;
		public static final double NUM_STEPS = 1000;
		public static final double RANGE = MAX_X - MIN_X;
		public static final double STEP_SIZE = RANGE / NUM_STEPS;
		public static double[] values;
		
		public NonlinearSample() {
			int i = 0;
			for (double xSample = 0; xSample < MAX_X; xSample += STEP_SIZE) {
				values[i] = 1 / (1 + Math.exp(-xSample));
				i++;
			}
		}
	}
}