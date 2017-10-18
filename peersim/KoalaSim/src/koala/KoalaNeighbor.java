package koala;

import java.util.Comparator;

import peersim.config.Configuration;
import peersim.core.CommonState;
import topology.TopologyNode;
import topology.TopologyPathNode;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;

public class KoalaNeighbor extends TopologyPathNode{

	 
	private String idealID;
	private double latency = -1;
	private int latencyQuality = -1;
	private double rating = -1; 
	private boolean recentlyAdded = false;
	
	public KoalaNeighbor(TopologyNode knd) {
		super(knd.getCID(), knd.getSID());
		this.idealID = knd.getSID();
		this.latency = PhysicalDataProvider.getDefaultInterLatency();
	}
	
	public KoalaNeighbor(TopologyPathNode knd) {
		super(knd.getCID(), knd.getSID());
		this.idealID = knd.getSID();
		this.latency = PhysicalDataProvider.getDefaultInterLatency();
	}
	
//	public KoalaNeighbor(String nodeID, String koalaID) {
//		super(nodeID, koalaID);
//		this.idealID = koalaID;
//	}
	
	public KoalaNeighbor(TopologyNode knd, double latency, int latencyQuality) {
		super(knd.getCID(), knd.getSID());
		this.latency = latency;
		this.latencyQuality = latencyQuality;
		this.idealID = knd.getSID();
	}
	
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
	
	public void updateLatency(KoalaNeighbor updated)
	{
		if(updated.getLatencyQuality() >= getLatencyQuality())
		{
			setLatency(updated.getLatency());
			setLatencyQuality(updated.getLatencyQuality());
		}
	}
	
	public KoalaNeighbor cclone(){
		String oldIID = getIdealID(); 
		KoalaNeighbor kn = new KoalaNeighbor(super.cclone(), latency, latencyQuality);
		kn.setIdealID(oldIID);
		kn.setRating(rating);
		return kn; 
	}
	
	public void copy(KoalaNeighbor kn, boolean alsoIdeal){
		this.setCID(kn.getCID());
		this.setSID(kn.getSID());
		this.setLatency(kn.getLatency());
		this.setLatencyQuality(kn.getLatencyQuality());
		this.setRecentlyAdded(kn.isRecentlyAdded());
		if(alsoIdeal) this.setIdealID(kn.getIdealID());
	}
//	public KoalaNeighbor clone(){
//		String oldIID = getIdealID(); 
//		KoalaNeighbor kn = new KoalaNeighbor(super.clone(), latency, latencyQuality);
//		kn.setIdealID(oldIID);
//		return kn; 
//	}
	
	public void setRating(double r){
		rating = r;
	}
	
	public double getRating(){
		return rating;
	}

	public void setRecentlyAdded(boolean ra){
		recentlyAdded = ra;
	}
	
	public boolean isRecentlyAdded(){
		return recentlyAdded;
	}
	
	public int getLatencyQuality() {
		return latencyQuality;
	}

	public void setLatencyQuality(int latencyQuality) {
		this.latencyQuality = latencyQuality;
	}
	
	
	public String toString(){
		String ideal = getIdealID() == null? "": " " + getIdealID();
		return "(id:" + getSID() + " iid:" +  ideal+" lat:"+ PhysicalDataProvider.round(getLatency())+" lq:" + getLatencyQuality()+")";
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
	
	public boolean isBelowThreshold(){
		double helpTH = NodeUtilities.COLABORATIVE_THRESHOLD; 
		helpTH /= 100;
		if(helpTH == 1) return true;
		return rating < 1+helpTH;
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
