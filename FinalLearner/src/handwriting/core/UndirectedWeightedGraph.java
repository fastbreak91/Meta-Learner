package handwriting.core;

import java.util.*;

public class UndirectedWeightedGraph<T,W> implements Iterable<T> {
	private Map<T,Map<T, W>> weights;
	
	public UndirectedWeightedGraph() {
		weights = new LinkedHashMap<T,Map<T,W>>();
	}
	
	public boolean hasNode(T node) {
		return weights.containsKey(node);
	}
	
	public void addNode(T node) {
		weights.put(node, new LinkedHashMap<T,W>());
	}
	
	private void addIfAbsent(T node) {
		if (!hasNode(node)) {
			addNode(node);
		}
	}
	
	public void updateEdge(T one, T two, W weight) {
		addIfAbsent(one);
		addIfAbsent(two);
		weights.get(one).put(two, weight);
		weights.get(two).put(one, weight);
	}
	
	public W getWeight(T one, T two) {
		return weights.get(one).get(two);
	}
	
	public void removeEdge(T one, T two) {
		weights.get(one).remove(two);
		weights.get(two).remove(one);
	}
	
	public void removeNode(T node) {
		for (T other: weights.get(node).keySet()) {
			weights.get(other).remove(node);
		}
		weights.remove(node);
	}
	
	public int size() {return weights.size();}

	@Override
	public Iterator<T> iterator() {
		return weights.keySet().iterator();
	}
	
	public Iterable<T> allNeighborsOf(T node) {
		return weights.get(node).keySet();
	}
	
	public boolean hasNeighbors(T node) {
		return weights.get(node).size() > 0;
	}
}
