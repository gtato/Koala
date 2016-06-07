package messaging;

import koala.KoalaMessage;
import koala.KoalaNeighbor;

public class KoalaGNMsgContent extends KoalaMsgContent{
	private String[] candidates;
	private KoalaNeighbor neighbor;
	
	public KoalaGNMsgContent(String[] candidates, KoalaNeighbor neighbor) {
		super(KoalaMessage.NGN);
		this.candidates = candidates;
		this.neighbor = neighbor;
	}

	public String[] getCandidates() {
		return candidates;
	}

	public void setCandidates(String[] candidates) {
		this.candidates = candidates;
	}

	public KoalaNeighbor getNeighbor() {
		return neighbor;
	}

	public void setNeighbor(KoalaNeighbor neighbor) {
		this.neighbor = neighbor;
	}
	
	
}
