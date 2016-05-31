package koala;



import java.util.ArrayList;

import com.google.gson.Gson;

import peersim.core.Linkable;
import peersim.core.Node;
import peersim.core.Protocol;
import example.hot.InetCoordinates;

public class KoalaNode extends InetCoordinates implements Protocol, Linkable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int dcID;
	private int nodeID;
	private String bootstrapID;
	
	private boolean gateway;
	private boolean joined;
	private KoalaRoutingTable routingTable;
	        
	Gson gson; 
	
	
	
	public KoalaNode(String prefix) {
		super(prefix);
		gson = new Gson();
		routingTable = new KoalaRoutingTable(this.getID());
	}
	
	public Object clone() {
		KoalaNode inp = null;
        inp = (KoalaNode) super.clone();
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
	
	public KoalaRoutingTable getRoutingTable() {
		return routingTable;
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
	
	public String toJson(){
		return gson.toJson(this);
	}
	
	public KoalaNode fromJson(String jsonObject){
		return gson.fromJson(jsonObject, KoalaNode.class);
	}
}
