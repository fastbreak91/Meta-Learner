package handwriting.core;

public interface DecisionTreeNode {
	
	public DecisionTreeNode rightChild();
	public DecisionTreeNode leftChild();
	
	public int getPixelx();
	public int getPixely();
	
	public String getLabel();
	
}