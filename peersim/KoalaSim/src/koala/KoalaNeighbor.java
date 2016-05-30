package koala;

import peersim.core.Node;


public class KoalaNeighbor{
	
	public static final int LS=0;
	public static final int LP=1;
	
	public static final int GS=2;
	public static final int GP=3;
	
	private Node node;
	private int role;
	public KoalaNeighbor(Node node, int role) {
		super();
		this.node = node;
		this.role = role;
	}
	public Node getNode() {
		return node;
	}
	public void setNode(Node node) {
		this.node = node;
	}
	public int getRole() {
		return role;
	}
	public void setRole(int role) {
		this.role = role;
	}

	
}
