package handwriting.learners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import handwriting.core.*;
import handwriting.learners.GrowingNeuralGas.Neuron;
import handwriting.learners.SelfOrgMap.SelfOrgMapNeuron;

public class SuperLearner extends RecognizerAI {
	private DecisionTreeNode root;
	private HashMap<Drawing, Double> drawingweights;
	private HashMap<DecisionTreeNode, Double> treeweights;
	private HashSet<String> AllLabelsV;
	private ArrayList<String> OneEachLabel;
	private SampleData data;
	private HashMap<MultiLayer, String> resultMap;
	private double[] dweights;
	private double[][] inputweights;
	private double[][] goalweights;
	private SelfOrgMap SOM;
	private int SOMFrequency[][][];
	private GrowingNeuralGas GNG;
	private int GNGFrequency[][];
	private HashMap<Neuron,Integer> NodeValues;
	
	//boosted trees parameter
	private int numBoostedTrees = 20;
	//perceptron parameters
	private int perceptronHiddenNodes = 15;
	private int perceptronIterations = 100;
	private double perceptronLearningRate = .3;
	//SOM parameters
	private int SOMGridSize = 5;
	private int SOMiterations = 35;
	//GNG parameters
	private int GNGiterations = 3;
	private double maxTolerableDistance = 35.0;
	private double k = 1250.0;
	
	
	public SuperLearner(SampleData data){
		this.data = data;
		//boosted trees constructor
		root = makeTree(data);
		drawingweights = new  HashMap<Drawing, Double>();
		treeweights = new  HashMap<DecisionTreeNode, Double>();
		if (data.numDrawings()==0){return;}
		startDrawingWeights(data);
		generateTrees(data);
		
		//perceptron constructor (includes training)
		resultMap = new HashMap<MultiLayer, String>();
		AllLabelsV = new HashSet<String>();
		for(String label: data.allLabels()){AllLabelsV.add(label);}
		OneEachLabel = new ArrayList<String>(AllLabelsV);
		for(int i = 0 ; i < OneEachLabel.size(); i++){
			resultMap.put(new MultiLayer(400, perceptronHiddenNodes ,1), OneEachLabel.get(i));
		}
		inputweights = new double[data.numDrawings()][];
		for(int i = 0; i < data.numDrawings(); i++){
			inputweights[i] = new double[400];
			Drawing drawing = data.getDrawing(i);
			int pixelindex = 0;
			for(int x = 0; x < drawing.getWidth(); x++){
				for(int y = 0; y < drawing.getHeight(); y++ ){
					if(drawing.isSet(x, y)){
						inputweights[i][pixelindex] = 1.0;
					}
					else{
						inputweights[i][pixelindex] = 0.0;
					}
					pixelindex++;
				}
			}
		}
		for(MultiLayer Layer : resultMap.keySet()){
			goalweights = new double[data.numDrawings()][];
			for(int i= 0; i < data.numDrawings(); i++){
				goalweights[i] = new double[1];
				if(resultMap.get(Layer) == data.getLabelFor(i)){
					goalweights[i][0] = 1.0;
				}
				else{
					goalweights[i][0] =0.0;
				}
			}
			Layer.trainN(inputweights,goalweights, perceptronIterations, perceptronLearningRate);
		}
		
	//SOM constructor (includes training)
		SOMFrequency = new int[data.numLabels()][SOMGridSize][SOMGridSize];
		this.SOM = new SelfOrgMap(SOMGridSize, SOMiterations, data); //Second number is iterations
		int count = 0;
		for (String label: data.allLabels()) {
			int labelValue = count;
			count += 1;
			for (int i = 0; i < data.numDrawingsFor(label); ++i) {
				Drawing d = data.getDrawing(label, i);
				FloatDrawing FD = new FloatDrawing(d);
				SelfOrgMapNeuron winner = SOM.bestMatchFor(FD);
				SOMFrequency[labelValue][winner.getX()][winner.getY()] += 1;
			}
		}
		
	//GNG constructor (includes training)
		this.GNG = new GrowingNeuralGas(data, maxTolerableDistance, k);
		this.NodeValues = new HashMap<Neuron, Integer>();
		for(int i = 0; i < GNGiterations; ++i){
			GNG.trainOnce(data);
		}
		GNGFrequency = new int[data.numLabels()][GNG.numNodes()];
		Integer Labelcount = 0;
		Integer Nodecount = 0;
		
		
		Iterator<Neuron> GraphIterator = GNG.iterator();
		while(GraphIterator.hasNext()){
			Neuron ThisNeuron = GraphIterator.next();
			NodeValues.put(ThisNeuron, Nodecount);
			Nodecount += 1;
		}
		
		for (int b = 0; b<data.numLabels();++b) {
			int labelValue = Labelcount;
			Labelcount += 1;
			for (int l = 0; l<GNG.numNodes();++l){
				GNGFrequency[labelValue][l] = 0;
			}
		}
		
		Labelcount = 0;
		for (String label: data.allLabels()) {
			Integer ThislabelValue = Labelcount;
			Labelcount += 1;
			for (int i = 0; i < data.numDrawingsFor(label); ++i) {
				Drawing d = data.getDrawing(label, i);
				FloatDrawing FD = new FloatDrawing(d);
				Neuron winner = GNG.bestMatchFor(FD);
				GNGFrequency[ThislabelValue][NodeValues.get(winner)] += 1;
			}
		}
	}
	
	
	//Decision Tree Training
	public DecisionTreeNode makeTree(SampleData data) {
		if (data.numDrawings() == 0){
			return null;
		}
		if (data.numLabels() == 1){
			String lab = data.getLabelFor(0);
			DecisionTreeNode result = new LeafNode().setLabel(lab);
			return result;
		}
		else{
			double bestgain = 0.0;
			int bestx = 0;
			int besty = 0;
			SampleData allOn = new SampleData();
			SampleData allOff = new SampleData();
			SampleData bestOn = new SampleData();
			SampleData bestOff = new SampleData();
			for(int x = 0; x < data.getDrawingWidth(); ++x){
				for(int y = 0; y < data.getDrawingHeight();++y){
					double gain = 0.000;
					allOn = getBestOn(x,y,data);
					allOff = getBestOff(x,y,data);
					if(allOn.numDrawings() != data.numDrawings() && allOff.numDrawings() != data.numDrawings()){
						double Hparent = Gini(data);
						double Hleft = Gini(allOn);
						double Hright = Gini(allOff);
						gain = Hparent - (Hleft + Hright);
						if (gain > bestgain){
							bestgain = gain;
							bestx = x;
							besty = y;
						}
					}
				}
			}
		bestOn = getBestOn(bestx, besty, data);
		bestOff = getBestOff(bestx, besty, data);
		return new InnerNode(bestx,besty,makeTree(bestOn),makeTree(bestOff));}}
	
	
	//Decision and Boosted both use these helper methods	
	public SampleData getBestOn(int x, int y, SampleData data){
		SampleData BestOn = new SampleData();
		for (String label: data.allLabels()) {
			for (int i = 0; i < data.numDrawingsFor(label); ++i) {
				if (data.getDrawing(label, i).isSet(x, y)){
					BestOn.addDrawing(label, data.getDrawing(label, i));
				}
			}
		}
		return BestOn;
	}
	
