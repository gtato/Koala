package koala;




public class KoalaNeighbor{

	private String nodeID;
	private double latency = -1;
	private int latencyQuality = -1;

	
	public KoalaNeighbor(String nodeID) {
		super();
		this.nodeID = nodeID;
	}
	
	public KoalaNeighbor(String nodeID, double latency, int latencyQuality) {
		super();
		this.nodeID = nodeID;
		this.latency = latency;
		this.latencyQuality = latencyQuality;
	}
	
	public KoalaNeighbor(String nodeID, double latency) {
		super();
		this.nodeID = nodeID;
		this.latency = latency;
		
	}
	
	
	public String getNodeID() {
		return nodeID;
	}
	
	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}
	
	public double getLatency() {
		return latency;
	}
	
	public void setLatency(double latency) {
		this.latency = latency;
	}
	
	public void update(KoalaNeighbor updated)
	{
		if(updated.getLatencyQuality() >= getLatency())
		{
			setLatency(updated.getLatency());
			setLatencyQuality(updated.getLatencyQuality());
		}
	}
	
	public boolean equals(KoalaNeighbor n){
		return this.getNodeID().equals(n.getNodeID());
	}

	public int getLatencyQuality() {
		return latencyQuality;
	}

	public void setLatencyQuality(int latencyQuality) {
		this.latencyQuality = latencyQuality;
	}
	
	
	
	public String toString(){

		return getNodeID() + ": ["+getLatency()+", " + getLatencyQuality()+"]";
	}

}
