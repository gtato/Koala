package koala;




public class KoalaNeighbor{
	
//	public static final int LS=0;
//	public static final int LP=1;
//	
//	public static final int GS=2;
//	public static final int GP=3;
//	
//	public static final int OTHER=25;
//	
	private String nodeID;
//	private int role;
//	private boolean isUnique;
	private int latency = -1;
	private int latencyQuality = -1;

	public KoalaNeighbor(String nodeID) {
		super();
		this.nodeID = nodeID;
//		this.role = role;
//		isUnique = this.role <= 3;
		
	}
	
	public KoalaNeighbor(String nodeID, int latency, int latencyQuality) {
		super();
		this.nodeID = nodeID;
		this.latency = latency;
		this.latencyQuality = latencyQuality;
	}
	
	public KoalaNeighbor(String nodeID, int latency) {
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
	
//	public int getRole() {
//		return role;
//	}
//	
//	public void setRole(int role) {
//		this.role = role;
//		this.isUnique = this.role <= 3;
//	}
//
//	public boolean isUnique() {
//		return isUnique;
//	}
	
	public int getLatency() {
		return latency;
	}
	
	public void setLatency(int latency) {
		this.latency = latency;
	}
	
	public void update(KoalaNeighbor updated)
	{
//		setRole(updated.getRole());
		setLatency(updated.getLatency());
		setLatencyQuality(updated.getLatencyQuality());
	}
	
	public boolean equals(KoalaNeighbor n){
//		return this.getNodeID() == n.getNodeID() && this.getRole() == n.getRole();
		return this.getNodeID() == n.getNodeID();
	}

	public int getLatencyQuality() {
		return latencyQuality;
	}

	public void setLatencyQuality(int latencyQuality) {
		this.latencyQuality = latencyQuality;
	}
}