	public SampleData getBestOff(int x, int y, SampleData data){
		SampleData BestOff = new SampleData();
		for (String label: data.allLabels()) {
			for (int i = 0; i < data.numDrawingsFor(label); ++i) {
				if (!data.getDrawing(label, i).isSet(x, y)){
					BestOff.addDrawing(label, data.getDrawing(label, i));
				}
			}
		}
		return BestOff;
	}
	
	public double Gini(SampleData data){
		double portionsum = 0.0;
		double portion = 0.0;
		double numdrawings = data.numDrawings();
		for (String label: data.allLabels()) {
			double drawforlabel = data.numDrawingsFor(label);
			portion = drawforlabel/numdrawings;
			portionsum += portion*portion;
		}
		double result = 1 - portionsum;
		return result;
	}
	
	//Boosted Trees Training
	
	public void generateTrees(SampleData data){
		int numTrees = numBoostedTrees;
		for (int i=0;i<numTrees;i=i+1){
			double error = 0;
			DecisionTreeNode tree = null;
			while (0 == error || error > .5){
				SampleData onethird = generateThirdofData(data);
				tree = makeTree(onethird);
				error = calcError(tree, data);
			}
			double weightsum = 0;
			for (String label:data.allLabels()){
				for (int i2=0;i2<data.numDrawingsFor(label);i2=i2+1){
					Drawing drawing = data.getDrawing(label, i2);
					double weight = drawingweights.get(drawing)*(error/(1-error));
					if(classifyDraw(tree, drawing) == label){
						drawingweights.put(drawing, weight);
					}
					weightsum = weightsum + weight;
				}
			}
			for (int i3=0;i3<data.numDrawings();i3=i3+1){
				Drawing drawing = data.getDrawing(i3);
				double weight = drawingweights.get(drawing)/weightsum;
				drawingweights.remove(drawing);
				drawingweights.put(drawing, weight);
			}
			treeweights.put(tree, Math.log((1-error)/error));
		}
	}
	
