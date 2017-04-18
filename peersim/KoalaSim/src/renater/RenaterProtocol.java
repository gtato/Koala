package renater;

import java.util.HashMap;

import messaging.KoalaMessage;
import messaging.KoalaRouteMsgContent;
import messaging.TopologyMessage;
import peersim.config.Configuration;
import peersim.core.CommonState;

import peersim.core.Node;
import topology.TopologyPathNode;
import topology.TopologyProtocol;
import utilities.NodeUtilities;

public class RenaterProtocol extends TopologyProtocol {

	public static HashMap<Integer, TopologyMessage> REC_MSG =  new HashMap<Integer, TopologyMessage>();
	public static int SUCCESS = 0;
	public static int FAIL = 0;
	
	RenaterNode myNode;
	private static final String PAR_SKIP= "skip";
	private boolean skip;
	public RenaterProtocol(String prefix) {
		super(prefix);
		skip = Configuration.getBoolean(prefix + "." +PAR_SKIP, false); 
	}

	@Override
	public void join() {
		myNode.setJoined(true);
	}
	
	protected void onRoute(KoalaMessage msg) {
		String nid = ((KoalaRouteMsgContent) msg.getContent()).getNode().getCID();
		
		if(skip){
			myNode.setAllRoute(nid, msg);
			onSuccess(msg);
			return;
		}
		
		if (!nid.equals(myNode.getCID())){
			String dest = myNode.getRoute(nid, msg);
			if (dest != null)
				send(new TopologyPathNode(dest), msg);
			else
				onFail(msg); // happens if the graph is disconnected, in that case we are in a real trouble
		}else {
			onSuccess(msg);
		}
		
	}


	@Override
	public void intializeMyNode(Node node, int pid) {
		super.intializeMyNode(node, pid);
		myNode = (RenaterNode) node.getProtocol(NodeUtilities.RID);
	}

	

	@Override
	protected String getProtocolName() {
		return "renater";
	}

	
	@Override
	protected void onReceiveLatency(TopologyPathNode dest, double l) {
		// TODO Auto-generated method stub
		
	}

//	@Override
//	protected HashMap<Integer, TopologyMessage> getMsgStorage() {
//		return NodeUtilities.REN_MSG;
//	}

	@Override
	public void handleMessage(TopologyMessage msg) {
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
