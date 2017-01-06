package spaasclient;

public class Body {
	private Node[] nodes;
	private Edge[] edges;
	private String[] path;
	private double latency;
	
	
	public Node[] getNodes() {
		return nodes;
	}
	public void setNodes(Node[] nodes) {
		this.nodes = nodes;
	}
	public Edge[] getEdges() {
		return edges;
	}
	public void setEdges(Edge[] edges) {
		this.edges = edges;
	}
	public String[] getPath() {
		return path;
	}
	public void setPath(String[] path) {
		this.path = path;
	}
	
	public double getLatency() {
		return latency;
	}
	public void setLatency(double latency) {
		this.latency = latency;
	}
}
