package renater;

import java.util.ArrayList;
import java.util.HashMap;

import peersim.core.Node;
import topology.TopologyNode;
import utilities.NodeUtilities;

public class RenaterNode extends TopologyNode{

	private String gateway;
	
	private ArrayList<Node> neighbors;
	private HashMap<String, RenaterEdge> edges;
	private HashMap<String, String> routes;
	
	
	
	public RenaterNode(String prefix) {
		super(prefix);
		neighbors = new ArrayList<Node>();
        routes = new  HashMap<String, String>();
        edges = new  HashMap<String, RenaterEdge>();
	}
	
	public Object clone() {
		RenaterNode inp = null;
        inp = (RenaterNode) super.clone();
        neighbors = new ArrayList<Node>();
        routes = new  HashMap<String, String>();
        edges = new  HashMap<String, RenaterEdge>();
        return inp;
    }
	
	public boolean isGateway() {
        return gateway == null;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    
    
	public void addRoute(String dest, String next ){
		routes.put(dest, next);
	}

	public String getRoute(String dest){
		if(routes.containsKey(dest))
			return routes.get(dest);
		for(String key : routes.keySet()){
			if(NodeUtilities.getDCID(key) == NodeUtilities.getDCID(dest))
				return routes.get(key);
		}
		if(!isGateway())
			return gateway;
		return null;
	}

	@Override
	public void onKill() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean addNeighbor(Node neighbour) {
		if (!neighbors.contains(neighbour))
			return neighbors.add(neighbour);
		return false;
	}

	public boolean addEdge(String nodeID, RenaterEdge edge) {
		edges.put(nodeID, edge);
		return true;
	}
	
	public RenaterEdge getEdge(String index) {
		if(edges.containsKey(index))
			return edges.get(index);
		return null;
	}
	
	@Override
	public boolean contains(Node neighbor) {
		for(Node neigh : neighbors)
			if(neigh.equals(neighbor))
				return true;
		return false;
	}

	@Override
	public int degree() {
		return neighbors.size();
	}

	@Override
	public Node getNeighbor(int i) {
		return neighbors.get(i);
	}

	@Override
	public void pack() {
		// TODO Auto-generated method stub
		
	}

	public String toString(){
		return getID();
	}
}
