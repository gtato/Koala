package koala.initializers;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;

import koala.KoalaNode;
import koala.RenaterEdge;
import koala.RenaterNode;
import koala.utility.Dijkstra;
import koala.utility.NodeUtilities;
import koala.utility.Dijkstra.Edge;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.WireGraph;
import peersim.graph.FastUndirGraph;
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
		for (int i = Network.size()-1; i >= 0; i--) {
            Node n = (Node) g.getNode(i);            
            RenaterNode rn = (RenaterNode)n.getProtocol(pid);
            if(rn.isGateway()){
            	gateways.add(rn);
            	gateway_indexes.add(i);
            	centerIndex = i;
            }else{
            	Node m = (Node) g.getNode(centerIndex);
                RenaterNode rm = (RenaterNode)m.getProtocol(pid);
//                rn.addNeighbor(m);
//				rm.addNeighbor(n);
            	((RenaterGraph)g).setEdge(i, centerIndex, new RenaterEdge(2e-2,getBitRate(), getSpeed()));
            }
		}
		
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
			
			Node n = (Node)g.getNode(gateway_indexes.get(i));
			RenaterNode kn = (RenaterNode) n.getProtocol(pid);
			
			for(int j=0; j < dists.size() && j<k; j++)
			{
				Node m = (Node)g.getNode(dists.get(j).getKey());
				RenaterNode km = (RenaterNode) m.getProtocol(pid);
				//kn.addNeighbor(m);
				//km.addNeighbor(n);
				RenaterEdge re = new RenaterEdge(dists.get(j).getValue(), getBitRate(), getSpeed());
				((RenaterGraph)g).setEdge(gateway_indexes.get(i), dists.get(j).getKey(), re);
				
			}
		}
		
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
    			rn.addRoute(rm.getID(), path.get(1).getID());
        	}
            
		}
		
//		for (int i = 0; i < Network.size(); i++) {
//			Node n = (Node) g.getNode(i);
//            RenaterNode rn = (RenaterNode)n.getProtocol(pid);
//            if(rn.isGateway()){
//            	dijkstra.execute(rn);
//            	for (int j = 0; j < Network.size(); j++) {
//            		Node m = (Node) g.getNode(j);
//                    RenaterNode rm = (RenaterNode)m.getProtocol(pid);
//            		if (rm.isGateway() && !n.equals(m)){
//            			LinkedList<RenaterNode> path = dijkstra.getPath(rm);
//            			rn.addRoute(rm.getID(), path.get(1).getID());
//            		}
//            	}
//            }
//		}
	         

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
