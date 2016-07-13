package koala.initializers;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import koala.RenaterEdge;
import koala.RenaterNode;
import koala.utility.Dijkstra;
import koala.utility.PhysicalDataProvider;
import koala.utility.NodeUtilities;
import koala.utility.Dijkstra.Edge;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.WireGraph;
import peersim.graph.Graph;
import koala.RenaterGraph;

public class WireRenater extends WireGraph {

	private static final String PAR_PROT = "protocol";
	
	private static final String PAR_K = "k";
	private final int pid;
	private final int k;
	
	
	
	public WireRenater(String prefix) {
		super(prefix);
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
		k = Configuration.getInt(prefix + "." + PAR_K);
		g = new RenaterGraph(pid,false);
	}

	@Override
	public void wire(Graph g) {
		
		int centerIndex = -1;
		ArrayList<RenaterNode> gateways = new ArrayList<RenaterNode>();
		ArrayList<Integer> gateway_indexes = new ArrayList<Integer>();
		RenaterNode lastGW = null;
		for (int i = Network.size()-1; i >= 0; i--) {
            Node n = (Node) g.getNode(i);            
            RenaterNode rn = (RenaterNode)n.getProtocol(pid);
            if(rn.isGateway()){
            	gateways.add(rn);
            	PhysicalDataProvider.addGatewayID(rn.getID());
            	gateway_indexes.add(i);
            	centerIndex = i;
            	lastGW = rn;
            }else{
            	lastGW.addRoute(rn.getID(), rn.getID());
            	((RenaterGraph)g).setEdge(i, centerIndex, new RenaterEdge(PhysicalDataProvider.getIntraDCLatency(NodeUtilities.getDCID(rn.getID()))));
            }
		}
		
		
		
//		wireDC(gateway_indexes, gateways);
//		wireDCWaxman(gateway_indexes, gateways);
		wireGradual(gateway_indexes, gateways);
//		for(int i = 0; i < distances.size(); i++){
//			ArrayList<AbstractMap.SimpleEntry<Integer, Double>> dists = distances.get(i);
//			Collections.sort(dists, new Comparator<AbstractMap.SimpleEntry<Integer, Double>>() {
//			@Override
//			public int compare(AbstractMap.SimpleEntry<Integer, Double> o1, AbstractMap.SimpleEntry<Integer, Double> o2) {
//				return o1.getValue().compareTo(o2.getValue());
//				
//			}
//			});
//			
//			for(int j=0; j < dists.size() && j<k; j++)
//			{
//				RenaterEdge re = new RenaterEdge(dists.get(j).getValue(), getBitRate(), getSpeed());
//				((RenaterGraph)g).setEdge(gateway_indexes.get(i), dists.get(j).getKey(), re);
//			}
//		}
		
		Dijkstra dijkstra = initializeDijkstra(g, gateway_indexes);
		
		for (int i = 0; i < gateway_indexes.size(); i++) {
			Node n = (Node) g.getNode(gateway_indexes.get(i));
            RenaterNode rn = (RenaterNode)n.getProtocol(pid);
        	dijkstra.execute(rn);
        	for (int j = 0; j < gateway_indexes.size(); j++) {
        		if (i==j) continue;
        		Node m = (Node) g.getNode(gateway_indexes.get(j));
                RenaterNode rm = (RenaterNode)m.getProtocol(pid);
    			LinkedList<RenaterNode> path = dijkstra.getPath(rm);
    			PhysicalDataProvider.addLatency(rn.getID(), rm.getID(), dijkstra.getShortestDistance(rm));
    			PhysicalDataProvider.addPath(rn.getID(), rm.getID(), path);
    			if(path != null)
    				rn.addRoute(rm.getID(), path.get(1).getID());
        	}
            
		}
		

	}
	
