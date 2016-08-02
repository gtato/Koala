package koala;


import messaging.KoalaMessage;
import messaging.KoalaRouteMsgContent;
import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;

public class RenaterProtocol extends TopologyProtocol implements CDProtocol {

	RenaterNode myNode;
	public RenaterProtocol(String prefix){
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
		String nid = ((KoalaRouteMsgContent)msg.getContent()).getId();
		if(!nid.equals(myNode.getID()))
            send(myNode.getRoute(nid), msg);
		else{
			onReceivedMsg(msg);
		}
//		myNode.degree();
	}

	@Override
	protected void onRoutingTable(KoalaMessage msg) {
		// TODO Auto-generated method stub
		
	}

//	@Override
//	public boolean hasJoined() {
//		return true;
//	}

	@Override
	public void intializeMyNode(Node node) {
		super.intializeMyNode(node);
		myNode = (RenaterNode) (Linkable) node.getProtocol(linkPid);
	}

	@Override
	protected void checkPiggybacked(KoalaMessage msg) {
		// TODO Auto-generated method stub
		
	}
//
//	@Override
//	protected void checkStatus() {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	protected String getProtocolName() {
		return "renater";
	}

	@Override
	protected void onLongLink(KoalaMessage msg) {
		// do nothing 
	}

}
