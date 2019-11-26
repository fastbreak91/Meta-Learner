package handwriting.learners;

import handwriting.core.Drawing;
import handwriting.core.SampleData;
import handwriting.core.UndirectedWeightedGraph;
import handwriting.learners.SelfOrgMap.SelfOrgMapNeuron;

import java.util.*;

public class GrowingNeuralGas implements Iterable<GrowingNeuralGas.Neuron> {
	private UndirectedWeightedGraph<Neuron,Integer> nodes;
	private int maxEdgeAge = Integer.MAX_VALUE;
	private double neighborRate = 0.0006, learningRate = 0.05, errorDecay = 0.0005, splitDecay = 0.5;
	private double maxTolerableDistance, k;
	
	public GrowingNeuralGas(SampleData data, double maxTolerableDistance, double k) {
		init(data.getDrawingWidth(), data.getDrawingHeight());
		this.maxTolerableDistance = maxTolerableDistance;
		this.k = k;
	}
	
	public Neuron bestMatchFor(FloatDrawing target) {
        /* Return the best matching Neuron */
		double bestdistance = Double.MAX_VALUE;
		Neuron best = null;
		Iterator<Neuron> nodeIterator = nodes.iterator();
		while (nodeIterator.hasNext()){
			Neuron ThisNode = nodeIterator.next();
			FloatDrawing ThisIdeal = ThisNode.getIdealInput();
				double distance = target.distance(ThisIdeal);
				if (distance < bestdistance){
					bestdistance = distance;
					best = ThisNode;
				}
			}
		return best;
	}
	
	public Neuron SecondbestMatchFor(FloatDrawing target, Neuron best) {
		Neuron Secondbest = null;
		double bestdistance = Double.MAX_VALUE;
		Iterator<Neuron> nodeIterator = nodes.iterator();
		while (nodeIterator.hasNext()){
			Neuron ThisNode = nodeIterator.next();
			FloatDrawing ThisIdeal = ThisNode.getIdealInput();
				double distance = target.distance(ThisIdeal);
				if (distance < bestdistance && ThisNode != best){
					bestdistance = distance;
					Secondbest = ThisNode;
				}
			}
		return Secondbest;
	}
	
	public int numNodes() {return nodes.size();}
	
	private void init(int width, int height) {
		nodes = new UndirectedWeightedGraph<Neuron,Integer>();
		nodes.updateEdge(new Neuron(width, height), new Neuron(width, height), 0);
	}
	
