package topology.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import javax.xml.stream.events.NotationDeclaration;

import chord.ChordNode;
import messaging.ChordLookUpContent;
import messaging.ChordMessage;
import messaging.KoalaMessage;
import messaging.KoalaRouteMsgContent;
import messaging.TopologyMessage;
import koala.KoalaNode;
import koala.KoalaProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.reports.GraphObserver;
import peersim.transport.Transport;
import renater.RenaterNode;
import renater.RenaterProtocol;
import topology.TopologyNode;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;

public class TrafficGenerator extends GraphObserver {


	
	private int msgID;
	private int msgCategory;
//	private boolean allAdded = false; 
	
//	private ArrayList<Node[]> routes = new ArrayList<Node[]>();
	private Random rand;
	
	public TrafficGenerator(String prefix) {
		super(prefix);
		msgID = 0;
//		initilizeRoutes();
		long seed = Configuration.getLong("random.seed", 12345678);
		rand = new Random(seed);
	}

	@Override
	public boolean execute() {
		updateGraph();
		route();
		return false;
	}


	

	private void route() {
		Node src = null, dst=null;
		int trials = 50;
//		do{
//
//			src =  Network.get(rand.nextInt(Network.size()));
//			dst =  Network.get(rand.nextInt(Network.size()));
//			if(--trials == 0){
//				Node[] srcdst = extraSrcDst();
//				if(srcdst == null)
//					break;
//				src = srcdst[0];
//				dst = srcdst[1];
//				trials = 50;
//			} 
//		}while(!src.isUp() || !dst.isUp() || src.equals(dst));
		ArrayList<Node> srcdst; 
		do{
			if(NodeUtilities.UPS.size() <= 1){
				trials = 0; break;
			}
			srcdst = getRandNodes(1, 2);
			src = srcdst.get(0);
			dst = srcdst.get(1);
		}while((src.equals(dst) || src==null || dst==null) && --trials > 0);
		
		
		if(trials == 0){
			System.out.println("Not enough nodes are up, skipping routing this time");
			return;
		}
		
		TopologyNode sourc =(RenaterNode)src.getProtocol(NodeUtilities.RID);
		TopologyNode dest = (RenaterNode)dst.getProtocol(NodeUtilities.RID);
		
		Transport tr = (Transport)src.getProtocol(NodeUtilities.TRID);
		
		if(NodeUtilities.RPID >= 0){
        	KoalaMessage msg = new KoalaMessage( new KoalaRouteMsgContent((RenaterNode)dst.getProtocol(NodeUtilities.RID)));
        	msg.setID(msgID); msg.setSentCycle(CommonState.getTime()); msg.setCategory(msgCategory);
        	tr.send(null, src, msg, NodeUtilities.RPID);
		}
		
		if(NodeUtilities.KPID >= 0){
			KoalaMessage msg = new KoalaMessage(new KoalaRouteMsgContent((KoalaNode)dst.getProtocol(NodeUtilities.KID)));
        	msg.setID(msgID); msg.setSentCycle(CommonState.getTime()); msg.setCategory(msgCategory);
        	tr.send(null, src, msg, NodeUtilities.KPID);
		}
		
		if(NodeUtilities.FKPID >= 0){
			KoalaMessage msg = new KoalaMessage(new KoalaRouteMsgContent((KoalaNode)dst.getProtocol(NodeUtilities.FKID)));
        	msg.setID(msgID); msg.setSentCycle(CommonState.getTime()); msg.setCategory(msgCategory);
        	tr.send(null, src, msg, NodeUtilities.FKPID);
		}
		
		if(NodeUtilities.LKPID >= 0){
			KoalaMessage msg = new KoalaMessage(new KoalaRouteMsgContent((KoalaNode)dst.getProtocol(NodeUtilities.LKID)));
        	msg.setID(msgID); msg.setSentCycle(CommonState.getTime()); msg.setCategory(msgCategory);
        	tr.send(null, src, msg, NodeUtilities.LKPID);
		}
		
		if(NodeUtilities.CPID >= 0){
			ChordNode destCn = (ChordNode)dst.getProtocol(NodeUtilities.CID);
			ChordLookUpContent cc = new ChordLookUpContent(ChordMessage.LOOK_UP, destCn);
			ChordMessage msg = new ChordMessage(cc);
        	msg.setID(msgID); msg.setSentCycle(CommonState.getTime()); msg.setCategory(msgCategory);
        	tr.send(null, src, msg, NodeUtilities.CPID);
		}
		
		
		System.out.println("(" + CommonState.getTime() + ") ROUTE " + sourc.getCID() + " -> " + dest.getCID());
		ResultCollector.addSentMsg(msgID, dst);
		msgID++;
		
	}
	
//	private Node getDestination(Node src)
//	{
//		if(NodeUtilities.LOCALITY.equals(NodeUtilities.LOC_UNIFORM))
//		return null;
//	}

