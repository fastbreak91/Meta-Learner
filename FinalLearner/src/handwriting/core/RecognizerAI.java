package handwriting.core;


import java.util.ArrayList;

import javax.swing.JPanel;

abstract public class RecognizerAI {
	abstract public String classify(Drawing d);
	
	public boolean hasVisualization() {
		return false;
	}

	public JPanel getVisualization() {
		return null;
	}	
	
	public boolean allTestsCorrect(SampleData testData) {
		return numCorrectTests(testData) == testData.numDrawings();
	}
	
	// Returns the number of samples from testData that were correctly classified
	public int numCorrectTests(SampleData testData) {
		int passed = 0;
		for (String label: testData.allLabels()) {
			for (int j = 0; j < testData.numDrawingsFor(label); ++j) {
				if (classify(testData.getDrawing(label, j)).equals(label)) {
					passed += 1;
				}
			}
		}
		return passed;
	}
	
	public static double stdDev(ArrayList<Integer> data) {
		double mean = RecognizerAI.mean(data);
		double ssd = 0.0;
		for (int d: data) {
			double diff = mean - d;
			ssd += diff * diff;
		}
		double variance = ssd / data.size();
		return Math.sqrt(variance);
	}

	public static double mean(ArrayList<Integer> data) {
		double sum = 0.0;
		for (int d: data) {sum += d;}
		return sum / data.size();
	}
	
}