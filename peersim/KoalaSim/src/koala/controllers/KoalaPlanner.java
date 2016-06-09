package koala.controllers;

import java.util.ArrayList;

import messaging.KoalaMessage;
import messaging.KoalaMsgContent;
import koala.KoalaNode;
import koala.KoalaProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.reports.GraphObserver;

public class KoalaPlanner extends GraphObserver {

	private static final String PAR_TARGET_PROT = "protocol";
	private static final String PAR_TARGET_NODE = "node";
	private final int targetProtPid;
	private final int targetNodePid;
	private KoalaMessage joinMsg;
	
	public KoalaPlanner(String prefix) {
		super(prefix);
		targetProtPid = Configuration.getPid(prefix + "." + PAR_TARGET_PROT);
		targetNodePid = Configuration.getPid(prefix + "." + PAR_TARGET_NODE);
		joinMsg = new KoalaMessage("", new KoalaMsgContent(KoalaMessage.JOIN));
	}

	@Override
	public boolean execute() {
		updateGraph();
		
		if (hasFinished()){		
			System.out.println("simulation probably finished at: " + CommonState.getTime());
			return true;
		}
		ArrayList<Node> nodes = getNodesToJoin(1);
		for(Node node : nodes){
			KoalaProtocol currentProt = (KoalaProtocol) node.getProtocol(targetProtPid);
        	currentProt.registerMsg(joinMsg);
		}
		
		return false;
	}
	
	
	private ArrayList<Node> getNodesToJoin(int n){
		ArrayList<Node> toRet = new ArrayList<Node>();
		ArrayList<Integer> notJoinIndexes = new ArrayList<Integer>();
		for (int i = 0; i < g.size(); i++) {
        	KoalaNode currentNode = (KoalaNode) ((Node)g.getNode(i)).getProtocol(targetNodePid);
            if(!currentNode.hasJoined())
            	notJoinIndexes.add(i);
        }
		
		int max = Math.min(n, notJoinIndexes.size());
		for(int i = 0; i< max; i++){
			int sel = CommonState.r.nextInt(notJoinIndexes.size());
			toRet.add((Node)g.getNode(sel));
		}
		
		return toRet;
		
	}
	
	private boolean hasFinished(){
		boolean finished = true;
		for (int i = 0; i < g.size(); i++) {
        	KoalaNode currentNode = (KoalaNode) ((Node)g.getNode(i)).getProtocol(targetNodePid);
        	KoalaProtocol currentProt = (KoalaProtocol) ((Node)g.getNode(i)).getProtocol(targetProtPid);
        	
        	if(!currentNode.hasJoined() || !currentProt.hasEmptyQueue())
            	finished = false;
        }
		return finished;
	}

}
