package koala;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import peersim.core.Linkable;
import peersim.core.Node;
import peersim.core.Protocol;
import example.hot.InetCoordinates;

public class RenaterNode extends InetCoordinates implements Protocol, Linkable {

	private String gateway;
	private String id;
	
	private ArrayList<Node> neighbors;
	private HashMap<String, String> routes;
	
	
	
	public RenaterNode(String prefix) {
		super(prefix);
		neighbors = new ArrayList<Node>();
        routes = new  HashMap<String, String>();
	}
	
	public Object clone() {
		RenaterNode inp = null;
        inp = (RenaterNode) super.clone();
        neighbors = new ArrayList<Node>();
        routes = new  HashMap<String, String>();
        return inp;
    }
	
	public boolean isGateway() {
        return gateway == null;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
	}
    
	public void addRoute(String dest, String next ){
		routes.put(dest, next);
	}

	public String getRoute(String dest){
		if(routes.containsKey(dest))
			return routes.get(dest);
		for(String key : routes.keySet()){
			if(key.split("-")[0].equals(dest.split("-")[0]))
				return routes.get(dest);
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
		if (!this.contains(neighbour))
			return neighbors.add(neighbour);
		return false;
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
