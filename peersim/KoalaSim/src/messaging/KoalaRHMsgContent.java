package messaging;

import koala.KoalaNeighbor;
import topology.TopologyPathNode;

public class KoalaRHMsgContent extends KoalaMsgContent{
	
	private TopologyPathNode neighbor;
	private double score;
	private boolean request;
	private String label;
	
	public KoalaRHMsgContent(String label, TopologyPathNode neighbor, double score, boolean request) {
		super(KoalaMessage.RH);
		this.label = label;
		this.neighbor = neighbor;
		this.score = score;
		this.request = request;
	}

	public TopologyPathNode getNeighbor() {
		return neighbor;
	}

	public void setNeighbor(TopologyPathNode neighbor) {
		this.neighbor = neighbor;
	}
	
	
	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
	
	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public boolean isRequest() {
		return this.request;
	}
	
	public void setRequest(boolean req) {
		this.request = req;
	}
}