	private void  wireDC(ArrayList<Integer> gateway_indexes, ArrayList<RenaterNode> gateways){
		ArrayList<ArrayList< AbstractMap.SimpleEntry<Integer, Double>>> distances = new ArrayList<ArrayList<AbstractMap.SimpleEntry<Integer, Double>>>(gateways.size());  
		for(int i = 0; i < gateways.size(); i++){
			ArrayList<AbstractMap.SimpleEntry<Integer, Double>> dists = new ArrayList<AbstractMap.SimpleEntry<Integer, Double>>();
			for(int j = 0; j < gateways.size(); j++){
				if(i != j)
					dists.add(new AbstractMap.SimpleEntry<Integer, Double>(gateway_indexes.get(j), NodeUtilities.getPhysicalDistance(gateways.get(i), gateways.get(j))));
			}
			distances.add(dists);
		}
		
		for(int i = 0; i < distances.size(); i++){
			ArrayList<AbstractMap.SimpleEntry<Integer, Double>> dists = distances.get(i);
			Collections.sort(dists, new Comparator<AbstractMap.SimpleEntry<Integer, Double>>() {
			@Override
			public int compare(AbstractMap.SimpleEntry<Integer, Double> o1, AbstractMap.SimpleEntry<Integer, Double> o2) {
				return o1.getValue().compareTo(o2.getValue());
				
			}
			});
			
			for(int j=0; j < dists.size() && j<k; j++)
			{
				RenaterEdge re = new RenaterEdge(dists.get(j).getValue(), getBitRate(), getSpeed());
				((RenaterGraph)g).setEdge(gateway_indexes.get(i), dists.get(j).getKey(), re);
			}
		}
	}
	
	private void  wireDCWaxman(ArrayList<Integer> gateway_indexes, ArrayList<RenaterNode> gateways){
		HashMap<String, Double> distances = new HashMap<String, Double>();
		
		for(int i = 0; i < gateway_indexes.size(); i++){
			for(int j = 0; j < gateway_indexes.size(); j++){
				if(i!=j){
					String id = NodeUtilities.getKeyID(i,j);
					if(!distances.containsKey(id))
						distances.put(id, NodeUtilities.getPhysicalDistance(gateways.get(i), gateways.get(j)));
				}
			}
		}
		
		double L = Collections.max(distances.values()); 
	   	double a = 0.15;
	   	double b = 0.13;
	   	
//	   	HashMap<String, Double> probablities = new HashMap<String, Double>();
	   	ArrayList<String> ids = new ArrayList<String>();
	   	ArrayList<AbstractMap.SimpleEntry<String, Double>> probablities = new ArrayList<AbstractMap.SimpleEntry<String, Double>>();
	   	
	   	for(int i = 0; i < gateway_indexes.size(); i++){
			for(int j = 0; j < gateway_indexes.size(); j++){
				if(i!=j){
					String id = NodeUtilities.getKeyID(i, j);
					if(!ids.contains(id)){
						double p = b * Math.pow(Math.E, -distances.get(id)/L*a);
						probablities.add(new AbstractMap.SimpleEntry<String, Double>(id, p));
						ids.add(id);
					}
				}
			}
		}

	   	Collections.sort(probablities, new Comparator<AbstractMap.SimpleEntry<String, Double>>() {

			@Override
			public int compare(SimpleEntry<String, Double> o1,
					SimpleEntry<String, Double> o2) {
				return -1*o1.getValue().compareTo(o2.getValue());
			}
		} );
	   	
	   	
	   	for(int i =0; i < probablities.size(); i++){
	   		AbstractMap.SimpleEntry<String, Double> entry = probablities.get(i);
	   		
	   		if(entry.getValue() > CommonState.r.nextDouble()){
		   		int[] inds = NodeUtilities.getIDsFromKey(entry.getKey());
		   		RenaterEdge re = new RenaterEdge(distances.get(entry.getKey()), getBitRate(), getSpeed());
		   		((RenaterGraph)g).setEdge(gateway_indexes.get(inds[0]), gateway_indexes.get(inds[1]), re);
				
	//	   		System.out.println(entry.getKey() + " " + entry.getValue());
		   		System.out.println(gateways.get(inds[0]).getID() +"|"+ gateways.get(inds[1]).getID() + " " + entry.getValue());
	   		}
	   	}
	   	
	}
	
	
	private void  wireGradual(ArrayList<Integer> gateway_indexes, ArrayList<RenaterNode> gateways){
		double minDistance = Double.MAX_VALUE;
		double distance;
		int closestCenterIndex = -1;
		RenaterNode center = new RenaterNode("xxx");
		center.setX(0.5);
		center.setY(0.5);
		for(int i = 0; i < gateways.size(); i++){
			distance = NodeUtilities.getPhysicalDistance(gateways.get(i), center);
			if(distance < minDistance){
				minDistance = distance;
				closestCenterIndex = i;
			}
		}
		
//		Collections.shuffle(gateway_indexes, new Random(5));
//		Collections.shuffle(gateways, new Random(5));
		ArrayList<Integer> linked = new ArrayList<Integer>();
		linked.add(closestCenterIndex);
		System.out.println("first: " + gateways.get(closestCenterIndex));
		for(int i = 0; i < gateways.size(); i++){
			if(i == closestCenterIndex) continue;
			System.out.println("linking: " + gateways.get(i));
			minDistance = Double.MAX_VALUE;
			int closestNode = -1;
			for(int j = 0; j < linked.size();j++){
				distance = NodeUtilities.getPhysicalDistance(gateways.get(i), gateways.get(linked.get(j)));
				System.out.println("distance betnween " + gateways.get(i).getID() + " and "+gateways.get(linked.get(j)).getID() + " is " + distance);
				if(distance < minDistance){
					minDistance = distance;
					closestNode = linked.get(j);
				}
			}
			RenaterEdge re = new RenaterEdge(minDistance, getBitRate(), getSpeed());
			((RenaterGraph)g).setEdge(gateway_indexes.get(i), gateway_indexes.get(closestNode), re);
			System.out.println("setting link between " + gateways.get(i).getID() + " and " + gateways.get(closestNode));
			linked.add(i);
		}
	}
	
	
	
	
	private Dijkstra initializeDijkstra(Graph g, ArrayList<Integer> gateway_cords){
		List<RenaterNode> nodes = new ArrayList<RenaterNode>();
		List<Edge> edges = new ArrayList<Edge>();
		Dijkstra.Graph dg = new Dijkstra.Graph(nodes, edges);

		
		
		for (int i = 0; i < gateway_cords.size(); i++) {
			Node n = (Node)g.getNode(gateway_cords.get(i));
			RenaterNode kn = (RenaterNode) n.getProtocol(pid);
			nodes.add(kn);
		}
		
		for (int i = 0; i < gateway_cords.size(); i++) {
			Node n = (Node)g.getNode(gateway_cords.get(i));
			RenaterNode kn = (RenaterNode) n.getProtocol(pid);
			
			for (int j = 0; j < kn.degree(); j++) {
				RenaterNode km = (RenaterNode) kn.getNeighbor(j).getProtocol(pid);
				RenaterEdge re = kn.getEdge(km.getID());
//				dg.addEdge(i+":"+j, kn, km, NodeUtilities.getPhysicalDistance(kn, km));
				dg.addEdge(i+":"+j, kn, km, re.getLatency());
			}
		}
		
		return new Dijkstra(dg);
	}
	
