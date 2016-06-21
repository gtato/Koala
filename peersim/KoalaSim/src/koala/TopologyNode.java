package koala;

import peersim.core.Linkable;
import peersim.core.Node;
import peersim.core.Protocol;
import example.hot.InetCoordinates;

public class TopologyNode extends InetCoordinates implements Protocol, Linkable{

	private String id;
	
	public TopologyNode(String prefix) {
		super(prefix);
		// TODO Auto-generated constructor stub
	}

	public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
	}

	@Override
	public void onKill() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean addNeighbor(Node neighbour) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Node neighbor) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int degree() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Node getNeighbor(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void pack() {
		// TODO Auto-generated method stub
		
	}
}
