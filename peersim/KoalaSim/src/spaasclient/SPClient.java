package spaasclient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import peersim.graph.Graph;

import com.google.gson.Gson;

import renater.RenaterEdge;
import renater.RenaterNode;
import spaasclient.Body;
import spaasclient.Edge;
import spaasclient.Node;
import spaasclient.Request;
import utilities.NodeUtilities;


public class SPClient {
	
	private static Gson gson = new Gson();
	
	private static int cacheSize = 5000;
	private static HashMap<String, ShortestPath> cache = new HashMap<String, ShortestPath>();
	private static ArrayList<String> cacheIndex = new ArrayList<String>();
	private static double min = Double.MAX_VALUE;
	private static int cacheHit= 0;
	private static int cacheMiss= 0;
	
	public static void main(String[] args){
		Random r = new Random(123);
//		Gson gson = new Gson();
		
		
		String[] ids = new String[]{"A", "B", "C", "D", "E"};
		
		Node[] nodes = new Node[5];
		ArrayList<Edge> edgesList = new ArrayList<Edge>();
		
		for(int i = 0; i < ids.length; i++)
			nodes[i] = new Node(ids[i], r.nextInt(100), r.nextInt(100));

		Edge[] edges = new Edge[6];
		edges[0] = new Edge("A", "B");
		edges[1] = new Edge("A", "E");
		edges[2] = new Edge("B", "C");
		edges[3] = new Edge("C", "E");
		edges[4] = new Edge("C", "D");
		edges[5] = new Edge("E", "D");
		
//		for(int i = 0; i < ids.length; i++)
//			for(int j = i+1; j < ids.length-1; j++){
//				if(r.nextDouble() > 0.2)
//					edgesList.add(new Edge(nodes[i].getId(), nodes[j].getId()));
//			}
		
		
//		Edge[] edges = new Edge[edgesList.size()];
//		edges = edgesList.toArray(edges);
		Body b = new Body();
		b.setNodes(nodes);
		b.setEdges(edges);
		Request req = new Request("uploadGraph", b);
		String data = gson.toJson(req);
//		System.out.println(data);
		String reply = post(data);
		System.out.println(reply);
		
		b = new Body();
		ArrayList<String> p = new ArrayList<String>();
		p.add("A"); p.add("D");
		b.setPath(p);
		req = new Request("sp", b);
		data = gson.toJson(req);
		
		reply = post(data);
		System.out.println(reply);
	} 
	
	public static String post(String data){
		Document doc;
		try {
			doc = Jsoup.connect("http://localhost:8080")
					.header("Accept", "application/xml")
					.data("data", data)
					.post();
			return doc.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	public static void uploadGraph(Graph g){
		Node[] nodes = new Node[g.size()];
		ArrayList<Edge> edgesList = new ArrayList<Edge>();
		for(int i = 0; i < g.size(); i++){
			peersim.core.Node n = (peersim.core.Node) g.getNode(i);   
            RenaterNode rn = (RenaterNode)n.getProtocol(NodeUtilities.RID);
            nodes[i] = new Node(rn.getCID(), rn.getX(), rn.getY());
            for(int j = 0; j < rn.degree(); j++){
            	RenaterNode rm = (RenaterNode)rn.getNeighbor(j).getProtocol(NodeUtilities.RID);
            	RenaterEdge re = rn.getEdge(rm.getCID());
            	edgesList.add(new Edge(rn.getCID(), rm.getCID(), re.getLatency()));
            }
		}
		Edge[] edges = new Edge[edgesList.size()];
		edges = edgesList.toArray(edges);
		
		Body b = new Body();
		b.setNodes(nodes);
		b.setEdges(edges);
		Request req = new Request("uploadGraph", b);
		String data = gson.toJson(req);
//		System.out.println(data);
		String reply = post(data);
	}
	
	public static ShortestPath getSP(String source, String dst){
		ShortestPath sp = getFromCache(source, dst);
		if(sp != null){
			cacheHit++;
			return sp;
		}
		cacheMiss++;
		Body b = new Body();
		ArrayList<String> p = new ArrayList<String>();
		p.add(source); p.add(dst);
		b.setPath(p);
		Request req = new Request("sp", b);
//		req.setParameters("dijkstra");
//		req.setParameters("astar");
		String data = gson.toJson(req);
		
		String reply = post(data);
		Body response = gson.fromJson(reply, Body.class);
		ArrayList<String> path = response.getPath();
		double latency = response.getLatency();
		sp = new ShortestPath(path, latency);
		addToCache(source, dst, sp);
		return sp;
	}
	
	public static double[] getMinMax()
	{
		double[] minmax = new double[2];
		if(min == Double.MAX_VALUE)
		{
			Request req = new Request("minmax", new Body());
			String data = gson.toJson(req);
			String reply = post(data);
			minmax = gson.fromJson(reply, double[].class);
		}
		return minmax;
	}
	
	private static void addToCache(String source, String dest, ShortestPath sp){
		String srcdst = NodeUtilities.getKeyStrID(source, dest);
		if(cacheIndex.size() >= cacheSize){
			String last = cacheIndex.remove(0);
			cache.remove(last);
		}
		cacheIndex.add(srcdst);
		cache.put(srcdst, sp);
	}
	
	private static ShortestPath getFromCache(String source, String dest){
		String srcdst = NodeUtilities.getKeyStrID(source, dest);
		ShortestPath sp = cache.get(srcdst);
		if(sp != null && 
		   sp.getPath()!=null && 
		   sp.getPath().size()> 0 &&
		   sp.getPath().get(0).equals(dest))
				Collections.reverse(sp.getPath());
		
		return sp;
	}
	
	public static void printCacheStats(){
		System.out.println("hits: " + cacheHit + " misses: " + cacheMiss);
	}
	
	
}
