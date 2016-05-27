package koala;

import peersim.core.Linkable;
import peersim.core.Node;
import peersim.core.Protocol;
import example.hot.InetCoordinates;

public class RenaterNode extends InetCoordinates implements Protocol, Linkable {

	private int dcID;
	private int nodeID;
	private String bootstrapID;
	
	private boolean gateway;
	private boolean joined;
	
	public RenaterNode(String prefix) {
		super(prefix);
	}
	
	public Object clone() {
		RenaterNode inp = null;
        inp = (RenaterNode) super.clone();
        gateway = false;
        return inp;
    }

	public int getDCID() {
        return dcID;
    }

    public void setDCID(int dcID) {
        this.dcID = dcID;
    }

    public boolean isGateway() {
        return gateway;
    }

    public void setGateway(boolean gateway) {
        this.gateway = gateway;
    }
    
    public int getNodeID() {
		return nodeID;
	}

	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	public void setID(int dcID, int nodeID) {
		this.dcID = dcID;
		this.nodeID = nodeID;
	}
	
	public void setID(String id) {
		this.dcID = Integer.parseInt(id.split("-")[0]);
		this.nodeID = Integer.parseInt(id.split("-")[1]);
	}
	
	public String getID() {
		return this.dcID + "-" + this.nodeID;
	}
	
	public String getBootstrapID() {
		return bootstrapID;
	}

	public void setBootstrapID(String bootstrapID) {
		this.bootstrapID = bootstrapID;
	}
	
	public boolean hasJoined() {
		return joined;
	}

	public void setJoined(boolean joined) {
		this.joined = joined;
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
