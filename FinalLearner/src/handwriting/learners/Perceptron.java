package handwriting.learners;

public class Perceptron extends PerceptronNet {
    // First index is the input; second index is the output
    private double[][] weights;
    private double[][] deltas;
    private double[] outputs;
    private double[] errors;
    
    private int numInputs, numOutputs;
    
    protected double output(int i) {return outputs[i];}
    protected double error(int i) {return errors[i];}
    protected double weight(int i, int j) {return weights[i][j];}
    
    // Post: error(i) == error
    protected void setError(int i, double error) {errors[i] = error;}
    
    public int numInputNodes() {return numInputs;}
    public int numOutputNodes() {return numOutputs;}
    public int threshold() {return numInputNodes();}
    
    // returns number of inputs including the threshold
    public int numInputPaths() {return numInputNodes() + 1;}
    
    public Perceptron(int numIn, int numOut) {
        numInputs = numIn;
        numOutputs = numOut;
        
        initArrays();
        
        for (int j = 0; j < numInputPaths(); ++j) {
            for (int i = 0; i < numOutputNodes(); ++i) {
            	weights[j][i] = (Math.random() * 2.0) - 1.0;
                deltas[j][i] = 0.0;
            }
        }
        
        for (int i = 0; i < numOutputNodes(); ++i) {
            outputs[i] = 0.0;
            errors[i] = 0.0;
            weights[threshold()][i] = 0.0;
        }
    }
    
    // Creates a new Perceptron with the same weights as other
    public Perceptron(Perceptron other) {
        numInputs = other.numInputNodes();
        numOutputs = other.numOutputNodes();
        
        initArrays();
        
        for (int j = 0; j < numInputPaths(); ++j) {
            for (int i = 0; i < numOutputNodes(); ++i) {
                weights[j][i] = other.weights[j][i];
                deltas[j][i] = 0.0;
            }
        }
        
        for (int i = 0; i < numOutputNodes(); ++i) {
            outputs[i] = 0.0;
            errors[i] = 0.0;
            weights[threshold()][i] = 0.0;
        }
    }
    
    private void initArrays() {
        weights = new double[numInputPaths()][numOutputNodes()];
        deltas = new double[numInputPaths()][numOutputNodes()];
        outputs = new double[numOutputNodes()];
        errors = new double[numOutputNodes()];
    }
    
    public double getWeightFromTo(int inputNode, int outputNode) {
        return weights[inputNode][outputNode];
    }
    
    public void setWeightFromTo(double w, int inputNode, int outputNode) {
        weights[inputNode][outputNode] = w;
    }
    
    public void setThresholdWeight(double w, int outputNode) {
        weights[threshold()][outputNode] = w;
    }
    
    // Pre: inputs.length = numInputNodes()
    // Post: Returns values of output nodes
    
    
    public double[] compute(double[] inputs) {
    	for(int i = 0;i < numOutputNodes();i++){
    		double sum = 0.0;
    		for(int i2 = 0;i2< numInputNodes();i2++){
    			sum = sum + inputs[i2]*weights[i2][i];
    		}
    		sum = sum - weights[threshold()][i];
    		outputs[i] = sigmoid(sum);
    	}
        return outputs;
    }
    
 
    public void addToWeightDeltas(double[] inputs, double rate) {
    	for(int i = 0; i < numOutputNodes() ; i++){
    		double error = errors[i]*gradient(outputs[i])*rate;
    		for(int i2= 0; i2 < numInputNodes(); i2++){
    			deltas[i2][i] = error*inputs[i2];
    		}
    		deltas[threshold()][i]= deltas[threshold()][i] - error;
    	}
    	updateWeights();
    }

    public void updateWeights() {
        for (int j = 0; j < numInputPaths(); ++j) {
            for (int i = 0; i < numOutputNodes(); ++i) {
                weights[j][i] += deltas[j][i];
                deltas[j][i] = 0.0;
            }
        }
    }
    
    // Pre: inputs.length = numInputNodes(); 
    //      targets.length = numOutputNodes();
    //      0 < rate <= 1.0
    // Post: Computes delta updates from perceptron learning rule
    public void train(double[] inputs, double[] targets, double rate) {
        compute(inputs);
        for (int i = 0; i < numOutputNodes(); ++i) {
            setError(i, targets[i] - output(i));
        }
        addToWeightDeltas(inputs, rate);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numOutputNodes(); ++i) {
            sb.append("Perceptron output node ");
            sb.append(i);
            sb.append(" threshold: ");
            sb.append(weights[threshold()][i]);
            sb.append("\nIncoming weights:\n");
            for (int j = 0; j < numInputNodes(); ++j) {
                sb.append(weights[j][i]);
                sb.append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
