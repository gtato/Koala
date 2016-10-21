package renater.initializers;



import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.WireGraph;
import peersim.graph.Graph;
import renater.RenaterEdge;
import renater.RenaterGraph;
import renater.RenaterNode;
import utilities.Dijkstra;
import utilities.DijkstraPlus;
import utilities.KoaLite;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;
import utilities.Dijkstra.Edge;

public class WireRenater extends WireGraph {

	private static final String PAR_PROT = "protocol";
	private static final String PAR_WORLD = "world_size";
	private static final String PAR_STRATEGY = "strategy";
	
	private static final String PAR_K = "k";
	private final int pid;
	private final int k;
	private final double worldSize;
	private final String strategy;
	
	public WireRenater(String prefix) {
		super(prefix);
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
		k = Configuration.getInt(prefix + "." + PAR_K, 2);
		worldSize = Configuration.getDouble(prefix + "." + PAR_WORLD, 1.0);
		strategy = Configuration.getString(prefix + "." + PAR_STRATEGY, "closest").toLowerCase();
		g = new RenaterGraph(pid,false);
	}

	@Override
	public void wire(Graph g) {
		System.out.println("Wiring up renater nodes. Setting up paths according to Dijkstra");
		
		for (int i = 0; i < Network.size(); i++) {
            Node n = (Node) g.getNode(i);   
            RenaterNode rn = (RenaterNode)n.getProtocol(pid);
            if(!rn.isGateway()){
            	((RenaterGraph)g).setEdge(i, NodeUtilities.Nodes.get(rn.getGateway()).getIndex(), new RenaterEdge(PhysicalDataProvider.getIntraDCLatency(NodeUtilities.getDCID(rn.getID()))));
            }
		}
		ArrayList<RenaterNode> gateways = new ArrayList<RenaterNode>(NodeUtilities.Gateways.values());
		switch(strategy){
			case "closest":
				wireDC(gateways); break;
			case "gradual":
				wireGradual(gateways); break;
			case "gradualrandom":
				wireGradualRandom(gateways); break;
			case "waxman":
				wireDCWaxman(gateways); break;	
		}
		
		KoaLite.createDB();
		
		if (NodeUtilities.DijkstraPlus)
			computeDijsktraPlus(gateways);
		else
			computeDijsktra(gateways);
		
		
//		setRenaterRoutes(gateway_indexes);
		
		//set max latency (according to Dijkstra)
		NodeUtilities.initializeCategories();
//		PhysicalDataProvider.printLatencyStats();
		PhysicalDataProvider.setLatencyStats();
	}

	
	
