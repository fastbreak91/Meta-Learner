package handwriting.learners;

import handwriting.core.*;

public class SelfOrgMap {
	
	private FloatDrawing[][] grid;
	private SampleData data;
	
	public SelfOrgMap(int gridSize, int iterations, SampleData data) {
		this.data = data;
		grid = new FloatDrawing[gridSize][gridSize];
		for (int x = 0; x < gridSize; ++x) {
			for (int y = 0; y < gridSize; ++y) {
				grid[x][y] = new FloatDrawing(data.getDrawingWidth(), data.getDrawingHeight());
			}
		}
		/* Write training code here */
		double learningRate = 1; //reduce each iteration by a linear amount
		double radius = getGridSide()/2; //reduce each iteration by multiplying by the learning rate
		for (int i = 0; i < iterations; ++i) { //every iteration
			for (int j = 0; j < data.numDrawings() ; ++j){ //every input
				Drawing d = data.getDrawing(j);
				FloatDrawing FD = new FloatDrawing(d);
				SelfOrgMapNeuron winner = bestMatchFor(FD);
				for (int x = 0; x < gridSize; ++x) {
					for (int y = 0; y < gridSize; ++y) {
						double distance = winner.distanceTo(x, y);
						if (distance < radius * learningRate){
							grid[x][y].train(FD, learningRate);
						}
					}
					
				}
			}
			learningRate -= 1.0/iterations;
		}
	}
	
	public SelfOrgMapNeuron bestMatchFor(FloatDrawing target) {
		SelfOrgMapNeuron best = new SelfOrgMapNeuron(0, 0);
        /* Write code to identify best neuron here */
		double bestdistance = Double.MAX_VALUE;
		for (int x = 0; x < getGridSide(); ++x) {
			for (int y = 0; y < getGridSide(); ++y) {
				double distance = target.distance(grid[x][y]);
				if (distance < bestdistance){
					bestdistance = distance;
					best.x = x;
					best.y = y;
				}
			}
		}
		return best;
	}
	
	public SelfOrgMapNeuron at(int x, int y) {
		return new SelfOrgMapNeuron(x, y);
	}
	
	public int getGridSide() {return grid.length;}
	
	public class SelfOrgMapNeuron {
		private int x, y;
		
		private SelfOrgMapNeuron(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public int getX() {return x;}
		public int getY() {return y;}
		
		public double distanceTo(int x, int y) {
            /* Write the map's distance function here */
			//double noroot = (x-this.getX())*(x-this.getX()) + (y-this.getY())*(y-this.getY());
			//double result = Math.sqrt(noroot);
			int distx = Math.abs(x - this.getX()); //Manhattan
			int disty = Math.abs(y - this.getY());
            return distx + disty;
			//return result;
		}
		
		public FloatDrawing getIdealInput() {return grid[x][y];}
	}
}
