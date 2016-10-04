package utilities;

import java.util.*;

import peersim.core.Node;
import renater.RenaterNode;

public class Dijkstra {

	private Map<String, Edge> edges;
	private Set<RenaterNode> settledNodes;
	private Set<RenaterNode> unSettledNodes;
	private Map<RenaterNode, RenaterNode> predecessors;
	private Map<RenaterNode, Double> distance;
	private Map<String, Double> allDistance;

	public Dijkstra() {
		this.edges = new HashMap<String, Edge>();
		this.allDistance = new HashMap<String, Double>();
	}
	
	public Dijkstra(Graph graph) {
		// create a copy of the array so that we can operate on this array
		this.edges = new HashMap<String, Edge>(graph.getEdges());
		this.allDistance = new HashMap<String, Double>();
	}

	public void setGraph(Graph graph){
		this.edges = graph.getEdges();
		this.allDistance = new HashMap<String, Double>();
	}
	
	public void setDistance(RenaterNode src, RenaterNode dst, double dist){
		allDistance.put(NodeUtilities.getKeyStrID(src.getID(), dst.getID()), dist);
	}
	
	public void execute(RenaterNode source) {
		settledNodes = new HashSet<RenaterNode>();
		unSettledNodes = new HashSet<RenaterNode>();
		distance = new HashMap<RenaterNode, Double>();
		predecessors = new HashMap<RenaterNode, RenaterNode>();
		distance.put(source, 0.0);
		unSettledNodes.add(source);
		while (unSettledNodes.size() > 0) {
			RenaterNode node = getMinimum(unSettledNodes);
			settledNodes.add(node);
			unSettledNodes.remove(node);
			findMinimalDistances(node);
		}
	}

	private void findMinimalDistances(RenaterNode node) {
		ArrayList<Node> adjacentNodes = node.getNeighbors();// getNeighbors(node);
		for (Node adjac: adjacentNodes) {
			RenaterNode target = (RenaterNode)adjac.getProtocol(node.getPid());
			if (getShortestDistance(target) > getShortestDistance(node)
					+ getDistance(node, target)) {
				double dist = getShortestDistance(node) + getDistance(node, target);
				distance.put(target, dist);
				predecessors.put(target, node);
				unSettledNodes.add(target);
			}
		}

	}

	private double getDistance(RenaterNode node, RenaterNode target) {
		return edges.get(NodeUtilities.getKeyStrID(node.getID(), target.getID())).getWeight();
//		for (Edge edge : edges) {
//			if (edge.getSource().equals(node)
//					&& edge.getDestination().equals(target)) {
//				return edge.getWeight();
//			}
//		}
//		throw new RuntimeException("Should not happen");
	}

//	private List<RenaterNode> getNeighbors(RenaterNode node) {
//		node.getn
//		List<RenaterNode> neighbors = new ArrayList<RenaterNode>();
//		for (Edge edge : edges) {
//			if (edge.getSource().equals(node)
//					&& !isSettled(edge.getDestination())) {
//				neighbors.add(edge.getDestination());
//			}
//		}
//		return neighbors;
//	}

	public double getShortestDistanceBetween(RenaterNode src, RenaterNode dst){
		String key = NodeUtilities.getKeyStrID(src.getID(), dst.getID());
		if(allDistance.containsKey(key))
			return allDistance.get(key);
		return Double.MAX_VALUE;
	}
	
	private RenaterNode getMinimum(Set<RenaterNode> vertexes) {
		RenaterNode minimum = null;
		for (RenaterNode vertex : vertexes) {
			if (minimum == null) {
				minimum = vertex;
			} else {
				if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
					minimum = vertex;
				}
			}
		}
		return minimum;
	}

	private boolean isSettled(RenaterNode vertex) {
		return settledNodes.contains(vertex);
	}

	public double getShortestDistance(RenaterNode destination) {
		Double d = distance.get(destination);
		if (d == null) {
			return Double.MAX_VALUE;
		} else {
			return d;
		}
	}

	/*
	 * This method returns the path from the source to the selected target and
	 * NULL if no path exists
	 */
	public LinkedList<RenaterNode> getPath(RenaterNode target) {
		LinkedList<RenaterNode> path = new LinkedList<RenaterNode>();
		RenaterNode step = target;
		// check if a path exists
		if (predecessors.get(step) == null) {
			return null;
		}
		path.add(step);
		while (predecessors.get(step) != null) {
			step = predecessors.get(step);
			path.add(step);
		}
		// Put it into the correct order
		Collections.reverse(path);
		return path;
	}


	public static void main(String[] args){
		
//		List<RenaterNode> nodes = new ArrayList<RenaterNode>();
//		List<Edge> edges = new ArrayList<Edge>();
//		Graph graph = new Graph(nodes, edges);
//		
//		for (int i = 0; i < 11; i++) {
//			RenaterNode location = new RenaterNode("xxx");
//			location.setID("1-"+i);
//			nodes.add(location);
//		}
//		
//		graph.addEdge("Edge_0", 0, 1, 85);
//		graph.addEdge("Edge_1", 0, 2, 217);
//		graph.addEdge("Edge_2", 0, 4, 173);
//		graph.addEdge("Edge_3", 2, 6, 186);
//		graph.addEdge("Edge_4", 2, 7, 103);
//		graph.addEdge("Edge_5", 3, 7, 183);
//		graph.addEdge("Edge_6", 5, 8, 250);
//		graph.addEdge("Edge_7", 8, 9, 84);
//		graph.addEdge("Edge_8", 7, 9, 167);
//		graph.addEdge("Edge_9", 4, 9, 502);
//		graph.addEdge("Edge_10", 9, 10, 40);
//		graph.addEdge("Edge_11", 1, 10, 600);
//
//		
//		Dijkstra dijkstra = new Dijkstra(graph);
//		dijkstra.execute(nodes.get(0));
//		LinkedList<RenaterNode> path = dijkstra.getPath(nodes.get(10));
//
//
//		for (RenaterNode vertex : path) {
//			System.out.println(vertex);
//		}
	}



	public static class Graph {
		private final HashMap<String, RenaterNode> vertexes;
		private final HashMap<String, Edge> edges;

		public Graph(HashMap<String, RenaterNode> vertexes, HashMap<String, Edge> edges) {
			this.vertexes = vertexes;
			this.edges = edges;
		}

		public HashMap<String, RenaterNode> getVertexes() {
			return vertexes;
		}

		public HashMap<String, Edge> getEdges() {
			return edges;
		}
		
//		public void addEdge(String laneId, int sourceLocNo, int destLocNo, int duration) {
//			Edge lane = new Edge(laneId,vertexes.get(sourceLocNo), vertexes.get(destLocNo), duration);
//			edges.add(lane);
//		}
		
		public void addEdge(RenaterNode source, RenaterNode destination, double duration) {
			Edge lane = new Edge(NodeUtilities.getKeyStrID(source.getID(), destination.getID()),source, destination, duration);
			edges.put(lane.getId(), lane);
		}

	

	}

	public static class Edge {
		private final String id;
		private final RenaterNode source;
		private final RenaterNode destination;
		private final double weight;

		public Edge(String id, RenaterNode source, RenaterNode destination, double weight) {
			this.id = id;
			this.source = source;
			this.destination = destination;
			this.weight = weight;
		}

		public String getId() {
			return id;
		}

		public RenaterNode getDestination() {
			return destination;
		}

		public RenaterNode getSource() {
			return source;
		}

		public double getWeight() {
			return weight;
		}

		@Override
		public String toString() {
			return source + " " + destination;
		}

	}

}