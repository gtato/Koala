package koala.controllers;

import java.util.ArrayList;

import messaging.KoalaMessage;
import messaging.KoalaMsgContent;
import messaging.KoalaRouteMsgContent;
import koala.KoalaNode;
import koala.KoalaProtocol;
import koala.RenaterProtocol;
import koala.TopologyProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.reports.GraphObserver;

public class KoalaPlanner extends GraphObserver {

	private static final String PAR_TARGET_LPROT = "log_protocol";
	private static final String PAR_TARGET_PPROT = "phys_protocol";
	private static final String PAR_TARGET_NODE = "protocol";
	private final int targetLogProtPid;
	private final int targetPhysProtPid;
	private final int targetNodePid;
	private KoalaMessage joinMsg;
	
	
	
	public KoalaPlanner(String prefix) {
		super(prefix);
		targetPhysProtPid = Configuration.getPid(prefix + "." + PAR_TARGET_PPROT, -1);
		targetLogProtPid = Configuration.getPid(prefix + "." + PAR_TARGET_LPROT, -1);
		
		targetNodePid = Configuration.getPid(prefix + "." + PAR_TARGET_NODE);
		joinMsg = new KoalaMessage("", new KoalaMsgContent(KoalaMessage.JOIN));
		
	}

	@Override
	public boolean execute() {
		updateGraph();
		
//		if (hasFinished()){		
//			System.out.println("simulation probably finished at: " + CommonState.getTime());
//			return true;
//		}
		
//		joinNodes();
		route();
		return false;
	}

	private void joinNodes() {
		ArrayList<Node> nodes = getRandomNodes(1, false);
		for(Node node : nodes){
			if(targetLogProtPid > 0){
				KoalaProtocol koatProt = (KoalaProtocol) node.getProtocol(targetLogProtPid);
				koatProt.registerMsg(joinMsg);
			}
			
			if(targetPhysProtPid > 0){
	        	RenaterProtocol retProt = (RenaterProtocol) node.getProtocol(targetPhysProtPid);
	        	retProt.registerMsg(joinMsg);
			}
		}
	}
	
	
	private void route() {
		int count = 1;
		ArrayList<Node> sources = getRandomNodes(count, true);
		ArrayList<Node> dests = getRandomNodes(count, true);
		
		
		
		for(int i = 0; i < count; i++){
			Node src = sources.get(i);
			KoalaNode dest = (KoalaNode)dests.get(i).getProtocol(targetNodePid);
			
			if(targetLogProtPid > 0){
				KoalaProtocol koatProt = (KoalaProtocol) src.getProtocol(targetLogProtPid);
				koatProt.registerMsg(new KoalaMessage("", new KoalaRouteMsgContent(dest.getID())));
			}
			
			if(targetPhysProtPid > 0){
	        	RenaterProtocol retProt = (RenaterProtocol) src.getProtocol(targetPhysProtPid);
	        	retProt.registerMsg(new KoalaMessage("", new KoalaRouteMsgContent(dest.getID())));
			}
		}
		
	}
	
	
	
	
	private ArrayList<Node> getRandomNodes(int n, boolean joined){
		ArrayList<Node> toRet = new ArrayList<Node>();
		ArrayList<Integer> complyingIndexes = new ArrayList<Integer>();
		for (int i = 0; i < g.size(); i++) {
        	int id = targetLogProtPid > 0 ? targetLogProtPid : targetPhysProtPid;
			TopologyProtocol currentNode = (TopologyProtocol)((Node)g.getNode(i)).getProtocol(id);
        	boolean cond = joined ? currentNode.hasJoined() : !currentNode.hasJoined();
//        	if(cond && ((KoalaNode)((Node)g.getNode(i)).getProtocol(targetNodePid)).isGateway()  )
        	if(cond)
            	complyingIndexes.add(i);
        }
		
		int max = Math.min(n, complyingIndexes.size());
		for(int i = 0; i< max; i++){
			int sel = CommonState.r.nextInt(complyingIndexes.size());
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
