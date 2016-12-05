package chord;


import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import topology.TopologyProtocol;
import utilities.NodeUtilities;

import java.math.*;
import java.util.HashMap;

import messaging.ChordLookUpContent;
import messaging.ChordMessage;
import messaging.KoalaMessage;
import messaging.KoalaRouteMsgContent;
import messaging.TopologyMessage;


public class ChordProtocol extends TopologyProtocol {

	public static HashMap<Integer, TopologyMessage> REC_MSG =  new HashMap<Integer, TopologyMessage>();
	public static int SUCCESS = 0;
	public static int FAIL = 0;
	
	ChordNode myNode;

	public ChordProtocol(String prefix) {
		super(prefix);
	}


//	public Object clone() {
//		ChordProtocol cp = (ChordProtocol) super.clone();
//		return cp;
//	}
	
	@Override
	public void intializeMyNode(Node node, int pid) {
		super.intializeMyNode(node, pid);
		myNode = (ChordNode) (Linkable) node.getProtocol(linkPid);
		
	}

	@Override
	public void join() {
		
		// search a bootstrap node to join  
		Node n;
		do {
			n = Network.get(CommonState.r.nextInt(Network.size()));
		} while (n == null || !n.isUp());
		
		if(myNode.chordId == null){
			myNode.chordId = NodeUtilities.generateNewChordID();
			NodeUtilities.CHORD_NODES.put(myNode.chordId, node);
		}
		ChordNode cpRemote = NodeUtilities.getChordFromNode(n);
		myNode.successorList = new ChordNode[NodeUtilities.SUCC_SIZE];
		myNode.fingerTable = new ChordNode[NodeUtilities.M];

		findSuccessor(cpRemote.getID(), myNode.chordId, "successor first");
		for (int i = 0; i < myNode.fingerTable.length; i++) {
			long a = (long) (myNode.chordId.longValue() + Math.pow(2, i)) %(long)Math.pow(2, NodeUtilities.M);
			BigInteger id = new BigInteger(a+"");
			findSuccessor(cpRemote.getID(), id, "finger " + i);
		}
//		System.out.println("Node " + myNode.chordId + " is in da house");
	}


	
	@Override
	protected String getProtocolName() {
		return "chord";
	}


	protected void onRoute(ChordMessage msg) {
		BigInteger target = ((ChordLookUpContent)msg.getContent()).getChordNode().chordId;
		
		if(msg.isType(ChordMessage.LOOK_UP) && target.equals(myNode.chordId)){
			onSuccess(msg); return;
		}
		
		else if (msg.isType(ChordMessage.SUCCESSOR) && ChordNode.inAB(target, myNode.chordId, myNode.successorList[0].chordId)) {
			ChordLookUpContent content =  new ChordLookUpContent(ChordMessage.SUCCESSOR_FOUND,  myNode.successorList[0]);
			if(msg.isType(ChordMessage.SUCCESSOR) && target.equals(myNode.chordId))
				content.setChordNode(myNode);
			ChordMessage finalmsg = new ChordMessage(content);
			finalmsg.setLabel(msg.getLabel());
			send(msg.getLastSender(), finalmsg);
		}
		else{
			ChordNode dest = myNode.closestPrecedingNode(target);
			if(dest != null && !dest.getID().equals(myNode.getID()))
				send(dest.getID(), msg);
			else
				onFail();
		}
		
	}
	
	
	public void onSuccessorFound(ChordMessage msg){
		String label = msg.getLabel();
		
		ChordNode succ = ((ChordLookUpContent)msg.getContent()).getChordNode();
		
		if(label.contains("successor")) //predecessor
		{
			ChordNode pred = succ.predecessor;
			if(label.contains("first") || pred.equals(myNode) || !pred.isUp()){
				myNode.successorList[0] = succ;
				if(label.contains("first")) myNode.predecessor = pred;
				System.arraycopy(succ.successorList,0,myNode.successorList,1,myNode.successorList.length-1);
			}
			else if(label.contains("stabilize")){
				if (pred.isUp() && ChordNode.inAB(pred.chordId, myNode.chordId, succ.chordId)){
					myNode.successorList[0] = pred;
					myNode.successorList[1] = succ;
					System.arraycopy(succ.successorList,0,myNode.successorList,2,myNode.successorList.length-2);
				}
				notify(myNode.successorList[0].getID());
			}
		}
		else if(label.contains("finger")){
			int index = Integer.parseInt(label.split(" ")[1]);
			myNode.fingerTable[index] = succ;
		}
	}
	
	public void onNotify(ChordMessage msg){
		ChordNode node = ((ChordLookUpContent)msg.getContent()).getChordNode();
		if (myNode.predecessor == null || 
		 (ChordNode.inAB(node.chordId, myNode.predecessor.chordId, myNode.chordId)
				 && !node.chordId.equals(myNode.chordId)))
			myNode.predecessor = node;
	}

	public void findSuccessor(String nodeToAsk, BigInteger id, String label){
		ChordLookUpContent content =  new ChordLookUpContent(ChordMessage.SUCCESSOR, new ChordNode(id));
		ChordMessage predmsg = new ChordMessage(content);
		predmsg.setLabel(label);
		send(nodeToAsk, predmsg);
	}

	@Override
	protected void onReceiveLatency(String dest, double l) {
		// TODO Auto-generated method stub
		
	}
	
	

	public void notify(String nodeId){
		ChordLookUpContent content =  new ChordLookUpContent(ChordMessage.NOTIFY, myNode);
		ChordMessage notifyMsg = new ChordMessage(content);
		send(nodeId, notifyMsg);
	}
	
	public void stabilize() {
		for(ChordNode succ: myNode.successorList){
			if(succ != null && succ.isUp()){
//				myNode.successorList[0] = succ;
				findSuccessor(succ.getID(), succ.chordId, "successor stabilize");
				return;
			}
		}
		System.err.println("All successors of node " + myNode.chordId + " are down!");
		System.exit(1); //something went totally wrong
		
	}
	
	public void fixFingers(){
		if(myNode.fingerToFix >= myNode.fingerTable.length)
			myNode.fingerToFix = 0;
		long a = (long) (myNode.chordId.longValue() + Math.pow(2, myNode.fingerToFix)) %(long)Math.pow(2, NodeUtilities.M);
		BigInteger id = new BigInteger(a+"");
		findSuccessor(myNode.getID(), id, "finger " + myNode.fingerToFix);
		
	}




	@Override
	protected void handleMessage(TopologyMessage msg) {
		ChordMessage cmsg = (ChordMessage) msg;
		switch(msg.getType()){
		case ChordMessage.LOOK_UP:
			onRoute(cmsg);
			break;
		case ChordMessage.SUCCESSOR:
			onRoute(cmsg);
			break;
		case ChordMessage.SUCCESSOR_FOUND:
			onSuccessorFound(cmsg);
			break;
		case ChordMessage.FINAL:
			onSuccess(cmsg);
			break;
		case ChordMessage.NOTIFY:
			onNotify(cmsg);
			break;
		
		}
		
	}


	protected void onSuccess(TopologyMessage msg) {
		msg.setReceivedCycle(CommonState.getTime());
		REC_MSG.put(msg.getID(), msg);
		SUCCESS++;
//		System.out.println(msg.getID()+  " ("+this.getClass().getName() +") "+ myNode.getID()+" got a message through: ["+msg.pathToString()+"] with latency: " +msg.getLatency());
	}
	
	protected void onFail(){
		FAIL++;
	}


	
}
