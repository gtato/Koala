package koala;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import messaging.KoalaMessage;
import messaging.KoalaRTMsgConent;
import messaging.KoalaRouteMsgContent;
import messaging.TopologyMessage;
import peersim.core.CommonState;
import peersim.core.Node;
import renater.RenaterNode;
import topology.TopologyPathNode;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;


public class LeaderKoalaProtocol extends KoalaProtocol{

	public static HashMap<Integer, TopologyMessage> REC_MSG =  new HashMap<Integer, TopologyMessage>();
	public static int SUCCESS = 0;
	public static int FAIL = 0;
	public static int INT_FAIL = 0;
	
	public LeaderKoalaProtocol(String prefix) {
		super(prefix);
		helpSupported = false;
	}

//	protected boolean getBootCond(Node n){
//		KoalaNode each = (KoalaNode) n.getProtocol(NodeUtilities.getLinkable(myPid));
//		RenaterNode eachRn = (RenaterNode) n.getProtocol(NodeUtilities.RID);
//		return each.isUp() && each.hasJoined() && eachRn.isGateway();
//	}
	
	protected Node getBootstrap(){
		if(lastBootstraps.size()==0) return null;
		if(!myNode.isLeader())
			return getCloseBootstrap();
		return NodeUtilities.Nodes.get(lastBootstraps.get(CommonState.r.nextInt(lastBootstraps.size())));
	}
	
	protected void onRoutingTable(KoalaMessage msg) {
		KoalaNode source = ((KoalaRTMsgConent)msg.getContent()).getNode();
		TopologyPathNode msgSender = msg.getLastSender();
//      TopologyPathNode msgSender = receviedMsg.getSender();
		boolean selfJoining = myNode.isJoining();
		if(myNode.isLocal(source.getSID())){
			ArrayList<KoalaNeighbor> receivedNeighbors = source.getRoutingTable().getNeighborsContainer();
			receivedNeighbors.add(new KoalaNeighbor(source));
			
			for(KoalaNeighbor recNeighbor: receivedNeighbors){
				if(recNeighbor.equals(myNode) || !myNode.isLocal(recNeighbor)) continue;
				if(initializeMode && selfJoining && myNode.isLocal(recNeighbor) && myNode.getRoutingTable().getLocals().size()>0 ) continue;
				boolean isSender = recNeighbor.equals(msgSender);
				boolean isSource = recNeighbor.equals(source);
				double l = isSender ? msg.getLatency() : recNeighbor.getLatency();
				int lq = myNode.getLatencyQuality(isSender, source.getSID(), recNeighbor);
				
				KoalaNeighbor potentialKN = new KoalaNeighbor(recNeighbor, l, lq);
				int res  = myNode.tryAddNeighbour(potentialKN, false);
				if(res == 2 && isSource && source.isJoining()){
					KoalaMessage newMsg = new KoalaMessage(new KoalaRTMsgConent(myNode));
					broadcastLocalNeighbor(recNeighbor);
					send(recNeighbor, newMsg);
				}
			}
		}else{
			if(myNode.isLeader())
				super.onRoutingTable(msg);
			else
				send(myNode.getLeaderNeighor(), msg);
		}
		
		myNode.setJoining(false);//not sure about this
	}
	
	
	protected KoalaNeighbor getRoute(String dest, KoalaMessage msg){
		KoalaNeighbor ret = null;
		
//		if(msg.getContent() instanceof KoalaRouteMsgContent){
		if(myNode.isLeader())
			ret = myNode.getRoute(dest, msg);
		else{
			if(myNode.isLocal(dest))
				ret = myNode.getRoute(dest, msg);
			else
				ret = myNode.getLeaderNeighor();
				
		}
//		}
//		else{
//			boolean neighDown = ((KoalaRTMsgConent)msg.getContent()).getNeighborsDown();
//			boolean isJoining = ((KoalaRTMsgConent)msg.getContent()).getNode().isJoining();
//		
//			if(!isJoining && neighDown)
//				ret = myNode.getClosestEntryBefore(kn.getSID());
//			else
//				ret = myNode.getRoute(kn, msg);
//		}
//		areNeighborsDown();
//		
//		if(NodeUtilities.getDCID(kn.getSID())  ==  NodeUtilities.getDCID(myNode.getSID())  
//	    && NodeUtilities.getDCID(ret.getSID()) != NodeUtilities.getDCID(myNode.getSID())){
//			System.out.println("I am calling outsiders to solve my problems");
//		}
			
		
		return ret;
	}
	
	
	
