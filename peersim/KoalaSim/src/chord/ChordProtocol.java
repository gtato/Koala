package chord;


import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import topology.TopologyPathNode;
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
		myNode = (ChordNode) node.getProtocol(NodeUtilities.CID);
		
	}

	@Override
	public void join() {
		
		// search a bootstrap node to join  
		Node n;
		do {
			n = Network.get(CommonState.r.nextInt(Network.size()));
		} while (n == null || !n.isUp() || n.equals(myNode.getNode()));
		
		if(myNode.getSID() == null){
			myNode.setSID(NodeUtilities.generateNewChordID());
			NodeUtilities.CHORD_NODES.put(myNode.getSID(), node);
		}
		ChordNode cpRemote = NodeUtilities.getChordFromNode(n);
		myNode.successorList = new ChordNode[NodeUtilities.SUCC_SIZE];
		myNode.fingerTable = new ChordNode[NodeUtilities.M];
		myNode.predecessor = null;
		
		TopologyPathNode remotetpn = new TopologyPathNode(cpRemote);
		findSuccessor(remotetpn, myNode.getSID(), "successor first");
		for (int i = 0; i < myNode.fingerTable.length; i++) {
			long a = (long) (myNode.getLID() + Math.pow(2, i)) %(long)Math.pow(2, NodeUtilities.M);
			findSuccessor(remotetpn, a+"", "finger " + i);
		}
//		System.out.println("Node " + myNode.chordId + " is in da house");
	}


	
	@Override
	protected String getProtocolName() {
		return "chord";
	}


	protected void onRoute(ChordMessage msg) {
		ChordLookUpContent reccontent = (ChordLookUpContent)msg.getContent(); 
		String target = reccontent.getChordNode().getSID();
		String joining = reccontent.getJoiningNode() != null ? reccontent.getJoiningNode().getSID() : null;
		
		if(!myNode.hasJoined()) return;
		
		if(msg.isType(ChordMessage.LOOK_UP) && target.equals(myNode.getSID())){
			onSuccess(msg); return;
		}
		
		else if (msg.isType(ChordMessage.SUCCESSOR) && ChordNode.inAB(target, myNode.getSID(), myNode.successorList[0].getSID())) {
			ChordLookUpContent content =  new ChordLookUpContent(ChordMessage.SUCCESSOR_FOUND,  myNode.successorList[0]);
			if(target.equals(myNode.getSID()))
				content.setChordNode(myNode);
			if(joining != null && joining.equals(myNode.successorList[0].getSID()))
				content.setChordNode(myNode.successorList[1]);
			
			ChordMessage finalmsg = new ChordMessage(content);
			TopologyPathNode dest = msg.getFirstSender();
			if (dest == null) dest = new TopologyPathNode(myNode);
			finalmsg.setLabel(msg.getLabel());
//			send(msg.getLastSender(), finalmsg);
			send(dest, finalmsg);
		}
		else{
			ChordNode dest = myNode.closestPrecedingNode(target, joining);
			if(dest != null && !dest.getCID().equals(myNode.getCID()))
				send(new TopologyPathNode(dest), msg);
			else
				onFail(msg);
		}
		
	}
	
	
	public void onSuccessorFound(ChordMessage msg){
		String label = msg.getLabel();
		
		ChordNode succ = ((ChordLookUpContent)msg.getContent()).getChordNode();
		
		if(label.contains("successor")) //predecessor
		{
			ChordNode pred = succ.predecessor;
			String predID = pred != null ? pred.getCID(): null;
			if(label.contains("first") || myNode.equals(pred) || !NodeUtilities.isUp(predID)){
				myNode.successorList[0] = succ;
				if(label.contains("first") && !myNode.equals(pred)) myNode.predecessor = pred;
				System.arraycopy(succ.successorList,0,myNode.successorList,1,myNode.successorList.length-1);
				if(label.contains("first")) myNode.setJoined(true);
			}
			else if(label.contains("stabilize")){
				if (pred.isUp() && ChordNode.inAB(pred.getSID(), myNode.getSID(), succ.getSID())){
					myNode.successorList[0] = pred;
					myNode.successorList[1] = succ;
					System.arraycopy(succ.successorList,0,myNode.successorList,2,myNode.successorList.length-2);
				}
				notify(new TopologyPathNode(myNode.successorList[0]));
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
		 (ChordNode.inAB(node.getSID(), myNode.predecessor.getSID(), myNode.getSID())
				 && !node.getSID().equals(myNode.getSID())))
			myNode.predecessor = node;
	}

	public void findSuccessor(TopologyPathNode nodeToAsk, String id, String label){
		ChordLookUpContent content =  new ChordLookUpContent(ChordMessage.SUCCESSOR, new ChordNode(id));
		if(!myNode.isJoining())
			content.setJoiningNode(myNode);
		ChordMessage predmsg = new ChordMessage(content);
		predmsg.setLabel(label);
		send(nodeToAsk, predmsg);
	}

	@Override
	protected void onReceiveLatency(TopologyPathNode dest, double l) {
		// TODO Auto-generated method stub
		
	}
	
	

	public void notify(TopologyPathNode nodeId){
		ChordLookUpContent content =  new ChordLookUpContent(ChordMessage.NOTIFY, myNode);
		ChordMessage notifyMsg = new ChordMessage(content);
		send(nodeId, notifyMsg);
	}
	
	public void stabilize() {
		for(ChordNode succ: myNode.successorList){
			if(succ != null && succ.isUp()){
//				myNode.successorList[0] = succ;
				findSuccessor(new TopologyPathNode(succ), succ.getSID(), "successor stabilize");
				return;
			}
		}
		System.err.println("All successors of node " + myNode.getSID() + " are down!");
		System.exit(1); //something went totally wrong
		
	}
	
	public void fixFingers(){
		if(myNode.fingerToFix >= myNode.fingerTable.length)
			myNode.fingerToFix = 0;
		long a = (long) (myNode.getLID() + Math.pow(2, myNode.fingerToFix)) %(long)Math.pow(2, NodeUtilities.M);
		findSuccessor(new TopologyPathNode(myNode), a+"", "finger " + myNode.fingerToFix);
		myNode.fingerToFix++;
	}




	@Override
	public void handleMessage(TopologyMessage msg) {
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
	
	protected void onFail(TopologyMessage msg){
		FAIL++;
	}


	
}
