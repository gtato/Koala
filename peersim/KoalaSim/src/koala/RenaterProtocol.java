package koala;


import messaging.KoalaMessage;
import messaging.KoalaRouteMsgContent;
import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;

public class RenaterProtocol extends TopologyProtocol implements CDProtocol {

	public RenaterProtocol(String prefix){
		super(prefix);
	}

	@Override
	protected void join() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onNewGlobalNeighbours(KoalaMessage msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onRoute(KoalaMessage msg) {
		String nid = ((KoalaRouteMsgContent)msg.getContent()).getId();
//		myNode.degree();
	}

	@Override
	protected void onRoutingTable(KoalaMessage msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasJoined() {
		return true;
	}
	
	

}
