package renater;

import java.util.HashMap;

import messaging.KoalaMessage;
import messaging.KoalaRouteMsgContent;
import messaging.TopologyMessage;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import topology.TopologyProtocol;
import utilities.NodeUtilities;

public class RenaterProtocol extends TopologyProtocol {

	public static HashMap<Integer, TopologyMessage> REC_MSG =  new HashMap<Integer, TopologyMessage>();
	public static int SUCCESS = 0;
	public static int FAIL = 0;
	
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
				onFail(msg); // happens if the graph is disconnected, in that case we are in a real trouble
		}else {
			onSuccess(msg);
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

//	@Override
//	protected HashMap<Integer, TopologyMessage> getMsgStorage() {
//		return NodeUtilities.REN_MSG;
//	}

	@Override
	protected void handleMessage(TopologyMessage msg) {
		switch(msg.getType()){
		case KoalaMessage.ROUTE:
			onRoute((KoalaMessage)msg);
			break;
		}
		
	}


	protected void onSuccess(TopologyMessage msg) {
		msg.setReceivedCycle(CommonState.getTime());
		REC_MSG.put(msg.getID(), msg);
		SUCCESS++;
//		System.out.println(msg.getID()+  " ("+this.getClass().getName() +") "+ myNode.getID()+" got a message through: ["+msg.pathToString()+"] with latency: " +msg.getLatency());
	}
	
	protected void onFail(TopologyMessage msg){
		FAIL++;
	}

	


}
