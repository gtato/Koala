package koala;

import java.util.Comparator;

import utilities.NodeUtilities;

public class KoalaNeighbor{

	private String nodeID;
	private String idealID;
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
	
	public boolean equals(Object n){
		if (KoalaNode.class.isInstance(n))
			return this.equals((KoalaNode)n);
		if (KoalaNeighbor.class.isInstance(n))
			return this.equals((KoalaNeighbor)n);
		return false;
	}
	
	public boolean equals(KoalaNeighbor n){
		return this.getNodeID().equals(n.getNodeID());
	}
	public boolean equals(KoalaNode n){
		return this.getNodeID().equals(n.getID());
	}

	public int getLatencyQuality() {
		return latencyQuality;
	}

	public void setLatencyQuality(int latencyQuality) {
		this.latencyQuality = latencyQuality;
	}
	
	
	public String toString(){
		String ideal = getIdealID() == null? "": " " + getIdealID();
		return getNodeID() + " ["+getLatency()+", " + getLatencyQuality()+"]" + ideal;
	}

	public String getIdealID() {
		return idealID;
	}

	public void setIdealID(String idealID) {
		this.idealID = idealID;
	}

	public static class NeighborComparator implements Comparator<KoalaNeighbor>{
		String referenceId;
		public NeighborComparator(String ref){
			referenceId = ref;
		}
		@Override
		public int compare(KoalaNeighbor arg0, KoalaNeighbor arg1) {
			int dist1 = NodeUtilities.distance(referenceId, arg0.getNodeID());
			int dist2 = NodeUtilities.distance(referenceId, arg1.getNodeID());
			return dist1 - dist2;
		}
		
	}
}
