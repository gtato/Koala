package koala.controllers;

import java.util.ArrayList;

import messaging.KoalaMessage;
import messaging.KoalaMsgContent;
import messaging.KoalaRouteMsgContent;
import koala.KoalaNode;
import koala.KoalaProtocol;
import koala.RenaterNode;
import koala.RenaterProtocol;
import koala.TopologyNode;
import koala.TopologyProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.reports.GraphObserver;

public class KoalaPlanner extends GraphObserver {


	private static final String PAR_KOALA_PROTOCOL= "kprotocol";
	private static final String PAR_RENATER_PROTOCOL= "protocol";
	
	private final int renProtPid;
	private final int renNodePid;
	private final int koaProtPid;
	private final int koaNodePid;
	
	private KoalaMessage joinMsg;
	private int msgID;
	private boolean allAdded = false; 
	
	public KoalaPlanner(String prefix) {
		super(prefix);
		
		renProtPid = Configuration.getPid(prefix + "." + PAR_RENATER_PROTOCOL);
		renNodePid = FastConfig.getLinkable(renProtPid);
		
		koaProtPid = Configuration.getPid(prefix + "." + PAR_KOALA_PROTOCOL, -1);
		if(koaProtPid > -1)
			koaNodePid = FastConfig.getLinkable(koaProtPid);
		else
			koaNodePid = -1;
		
		joinMsg = new KoalaMessage(new KoalaMsgContent(KoalaMessage.JOIN));
		msgID = 0;
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
		if(CommonState.getTime() % 10 == 0)
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
				koatProt.registerMsg(joinMsg);
			}
			
			if(renProtPid > 0){
	        	RenaterProtocol retProt = (RenaterProtocol) node.getProtocol(renProtPid);
	        	retProt.registerMsg(joinMsg);
			}
//			System.out.println("(" + CommonState.getTime() + ") JOIN: " + ((RenaterNode) node.getProtocol(renNodePid)).getID());
		}
	}
	
	
	private void route() {
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
			TopologyNode sourc = koaProtPid >= 0 ? (KoalaNode)sources.get(i).getProtocol(koaNodePid) : (RenaterNode)sources.get(i).getProtocol(renNodePid);
			TopologyNode dest = koaProtPid >= 0 ? (KoalaNode)dests.get(i).getProtocol(koaNodePid) : (RenaterNode)dests.get(i).getProtocol(renNodePid);
			
			if(koaProtPid > 0){
				KoalaProtocol koatProt = (KoalaProtocol) src.getProtocol(koaProtPid);
				KoalaMessage msg = new KoalaMessage(new KoalaRouteMsgContent(dest.getID()));
	        	msg.addToPath(sourc.getID());
	        	msg.setID(msgID);
				koatProt.registerMsg(msg);
			}
			
			if(renProtPid > 0){
	        	RenaterProtocol retProt = (RenaterProtocol) src.getProtocol(renProtPid);
	        	KoalaMessage msg = new KoalaMessage( new KoalaRouteMsgContent(dest.getID()));
	        	msg.addToPath(sourc.getID());
	        	msg.setID(msgID);
	        	retProt.registerMsg(msg);
			}
			System.out.println("(" + CommonState.getTime() + ") ROUTE: " + sourc.getID() + " -> " + dest.getID());
			ResultCollector.addSentMsg(msgID, dests.get(i));
			msgID++;
			
		}
		
	}
	
	
	
	
	private ArrayList<Node> getRandomNodes(int n, boolean joined, ArrayList<Node> except){
		ArrayList<Node> toRet = new ArrayList<Node>();
		ArrayList<Integer> complyingIndexes = new ArrayList<Integer>();
		for (int i = 0; i < g.size(); i++) {
			if(except.contains(g.getNode(i)))
				continue;
        	//int id = koaProtPid >= 0 ? koaProtPid : renProtPid;
        	int nid = koaProtPid >= 0 ? koaNodePid : renNodePid;
        	//TopologyProtocol currentNode = (TopologyProtocol)((Node)g.getNode(i)).getProtocol(id);
        	TopologyNode ncurrentNode = (TopologyNode)((Node)g.getNode(i)).getProtocol(nid);
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
			//TODO: remove this later
//			sel = i;
			toRet.add((Node)g.getNode(complyingIndexes.get(sel)));
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