	private double getBitRate(){
		double[] bitrates = {1e9, 2.5e9, 10e9};
		int rand = CommonState.r.nextInt(100);
		if(rand < 90)
			return bitrates[2];
		else if(rand >= 90 && rand < 97)
			return bitrates[1];
		else
			return bitrates[0];
	}
	
	private double getSpeed(){
		double[] speeds = {2e8, 3e8};
		return speeds[1];
	}

//	public static void main(String[] args){
//		ArrayList<AbstractMap.SimpleEntry<Integer, Double>> dists = new ArrayList<AbstractMap.SimpleEntry<Integer, Double>>();
//		dists.add(new AbstractMap.SimpleEntry<Integer, Double>(3, 5.0));
//		dists.add(new AbstractMap.SimpleEntry<Integer, Double>(5, 9.0));
//		dists.add(new AbstractMap.SimpleEntry<Integer, Double>(1, 2.0));
//		Collections.sort(dists, new Comparator<AbstractMap.SimpleEntry<Integer, Double>>() {
//			@Override
//			public int compare(AbstractMap.SimpleEntry<Integer, Double> o1, AbstractMap.SimpleEntry<Integer, Double> o2) {
//				return o1.getValue().compareTo(o2.getValue());
//				
//			}
//		});
//		
//		for(int i = 0; i < dists.size(); i++){
//			System.out.println(dists.get(i).getValue());
//		}
//	}

}
