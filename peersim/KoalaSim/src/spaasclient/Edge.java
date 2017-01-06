package spaasclient;

public class Edge {
	private String source;
	private String destination;
	private double weight;

	public Edge(String source, String destination, double weight) {
		super();
		this.source = source;
		this.destination = destination;
		this.weight = weight;
	}

	public Edge(String source, String destination) {
		super();
		this.source = source;
		this.destination = destination;
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	
}
