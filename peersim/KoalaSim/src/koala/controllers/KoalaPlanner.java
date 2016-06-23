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
	
	
	public KoalaPlanner(String prefix) {
		super(prefix);
		
		renProtPid = Configuration.getPid(prefix + "." + PAR_RENATER_PROTOCOL);
		renNodePid = FastConfig.getLinkable(renProtPid);
		
		koaProtPid = Configuration.getPid(prefix + "." + PAR_KOALA_PROTOCOL, -1);
		if(koaProtPid > -1)
			koaNodePid = FastConfig.getLinkable(koaProtPid);
		else
			koaNodePid = -1;
		
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

//	private void joinNodes() {
//		ArrayList<Node> nodes = getRandomNodes(1, false);
//		for(Node node : nodes){
//			if(targetLogProtPid > 0){
//				KoalaProtocol koatProt = (KoalaProtocol) node.getProtocol(targetLogProtPid);
//				koatProt.registerMsg(joinMsg);
//			}
//			
//			if(targetPhysProtPid > 0){
//	        	RenaterProtocol retProt = (RenaterProtocol) node.getProtocol(targetPhysProtPid);
//	        	retProt.registerMsg(joinMsg);
//			}
//		}
//	}
	
	
	private void route() {
		int count = 1;
		ArrayList<Node> sources = getRandomNodes(count, true);
		ArrayList<Node> dests = getRandomNodes(count, true);
		
		
		
		for(int i = 0; i < count; i++){
			Node src = sources.get(i);
			TopologyNode dest = koaProtPid >= 0 ? (KoalaNode)dests.get(i).getProtocol(koaNodePid) : (RenaterNode)dests.get(i).getProtocol(renNodePid);
			
			if(koaProtPid > 0){
				KoalaProtocol koatProt = (KoalaProtocol) src.getProtocol(koaProtPid);
				koatProt.registerMsg(new KoalaMessage("", new KoalaRouteMsgContent(dest.getID())));
			}
			
			if(renProtPid > 0){
	        	RenaterProtocol retProt = (RenaterProtocol) src.getProtocol(renProtPid);
	        	retProt.registerMsg(new KoalaMessage("", new KoalaRouteMsgContent(dest.getID())));
			}
		}
		
	}
	
	
	
	
	private ArrayList<Node> getRandomNodes(int n, boolean joined){
		ArrayList<Node> toRet = new ArrayList<Node>();
		ArrayList<Integer> complyingIndexes = new ArrayList<Integer>();
		for (int i = 0; i < g.size(); i++) {
        	int id = koaProtPid >= 0 ? koaProtPid : renProtPid;
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
