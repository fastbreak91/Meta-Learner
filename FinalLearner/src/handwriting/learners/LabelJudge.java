package handwriting.learners;

import handwriting.core.Drawing;
import handwriting.core.RecognizerAI;
import handwriting.core.SampleData;
import handwriting.learners.SelfOrgMap.SelfOrgMapNeuron;

public class LabelJudge extends RecognizerAI{
	SampleData data;
	SelfOrgMap SOM;
	int Frequency[][][];
	int gridSize = 5;
	int iterations = 35;
	

public LabelJudge(SampleData data){
	Frequency = new int[data.numLabels()][gridSize][gridSize];
	this.data = data;
	this.SOM = new SelfOrgMap(gridSize, iterations, data); //Second number is iterations
	int count = 0;
	for (String label: data.allLabels()) {
		int labelValue = count;
		count += 1;
		for (int i = 0; i < data.numDrawingsFor(label); ++i) {
			Drawing d = data.getDrawing(label, i);
			FloatDrawing FD = new FloatDrawing(d);
			SelfOrgMapNeuron winner = SOM.bestMatchFor(FD);
			Frequency[labelValue][winner.getX()][winner.getY()] += 1;
		}
	}
}

	@Override
	public String classify(Drawing d) {
		FloatDrawing FD = new FloatDrawing(d);
		String result = "unknown";
		SelfOrgMapNeuron winner = SOM.bestMatchFor(FD);
		int BiggestFrequency = 0;
		int LabelValue = 0;
		for(int i = 0; i<data.numLabels();++i){
			int frequency = Frequency[i][winner.getX()][winner.getY()];
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