	private double calcError(DecisionTreeNode tree, SampleData data) {
		double result = 0;
		for (String label: data.allLabels()) {
			for (int i = 0; i < data.numDrawingsFor(label); ++i) {
				Drawing drawing = data.getDrawing(label, i);
				if (!classifyDraw(tree, drawing).equals(label)){
					result = result + drawingweights.get(drawing);
				}
			}
		}
		return result;
	}

	private SampleData generateThirdofData(SampleData data) {
		SampleData result = new SampleData();
		for (int i=0;i<data.numDrawings();i=i+3){
			Random generator = new Random();
			int randomnum = generator.nextInt(data.numDrawings());
			Drawing randomdrawing = data.getDrawing(randomnum);
			result.addDrawing(data.getLabelFor(randomnum), randomdrawing);
		}
		return result;
	}
	
	public void startDrawingWeights(SampleData data){
		double numdrawings = data.numDrawings();
		for (int i = 0; i < data.numDrawings(); i=i+1){
			drawingweights.put(data.getDrawing(i), (1.00/numdrawings));
		}
	}
	
	public String classifyDraw(DecisionTreeNode tree, Drawing d) {
		DecisionTreeNode node = tree;
		while (node.getPixelx() != -1 && node.getPixely() != -1){
			if (d.isSet(node.getPixelx(), node.getPixely())){
				node = node.leftChild();
			}
			else{
				node=node.rightChild();
			}
		}
		return node.getLabel();
	}
	


//Testing Begins
	public String classify(Drawing d) {
		String DecisionTreeLabel = getDecisionTreeLabel(d);
		String BoostedTreesLabel = getBoostedTreesLabel(d);
		String PerceptronLabel = getPerceptronLabel(d);
		String SOMLabel = getSOMLabel(d);
		String GNGLabel = getGNGLabel(d);
		HashMap<String,Integer> Votes = new  HashMap<String, Integer>();
		if(Votes.containsKey(DecisionTreeLabel)){
			int frequency = Votes.get(DecisionTreeLabel);
			Votes.remove(DecisionTreeLabel);
			Votes.put(DecisionTreeLabel, frequency + 1);
		}
		else{
			Votes.put(DecisionTreeLabel, 1);
		}
		if(Votes.containsKey(BoostedTreesLabel)){
			int frequency = Votes.get(BoostedTreesLabel);
			Votes.remove(BoostedTreesLabel);
			Votes.put(BoostedTreesLabel, frequency + 1);
		}
		else{
			Votes.put(BoostedTreesLabel, 1);
		}
		if(Votes.containsKey(PerceptronLabel)){
			int frequency = Votes.get(PerceptronLabel);
			Votes.remove(PerceptronLabel);
			Votes.put(PerceptronLabel, frequency + 1);
		}
		else{
			Votes.put(PerceptronLabel, 1);
		}
		if(Votes.containsKey(SOMLabel)){
			int frequency = Votes.get(SOMLabel);
			Votes.remove(SOMLabel);
			Votes.put(SOMLabel, frequency + 1);
		}
		else{
			Votes.put(SOMLabel, 1);
		}
		if(Votes.containsKey(GNGLabel)){
			int frequency = Votes.get(GNGLabel);
			Votes.remove(GNGLabel);
			Votes.put(GNGLabel, frequency + 1);
		}
		else{
			Votes.put(GNGLabel, 1);
		}
		
		String result = "unknown";
		int Majority = 0;
		for(String PossibleBest : Votes.keySet()){
			int Talley = Votes.get(PossibleBest);
			if (Talley == Majority){
				double coin = Math.random();
				if (coin < .5){
					result = PossibleBest;
				}
			}
			if (Talley > Majority){
				Majority = Talley;
				result = PossibleBest;
			}
		}

		//System.out.println(DecisionTreeLabel);
		//System.out.println(BoostedTreesLabel);
		//System.out.println(PerceptronLabel);
		//System.out.println(SOMLabel);
		//System.out.println(GNGLabel);
		
		return result;
	}

//Decision Tree Testing
	private String getDecisionTreeLabel(Drawing d) {
		DecisionTreeNode node = root;
		while (node.getPixelx() != -1 && node.getPixely() != -1){
			if (d.isSet(node.getPixelx(), node.getPixely())){
				node = node.leftChild();
			}
			else{
				node=node.rightChild();
			}
		}
		return node.getLabel();
	}
	
//Boosted Trees Testing
	private String getBoostedTreesLabel(Drawing d) {
		HashMap<String, Double> labelweights = new HashMap<String, Double>();
		String result = null;
		for (DecisionTreeNode tree: treeweights.keySet()){
			String label = classifyDraw(tree, d);
			if(labelweights.containsKey(label)){
				labelweights.put(label,  labelweights.get(label) + treeweights.get(tree));
			}
			else{
				labelweights.put(label, treeweights.get(tree));
			}
		}
		for (String label : labelweights.keySet()){
			if(result == null){
				result = label;
			}
			if(labelweights.get(label)>labelweights.get(result)){
				result = label;
			}
		}
		return result;
	}

//Perceptron Testing
	private String getPerceptronLabel(Drawing d) {
		dweights = new double[400];
		PixelIterator(d);
		String label = FindBestLabel();
		return label;
		}	
	
