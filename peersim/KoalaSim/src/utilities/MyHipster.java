package utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import es.usc.citius.hipster.algorithm.Algorithm.SearchListener;
import es.usc.citius.hipster.algorithm.Algorithm.SearchResult;
import es.usc.citius.hipster.algorithm.Hipster;
import es.usc.citius.hipster.graph.GraphBuilder;
import es.usc.citius.hipster.graph.GraphSearchProblem;
import es.usc.citius.hipster.graph.HipsterGraph;
import es.usc.citius.hipster.model.impl.WeightedNode;
import es.usc.citius.hipster.model.problem.SearchProblem;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import renater.RenaterEdge;
import renater.RenaterNode;
import spaasclient.ShortestPath;

public class MyHipster {
	
	private static int cacheSize = 5000;
	private static HashMap<String, ShortestPath> cache = new HashMap<String, ShortestPath>();
	private static ArrayList<String> cacheIndex = new ArrayList<String>();
	private static int cacheHit= 0;
	private static int cacheMiss= 0;
	
	private static HipsterGraph<String, Double> hg = null;
	private static GraphBuilder<String, Double> gb = null;
	
	public static void addEdge(String src, String dst, double val){
		if(gb == null)
			gb = GraphBuilder.<String, Double>create();
		gb.connect(src).to(dst).withEdge(val);
	}
	public static void createGraph(){
		hg = gb.createUndirectedGraph();
	}
	
	
	public static void createGraph(ArrayList<RenaterNode> gateways){
		GraphBuilder<String, Double> gb = GraphBuilder.<String, Double>create();
		int i=0;
		for (RenaterNode rn : gateways) {
			for (Node nnode : rn.getNeighbors()) {
				RenaterNode rm = (RenaterNode) nnode.getProtocol(NodeUtilities.RID);
				if(!rm.isGateway()) continue;
				RenaterEdge re = rn.getEdge(rm.getID());
				gb.connect(rn.getID()).to(rm.getID()).withEdge(re.getLatency());
//				System.out.println(rn.getID() + " ---" + re.getLatency() + "--- " + rm.getID());
			}
			System.out.println((i++) + " " +rn.getID());
		}
		hg = gb.createUndirectedGraph();
	}

	public static double getSPLatency(String src, String dst) {
		ShortestPath sp = getSP(src, dst);
		return sp.getLatency();
	}
	
	public static List<String> getSPPath(String src, String dst) {
		ShortestPath sp = getSP(src, dst);
		return sp.getPath();
	}
	
	private static ShortestPath getSP(String src, String dst){
		ShortestPath sp = getFromCache(src, dst);
		if(sp == null){
			SearchProblem p = GraphSearchProblem
	                .startingFrom(src)
	                .in(hg)
	                .takeCostsFromEdges()
	                .build();
			SearchResult sr = Hipster.createDijkstra(p).search(dst);
			WeightedNode fn = (WeightedNode)sr.getGoalNode();
			sp = new ShortestPath((List<String>) sr.getOptimalPaths().get(0), (double)fn.getCost());
			addToCache(src, dst,  sp);
			cacheMiss++;
		}else
			cacheHit++;
			
		return sp;
	}
	
	
	
	private static void addToCache(String source, String dest, ShortestPath sp){
//		String srcdst = NodeUtilities.getKeyStrID(source, dest);
		String srcdst = source+"|"+dest;
		if(cacheIndex.size() >= cacheSize){
			String last = cacheIndex.remove(0);
			cache.remove(last);
		}
		cacheIndex.add(srcdst);
		cache.put(srcdst, sp);
	}
	
	private static ShortestPath getFromCache(String source, String dest){
//		String srcdst = NodeUtilities.getKeyStrID(source, dest);
		String srcdst = source+"|"+dest;
		ShortestPath sp = cache.get(srcdst);
		return sp;
	}
	
	public static void printCacheStats(){
		System.out.println("hits: " + cacheHit + " misses: " + cacheMiss);
	}

	public static double[] getMinMax() {
		String src = ((RenaterNode) Network.get(CommonState.r.nextInt(Network.size())).getProtocol(NodeUtilities.RID)).getID();
		SearchProblem p = GraphSearchProblem.startingFrom(src).in(hg).takeCostsFromEdges().build();
		MinMaxListener<WeightedNode> mml = new MinMaxListener<WeightedNode>();
		Hipster.createDijkstra(p).search(mml);
		System.out.println(mml.getMinMax()[0] + " " + mml.getMinMax()[1]);
		return mml.getMinMax();
	}
	
	public static class MinMaxListener<N> implements SearchListener<N>{
		double min=Double.MAX_VALUE, max=-1;
		public double[] getMinMax(){
			return new double[]{min,max};
		}
		@Override
		public void handle(Object arg0) {
			WeightedNode fn = (WeightedNode)arg0;
			double lat = (double)fn.getCost();
			if(lat==0) return;
			if(lat < min) min = lat;
			if(lat > max) max = lat;
		}
		
	}
	
}
