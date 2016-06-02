package koala;

public class GlobalNeighborMsgContent {
	private String[] candidates;
	private KoalaNeighbor neighbor;
	
	public GlobalNeighborMsgContent(String[] candidates, KoalaNeighbor neighbor) {
		super();
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
