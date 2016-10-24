package renater;

import messaging.KoalaMessage;
import messaging.KoalaRouteMsgContent;
import peersim.core.Linkable;
import peersim.core.Node;
import topology.TopologyProtocol;

public class RenaterProtocol extends TopologyProtocol {

	RenaterNode myNode;

	public RenaterProtocol(String prefix) {
		super(prefix);
	}

	@Override
	public void join() {
		myNode.setJoined(true);
	}

	@Override
	protected void onNewGlobalNeighbours(KoalaMessage msg) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onRoute(KoalaMessage msg) {
		String nid = ((KoalaRouteMsgContent) msg.getContent()).getId();
		if (!nid.equals(myNode.getID()))
			send(myNode.getRoute(nid, msg), msg);
		else {
			onReceivedMsg(msg);
		}
		
	}

	@Override
	protected void onRoutingTable(KoalaMessage msg) {
		// TODO Auto-generated method stub

	}


	@Override
	public void intializeMyNode(Node node, int pid) {
		super.intializeMyNode(node, pid);
		myNode = (RenaterNode) (Linkable) node.getProtocol(linkPid);
	}

	@Override
	protected void checkPiggybackedAfter(KoalaMessage msg) {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected void checkPiggybackedBefore(KoalaMessage msg) {
		// TODO Auto-generated method stub

	}

	//
	// @Override
	// protected void checkStatus() {
	// // TODO Auto-generated method stub
	//
	// }

	@Override
	protected String getProtocolName() {
		return "renater";
	}

	@Override
	protected void onLongLink(KoalaMessage msg) {
		// do nothing
	}

	@Override
	protected void onReceiveLatency(String dest, double l) {
		// TODO Auto-generated method stub
		
	}



}