	public void trainOnce(SampleData data) {
        /* Train the network on every item from data one time */
		for (int j = 0; j < data.numDrawings() ; ++j){ //every input
			Drawing d = data.getDrawing(j);
			FloatDrawing FD = new FloatDrawing(d);
			Neuron winner = bestMatchFor(FD);
			Neuron second = SecondbestMatchFor(FD, winner);
			FloatDrawing winnerIdeal = winner.getIdealInput();
			FloatDrawing secondIdeal = second.getIdealInput();
			double DistanceWinner = winnerIdeal.distance(FD); //is also additional error
			double DistanceSecond = secondIdeal.distance(FD);
			double AdditionalUtil = DistanceWinner - DistanceSecond;
			winner.error = winner.error + DistanceWinner; //updates winner error
			winner.utility = winner.utility + AdditionalUtil; //updates winner utility
			winner.refVector.train(FD, learningRate); //trains winner against input
			nodes.updateEdge(winner, second, 0); //sets weight between winner and best to 0
			
			if(nodes.hasNeighbors(winner)){
				Iterator<Neuron> NeighborIterator = nodes.allNeighborsOf(winner).iterator();
				while (NeighborIterator.hasNext()){
					Neuron ThisNeighbor = NeighborIterator.next();
					Integer NewWeight = nodes.getWeight(winner, ThisNeighbor) + 1;
					nodes.updateEdge(winner, ThisNeighbor, NewWeight); //increments edge weight of neighbors
					ThisNeighbor.refVector.train(FD, neighborRate); //trains neighbors of winner against input
				}
			}
			
			Iterator<Neuron> GraphIterator = nodes.iterator();
			while (GraphIterator.hasNext()){
				Neuron ThisNode = GraphIterator.next();
				if(nodes.hasNeighbors(ThisNode)){
					Iterator<Neuron> ThisNodeNeighborIterator = nodes.allNeighborsOf(ThisNode).iterator();
					while (ThisNodeNeighborIterator.hasNext()){
						Neuron ThisNodeNeighbor = ThisNodeNeighborIterator.next();
						Integer ThisEdgeWeight = nodes.getWeight(ThisNode, ThisNodeNeighbor);
						if (ThisEdgeWeight > maxEdgeAge){
							nodes.removeEdge(ThisNode, ThisNodeNeighbor); //removes old edges
						}
					}
				}
				else{
					nodes.removeNode(ThisNode); //removes all nodes without a neighbor
				}
			}
			
			double BiggestError = -1.0;
			Neuron BiggestErrorNeuron = null;
			double NeighborlyBiggestError = -1.0;
			Neuron NeighborlyBiggestErrorNeuron = null;
			Iterator<Neuron> worstGraphIterator = nodes.iterator();
			while (worstGraphIterator.hasNext()){
				Neuron ThisNode = worstGraphIterator.next();
				double ThisError = ThisNode.error;
				if (BiggestError < ThisError){
					BiggestError = ThisError;
					BiggestErrorNeuron = ThisNode;
				}
				if (nodes.hasNeighbors(BiggestErrorNeuron)){
					Iterator<Neuron> NeighborlyGraphIterator = nodes.allNeighborsOf(BiggestErrorNeuron).iterator();
					while (NeighborlyGraphIterator.hasNext()){
						Neuron ThisNeighbor = NeighborlyGraphIterator.next();
						double ThisNeighborlyError = ThisNeighbor.error;
						if (ThisNeighborlyError > NeighborlyBiggestError){
							NeighborlyBiggestError = ThisNeighborlyError;
							NeighborlyBiggestErrorNeuron = ThisNeighbor;
						}
					}
				}
				else {
					System.out.println("Error: The Neuron with the Biggest error had no neighbors...");
				}
			}
			
			
			if (DistanceWinner > maxTolerableDistance){ //then invoke "add a new node" procedure
				if (BiggestErrorNeuron == null){
					System.out.println("Error: Nullpointer BiggestErrorNeuron");
				}
				if (NeighborlyBiggestErrorNeuron == null){
					System.out.println("Error: Nullpointer NeighborlyBiggestErrorNeuron");
				}
				Neuron NewNeuron = new Neuron(data.getDrawingWidth(), data.getDrawingHeight());
				FloatDrawing NewIdealInput = BiggestErrorNeuron.getIdealInput().GetMean(NeighborlyBiggestErrorNeuron.getIdealInput());
				NewNeuron.refVector = NewIdealInput;
				NewNeuron.error = (BiggestErrorNeuron.error + NeighborlyBiggestErrorNeuron.error)/2.0;
				if (BiggestErrorNeuron.utility > NeighborlyBiggestErrorNeuron.utility){
					NewNeuron.utility = BiggestErrorNeuron.utility;
				}
				else{
					NewNeuron.utility = NeighborlyBiggestErrorNeuron.utility;
				}
				nodes.addNode(NewNeuron);
				nodes.updateEdge(NewNeuron, BiggestErrorNeuron, 0);
				nodes.updateEdge(NewNeuron, NeighborlyBiggestErrorNeuron, 0);
				nodes.removeEdge(BiggestErrorNeuron, NeighborlyBiggestErrorNeuron);
				BiggestErrorNeuron.error = BiggestErrorNeuron.error*splitDecay;
				BiggestErrorNeuron.utility = BiggestErrorNeuron.utility*splitDecay;
				NeighborlyBiggestErrorNeuron.error = NeighborlyBiggestErrorNeuron.error*splitDecay;
				NeighborlyBiggestErrorNeuron.utility = NeighborlyBiggestErrorNeuron.utility*splitDecay;
			}
			
			Iterator<Neuron> UselessGraphIterator = nodes.iterator();
			Neuron MostUselessNode = null;
			double LowestUtility = Double.MAX_VALUE;
			while(UselessGraphIterator.hasNext()){
				Neuron ThisUselessNode = UselessGraphIterator.next();
				if (ThisUselessNode.utility < LowestUtility){
					LowestUtility = ThisUselessNode.utility;
					MostUselessNode = ThisUselessNode;
				}
			}
			if(k < (BiggestErrorNeuron.error/MostUselessNode.utility)){
				nodes.removeNode(MostUselessNode); //purges the most useless node if exceeds k
			}
			
			Iterator<Neuron> DecayGraphIterator = nodes.iterator();
			while(DecayGraphIterator.hasNext()){
				Neuron ThisDecayingNode = DecayGraphIterator.next();
				ThisDecayingNode.error = ThisDecayingNode.error * errorDecay;
				ThisDecayingNode.utility = ThisDecayingNode.utility * errorDecay;
			}
		
		}
	}
	
	public class Neuron {
		
		private Neuron(int width, int height) {
			refVector = new FloatDrawing(width, height);
			error = 0;
		}
		
		private Neuron(Neuron one, Neuron two) {
            /* Create a new Neuron that is the mean of one and two */
		}
		
		private FloatDrawing refVector;
		private double error, utility;
		
		public FloatDrawing getIdealInput() {return refVector;}
	}
	
	@Override
	public Iterator<Neuron> iterator() {
		return nodes.iterator();
	}
}
