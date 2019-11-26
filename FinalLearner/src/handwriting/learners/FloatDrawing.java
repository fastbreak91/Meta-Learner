package handwriting.learners;

import handwriting.core.Drawing;

public class FloatDrawing {
	private double[][] drawing; //[] is like input node, the doubles are the weights, each float drawing is all of the weights associated with one pixel
	
	public FloatDrawing(int width, int height) {
		drawing = new double[width][height];
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				drawing[x][y] = Math.random();
			}
		}
	}
	
	public FloatDrawing(Drawing d) {
        /* Write code to construct a FloatDrawing from a Drawing */
		
		int width = d.getWidth();
		int height = d.getHeight();
		drawing = new double[width][height];
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				if (d.isSet(x, y)){
					this.drawing[x][y] = 1;
				}
				else{
					this.drawing[x][y] = 0;
				}
			}
		}
	}
	
	public double distance(FloatDrawing that) {
        /* Write a distance function for FloatDrawings */
		int width = this.getWidth();
		int height = this.getHeight();
		double result = 0;
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				double InputNodeValue = this.drawing[x][y];
				double OutputNodeValue = that.drawing[x][y];
				double distance = (InputNodeValue - OutputNodeValue)*(InputNodeValue - OutputNodeValue);
				result += distance;
			}
		}
        return result;
	}
	
	public void train(FloatDrawing that, double scale) { //uses algorithm found in training thing
        /* Write code to train this FloatDrawing */
		int width = this.getWidth();
		int height = this.getHeight();
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				double weight = this.at(x, y);
				double inputVal = that.at(x, y);
				weight = weight + (scale * (inputVal - weight));
				this.drawing[x][y] = weight;
			}		
		}
	}
	
	public double at(int x, int y) {
		return drawing[x][y];
	}
	
	public int getWidth() {
		return drawing.length;
	}
	
	public int getHeight() {
		return drawing[0].length;
	}

	public FloatDrawing GetMean(FloatDrawing that) {
		int width = this.getWidth();
		int height = this.getHeight();
		FloatDrawing result = new FloatDrawing(width, height);
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				double thisPixelWeight = (this.at(x, y)+that.at(x, y))/2.0;
				result.drawing[x][y] = thisPixelWeight;
			}
		}
		return result;
	}
}
