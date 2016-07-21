package koala;




public class KoalaNeighbor{

	private String nodeID;
	private double latency = -1;
	private int latencyQuality = -1;
	private boolean foreverAlone; //probably has not found it's DC

	public KoalaNeighbor(String nodeID, boolean foreverAlone) {
		super();
		this.nodeID = nodeID;
		this.foreverAlone = foreverAlone;
	}
	
	public KoalaNeighbor(String nodeID, double latency, int latencyQuality, boolean foreverAlone) {
		super();
		this.nodeID = nodeID;
		this.latency = latency;
		this.latencyQuality = latencyQuality;
		this.foreverAlone = foreverAlone;
		
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
		 
		setForeverAlone(updated.isForeverAlone());
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
		String alone = isForeverAlone() ? "(alone)" : "(not alone)";
		return getNodeID() + ": ["+getLatency()+", " + getLatencyQuality()+"] " + alone ;
	}

	public boolean isForeverAlone() {
		return foreverAlone;
	}

	public void setForeverAlone(boolean foreverAlone) {
		this.foreverAlone = foreverAlone;
	}
}
