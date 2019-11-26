package handwriting.learners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import handwriting.core.RecognizerAI;
import handwriting.core.Drawing;
import handwriting.core.SampleData;

public class RPixel  extends RecognizerAI{
	HashSet<String> AllLabelsV;
	ArrayList<String> OneEachLabel;
	SampleData data;
	HashMap<MultiLayer, String> resultMap;
	double[] dweights;
	double[][] inputweights;
	double[][] goalweights;
	
	@Override
	public String classify(Drawing d) {
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

	public RPixel(SampleData data){
		resultMap = new HashMap<MultiLayer, String>();
		AllLabelsV = new HashSet<String>();
		for(String label: data.allLabels()){AllLabelsV.add(label);}
		OneEachLabel = new ArrayList<String>(AllLabelsV);
		this.data = data;
		for(int i = 0 ; i < OneEachLabel.size(); i++){
			resultMap.put(new MultiLayer(400, 15 ,1), OneEachLabel.get(i)); //HiddenNodes is the middle number
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
			Layer.trainN(inputweights,goalweights, 100, .3); //iterations is the first number, learning bias is the second
		}
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
			}
		}
	}

}