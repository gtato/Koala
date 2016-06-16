package koala.utility;

import java.util.*;

public class Dijkstra {

	private final List<Vertex> nodes;
	private final List<Edge> edges;
	private Set<Vertex> settledNodes;
	private Set<Vertex> unSettledNodes;
	private Map<Vertex, Vertex> predecessors;
	private Map<Vertex, Integer> distance;

	public Dijkstra(Graph graph) {
		// create a copy of the array so that we can operate on this array
		this.nodes = new ArrayList<Vertex>(graph.getVertexes());
		this.edges = new ArrayList<Edge>(graph.getEdges());
	}

	public void execute(Vertex source) {
		settledNodes = new HashSet<Vertex>();
		unSettledNodes = new HashSet<Vertex>();
		distance = new HashMap<Vertex, Integer>();
		predecessors = new HashMap<Vertex, Vertex>();
		distance.put(source, 0);
		unSettledNodes.add(source);
		while (unSettledNodes.size() > 0) {
			Vertex node = getMinimum(unSettledNodes);
			settledNodes.add(node);
			unSettledNodes.remove(node);
			findMinimalDistances(node);
		}
	}

	private void findMinimalDistances(Vertex node) {
		List<Vertex> adjacentNodes = getNeighbors(node);
		for (Vertex target : adjacentNodes) {
			if (getShortestDistance(target) > getShortestDistance(node)
					+ getDistance(node, target)) {
				distance.put(target,
						getShortestDistance(node) + getDistance(node, target));
				predecessors.put(target, node);
				unSettledNodes.add(target);
			}
		}

	}

	private int getDistance(Vertex node, Vertex target) {
		for (Edge edge : edges) {
			if (edge.getSource().equals(node)
					&& edge.getDestination().equals(target)) {
				return edge.getWeight();
			}
		}
		throw new RuntimeException("Should not happen");
	}

	private List<Vertex> getNeighbors(Vertex node) {
		List<Vertex> neighbors = new ArrayList<Vertex>();
		for (Edge edge : edges) {
			if (edge.getSource().equals(node)
					&& !isSettled(edge.getDestination())) {
				neighbors.add(edge.getDestination());
			}
		}
		return neighbors;
	}

	private Vertex getMinimum(Set<Vertex> vertexes) {
		Vertex minimum = null;
		for (Vertex vertex : vertexes) {
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

	private boolean isSettled(Vertex vertex) {
		return settledNodes.contains(vertex);
	}

	private int getShortestDistance(Vertex destination) {
		Integer d = distance.get(destination);
		if (d == null) {
			return Integer.MAX_VALUE;
		} else {
			return d;
		}
	}

	/*
	 * This method returns the path from the source to the selected target and
	 * NULL if no path exists
	 */
	public LinkedList<Vertex> getPath(Vertex target) {
		LinkedList<Vertex> path = new LinkedList<Vertex>();
		Vertex step = target;
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

	private static void addLane(List<Vertex> nodes, List<Edge> edges, String laneId, int sourceLocNo, int destLocNo, int duration) {
		Edge lane = new Edge(laneId,nodes.get(sourceLocNo), nodes.get(destLocNo), duration);
		edges.add(lane);
	}


	public static void main(String[] args){
		List<Vertex> nodes = new ArrayList<Vertex>();
		List<Edge> edges = new ArrayList<Edge>();

		for (int i = 0; i < 11; i++) {
			Vertex location = new Vertex("Node_" + i, "Node_" + i);
			nodes.add(location);
		}

		addLane(nodes, edges, "Edge_0", 0, 1, 85);
		addLane(nodes, edges, "Edge_1", 0, 2, 217);
		addLane(nodes, edges, "Edge_2", 0, 4, 173);
		addLane(nodes, edges, "Edge_3", 2, 6, 186);
		addLane(nodes, edges, "Edge_4", 2, 7, 103);
		addLane(nodes, edges, "Edge_5", 3, 7, 183);
		addLane(nodes, edges, "Edge_6", 5, 8, 250);
		addLane(nodes, edges, "Edge_7", 8, 9, 84);
		addLane(nodes, edges, "Edge_8", 7, 9, 167);
		addLane(nodes, edges, "Edge_9", 4, 9, 502);
		addLane(nodes, edges, "Edge_10", 9, 10, 40);
		addLane(nodes, edges, "Edge_11", 1, 10, 600);

		Graph graph = new Graph(nodes, edges);
		Dijkstra dijkstra = new Dijkstra(graph);
		dijkstra.execute(nodes.get(0));
		LinkedList<Vertex> path = dijkstra.getPath(nodes.get(10));


		for (Vertex vertex : path) {
			System.out.println(vertex);
		}
	}



	public static class Graph {
		private final List<Vertex> vertexes;
		private final List<Edge> edges;

		public Graph(List<Vertex> vertexes, List<Edge> edges) {
			this.vertexes = vertexes;
			this.edges = edges;
		}

		public List<Vertex> getVertexes() {
			return vertexes;
		}

		public List<Edge> getEdges() {
			return edges;
		}

	}

	public static class Edge {
		private final String id;
		private final Vertex source;
		private final Vertex destination;
		private final int weight;

		public Edge(String id, Vertex source, Vertex destination, int weight) {
			this.id = id;
			this.source = source;
			this.destination = destination;
			this.weight = weight;
		}

		public String getId() {
			return id;
		}

		public Vertex getDestination() {
			return destination;
		}

		public Vertex getSource() {
			return source;
		}

		public int getWeight() {
			return weight;
		}

		@Override
		public String toString() {
			return source + " " + destination;
		}

	}

	public static class Vertex {
		final private String id;
		final private String name;

		public Vertex(String id, String name) {
			this.id = id;
			this.name = name;
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Vertex other = (Vertex) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return name;
		}

	}
}