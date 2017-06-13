package messaging;

import koala.KoalaNeighbor;

public class KoalaNLNMsgContent extends KoalaMsgContent{
	
	private KoalaNeighbor neighbor;
	
	public KoalaNLNMsgContent(KoalaNeighbor neighbor) {
		super(KoalaMessage.NLN);
		this.neighbor = neighbor;
	}

	public KoalaNeighbor getNeighbor() {
		return neighbor;
	}

	public void setNeighbor(KoalaNeighbor neighbor) {
		this.neighbor = neighbor;
	}
	
}

