package handwriting.learners;

import java.util.HashMap;
import java.util.Random;

import handwriting.core.*;

public class BoostedTree extends RecognizerAI {
	private HashMap<Drawing, Double> drawingweights;
	private HashMap<DecisionTreeNode, Double> treeweights;
	
	public BoostedTree(SampleData data){
		drawingweights = new  HashMap<Drawing, Double>();
		treeweights = new  HashMap<DecisionTreeNode, Double>();
		if (data.numDrawings()==0){return;}
		startDrawingWeights(data);
		generateTrees(data);
	}
	
	public void generateTrees(SampleData data){
		int numTrees = 5;
		for (int i=0;i<numTrees;i=i+1){
			//System.out.println(i);
			double error = 0;
			DecisionTreeNode tree = null;
			while (0 == error || error > .5){
				SampleData onethird = generateThirdofData(data);
				tree = makeTree(onethird);
				error = calcError(tree, data);
				//System.out.println(error);
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
		double weightsum = 0.0;
		double result = 0.0;
		
		for (int i = 0; i < data.numDrawings(); i++){
			weightsum += drawingweights.get(data.getDrawing(i));
		}
		
		for (String label: data.allLabels()){
			double weight = 0;
			
			for (int i2=0; i2 < data.numDrawings(); i2++){
				if(label.equals(data.getLabelFor(i2))){
					weight += (drawingweights.get(data.getDrawing(i2)) / weightsum);
				}
			}
		result += (weight*weight);
		}
		result = 1 - result;
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

	@Override
	public String classify(Drawing d) {
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
}