	private void  wireDC(ArrayList<RenaterNode> gateways){
		ArrayList<ArrayList< AbstractMap.SimpleEntry<Integer, Double>>> distances = new ArrayList<ArrayList<AbstractMap.SimpleEntry<Integer, Double>>>(gateways.size());  
		for(int i = 0; i < gateways.size(); i++){
			ArrayList<AbstractMap.SimpleEntry<Integer, Double>> dists = new ArrayList<AbstractMap.SimpleEntry<Integer, Double>>();
			for(int j = 0; j < gateways.size(); j++){
//			for(int j = i+1; j < gateways.size(); j++){
				if(i != j)
					dists.add(new AbstractMap.SimpleEntry<Integer, Double>(j, PhysicalDataProvider.getPhysicalDistance(gateways.get(i), gateways.get(j), worldSize)));
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
			
			RenaterNode rn = gateways.get(i);
			for(int j=0; j < dists.size(); j++)
			{
				RenaterNode rm = gateways.get(dists.get(j).getKey());
				if (rm.getEdge(rn.getID()) != null) continue;
				RenaterEdge re = new RenaterEdge(dists.get(j).getValue(), PhysicalDataProvider.getBitRate(), PhysicalDataProvider.getSpeed());
				((RenaterGraph)g).setEdge(rn.getNode().getIndex(), rm.getNode().getIndex(), re);
//				if(rn.degree() >= k)
//					break;
				if(CommonState.r.nextDouble() > 1/(rn.degree()+0.5))
					break;
			}
			
		}
		
		ArrayList<RenaterNode> ambassadors =  getAmbassadors(gateways);
		if(ambassadors.size() > 1)
			wireDC(ambassadors);

		
//		computeDijsktra(gateways);
		
	}
	
	private void  wireDCWaxman(ArrayList<RenaterNode> gateways){

		double maxDist = 0;
		for(int i = 0; i < gateways.size(); i++){
			for(int j = i+1; j < gateways.size(); j++){
				double dist = PhysicalDataProvider.getPhysicalDistance(gateways.get(i), gateways.get(j), worldSize);
				if(dist > maxDist)
					maxDist = dist;
			}
		}
		
		double L = maxDist; 
	   	double a = 0.05;
	   	double b = 0.13;
	   	
	   	for(int i = 0; i < gateways.size(); i++){
			for(int j = i+1; j < gateways.size(); j++){
				double dist = PhysicalDataProvider.getPhysicalDistance(gateways.get(i), gateways.get(j), worldSize);
				double p = b * Math.pow(Math.E, -dist/(L*a));
				if(p > CommonState.r.nextDouble()){
			   		RenaterEdge re = new RenaterEdge(dist, PhysicalDataProvider.getBitRate(), PhysicalDataProvider.getSpeed());
			   		((RenaterGraph)g).setEdge(gateways.get(i).getNode().getIndex(), gateways.get(j).getNode().getIndex(), re);
				}
				
			}
		}

	   	ArrayList<RenaterNode> ambassadors =  getAmbassadors(gateways);
		if(ambassadors.size() > 1)
			wireDC(ambassadors);
		
//	   	computeDijsktra(gateways);
	}
	
	
	private void  wireGradual(ArrayList<RenaterNode> gateways){
		double minDistance = Double.MAX_VALUE;
		double distance;
		RenaterNode closestCenter = null;
		RenaterNode center = new RenaterNode("xxx");
		center.setX(0.5);
		center.setY(0.5);
		for(int i = 0; i < gateways.size(); i++){
			distance = PhysicalDataProvider.getPhysicalDistance(gateways.get(i), center, worldSize);
			if(distance < minDistance){
				minDistance = distance;
				closestCenter = gateways.get(i);
			}
		}
		
		ArrayList<RenaterNode> linked = new ArrayList<RenaterNode>();
		linked.add(closestCenter);
		for(int i = 0; i < gateways.size(); i++){
			if(gateways.get(i).getID().equals(closestCenter.getID())) continue;
			minDistance = Double.MAX_VALUE;
			RenaterNode closestNode = null;
			for(int j = 0; j < linked.size();j++){
				distance = PhysicalDataProvider.getPhysicalDistance(gateways.get(i), linked.get(j), worldSize);
				if(distance < minDistance){
					minDistance = distance;
					closestNode = linked.get(j);
				}
			}
			RenaterEdge re = new RenaterEdge(minDistance, PhysicalDataProvider.getBitRate(), PhysicalDataProvider.getSpeed());
			((RenaterGraph)g).setEdge(gateways.get(i).getNode().getIndex(), closestNode.getNode().getIndex(), re);
			linked.add(gateways.get(i));
		}
		
//		for(int i =0; i < 1; i++)
//			addExtraLinks(gateway_indexes, gateways);
		
//		computeDijsktra(gateways);
		
	}
	
	private void  wireGradualRandom(ArrayList<RenaterNode> gateways){
		
		ArrayList<RenaterNode> linked = new ArrayList<RenaterNode>();
		linked.add(gateways.get(0));
		for(int i = 0; i < gateways.size(); i++){
			RenaterNode aNode = linked.get(CommonState.r.nextInt(linked.size()));
			double distance = PhysicalDataProvider.getPhysicalDistance(gateways.get(i), aNode, worldSize);
			RenaterEdge re = new RenaterEdge(distance, PhysicalDataProvider.getBitRate(), PhysicalDataProvider.getSpeed());
			((RenaterGraph)g).setEdge(gateways.get(i).getNode().getIndex(), aNode.getNode().getIndex(), re);
			linked.add(gateways.get(i));
		}
		
//		for(int i =0; i < 1; i++)
//			addExtraLinks(gateway_indexes, gateways);
		
//		computeDijsktra(gateways);
		
	}
	
	
	private ArrayList<RenaterNode> getAmbassadors(ArrayList<RenaterNode> gateways){
		ArrayList<HashSet<String>> groups = checkConnectivity(gateways);
		System.out.println("Groups: " + groups.size());
		
		
		ArrayList<RenaterNode> ambassadors = new ArrayList<RenaterNode>(); 
		for(HashSet<String> s : groups){
			for(String n : s){
				ambassadors.add((RenaterNode)NodeUtilities.Nodes.get(n).getProtocol(pid));
				break;
			}
		}
		return ambassadors;
		
	}
	
//	private void addExtraLinks(ArrayList<Integer> gateway_indexes, ArrayList<RenaterNode> gateways){
//		computeDijsktra(g, gateway_indexes);
//		
//		ArrayList<Integer> singleNeighborIndexes = new ArrayList<Integer>();
//		ArrayList<RenaterNode> singleNeighbors = new ArrayList<RenaterNode>();
//		for(int i=0; i < g.size(); i++){
//			RenaterNode rn = ((RenaterNode)((Node)g.getNode(i)).getProtocol(pid)); 
//			if(rn.isGateway()){
//				int gdegree = 0;
//				for(int j = 0; j < rn.degree(); j++){ 
//					RenaterNode rm = ((RenaterNode)rn.getNeighbor(j).getProtocol(pid));
//					if(!NodeUtilities.sameDC(rn, rm))
//						gdegree++;
//				}
//				if(gdegree == 1){
//					singleNeighborIndexes.add(i);
//					singleNeighbors.add(rn);
//				}
//			}
//		}
//		
//		
//		
//		
//		ArrayList<ArrayList< AbstractMap.SimpleEntry<Integer, Double>>> distances = new ArrayList<ArrayList<AbstractMap.SimpleEntry<Integer, Double>>>(gateways.size());  
//		for(int i = 0; i < singleNeighborIndexes.size(); i++){
//			ArrayList<AbstractMap.SimpleEntry<Integer, Double>> dists = new ArrayList<AbstractMap.SimpleEntry<Integer, Double>>();
//			for(int j = 0; j < gateway_indexes.size(); j++){
//				if(singleNeighborIndexes.get(i) != gateway_indexes.get(j))
//					dists.add(new AbstractMap.SimpleEntry<Integer, Double>(j, PhysicalDataProvider.getPhysicalDistance(singleNeighbors.get(i), gateways.get(j), worldSize)));
//			}
//			distances.add(dists);
//		}
//		
//		ArrayList<EdgeEntry> probabilites = new ArrayList<EdgeEntry>();
//		
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
//			for(int j=0; j < dists.size(); j++)
//			{
//				if (((RenaterGraph)g).getEdge(singleNeighborIndexes.get(i), gateway_indexes.get(dists.get(j).getKey())) != null) continue;
//				
//				double pathDist = PhysicalDataProvider.getLatency(singleNeighbors.get(i).getID(), gateways.get(dists.get(j).getKey()).getID());
//				probabilites.add(new EdgeEntry(singleNeighborIndexes.get(i), 
//												gateway_indexes.get(dists.get(j).getKey()),
//												dists.get(j).getValue(), 
//												pathDist
//												));
//						
//						
//			}
//		}
//		
//		
//		Collections.sort(probabilites, new Comparator<EdgeEntry>() {
//			@Override
//			public int compare(EdgeEntry o1, EdgeEntry o2) {
//				return -new Double(o1.getRatio()).compareTo(o2.getRatio());
//				
//			}
//			});
//		
//		int[] last_idx = new int[]{-1,-1};
//		ArrayList<Integer> inxRemove = new ArrayList<Integer>(); 
//		
//		for(int i = 0; i< probabilites.size(); i++){
//			if(probabilites.get(i).getSrc() == last_idx[0]
//				&& probabilites.get(i).getDst() == last_idx[1])
//				inxRemove.add(i);
//			last_idx[0] = probabilites.get(i).getSrc();
//			last_idx[1] = probabilites.get(i).getDst();
//			
//		}
//		
//		for(int i = 0; i< inxRemove.size(); i++)
//			probabilites.remove(inxRemove.get(i));
//			
//		
//		
//		ArrayList<Integer> linkd = new ArrayList<Integer>();
//		int n = 1;
//		int j = 0;
//		for(int i = 0; i < probabilites.size() && j < n; i++){
//			if(linkd.contains(probabilites.get(i).getSrc()) || linkd.contains(probabilites.get(i).getDst()))
//				 continue;
//			
//			linkd.add(probabilites.get(i).getSrc());
//			linkd.add(probabilites.get(i).getDst());
//			
//
//			RenaterEdge re = new RenaterEdge(probabilites.get(i).getGeoDist(), PhysicalDataProvider.getBitRate(), PhysicalDataProvider.getSpeed());
//			((RenaterGraph)g).setEdge(probabilites.get(i).getSrc(), probabilites.get(i).getDst(), re);
//			
//			j++;
////			System.out.println(probabilites.get(i).getSrc() + "-" + probabilites.get(i).getDst() + " " + probabilites.get(i).getRatio());
//		}
//			
//		
//	}
	
	
	private ArrayList<HashSet<String>>  checkConnectivity(ArrayList<RenaterNode> gateways){
		ArrayList<HashSet<String>> groups = new ArrayList<HashSet<String>>();
		
		for(int i = 0; i < gateways.size(); i++){
			boolean skip = false;
			RenaterNode rn = gateways.get(i);
			for(HashSet<String> set : groups){
				if(set.contains(rn.getID())){
					skip = true; break;
				}
			}
			if(skip)
				continue;
			HashSet<String> s = new HashSet<String>();
			addNeighborsToSet(rn, s);
			groups.add(s);

		}
		return groups; 
	}
	private boolean addNeighborsToSet(RenaterNode rn, HashSet<String> s){
		boolean addedSmth = false;
		s.add(rn.getID());
		ArrayList<RenaterNode> rneigs = new ArrayList<RenaterNode>();
		for(Node neighbor : rn.getNeighbors()){
			RenaterNode rneighbor = (RenaterNode)neighbor.getProtocol(pid);
			if (s.add(rneighbor.getID())){
				addedSmth = true;
				rneigs.add(rneighbor);
			}
		}
		
		for(RenaterNode neighbor : rneigs){
			addNeighborsToSet(neighbor, s);
		}
		
		return addedSmth;
	}
	

	
	private Dijkstra computeDijsktra(ArrayList<RenaterNode> gateways) {
		long startTime = System.currentTimeMillis();
		boolean file_exists = Files.exists(Paths.get(PhysicalDataProvider.DijsktraFile)); 
		if(file_exists) { 
		    System.out.println("Found Dijkstra file!");
		    PhysicalDataProvider.loadRoutes();
		    
		}
		
		Dijkstra dijkstra = null;
		if(!file_exists){
			dijkstra = initializeDijkstra(gateways);
			PhysicalDataProvider.clearLists();
		}
		
		double perc,prevPerc;
		prevPerc = 0; 
		
		for (int i = 0; i < gateways.size(); i++) {
			
            RenaterNode rn = gateways.get(i);
            if(!file_exists)
            	dijkstra.execute(rn);
        	for (int j = i+1; j < gateways.size(); j++) {
                RenaterNode rm = gateways.get(j);
    			
                if(!file_exists){
    				LinkedList<RenaterNode> path = dijkstra.getPath(rm);
	    			double dist = dijkstra.getShortestDistance(rm);
	    			dijkstra.setDistance(rn, rm, dist);
	    			PhysicalDataProvider.addLatency(rn.getID(), rm.getID(), dist );
	    			PhysicalDataProvider.addPath(rn.getID(), rm.getID(), path);
    			}
    			
        	}
        	
        	perc = (double)100*i/gateways.size();
			String txt = i < gateways.size()-1 ? perc + "%, " : perc + "%"; 
			if(perc - prevPerc > 10){
				System.out.print(txt);
				prevPerc = perc;
			}
            
		}
		System.out.println(" Done.");
//		if(!file_exists)
//			PhysicalDataProvider.saveRoutes();
		
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Dijkstra computed in "+totalTime + " ms");
		return dijkstra;
	}
	
	private void computeDijsktraPlus(ArrayList<RenaterNode> gateways) {
		if(KoaLite.dbExists()){
			return;
		}
			
			
			
		long startTime = System.currentTimeMillis();
		
		HashMap<String, DijkstraPlus.Vertex> vertexes = null;
		vertexes = initializeDijkstraPlus(gateways);
		PhysicalDataProvider.clearLists();
		
		
		double perc,prevPerc;
		prevPerc = 0; 
		
		for (int i = 0; i < gateways.size(); i++) {
			
            RenaterNode rn = gateways.get(i);
            DijkstraPlus.Vertex vn = vertexes.get(rn.getID());
            System.out.println(i+ "  computing paths for " + rn.getID());
            
            DijkstraPlus.computePaths(vn);
            
            ArrayList<String> entries = new ArrayList<String>();
        	for (int j = i+1; j < gateways.size(); j++) {
                RenaterNode rm = gateways.get(j);
                DijkstraPlus.Vertex vm = vertexes.get(rm.getID());
            	List<DijkstraPlus.Vertex> path = DijkstraPlus.getShortestPathTo(vm);
    			double dist = vm.minDistance;
    			String id = NodeUtilities.getKeyStrID(rn.getID(), rm.getID());
    			entries.add(id+";"+path+";"+dist);
	    			
    			 			
        	}
        	KoaLite.insertBatch(entries);
        	DijkstraPlus.resetVertexes(vertexes);
        	
        	perc = (double)100*i/gateways.size();
			String txt = i < gateways.size()-1 ? perc + "%, " : perc + "%"; 
			if(perc - prevPerc > 10){
				System.out.print(txt);
				prevPerc = perc;
			}
            
		}
		System.out.println(" Done.");

		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Dijkstra computed in "+totalTime + " ms");
		
	}
	
	
	private HashMap<String, DijkstraPlus.Vertex> initializeDijkstraPlus(ArrayList<RenaterNode> gateways){
//		ArrayList<DijkstraPlus.Vertex> vertexes = new ArrayList<DijkstraPlus.Vertex>();
		HashMap<String, DijkstraPlus.Vertex> vertexMap = new HashMap<String, DijkstraPlus.Vertex>();
		for(int i = 0; i < gateways.size(); i++){
    		RenaterNode rn = gateways.get(i);
    		DijkstraPlus.Vertex each = new DijkstraPlus.Vertex(rn.getID());
    		each.adjacencies = new ArrayList<DijkstraPlus.Edge>();
//    		vertexes.add(each);
    		vertexMap.put(each.name, each);
    	}
		
		
		for(int i = 0; i < gateways.size(); i++){
    		RenaterNode rn = gateways.get(i);
    		DijkstraPlus.Vertex each = vertexMap.get(rn.getID());
    		for(int j = 0; j < rn.degree(); j++){
    			RenaterNode rm = (RenaterNode) rn.getNeighbor(j).getProtocol(rn.getPid());
    			RenaterEdge re = rn.getEdge(rm.getID());
    			DijkstraPlus.Vertex other = vertexMap.get(rm.getID());
    			
        		each.adjacencies.add(new DijkstraPlus.Edge(other, re.getLatency()));
        		other.adjacencies.add(new DijkstraPlus.Edge(each, re.getLatency()));
    		}
    	}
		
		return vertexMap;
	}
	
	
	private Dijkstra initializeDijkstra(ArrayList<RenaterNode> gateways){
		HashMap<String, RenaterNode> nodes = new HashMap<String, RenaterNode>();
		HashMap<String, Edge> edges = new HashMap<String, Edge>();
		Dijkstra.Graph dg = new Dijkstra.Graph(nodes, edges);
		
		
		
		for (int i = 0; i < gateways.size(); i++) {
			RenaterNode kn = gateways.get(i);
			nodes.put(kn.getID(), kn);
		}
		
		for (int i = 0; i < gateways.size(); i++) {
			RenaterNode kn = gateways.get(i);
			
			for (int j = 0; j < kn.degree(); j++) {
				RenaterNode km = (RenaterNode) kn.getNeighbor(j).getProtocol(pid);
				if(!km.isGateway()) continue;
				RenaterEdge re = kn.getEdge(km.getID());
//				dg.addEdge(i+":"+j, kn, km, NodeUtilities.getPhysicalDistance(kn, km));
				dg.addEdge(kn, km, re.getLatency());
			}
		}
		
		return new Dijkstra(dg);
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

//	public class EdgeEntry{
//		int src;
//		int dst;
//		double geoDist;
//		double pathDist;
//		public EdgeEntry(int src, int dst, double geoDist, double pathDist) {
//			super();
//			this.src = src;
//			this.dst = dst;
//			this.geoDist = geoDist;
//			this.pathDist = pathDist;
//		}
//		public int getSrc() {
//			return src;
//		}
//		public int getDst() {
//			return dst;
//		}
//		public double getGeoDist() {
//			return geoDist;
//		}
//		public double getPathDist() {
//			return pathDist;
//		}
//		
//		public double getRatio() {
//			return pathDist/geoDist;
//		}
//	}
}
