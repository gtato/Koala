package koala.utility;

import java.util.*;

import koala.KoalaNode;

public class Dijkstra {

//	private List<KoalaNode> nodes;
	private List<Edge> edges;
	private Set<KoalaNode> settledNodes;
	private Set<KoalaNode> unSettledNodes;
	private Map<KoalaNode, KoalaNode> predecessors;
	private Map<KoalaNode, Double> distance;

	public Dijkstra() {
//		this.nodes = new ArrayList<KoalaNode>();
		this.edges = new ArrayList<Edge>();
	}
	
	public Dijkstra(Graph graph) {
		// create a copy of the array so that we can operate on this array
//		this.nodes = new ArrayList<KoalaNode>(graph.getVertexes());
		this.edges = new ArrayList<Edge>(graph.getEdges());
	}

	public void setGraph(Graph graph){
//		this.nodes = new ArrayList<KoalaNode>(graph.getVertexes());
		this.edges = new ArrayList<Edge>(graph.getEdges());
	}
	
	public void execute(KoalaNode source) {
		settledNodes = new HashSet<KoalaNode>();
		unSettledNodes = new HashSet<KoalaNode>();
		distance = new HashMap<KoalaNode, Double>();
		predecessors = new HashMap<KoalaNode, KoalaNode>();
		distance.put(source, 0.0);
		unSettledNodes.add(source);
		while (unSettledNodes.size() > 0) {
			KoalaNode node = getMinimum(unSettledNodes);
			settledNodes.add(node);
			unSettledNodes.remove(node);
			findMinimalDistances(node);
		}
	}

	private void findMinimalDistances(KoalaNode node) {
		List<KoalaNode> adjacentNodes = getNeighbors(node);
		for (KoalaNode target : adjacentNodes) {
			if (getShortestDistance(target) > getShortestDistance(node)
					+ getDistance(node, target)) {
				distance.put(target,
						getShortestDistance(node) + getDistance(node, target));
				predecessors.put(target, node);
				unSettledNodes.add(target);
			}
		}

	}

	private double getDistance(KoalaNode node, KoalaNode target) {
		for (Edge edge : edges) {
			if (edge.getSource().equals(node)
					&& edge.getDestination().equals(target)) {
				return edge.getWeight();
			}
		}
		throw new RuntimeException("Should not happen");
	}

	private List<KoalaNode> getNeighbors(KoalaNode node) {
		List<KoalaNode> neighbors = new ArrayList<KoalaNode>();
		for (Edge edge : edges) {
			if (edge.getSource().equals(node)
					&& !isSettled(edge.getDestination())) {
				neighbors.add(edge.getDestination());
			}
		}
		return neighbors;
	}

	private KoalaNode getMinimum(Set<KoalaNode> vertexes) {
		KoalaNode minimum = null;
		for (KoalaNode vertex : vertexes) {
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

	private boolean isSettled(KoalaNode vertex) {
		return settledNodes.contains(vertex);
	}

	private double getShortestDistance(KoalaNode destination) {
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
	public LinkedList<KoalaNode> getPath(KoalaNode target) {
		LinkedList<KoalaNode> path = new LinkedList<KoalaNode>();
		KoalaNode step = target;
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
		
		List<KoalaNode> nodes = new ArrayList<KoalaNode>();
		List<Edge> edges = new ArrayList<Edge>();
		Graph graph = new Graph(nodes, edges);
		
		for (int i = 0; i < 11; i++) {
			KoalaNode location = new KoalaNode("xxx");
			location.setID("1-"+i);
			nodes.add(location);
		}
		
		graph.addEdge("Edge_0", 0, 1, 85);
		graph.addEdge("Edge_1", 0, 2, 217);
		graph.addEdge("Edge_2", 0, 4, 173);
		graph.addEdge("Edge_3", 2, 6, 186);
		graph.addEdge("Edge_4", 2, 7, 103);
		graph.addEdge("Edge_5", 3, 7, 183);
		graph.addEdge("Edge_6", 5, 8, 250);
		graph.addEdge("Edge_7", 8, 9, 84);
		graph.addEdge("Edge_8", 7, 9, 167);
		graph.addEdge("Edge_9", 4, 9, 502);
		graph.addEdge("Edge_10", 9, 10, 40);
		graph.addEdge("Edge_11", 1, 10, 600);

		
		Dijkstra dijkstra = new Dijkstra(graph);
		dijkstra.execute(nodes.get(0));
		LinkedList<KoalaNode> path = dijkstra.getPath(nodes.get(10));


		for (KoalaNode vertex : path) {
			System.out.println(vertex);
		}
	}



	public static class Graph {
		private final List<KoalaNode> vertexes;
		private final List<Edge> edges;

		public Graph(List<KoalaNode> vertexes, List<Edge> edges) {
			this.vertexes = vertexes;
			this.edges = edges;
		}

		public List<KoalaNode> getVertexes() {
			return vertexes;
		}

		public List<Edge> getEdges() {
			return edges;
		}
		
		public void addEdge(String laneId, int sourceLocNo, int destLocNo, int duration) {
			Edge lane = new Edge(laneId,vertexes.get(sourceLocNo), vertexes.get(destLocNo), duration);
			edges.add(lane);
		}
		
		public void addEdge(String laneId, KoalaNode source, KoalaNode destination, double duration) {
			Edge lane = new Edge(laneId,source, destination, duration);
			edges.add(lane);
		}

	

	}

	public static class Edge {
		private final String id;
		private final KoalaNode source;
		private final KoalaNode destination;
		private final double weight;

		public Edge(String id, KoalaNode source, KoalaNode destination, double weight) {
			this.id = id;
			this.source = source;
			this.destination = destination;
			this.weight = weight;
		}

		public String getId() {
			return id;
		}

		public KoalaNode getDestination() {
			return destination;
		}

		public KoalaNode getSource() {
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