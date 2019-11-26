package handwriting.learners;

import java.util.HashMap;
import java.util.Iterator;

import handwriting.core.Drawing;
import handwriting.core.RecognizerAI;
import handwriting.core.SampleData;
import handwriting.learners.GrowingNeuralGas;
import handwriting.learners.GrowingNeuralGas.Neuron;

public class GNGJudge extends RecognizerAI{
	SampleData data;
	GrowingNeuralGas GNG;
	int Frequency[][];
	int iterations = 3;
	private double maxTolerableDistance = 35.0;
	private double k = 1250.0;
	HashMap<Neuron,Integer> NodeValues;
	
public GNGJudge(SampleData data){
	this.data = data;
	this.GNG = new GrowingNeuralGas(data, maxTolerableDistance, k);
	this.NodeValues = new HashMap<Neuron, Integer>();
	for(int i = 0; i<iterations; ++i){
		GNG.trainOnce(data); //trains
	}
	System.out.println(GNG.numNodes());
	Frequency = new int[data.numLabels()][GNG.numNodes()];
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
			Frequency[labelValue][l] = 0;
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
			Frequency[ThislabelValue][NodeValues.get(winner)] += 1;
		}
	}
	System.out.println(GNG.numNodes());
}

	@Override
	public String classify(Drawing d) {
		FloatDrawing FD = new FloatDrawing(d);
		String result = "unknown";
		Neuron winner = GNG.bestMatchFor(FD);
		int BiggestFrequency = 0;
		int LabelValue = 0;
		for(int i = 0; i<data.numLabels();++i){
			int frequency = Frequency[i][NodeValues.get(winner)];
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