package renater;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import messaging.KoalaMessage;
import messaging.KoalaRouteMsgContent;
import peersim.config.Configuration;
import peersim.core.Node;
import topology.TopologyNode;
import utilities.KoaLite;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;

public class RenaterNode extends TopologyNode{

	private String gateway;
	
	private ArrayList<Node> neighbors;
	private HashMap<String, RenaterEdge> edges;
//	private HashMap<String, String> routes;
	
	
	public RenaterNode(String prefix) {
		super(prefix);
		neighbors = new ArrayList<Node>();
//        routes = new  HashMap<String, String>();
        edges = new  HashMap<String, RenaterEdge>();
	}
	
	public Object clone() {
		RenaterNode inp = null;
        inp = (RenaterNode) super.clone();
        neighbors = new ArrayList<Node>();
//        routes = new  HashMap<String, String>();
        edges = new  HashMap<String, RenaterEdge>();
        return inp;
    }
	
	public boolean isGateway() {
        return gateway == null;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getGateway(){
    	return this.gateway;
    }
    
    
//	public void addRoute(String dest, String next ){
//		if(next.equals(this.getID()))
//			System.out.println("something went wrong");
//		routes.put(dest, next);
//	}

//	public String getRoute(String dest){
////		String path = PhysicalDataProvider.getPath(this.getID(), dest);
////		return path.split(" ")[1];
//		if(routes.containsKey(dest))
//			return routes.get(dest);
//		for(String key : routes.keySet()){
//			if(NodeUtilities.getDCID(key) == NodeUtilities.getDCID(dest))
//				return routes.get(key);
//		}
//		if(!isGateway())
//			return gateway;
//		return null;
//	}

	public String getRoute(String dest, KoalaMessage msg){
		if(!isGateway())
			return gateway;
		
		RenaterNode rnDest = NodeUtilities.getRenaterNode(dest);
		if(getID().equals(rnDest.getGateway()))
			return dest;
		
		KoalaRouteMsgContent krc = (KoalaRouteMsgContent) msg.getContent();
		ArrayList<String> shortestPath = krc.getShortestPath();
		if(shortestPath == null || shortestPath.size() == 0){
			if (Configuration.getBoolean("dijkstraplus", false))
				shortestPath =  rnDest.isGateway() ?  KoaLite.getPath(this.getID(), dest) : KoaLite.getPath(this.getID(), rnDest.getGateway());
			else{
				String sp = rnDest.isGateway() ? PhysicalDataProvider.getPath(getID(), dest) : PhysicalDataProvider.getPath(getID(), rnDest.getGateway()) ;
				shortestPath = new ArrayList<String>(Arrays.asList(sp.split(" "))); 
			}
			shortestPath.remove(0);
			krc.setShortestPath(shortestPath);
		}
			
		return shortestPath.remove(0);
		
		
	}

	
	public ArrayList<Node> getNeighbors() {
		return neighbors;
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
