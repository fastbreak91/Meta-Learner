package handwriting.learners;

public class MultiLayer extends PerceptronNet {
    private Perceptron inputToHidden, hiddenToOutput;
    
    // Invariant: 
    //   inputToHidden.numOutputNodes() == hiddenToOutput.numInputNodes()
    
    public int numInputNodes() {return inputToHidden.numInputNodes();}
    public int numHiddenNodes() {return inputToHidden.numOutputNodes();}
    public int numOutputNodes() {return hiddenToOutput.numOutputNodes();}
    
    public Perceptron getHiddenLayer() {return inputToHidden;}
    public Perceptron getOutputLayer() {return hiddenToOutput;}
    
    public MultiLayer(int numIn, int numHid, int numOut) {
        this(new Perceptron(numIn, numHid), new Perceptron(numHid, numOut));
    }
    
    // Pre: hidden.numOutputNodes() == output.numInputNodes()
    public MultiLayer(Perceptron hidden, Perceptron output) {
        inputToHidden = hidden;
        hiddenToOutput = output;
    }
    
    // Pre: inputs.length = numInputNodes()
    // Post: Returns value of output nodes
    public double[] compute(double[] inputs) {
        return hiddenToOutput.compute(inputToHidden.compute(inputs));
    }
    
    // Pre: train() has been called some number of times
    // Post: Weights are updated for one training cycle
    //       The incremental deltas are reset to zero for next cycle
    public void updateWeights() {
        inputToHidden.updateWeights();
        hiddenToOutput.updateWeights();
    }
    
    // Pre: getOutputLayer() has just had its weights and errors changed
    // Post: Backpropagates the weight changes and errors to getHiddenLayer()
    protected void backpropagate(double[] inputs, double rate) {
    	for(int i = 0; i < numHiddenNodes(); i++){
    		double error = 0.0;
    		for(int i2 = 0; i2 < numOutputNodes(); i2++){
    			error = error + hiddenToOutput.weight(i, i2)*hiddenToOutput.error(i2)*gradient(inputToHidden.output(i));}
    		inputToHidden.setError(i, error);
    	}
    	inputToHidden.addToWeightDeltas(inputs, rate);
    }

    // Pre: inputs.length = numInputNodes()
    //      targets.length = numOutputNodes()
    //      0 < rate <= 1.0
    // Post: Accumulates deltas for the given training pair following the
    //       backpropagation learning rule
    public void train(double[] inputs, double[] targets, double rate) {
        hiddenToOutput.train(getHiddenLayer().compute(inputs), targets, rate);
        backpropagate(inputs, rate);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Input to Hidden\n");
        sb.append(inputToHidden);
        sb.append("\nHidden to Output\n");
        sb.append(hiddenToOutput);
        return sb.toString();
    }
}
