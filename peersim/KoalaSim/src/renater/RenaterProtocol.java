package renater;

import java.util.HashMap;

import messaging.KoalaMessage;
import messaging.KoalaRouteMsgContent;
import messaging.TopologyMessage;
import peersim.core.Linkable;
import peersim.core.Node;
import topology.TopologyProtocol;
import utilities.NodeUtilities;

public class RenaterProtocol extends TopologyProtocol {

	RenaterNode myNode;

	public RenaterProtocol(String prefix) {
		super(prefix);
	}

	@Override
	public void join() {
		myNode.setJoined(true);
	}



	
	protected void onRoute(KoalaMessage msg) {
		
		String nid = ((KoalaRouteMsgContent) msg.getContent()).getId();
		if (!nid.equals(myNode.getID())){
			String dest = myNode.getRoute(nid, msg);
			if (dest != null)
				send(dest, msg);
			else
				onFail(); // happens if the graph is disconnected, in that case we are in a real trouble
		}else {
			onReceivedMsg(msg);
		}
		
	}


	@Override
	public void intializeMyNode(Node node, int pid) {
		super.intializeMyNode(node, pid);
		myNode = (RenaterNode) (Linkable) node.getProtocol(linkPid);
	}

	

	@Override
	protected String getProtocolName() {
		return "renater";
	}

	
	@Override
	protected void onReceiveLatency(String dest, double l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected HashMap<Integer, TopologyMessage> getMsgStorage() {
		return NodeUtilities.REN_MSG;
	}

	@Override
	protected void handleMessage(TopologyMessage msg) {
		switch(msg.getType()){
		case KoalaMessage.ROUTE:
			onRoute((KoalaMessage)msg);
			break;
		}
		
	}



}
