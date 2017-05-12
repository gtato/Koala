package koala;

import java.util.Comparator;

import topology.TopologyNode;
import topology.TopologyPathNode;
import utilities.NodeUtilities;

public class KoalaNeighbor extends TopologyPathNode{

	 
	private String idealID;
	private double latency = -1;
	private int latencyQuality = -1;
	 
	
	public KoalaNeighbor(TopologyNode knd) {
		super(knd.getCID(), knd.getSID());
		this.idealID = knd.getSID();
	}
	
	public KoalaNeighbor(TopologyPathNode knd) {
		super(knd.getCID(), knd.getSID());
		this.idealID = knd.getSID();
	}
	
//	public KoalaNeighbor(String nodeID, String koalaID) {
//		super(nodeID, koalaID);
//		this.idealID = koalaID;
//	}
	
	public KoalaNeighbor(TopologyPathNode tpn, double latency, int latencyQuality) {
		super(tpn.getCID(), tpn.getSID());
		this.latency = latency;
		this.latencyQuality = latencyQuality;
		this.idealID = tpn.getSID();
	}
	
	public KoalaNeighbor(TopologyPathNode tpn,  double latency) {
		super(tpn.getCID(), tpn.getSID());
		this.latency = latency;
		this.idealID = tpn.getSID();
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
	
	
	
	public KoalaNeighbor clone(){
		return new KoalaNeighbor(super.clone(), latency, latencyQuality);
	}
	
	

	public int getLatencyQuality() {
		return latencyQuality;
	}

	public void setLatencyQuality(int latencyQuality) {
		this.latencyQuality = latencyQuality;
	}
	
	
	public String toString(){
		String ideal = getIdealID() == null? "": " " + getIdealID();
		return "(id:" + getSID() + " iid:" +  ideal+" lat:"+(int)getLatency()+" lq:" + getLatencyQuality()+")";
	}

	public String getIdealID() {
		return idealID;
	}

	public void setIdealID(String idealID) {
		this.idealID = idealID;
	}

	public void reset(){
		latency = -1;
		latencyQuality = -1;
		setCID(NodeUtilities.DEFAULTID);
		setSID(NodeUtilities.DEFAULTID);
	}
	
	public static KoalaNeighbor getDefaultNeighbor(){
		TopologyPathNode tpd = new TopologyPathNode(NodeUtilities.DEFAULTID);
		return new KoalaNeighbor(tpd);
	}
	
	public static class NeighborComparator implements Comparator<KoalaNeighbor>{
		String referenceId;
		public NeighborComparator(String ref){
			referenceId = ref;
		}
		@Override
		public int compare(KoalaNeighbor arg0, KoalaNeighbor arg1) {
			int dist1 = NodeUtilities.distance(referenceId, arg0.getSID());
			int dist2 = NodeUtilities.distance(referenceId, arg1.getSID());
			return dist1 - dist2;
		}
		
	}
}
