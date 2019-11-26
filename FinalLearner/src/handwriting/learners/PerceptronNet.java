package handwriting.learners;

abstract public class PerceptronNet {
	abstract public double[] compute(double[] inputs);
	abstract public void train(double[] inputs, double[] targets, double rate);
	abstract public void updateWeights();
	
	public void trainN(double[][] inputs, double[][] targets, int iterations, double rate) {
		for (int i = 0; i < iterations; ++i) {
			for (int j = 0; j < inputs.length; ++j) {
				train(inputs[j], targets[j], rate);
			}
			updateWeights();
		}
	}
			
    public static double gradient(double fOfX) {
        return fOfX * (1.0 - fOfX);
    }
    
    // Pre: none
    // Post: Returns sigmoid function of input
    public static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
}
