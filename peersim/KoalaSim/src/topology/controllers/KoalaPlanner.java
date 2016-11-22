package topology.controllers;

import java.util.ArrayList;

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

public class KoalaPlanner extends GraphObserver {


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
	private boolean allAdded = false; 
	
	private ArrayList<Node[]> routes = new ArrayList<Node[]>();
	
	public KoalaPlanner(String prefix) {
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
		initilizeRoutes();
	}

	@Override
	public boolean execute() {
		updateGraph();
		
//		if (hasFinished()){		
//			System.out.println("simulation probably finished at: " + CommonState.getTime());
//			return true;
//		}
		
//		joinNodes(g.size());
	
//	if (CommonState.r.nextInt() % 2 == 0 && !allAdded)
		//if(true)
//		if(!allAdded)
//			joinNodes(1);
//		else 
//			route();
	//	if(CommonState.getTime() % 5 == 0)
			route();
//		if(CommonState.getTime() == 100)
//			joinNodes(1);
		return false;
	}

	private void joinNodes(int count) {
		ArrayList<Node> nodes = getRandomNodes(count, false, new ArrayList<Node>());
		if (nodes.size() == 0){
			allAdded = true;
//			System.out.println("(" + CommonState.getTime() + ") NOTHING TO JOIN ");
//			System.out.println("NOTHING TO JOIN ");
		}
		for(Node node : nodes){
			if(koaProtPid > 0){
				KoalaProtocol koatProt = (KoalaProtocol) node.getProtocol(koaProtPid);
				koatProt.join();
//				koatProt.registerMsg(joinMsg);
			}
			
			if(renProtPid > 0){
	        	RenaterProtocol retProt = (RenaterProtocol) node.getProtocol(renProtPid);
	        	retProt.join();
//	        	retProt.registerMsg(joinMsg);
			}
//			System.out.println("(" + CommonState.getTime() + ") JOIN: " + ((RenaterNode) node.getProtocol(renNodePid)).getID());
		}
	}
	
	
	private void route_old() {
		int count = 1;
		ArrayList<Node> sources = getRandomNodes(count, true, new ArrayList<Node>());
		ArrayList<Node> dests = getRandomNodes(count, true, sources);
		
		if(sources.size() == 0 || dests.size()==0){
			System.err.println("Not enough nodes have joined yet, try adding a bit more");
//			System.exit(0);
			System.out.println("(" + CommonState.getTime() + ") NOTHING TO ROUTE ");
			return;
		}
		
		for(int i = 0; i < count; i++){
			Node src = sources.get(i);
			Node dst = dests.get(i);
			TopologyNode sourc = koaProtPid >= 0 ? (KoalaNode)sources.get(i).getProtocol(koaNodePid) : (RenaterNode)sources.get(i).getProtocol(renNodePid);
			TopologyNode dest = koaProtPid >= 0 ? (KoalaNode)dests.get(i).getProtocol(koaNodePid) : (RenaterNode)dests.get(i).getProtocol(renNodePid);
			
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
				KoalaMessage msg = new KoalaMessage(new KoalaRouteMsgContent(destCn.chordId.toString()));
	        	msg.setID(msgID); msg.setSentCycle(CommonState.getTime());
	        	tr.send(null, src, msg, chordProtPid);
			}
			
			
			System.out.println("(" + CommonState.getTime() + ") ROUTE " + sourc.getID() + " -> " + dest.getID());
			ResultCollector.addSentMsg(msgID, dst);
			msgID++;
		}
	}
	
	private void route() {
		
		
		Node src, dst;
		do{
			if(routes.size() == 0)
				initilizeRoutes();
			Node[] srcdst = routes.remove(0); 
			src = srcdst[0];
			dst = srcdst[1];
		}while(!src.isUp() || !dst.isUp());
		
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
	
	private void initilizeRoutes(){
		for(int i = 0; i < CommonState.getEndTime(); i++){
			int count = 1;
			ArrayList<Node> sources = getRandomNodes(count, true, new ArrayList<Node>());
			ArrayList<Node> dests = getRandomNodes(count, true, sources);
			routes.add(new Node[]{sources.get(0), dests.get(0)});
		}
	}
	
	
	
	
	
	
	private ArrayList<Node> getRandomNodes(int n, boolean joined, ArrayList<Node> except){
		ArrayList<Node> toRet = new ArrayList<Node>();
		ArrayList<Integer> complyingIndexes = new ArrayList<Integer>();
		for (int i = 0; i < Network.size(); i++) {
			if(except.contains(Network.get(i)))
				continue;
        	//int id = koaProtPid >= 0 ? koaProtPid : renProtPid;
        	int nid = koaProtPid >= 0 ? koaNodePid : renNodePid;
        	//TopologyProtocol currentNode = (TopologyProtocol)((Node)g.getNode(i)).getProtocol(id);
        	TopologyNode ncurrentNode = (TopologyNode)(Network.get(i)).getProtocol(nid);
        	//boolean cond = joined ? currentNode.hasJoined() : !currentNode.hasJoined();
        	boolean cond = joined ? ncurrentNode.hasJoined() : !ncurrentNode.hasJoined();
//        	if(id == renProtPid)
//        		cond = true;
//        	if(cond && ((KoalaNode)((Node)g.getNode(i)).getProtocol(targetNodePid)).isGateway()  )
        	if(cond)
            	complyingIndexes.add(i);
        }
		
		int max = Math.min(n, complyingIndexes.size());
		
		for(int i = 0; i< max; i++){
			int sel = CommonState.r.nextInt(complyingIndexes.size());
			toRet.add(Network.get(complyingIndexes.get(sel)));
		}
		return toRet;
		
	}
	
	
//	private boolean hasFinished(){
//		boolean finished = true;
//		for (int i = 0; i < g.size(); i++) {
//        	KoalaNode currentNode = (KoalaNode) ((Node)g.getNode(i)).getProtocol(targetNodePid);
//        	TopologyProtocol currentProt = (TopologyProtocol) ((Node)g.getNode(i)).getProtocol(targetLogProtPid);
//        	
//        	if(!currentNode.hasJoined() || !currentProt.hasEmptyQueue())
//            	finished = false;
//        }
//		return finished;
//	}

}
