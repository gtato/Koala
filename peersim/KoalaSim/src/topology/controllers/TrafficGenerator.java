package topology.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.xml.stream.events.NotationDeclaration;

import chord.ChordNode;
import messaging.ChordLookUpContent;
import messaging.ChordMessage;
import messaging.KoalaMessage;
import messaging.KoalaRouteMsgContent;
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

public class TrafficGenerator extends GraphObserver {


	private static final String PAR_KOALA_PROTOCOL= "kprotocol";
	private static final String PAR_CHORD_PROTOCOL= "cprotocol";
	private static final String PAR_RENATER_PROTOCOL= "protocol";
	
	private final int renProtPid;
	private final int renNodePid;
	
	private final int koaProtPid;
	private final int koaNodePid;
	
	private final int chordProtPid;
	private final int chordNodePid;
	
	private int msgID;
//	private boolean allAdded = false; 
	
//	private ArrayList<Node[]> routes = new ArrayList<Node[]>();
	private Random rand;
	
	public TrafficGenerator(String prefix) {
		super(prefix);
	
		renProtPid = Configuration.getPid(prefix + "." + PAR_RENATER_PROTOCOL);
		renNodePid = FastConfig.getLinkable(renProtPid);
		
		koaProtPid = Configuration.getPid(prefix + "." + PAR_KOALA_PROTOCOL, -1);
		if(koaProtPid > -1)
			koaNodePid = FastConfig.getLinkable(koaProtPid);
		else
			koaNodePid = -1;
		
		chordProtPid = Configuration.getPid(prefix + "." + PAR_CHORD_PROTOCOL, -1);
		chordNodePid =  chordProtPid > -1 ? FastConfig.getLinkable(chordProtPid) : -1;
		
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
			srcdst = NodeUtilities.getRandNodes(1, 2);
			src = srcdst.get(0);
			dst = srcdst.get(1);
			 
		}while(src.equals(dst) && trials > 0);
		
		
		if(trials == 0){
			System.out.println("Not enough nodes are up, skipping routing this time");
			return;
		}
		
		TopologyNode sourc = koaProtPid >= 0 ? (KoalaNode)src.getProtocol(koaNodePid) : (RenaterNode)src.getProtocol(renNodePid);
		TopologyNode dest = koaProtPid >= 0 ? (KoalaNode)dst.getProtocol(koaNodePid) : (RenaterNode)dst.getProtocol(renNodePid);
		
		Transport tr = (Transport)src.getProtocol(FastConfig.getTransport(renProtPid));
		
		if(renProtPid >= 0){
        	KoalaMessage msg = new KoalaMessage( new KoalaRouteMsgContent(dest.getID()));
        	msg.setID(msgID); msg.setSentCycle(CommonState.getTime());
        	tr.send(null, src, msg, renProtPid);
		}
		
		if(koaProtPid >= 0){
			KoalaMessage msg = new KoalaMessage(new KoalaRouteMsgContent(dest.getID()));
        	msg.setID(msgID); msg.setSentCycle(CommonState.getTime());
        	tr.send(null, src, msg, koaProtPid);
		}
		
		if(chordProtPid >= 0){
			ChordNode destCn = (ChordNode)dst.getProtocol(chordNodePid);
			ChordLookUpContent cc = new ChordLookUpContent(ChordMessage.LOOK_UP, destCn);
			ChordMessage msg = new ChordMessage(cc);
        	msg.setID(msgID); msg.setSentCycle(CommonState.getTime());
        	tr.send(null, src, msg, chordProtPid);
		}
		
		
		System.out.println("(" + CommonState.getTime() + ") ROUTE " + sourc.getID() + " -> " + dest.getID());
		ResultCollector.addSentMsg(msgID, dst);
		msgID++;
		
	}

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
	
	
	
	
	
	
//	private ArrayList<Node> getRandomNodes(int n, boolean joined, ArrayList<Node> except){
//		ArrayList<Node> toRet = new ArrayList<Node>();
//		ArrayList<Integer> complyingIndexes = new ArrayList<Integer>();
//		for (int i = 0; i < Network.size(); i++) {
//			if(except.contains(Network.get(i)))
//				continue;
//        	//int id = koaProtPid >= 0 ? koaProtPid : renProtPid;
//        	int nid = koaProtPid >= 0 ? koaNodePid : renNodePid;
//        	//TopologyProtocol currentNode = (TopologyProtocol)((Node)g.getNode(i)).getProtocol(id);
//        	TopologyNode ncurrentNode = (TopologyNode)(Network.get(i)).getProtocol(nid);
//        	//boolean cond = joined ? currentNode.hasJoined() : !currentNode.hasJoined();
//        	boolean cond = joined ? ncurrentNode.hasJoined() : !ncurrentNode.hasJoined();
////        	if(id == renProtPid)
////        		cond = true;
////        	if(cond && ((KoalaNode)((Node)g.getNode(i)).getProtocol(targetNodePid)).isGateway()  )
//        	if(cond)
//            	complyingIndexes.add(i);
//        }
//		
//		int max = Math.min(n, complyingIndexes.size());
//		
//		for(int i = 0; i< max; i++){
//			int sel = CommonState.r.nextInt(complyingIndexes.size());
//			toRet.add(Network.get(complyingIndexes.get(sel)));
//		}
//		return toRet;
//		
//	}
	


}
