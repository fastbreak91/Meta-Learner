package handwriting.learners;

import handwriting.core.*;

public class DecisionTree extends RecognizerAI {
	private DecisionTreeNode root;
	
	public DecisionTree(SampleData data){
		root = makeTree(data);
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
		double portionsum = 0.0;
		double portion = 0.0;
		double numdrawings = data.numDrawings();
		for (String label: data.allLabels()) {
			double drawforlabel = data.numDrawingsFor(label);
			portion = drawforlabel/numdrawings;
			//System.out.println(portion);
			portionsum += portion*portion;
		}
		double result = 1 - portionsum;
		return result;
	}

	public String classify(Drawing d) {
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
}