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
		if(!nid.equals(myNode.getID()))
            send(myNode.getRoute(nid), msg);
		else{
			System.out.println("yey, I ("+myNode.getID()+") got a message");
			System.out.println("came to me using this path: " + msg.pathToString());
			System.out.println("it's latency is: " + msg.getLatency());
		}
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

	@Override
	protected void intializeMyNode(Node node) {
		super.intializeMyNode(node);
		myNode = (RenaterNode) (Linkable) node.getProtocol(linkPid);
	}
	
	

}