	private Node[] extraSrcDst() {
		ArrayList<Node> ups = new ArrayList<Node>();
		int randi = rand.nextInt(Network.size());
		for(int i = 0; i < Network.size();i++){
			int realI = (i+randi )%Network.size();
			if(Network.get(realI).isUp())
				ups.add(Network.get(i));
			
			if(ups.size() >= 20)
				break;
		}
		
		if(ups.size() <= 1)
			return null;
		Collections.shuffle(ups, rand);
		
		return new Node[]{ups.get(0), ups.get(1)};
		
	}
	
//	private void initilizeRoutes(){
//		for(int i = 0; i < CommonState.getEndTime(); i++){
//			int count = 1;
//			ArrayList<Node> sources = getRandomNodes(count, true, new ArrayList<Node>());
//			ArrayList<Node> dests = getRandomNodes(count, true, sources);
//			routes.add(new Node[]{sources.get(0), dests.get(0)});
//		}
//	}
	
	
	
	
	
	
	public ArrayList<Node> getRandNodes(int upOrDown, int nr){
		
		String[] vals = NodeUtilities.LOCALITY.split(" "); 
		
		if(vals.length != 3){
			msgCategory = TopologyMessage.CAT_UNDEFINED;
			return NodeUtilities.getUniformRandNodes(upOrDown,nr);
		}
		double loc = Double.parseDouble(vals[0])/100;
		double close = Double.parseDouble(vals[1])/100;
//		double global = Double.parseDouble(vals[2])/100;
		
		ArrayList<Node> ret = new ArrayList<Node>();
		Node src = NodeUtilities.getUniformRandNodes(1,1).get(0);
		Node dst = null;
		ret.add(src);
		RenaterNode rsrc = (RenaterNode)src.getProtocol(NodeUtilities.RID);
		double r = CommonState.r.nextDouble();
		if(r<loc)
			dst = getLocalDest(rsrc);
		else if(r<loc+close)
			dst = getCloseDest(rsrc);
		else 
			dst = getGlobalDest(rsrc);
		
		ret.add(dst);
		return ret;
	}
	
	private Node getLocalDest(RenaterNode src){
		Node dst = null; int trials=0, max_trials = 10;
		while(trials <= max_trials){
			//a bit cheating here but maybe it is faster
			String potentialid = NodeUtilities.getStrDCID(src.getCID()) + "-"+CommonState.r.nextInt(NodeUtilities.NR_NODE_PER_DC);
			dst = NodeUtilities.Nodes.get(potentialid);
			if(dst!=null && !dst.equals(src.getNode()) && dst.isUp()) break;
			trials++;
		}
		msgCategory = TopologyMessage.CAT_LOCAL;
		return dst;
	}
	private Node getCloseDest(RenaterNode src){
		Node dst = null; int trials=0, max_trials = 10;
		
		while(trials <= max_trials){
			RenaterNode potDst = null;
			if(src.isGateway())
				potDst = src.getRandomRenaterFriend();
			else{
				//gateway
				potDst = (RenaterNode)NodeUtilities.Nodes.get(src.getGateway()).getProtocol(NodeUtilities.RID);
				potDst = potDst.getRandomRenaterFriend();
			}
			if(potDst==null) break;
				
			dst = getLocalDest(potDst);
			if(dst.isUp()) break;
			trials++;
		}
		msgCategory = TopologyMessage.CAT_CLOSE;
		return dst; 
	}
	private Node getGlobalDest(RenaterNode src){
		Node dst = null; int trials=0, max_trials = 30;
		while(trials <= max_trials){
			dst = NodeUtilities.getUniformRandNodes(1,1).get(0);
			if(dst!=null && !dst.equals(src.getNode())&& !src.isFriend(dst) && dst.isUp()) break;
			trials++;
		}
		msgCategory = TopologyMessage.CAT_GLOBAL;
		return dst; 
	}
	
	
//	public ArrayList<Node> getNeighborsToLatency(RenaterNode rn, double lat) {
//		ArrayList<Node> bucket = new ArrayList<Node>();
//		Queue<RenaterNode> queue = new LinkedList<RenaterNode>();
//		queue.add(rn);
//		rn.setVisited(true);
//		double clat = 0;
//		while(!queue.isEmpty()) {
//			RenaterNode node = queue.remove();
//			RenaterNode child = null;
//			while((child=node.getUnvisitedChildNode())!=null) {
//				child.setVisited(true);
//				queue.add(child);
//			}
//		}
//		
//		//clean bucket
//		
//		
//		return bucket;
//	}
	


}