	protected void addPiggybacked(KoalaMessage km, String dest){
		// if I am forwarding to a neighbor 
		if(myNode.isLeader() && myNode.inNeighborsList(dest)){
			if (!myNode.isLocal(dest)){
				km.getPiggyBack().addAll(Arrays.asList(myNode.getRoutingTable().getGlobalSucessors()));
				km.getPiggyBack().addAll(Arrays.asList(myNode.getRoutingTable().getGlobalPredecessors()));
			}
		}
	}

	protected void initializeMsgPiggyback(KoalaMessage msg){
		msg.setIdealPiggyBack(true);
	}
	
	protected void addRandomLink(KoalaNeighbor rl){
		//Normally the following info should have been embeded inside the neighbor, but that is not so important for the moment 
		KoalaNode krl= (KoalaNode)NodeUtilities.Nodes.get(rl.getCID()).getProtocol(NodeUtilities.LKID);
		if(myNode.isLeader() && krl.isLeader())
			myNode.getRoutingTable().addRandLink(rl);
	}
	
	protected boolean addLongLink(KoalaNeighbor ll){
		//Normally the following info should have been embeded inside the neighbor, but that is not so important for the moment 
		KoalaNode krl= (KoalaNode)NodeUtilities.Nodes.get(ll.getCID()).getProtocol(NodeUtilities.LKID);
		if(myNode.isLeader() && krl.isLeader())
			myNode.getRoutingTable().addLongLink(ll);
		return false;
	}
	
	protected boolean addVicinityLink(KoalaNeighbor vl){
		KoalaNode kvl= (KoalaNode)NodeUtilities.Nodes.get(vl.getCID()).getProtocol(NodeUtilities.LKID);
		if(myNode.isLeader() && kvl.isLeader())
			return myNode.getRoutingTable().addVicinityLink(vl);
		return false;
	}
	
//	protected int addNeighbor(KoalaNeighbor n){
//		if(myNode.isLeader() || myNode.isLocal(n))
//			return myNode.tryAddNeighbour(n);
//		return -1;
//	}
	
	
	protected ArrayList<TopologyPathNode> getPath(){
		ArrayList<TopologyPathNode> pathcp = new ArrayList<TopologyPathNode>();
		for(TopologyPathNode tpn : receviedMsg.getPath())
			pathcp.add(tpn.cclone());
		pathcp.remove(0);
		return pathcp;
	} 
	
	protected ArrayList<KoalaNeighbor> getNeighbors(){
		return myNode.getRoutingTable().getGlobalNeighbors();
	}
	
	protected void broadcastGlobalNeighbor(KoalaNeighbor newNeig) {
		return;
	}
	
	
	@Override
	public void intializeMyNode(Node node, int pid) {
		super.intializeMyNode(node, pid);
		myNode = (KoalaNode) node.getProtocol(NodeUtilities.LKID);
	}

	protected void onSuccess(TopologyMessage msg) {
		msg.setReceivedCycle(CommonState.getTime());
		REC_MSG.put(msg.getID(), msg);
		SUCCESS++;
//		System.out.println(msg.getID()+  " ("+this.getClass().getName() +") "+ myNode.getID()+" got a message through: ["+msg.pathToString()+"] with latency: " +msg.getLatency());
	}
	
	protected void onFail(TopologyMessage msg){
		KoalaMessage kmsg = (KoalaMessage) msg;
		String failmsg = "LEADER failed to sent from " + kmsg.getFirstSender();
		if(msg.getContent() instanceof KoalaRouteMsgContent){
			String nid = ((KoalaRouteMsgContent)msg.getContent()).getNode().getSID();
			failmsg += " to " + nid;
			FAIL++;
		}else
			INT_FAIL++;
		
				
		System.out.println(failmsg);
		
	}
}
