package koala;

import java.util.ArrayDeque;
import java.util.HashMap;

import example.hot.InetCoordinates;
import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import koala.utility.ErrorDetection;
import koala.utility.KoalaJsonParser;
import koala.utility.PhysicalDataProvider;
import messaging.KoalaMessage;
import messaging.KoalaNGNMsgContent;

public abstract class TopologyProtocol implements CDProtocol {
	protected ArrayDeque<String> queue;
	protected TopologyNode myNode = null;

	protected HashMap<Integer, KoalaMessage> receivedMsgs;
	
	protected int linkPid = -1;
	protected int myPid = -1;

	protected boolean joined;
	
	public TopologyProtocol(String prefix) {
//		queue = new ArrayDeque<String>();
//        receivedMsgs = new HashMap<Integer, KoalaMessage>();
	}
	
	public Object clone() {
		TopologyProtocol inp = null;
        try {
            inp = (TopologyProtocol) super.clone();
            inp.queue = new ArrayDeque<String>();
            inp.receivedMsgs = new HashMap<Integer, KoalaMessage>();
        } catch (CloneNotSupportedException e) {
        } // never happens
        return inp;
    }
	
	public abstract boolean hasJoined();
	

	public void setJoined(boolean joined) {
		this.joined = joined;
	}

	
	public boolean hasEmptyQueue(){
		return queue.size() == 0;
	}
	
	public void registerMsg(KoalaMessage msg){
		String msgStr = KoalaJsonParser.toJson(msg);
		queue.add(msgStr);
	}

	protected void onReceivedMsg(KoalaMessage msg) {
		receivedMsgs.put(msg.getID(), msg);
//		System.out.println(msg.getID()+  " ("+this.getClass().getName() +") "+ myNode.getID()+" got a message through: ["+msg.pathToString()+"] with latency: " +msg.getLatency());
	}
	
	public KoalaMessage removeReceivedMsg(int msgID) {
		return receivedMsgs.remove(msgID);
	}
	
	public boolean hasReceivedMsg(int msgID) {
		return receivedMsgs.containsKey(msgID);
	}
	
	public KoalaMessage getReceivedMsg(int msgID) {
		return receivedMsgs.get(msgID);
	}
	
	public void receive()
	{
		if(queue.size() == 0)
			return;
		
		String msgStr = queue.remove();
		KoalaMessage msg = KoalaJsonParser.jsonToObject(msgStr, KoalaMessage.class);
		String logmsg = myNode.getID() + " received a message from " + msg.getSecondLastSenderNode()  + " a msg of type: " + msg.getTypeName();
		
		switch(msg.getType()){
			case KoalaMessage.RT:
				onRoutingTable(msg);
				break;
			case KoalaMessage.ROUTE:
				onRoute(msg);
				break;
			case KoalaMessage.NGN:
				logmsg += " " + ((KoalaNGNMsgContent )msg.getContent()).getNeighbor().getNodeID() ; 
				onNewGlobalNeighbours(msg);
				break;
			case KoalaMessage.JOIN:
				join();
				break;
		}
		if(CommonState.getTime() <= 69)
			System.out.println(logmsg);
	}
	protected abstract void join();

	protected abstract void onNewGlobalNeighbours(KoalaMessage msg);

	protected abstract void onRoute(KoalaMessage msg);

	protected abstract void onRoutingTable(KoalaMessage msg);
	
	protected void intializeMyNode(Node node){
		myNode = (TopologyNode) (Linkable) node.getProtocol(linkPid);
	}

	public void send(String destinationID, KoalaMessage msg)
	{
		Node each = null;
		for (int i = 0; i < Network.size(); i++) {
            each =  Network.get(i);
            if(((TopologyNode)each.getProtocol(linkPid)).getID().equals(destinationID))
            	break;
		}
		if(each != null){
			if(ErrorDetection.hasLoopCommunication(myNode.getID(),destinationID))
				System.out.println("problems in horizont");
				
//			System.out.println(me.getID() +"->"+ destinationID);
//			msg.setRandomLatency(myNode.getID(), destinationID);
			double l = PhysicalDataProvider.getLatency(myNode.getID(), destinationID);
//			double l = 1;
			if(l <= 0)
				System.out.println("someone invented time traveling!");
			msg.setLatency(msg.getLatency() + l);
			if(msg.getLastSenderNode() == null)
				msg.addToPath(myNode.getID());
			msg.addToPath(destinationID);
			((TopologyProtocol)each.getProtocol(myPid)).registerMsg(msg);
			/*TODO: uncomment this later*/
//			((KoalaProtocol)each.getProtocol(koalaPid)).receive();
		}
	}
	
	
	@Override
	public void nextCycle(Node node, int protocolID) {
		myPid = protocolID;
		linkPid = FastConfig.getLinkable(protocolID);
		intializeMyNode(node);
		
//		System.out.print(myNode.getID() + "  ");
		receive();
	}
	


	
}
