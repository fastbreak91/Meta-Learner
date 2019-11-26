package handwriting.core;

public class InnerNode implements DecisionTreeNode{
	private DecisionTreeNode rightChild;
	private DecisionTreeNode leftChild;
	private int Pixelx;
	private int Pixely;
	
	public InnerNode(int x, int y, DecisionTreeNode leftDown, DecisionTreeNode rightDown){
		this.setPixelx(x);
		this.setPixely(y);
		this.leftChild = leftDown;
		this.rightChild = rightDown;
	}
	
	public void setRightChild(DecisionTreeNode n){
		this.rightChild = n;
	}
	
	public void setLeftChild(DecisionTreeNode n){
		this.leftChild = n;
	}
	
	public void setPixelx(int x){
		Pixelx = x;
	}
	
	public void setPixely(int y){
		Pixely = y;
	}

	public int getPixelx(){
		return Pixelx;
	}
	
	public int getPixely(){
		return Pixely;
	}

	@Override
	public DecisionTreeNode rightChild() {
		return rightChild;
	}

	@Override
	public DecisionTreeNode leftChild() {
		return leftChild;
	}

	@Override
	public String getLabel() {
		return "inner";
	}
}
