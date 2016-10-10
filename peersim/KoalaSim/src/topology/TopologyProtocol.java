package topology;

import java.util.ArrayList;
import java.util.HashMap;

import koala.KoalaNeighbor;
import koala.KoalaProtocol;
import messaging.KoalaMessage;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;
import renater.initializers.RenaterInitializer;
import topology.controllers.ResultCollector;
import utilities.ErrorDetection;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;


public abstract class TopologyProtocol implements EDProtocol {
	protected Node node = null;
	protected TopologyNode myNode = null;
	protected Transport myTransport = null;

	protected HashMap<Integer, KoalaMessage> receivedMsgs;
	
	protected int linkPid = -1;
	protected int myPid = -1;
	protected int transId = -1;
	
	protected boolean logMsg;
	protected static boolean initializeMode; //true only when the koala ring is being statically initialized (false during the simulation)
	
	protected String msgSender;
	protected ArrayList<String> msgPath;
	protected ArrayList<String> msgPiggyBack;
	
	public TopologyProtocol(String prefix) {

        receivedMsgs = new HashMap<Integer, KoalaMessage>();
        logMsg = Configuration.getInt("logging.msg", 0) == 1;
	}
	
	public Object clone() {
		TopologyProtocol inp = null;
        try {
            inp = (TopologyProtocol) super.clone();
            inp.receivedMsgs = new HashMap<Integer, KoalaMessage>();
        } catch (CloneNotSupportedException e) {
        } // never happens
        return inp;
    }
	

	
	public static void setInitializeMode(boolean initMode){
		initializeMode = initMode;
	}
	

	protected void onReceivedMsg(KoalaMessage msg) {
		msg.setReceivedCycle(CommonState.getTime());
		receivedMsgs.put(msg.getID(), msg);
//		System.out.println(msg.getID()+  " ("+this.getClass().getName() +") "+ myNode.getID()+" got a message through: ["+msg.pathToString()+"] with latency: " +msg.getLatency());
	}
	
	public TopologyNode getMyNode(){
		return myNode;
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
	
	public String toString(){
		return "("+ getProtocolName()+") " +  myNode.toString();
	}
	
	public abstract void join();

	protected abstract void checkPiggybackedBefore(KoalaMessage msg);
	protected abstract void checkPiggybackedAfter(KoalaMessage msg);
	
	protected abstract String getProtocolName();
	
//	protected abstract void checkStatus();
	
	protected abstract void onNewGlobalNeighbours(KoalaMessage msg);

	protected abstract void onRoute(KoalaMessage msg);
	
	protected abstract void onRoutingTable(KoalaMessage msg);
	
	protected abstract void onLongLink(KoalaMessage msg);
	
	public void intializeMyNode(Node node, int pid){
		this.node = node;
		myPid = pid;
		linkPid = FastConfig.getLinkable(pid);
		transId = FastConfig.getTransport(pid);
		myNode = (TopologyNode) (Linkable) node.getProtocol(linkPid);
		if(transId > 0)
			myTransport = (Transport)node.getProtocol(transId);
	}

	public void send(String destinationID, KoalaMessage msg)
	{
		Node dest = NodeUtilities.Nodes.get(destinationID);

		if(dest != null){
			if(ErrorDetection.hasLoopCommunication(msg,destinationID))
				System.out.println("problems in horizon");
				
			String logmsg = "("+ CommonState.getTime()+") "+ myNode.getID() + " sending a message to " + destinationID  + " a msg of type: " + msg.getTypeName();
			if(logMsg)
			System.out.println(logmsg);

			double l = PhysicalDataProvider.getLatency(myNode.getID(), destinationID);

			if(l <= 0)
				System.out.println("someone invented time traveling!");
			msg.addLatency(l);
			if(msg.getLastSender() == null)
				msg.addToPath(myNode.getID());
			msg.addToPath(destinationID);
			

			
			if(NodeUtilities.getDCID(myNode.getID()) == NodeUtilities.getDCID(destinationID))
				ResultCollector.countIntra();
			else
				ResultCollector.countInter();
			
			if(initializeMode)
				((KoalaProtocol)dest.getProtocol(myPid)).receive(msg);
			else
				myTransport.send(node, dest, msg, myPid);
		}
	}


	
	@SuppressWarnings("unchecked")
	public void receive(KoalaMessage msg)
	{
		String logmsg = "("+ CommonState.getTime()+") "+ myNode.getID() + " received a message from " + msg.getLastSender()  + " a msg of type: " + msg.getTypeName();
		if(logMsg)
			System.out.println(logmsg);
		
		msgSender = msg.getLastSender();
		msgPath = (ArrayList<String>) msg.getPath().clone();
		msgPiggyBack = new ArrayList<String>();
		for(int i = 0; i < msg.getPiggyBack().size(); i++)
			msgPiggyBack.add(msg.getPiggyBack().get(i).getNodeID());
		
		if(!initializeMode)
			checkPiggybackedBefore(msg);
		
		switch(msg.getType()){
			case KoalaMessage.RT:
				onRoutingTable(msg);
				break;
			case KoalaMessage.ROUTE:
				onRoute(msg);
				break;
			case KoalaMessage.NGN:
//				logmsg += " " + ((KoalaNGNMsgContent )msg.getContent()).getNeighbor().getNodeID() ; 
				onNewGlobalNeighbours(msg);
				break;
			case KoalaMessage.JOIN:
				join();
				break;
			case KoalaMessage.LL:
				onLongLink(msg);
				break;
		}
		
		if(!initializeMode)
			checkPiggybackedAfter(msg);
		
	}

	
	

//	@Override
//	public void nextCycle(Node node, int protocolID) {
//		myPid = protocolID;
//		linkPid = FastConfig.getLinkable(protocolID);
//		intializeMyNode(node);
//		
////		System.out.print(myNode.getID() + "  ");
//		receive();
////		checkStatus();
//	}
	
	@Override
	public void processEvent(Node node, int pid, Object event) {
		intializeMyNode(node, pid);
		receive((KoalaMessage)event);
	}

	
}