	private String FindBestLabel() {
		String result = null;
		double weight = 0;
		for(MultiLayer Layer : resultMap.keySet()){
			if(weight < Layer.compute(dweights)[0]){
				result = resultMap.get(Layer);
				weight = Layer.compute(dweights)[0];
			}
		}
		return result;
		}
	
private void PixelIterator(Drawing drawing) {
	int pixelindex = 0;
	for(int x = 0; x < drawing.getWidth(); x++){
		for(int y = 0; y < drawing.getHeight(); y++ ){
			if(drawing.isSet(x, y)){
				dweights[pixelindex] = 1.0;
			}
			else{
				dweights[pixelindex] = 0.0;
			}
			pixelindex++;
		}}}

//SOM Testing
	private String getSOMLabel(Drawing d) {
		FloatDrawing FD = new FloatDrawing(d);
		String result = "unknown";
		SelfOrgMapNeuron winner = SOM.bestMatchFor(FD);
		int BiggestFrequency = 0;
		int LabelValue = 0;
		for(int i = 0; i<data.numLabels();++i){
			int frequency = SOMFrequency[i][winner.getX()][winner.getY()];
			if (BiggestFrequency < frequency){
				BiggestFrequency = frequency;
				LabelValue = i;
			}
			int count = 0;	
			for (String label: data.allLabels()) {
				if(count == LabelValue){
					result = label;
				}
				count += 1;
			}
		}
		return result;
	}

//GNG Testing
	private String getGNGLabel(Drawing d) {
		FloatDrawing FD = new FloatDrawing(d);
		String result = "unknown";
		Neuron winner = GNG.bestMatchFor(FD);
		int BiggestFrequency = 0;
		int LabelValue = 0;
		for(int i = 0; i<data.numLabels();++i){
			int frequency = GNGFrequency[i][NodeValues.get(winner)];
			if (BiggestFrequency < frequency){
				BiggestFrequency = frequency;
				LabelValue = i;
			}
			int count = 0;	
			for (String label: data.allLabels()) {
				if(count == LabelValue){
					result = label;
				}
			count += 1;
			}
		}
		return result;
	}
}