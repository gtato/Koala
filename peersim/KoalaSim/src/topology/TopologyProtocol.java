package topology;

import java.util.ArrayList;
import java.util.HashMap;

import koala.KoalaNeighbor;
import koala.KoalaProtocol;
import messaging.TopologyMessage;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;
import renater.RenaterProtocol;
import topology.controllers.ResultCollector;
import utilities.ErrorDetection;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;


public abstract class TopologyProtocol implements EDProtocol {
	protected Node node = null;
	protected TopologyNode myNode = null;
	protected Transport myTransport = null;

//	protected HashMap<Integer, KoalaMessage> receivedMsgs;
	
//	protected int linkPid = -1;
	protected int myPid = -1;
	protected int transId = -1;
	
//	protected int fails = 0;
	protected boolean logMsg;
	protected static boolean initializeMode; //true only when the koala ring is being statically initialized (false during the simulation)
	
	protected TopologyPathNode msgSender;
	protected ArrayList<TopologyPathNode> msgPath;
	protected ArrayList<KoalaNeighbor> msgPiggyBack;
	
	
	public TopologyProtocol(String prefix) {

//        receivedMsgs = new HashMap<Integer, KoalaMessage>();
        logMsg = Configuration.getInt("logging.msg", 0) == 1;
	}
	
	public Object clone() {
		TopologyProtocol inp = null;
        try {
            inp = (TopologyProtocol) super.clone();
//            inp.receivedMsgs = new HashMap<Integer, KoalaMessage>();
        } catch (CloneNotSupportedException e) {
        } // never happens
        return inp;
    }
	

	
	public static void setInitializeMode(boolean initMode){
		initializeMode = initMode;
	}
	

//	protected void onReceivedMsg(TopologyMessage msg) {
//		msg.setReceivedCycle(CommonState.getTime());
//		REC_MSG.put(msg.getID(), msg);
//		getMsgStorage().put(msg.getID(), msg);
//		SUCCESS++;
////		System.out.println(msg.getID()+  " ("+this.getClass().getName() +") "+ myNode.getID()+" got a message through: ["+msg.pathToString()+"] with latency: " +msg.getLatency());
//	}
	
	protected abstract void onSuccess(TopologyMessage msg);
	protected abstract void onFail(TopologyMessage msg);
		
	
//	public int getFails(){
//		return fails;
//	}
	
	public TopologyNode getMyNode(){
		return myNode;
	}
	
//	public KoalaMessage removeReceivedMsg(int msgID) {
//		return receivedMsgs.remove(msgID);
//	}
//	
//	public boolean hasReceivedMsg(int msgID) {
//		return receivedMsgs.containsKey(msgID);
//	}
//	
//	public KoalaMessage getReceivedMsg(int msgID) {
//		return receivedMsgs.get(msgID);
//	}
	
	public String toString(){
		String nodeid = myNode == null ? "not initialized" : myNode.toString(); 
		return "("+ getProtocolName()+") " + nodeid;
	}
	
	public abstract void join();

	
	protected abstract String getProtocolName();
	
//	protected abstract void checkStatus();
			
	protected abstract void onReceiveLatency(TopologyPathNode dest, double l);
	
//	protected abstract HashMap<Integer, TopologyMessage> getMsgStorage();
	
	public abstract void handleMessage(TopologyMessage msg);
	
	
	public void intializeMyNode(Node node, int pid){
		this.node = node;
		myPid = pid;
//		linkPid = FastConfig.getLinkable(pid);
		transId = NodeUtilities.TRID;
		myNode = (TopologyNode) node.getProtocol(NodeUtilities.getLinkable(pid));
		if(transId > 0)
			myTransport = (Transport)node.getProtocol(transId);
		NodeUtilities.CURRENT_PID = pid;
	}

	public void send(TopologyPathNode dest, TopologyMessage msg)
	{
		if(dest == null || dest.getSID().equals(myNode.getSID())){
			handleMessage(msg);
			return;
		}
		
		Node destNode = NodeUtilities.Nodes.get(dest.getCID());
		
		if(dest != null){
			if(ErrorDetection.hasLoopCommunication(msg,dest)){
				System.err.println("Message is going in cycles");
				System.exit(1);
			}
			
			if(msg.getHops() > 500){
				System.err.println("Too many hops, something is wrong here");
				onFail(msg);
				return;
			}
			
			String logmsg = "("+ CommonState.getTime()+") "+ myNode.getSID() + " sending a message to " + dest.getSID() + " a msg of type: " + msg.getTypeName();
			if(logMsg)
			System.out.println(logmsg);

//			String did = myPid == NodeUtilities.FKPID ? NodeUtilities.FlatMap.get(destinationID) : destinationID;
//			String sid = myPid == NodeUtilities.FKPID ? NodeUtilities.FlatMap.get(myNode.getID()) : myNode.getID();
			double l = PhysicalDataProvider.getLatency(myNode.getCID(), dest.getCID());

			if(l <= 0)
				System.out.println("someone invented time traveling!");
			msg.addLatency(l);
			if(msg.getLastSender() == null)
				msg.addToPath(new TopologyPathNode(myNode));
			msg.addToPath(dest.copy());
			
			
			if(NodeUtilities.getDCID(myNode.getSID()) == NodeUtilities.getDCID(dest.getSID()))
				ResultCollector.countIntra();
			else
				ResultCollector.countInter();
			
			if(this instanceof RenaterProtocol && !destNode.isUp())
			{
				((RenaterProtocol)destNode.getProtocol(myPid)).handleMessage(msg);
				return;
			}
			
			if(initializeMode)
				((KoalaProtocol)destNode.getProtocol(myPid)).receive(msg);
			else
				myTransport.send(node, destNode, msg, myPid);
			
			onReceiveLatency(dest, l);
		}
		
	}


	protected Node getNodeFromID(String id)
	{
		return NodeUtilities.Nodes.get(id);
	}
	
	
	
	

	@SuppressWarnings("unchecked")
	public void receive(TopologyMessage msg)
	{
		String logmsg = "("+ CommonState.getTime()+") "+ myNode.getSID() + " received a message from " + msg.getLastSender()  + " a msg of type: " + msg.getTypeName();
		if(logMsg)
			System.out.println(logmsg);
		
		msgSender = msg.getLastSender();
		msgPath = new ArrayList<TopologyPathNode>();
		for(TopologyPathNode tpn : msg.getPath())
			msgPath.add(tpn.copy());
		
		handleMessage(msg);
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
		receive((TopologyMessage)event);
	}

	

	
}
