package handwriting.core;

public class LeafNode implements DecisionTreeNode{
	private String label;

	
	public String getLabel(){
		return this.label;
	}
	
	public DecisionTreeNode setLabel(String lab){
		this.label = lab;
		return this;
	}

	@Override
	public DecisionTreeNode rightChild() {
		return null;
	}

	@Override
	public DecisionTreeNode leftChild() {
		return null;
	}

	@Override
	public int getPixelx() {
		return -1;
	}

	@Override
	public int getPixely() {
		return -1;
	}

}
