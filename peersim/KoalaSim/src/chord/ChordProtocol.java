package chord;


import peersim.core.Linkable;
import peersim.core.Node;
import topology.TopologyProtocol;
import utilities.NodeUtilities;

import java.math.*;
import java.util.HashMap;

import messaging.KoalaMessage;
import messaging.KoalaRouteMsgContent;
import messaging.TopologyMessage;


public class ChordProtocol extends TopologyProtocol {

	ChordNode myNode;

	public ChordProtocol(String prefix) {
		super(prefix);
	}


	public Object clone() {
		ChordProtocol cp = (ChordProtocol) super.clone();
		return cp;
	}
	
	@Override
	public void intializeMyNode(Node node, int pid) {
		super.intializeMyNode(node, pid);
		myNode = (ChordNode) (Linkable) node.getProtocol(linkPid);
		
	}

	@Override
	public void join() {
		// TODO Auto-generated method stub
		
	}


	
	@Override
	protected String getProtocolName() {
		return "chord";
	}


	protected void onRoute(KoalaMessage msg) {
		String tid = ((KoalaRouteMsgContent)msg.getContent()).getId();
		BigInteger target = new BigInteger(tid);
		if (!tid.equals(myNode.chordId.toString())){
			ChordNode dest = myNode.findUpSuccessor(target);
			if(dest != null)
				send(dest.getID(), msg);
			else
				onFail();
		} else{
			onReceivedMsg(msg);
		}		
	}


	@Override
	protected void onReceiveLatency(String dest, double l) {
		// TODO Auto-generated method stub
		
	}





	@Override
	protected void handleMessage(TopologyMessage msg) {
		switch(msg.getType()){
		case KoalaMessage.ROUTE:
			onRoute((KoalaMessage)msg);
			break;
		}
		
	}


	@Override
	protected HashMap<Integer, TopologyMessage> getMsgStorage() {
		return NodeUtilities.CHO_MSG;
		
	}

	
}
