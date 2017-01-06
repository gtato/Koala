package messaging;

import chord.ChordNode;

public class ChordLookUpContent extends TopologyMessageContent{

	ChordNode chordNode;
	ChordNode joiningNode;
	
	public ChordLookUpContent(int msgType, ChordNode cn) {
		super(msgType);
		this.chordNode = (ChordNode) cn.clone();
	}
	
	public ChordNode getChordNode() {
		return chordNode;
	}
	
	public void setChordNode(ChordNode chordNode) {
		this.chordNode = chordNode;
	}
	
	public ChordNode getJoiningNode() {
		return joiningNode;
	}
	
	public void setJoiningNode(ChordNode chordNode) {
		this.joiningNode = chordNode;
	}
	

}
