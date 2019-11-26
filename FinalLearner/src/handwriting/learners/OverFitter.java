package handwriting.learners;

import handwriting.core.*;

public class OverFitter extends RecognizerAI {
	private SampleData data;
	
	public OverFitter(SampleData data) {
		this.data = data;
	}

	public String classify(Drawing d) {
		for (String label: data.allLabels()) {
			for (int i = 0; i < data.numDrawingsFor(label); ++i) {
				if (d.equals(data.getDrawing(label, i))) {
					return label;
				}
			}
		}
		
		return "Unknown";
	}

}
