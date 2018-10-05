package topology;

import java.util.ArrayList;

import koala.KoalaNode;
import utilities.NodeUtilities;

public class TopologyPathNode{
	private String commonID;
	private String specificID;
	private ArrayList<Double> vivaldiCoordinates;
	private double vivaldiUncertainty = 1000;
	
	public TopologyPathNode(String commonID, String specificID, ArrayList<Double> coords, double uncertainty) {
		super();
		this.commonID = commonID;
		this.specificID = specificID;
		this.vivaldiCoordinates = coords;
		this.vivaldiUncertainty = uncertainty;
	}
	
	public TopologyPathNode(String commonID, String specificID) {
		super();
		this.commonID = commonID;
		this.specificID = specificID;
		resetVivaldiCoords();
	}
	
	public TopologyPathNode(String commonID) {
		super();
		this.commonID = commonID;
		this.specificID = commonID;
		resetVivaldiCoords();
	}
	
	public TopologyPathNode(TopologyNode tn) {
		super();
		this.commonID = tn.getCID();
		this.specificID = tn.getSID();
		resetVivaldiCoords();
	}
	
	
	public String getCID() {
		return commonID;
	}
	
	public void setCID(String commonID) {
		this.commonID = commonID;
	}
	
	public String getSID() {
		return specificID;
	}
	
	public void setSID(String specificID) {
		this.specificID = specificID;
	}
	
	
	public String toString(){
		return specificID;
	}
	
	public int getDCID(){
		return NodeUtilities.getDCID(specificID);
	}
	
	public int getNodeID(){
		return NodeUtilities.getNodeID(specificID);
	}
	
	public void resetVivaldiCoords() {
		vivaldiUncertainty = 1000;
		vivaldiCoordinates = new ArrayList<Double>();
		for(int i =0;i < NodeUtilities.VIV_DIMENSIONS;i++)
			vivaldiCoordinates.add(0.0);
	}
	
	public ArrayList<Double> getVivaldiCoordinates() {
		return vivaldiCoordinates;
	}

	public void setVivaldiCoordinates(ArrayList<Double> vivaldi_coordinates) {
		this.vivaldiCoordinates = vivaldi_coordinates;
	}

	public double getVivaldiUncertainty() {
		return vivaldiUncertainty;
	}

	public void setVivaldiUncertainty(double vivaldi_uncertainty) {
		this.vivaldiUncertainty = vivaldi_uncertainty;
	}
	
//	public TopologyPathNode clone(){
//		return new TopologyPathNode(commonID, specificID);
//	}
	
	public TopologyPathNode cclone(){
		return new TopologyPathNode(commonID, specificID, vivaldiCoordinates, vivaldiUncertainty);
	}
	
	public boolean equals(Object n){
		if (KoalaNode.class.isInstance(n))
			return this.equals((KoalaNode)n);
		if (TopologyPathNode.class.isInstance(n))
			return this.equals((TopologyPathNode)n);
		if (String.class.isInstance(n))
			return this.equals((String)n);
		return false;
	}
	
	public boolean equals(TopologyPathNode n){
		return this.getSID().equals(n.getSID());
	}
	
	public boolean equals(KoalaNode n){
		return this.getSID().equals(n.getSID());
	}
	
	public boolean equals(String n){
		return this.getSID().equals(n);
	}
}
