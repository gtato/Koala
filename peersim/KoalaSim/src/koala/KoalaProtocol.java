package koala;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import messaging.KoalaNGNMsgContent;
import messaging.KoalaMessage;
import messaging.KoalaRTMsgConent;
import messaging.KoalaRouteMsgContent;
import koala.utility.ErrorDetection;
import koala.utility.KoalaJsonParser;
import koala.utility.NodeUtilities;
import peersim.core.CommonState;
import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;


public class KoalaProtocol extends TopologyProtocol implements CDProtocol{

	KoalaNode myNode;
	public KoalaProtocol(String prefix) {
		super(prefix);
	}
		
	public void join()
	{
		if(myNode.hasJoined())
			return;
		
		Node bootstrap = getBootstrap();
		
		if (bootstrap == null)
			myNode.setJoined(true);
		else{
			KoalaNode bootstrapRn = (KoalaNode)bootstrap.getProtocol(linkPid);
			String bootstrapID = bootstrapRn.getID();
			myNode.setBootstrapID( bootstrapID );
			KoalaNeighbor first = new KoalaNeighbor(bootstrapID);
			myNode.tryAddNeighbour(first);
			
			myNode.setJoined(true);
			KoalaMessage km = new KoalaMessage(new KoalaRTMsgConent(myNode));
			send(bootstrapID, km);
		}
	}
	
	private Node getBootstrap_old()
	{

		KoalaNode each;
		ArrayList<Node> joined = new ArrayList<Node>();
		for (int i = 0; i < Network.size(); i++) {
            each = (KoalaNode) Network.get(i).getProtocol(linkPid);
            if(each.hasJoined())
            	joined.add(Network.get(i));   	
		}
		if(joined.size() == 0)
			return null;
		
		return joined.get(CommonState.r.nextInt(joined.size()));
	}
	
	private Node getBootstrap()
	{

		KoalaNode each;
		ArrayList<Node> joined = new ArrayList<Node>();
		ArrayList<Node> joinedInMyDC = new ArrayList<Node>();
		for (int i = 0; i < Network.size(); i++) {
            each = (KoalaNode) Network.get(i).getProtocol(linkPid);
            if(each.hasJoined()){
            	joined.add(Network.get(i));
            	if(myNode.isLocal(each.getID()))
            		joinedInMyDC.add(Network.get(i));
            }
		}
		if(joined.size() == 0)
			return null;
		
		if(joinedInMyDC.size() > 0)
			return joinedInMyDC.get(CommonState.r.nextInt(joinedInMyDC.size()));
		
		return joined.get(CommonState.r.nextInt(joined.size()));
	}
	

	


	protected void onNewGlobalNeighbours(KoalaMessage msg) {
		KoalaNGNMsgContent content = (KoalaNGNMsgContent )msg.getContent();
		
        ArrayList<String> respees = new ArrayList<String>();
        for(String  c : content.getCandidates()){
            if(myNode.isResponsible(c))
                if(respees.size() == 0)
                {
                	KoalaMessage km = new KoalaMessage(new KoalaRTMsgConent(myNode), true);
                	send(content.getNeighbor().getNodeID(), km);
                }
 
                respees.add(c);
        }
        content.getNeighbor().setLatencyQuality(2);
        int rnes = myNode.tryAddNeighbour(content.getNeighbor());
        if(rnes != 2)
            return;
        
        List<String> cands = new ArrayList<String>();
        for(String cand : content.getCandidates())
        	if (!respees.contains(cand))
        		cands.add(cand);
        
        Set<String> add_cands = myNode.createRandomIDs(respees.size() - 1);
        cands.addAll(add_cands);
        Set<String> new_cands = new HashSet<String>(cands);
        content.setCandidates(new_cands.toArray(new String[new_cands.size()]));
        msg.setContent(content);
        
        KoalaNeighbor target = myNode.getRoutingTable().getLocalPredecessor();
        if (msg.getLastSender().equals(target))
            target = myNode.getRoutingTable().getLocalSucessor();
        if(!NodeUtilities.isDefault(target))
        	send(target.getNodeID(), msg);
	}

	protected void onRoutingTable(KoalaMessage msg) {
		String lastSender = msg.getLastSender();
		KoalaNode source = ((KoalaRTMsgConent)msg.getContent()).getNode();
//		boolean isForeverAlone = knowSenderLocalNeighs(sender);
		boolean sourceJoining = source.getJoining();
		boolean selfJoining = myNode.isJoining();
		ArrayList<KoalaNeighbor> senderOldNeighbors = source.getRoutingTable().getOldNeighborsContainer();
		ArrayList<KoalaNeighbor> newNeighbors = new ArrayList<KoalaNeighbor>();
		ArrayList<KoalaNeighbor> receivedNeighbors = source.getRoutingTable().getNeighborsContainer();
		receivedNeighbors.addAll(senderOldNeighbors);
		
		receivedNeighbors.add(new KoalaNeighbor(source.getID()));
		
		
		
		Set<String> neighborsBefore = myNode.getRoutingTable().getNeighboursIDs();
		Set<Integer> dcsBefore = new HashSet<Integer>(); 
		for(String neighID : neighborsBefore)
			dcsBefore.add(NodeUtilities.getDCID(neighID));
		dcsBefore.add(myNode.getDCID());
		
		
		ArrayList<KoalaNeighbor> myOldNeighbors = new ArrayList<KoalaNeighbor>();
		for(KoalaNeighbor recNeighbor: receivedNeighbors){
			boolean isSource = recNeighbor.getNodeID().equals(source.getID());
			boolean isSender = recNeighbor.getNodeID().equals(lastSender);
			if(recNeighbor.getNodeID().equals(myNode.getID()))
				continue;
			if(selfJoining && myNode.isLocal(source.getID()))
				dcsBefore.add(NodeUtilities.getDCID(recNeighbor.getNodeID()));

			double l = isSender ? msg.getLatency() : recNeighbor.getLatency();
			int lq = myNode.getLatencyQuality(isSender, source.getID(), recNeighbor);
			
            int res  = myNode.tryAddNeighbour(new KoalaNeighbor(recNeighbor.getNodeID(), l, lq));
			ArrayList<KoalaNeighbor> oldies = myNode.getRoutingTable().getOldNeighborsContainer();
			myOldNeighbors.addAll(oldies);

			myNode.updateLatencyPerDC(recNeighbor.getNodeID(), l, lq);
			
			if( res == 2 || (res == 1 && isSource && sourceJoining))
				newNeighbors.add(new KoalaNeighbor(recNeighbor.getNodeID(), l));
			else if (res < 0 && recNeighbor.getNodeID().equals(source.getID())){				
				String dest = myNode.getRoute(source.getID(), msg);
				msg.setConfidential(false);
				send(dest, msg);
			}


		}
		myNode.updateLatencies();

		Set<String> neighborsAfter = myNode.getRoutingTable().getNeighboursIDs();
//		System.out.println();
//		if(myNode.getID().equals("5-1"))
//			System.out.println("before: " + neighborsBefore + " after:" + neighborsAfter + " received: " + receivedNeighbors);
		for(KoalaNeighbor newNeig : newNeighbors){
			if(neighborsAfter.contains(newNeig.getNodeID()) && !neighborsBefore.contains(newNeig.getNodeID()) || newNeig.getNodeID().equals(source.getID()))
			{
				KoalaMessage newMsg = new KoalaMessage(new KoalaRTMsgConent(myNode));
				if(myNode.isLocal(newNeig.getNodeID())){
					send(newNeig.getNodeID(), newMsg);
				}else
				{
					boolean newDC = !dcsBefore.contains(NodeUtilities.getDCID(newNeig.getNodeID()));
					if(newDC && !selfJoining)
						broadcastGlobalNeighbor(newNeig);
					if(!myNode.isLocal(source.getID())  && (!msg.isConfidential() || newDC) )
					{
						newMsg.setConfidential(true); 
						send(newNeig.getNodeID(), newMsg);
					}
				}
			}
		}
		
//		sendToForeverAlone(myOldNeighbors);
		
	}

//	private void sendToForeverAlone(ArrayList<KoalaNeighbor> myOldNeighbors) {
//		KoalaMessage newMsg = new KoalaMessage(myNode.getID(), new KoalaRTMsgConent(myNode));
//		for(KoalaNeighbor oldie: myOldNeighbors){
//			if(oldie.isForeverAlone())
////				System.out.println(oldie.getNodeID() + " is forever alone maybe " + myNode.getID() +" should introduce it to new people");
//				send(oldie.getNodeID(), newMsg);
//		}
//		
//	}

//	private boolean knowSenderLocalNeighs(KoalaNode sender){
//		ArrayList<KoalaNeighbor> senderNeighbors = sender.getRoutingTable().getNeighborsContainer();
//		boolean hasLocalNeigs = false;
//		for(KoalaNeighbor kn : senderNeighbors){
//			if(NodeUtilities.sameDC(kn.getNodeID(), sender.getID()) && !kn.getNodeID().equals(sender.getID()))
//				hasLocalNeigs = true;
//		}
//		if(hasLocalNeigs)
//			return false;
//		Set<String> neighborsBefore = myNode.getRoutingTable().getNeighboursIDs();
//		for(String knID : neighborsBefore){
//			if(NodeUtilities.sameDC(knID, sender.getID()) && !knID.equals(sender.getID()))
//				return true;
//		}
//		return false;
//	}
	
//	private boolean nodeIsForeverAlone(KoalaNode sender){
//		ArrayList<KoalaNeighbor> senderNeighbors = sender.getRoutingTable().getNeighborsContainer();
//		for(KoalaNeighbor kn : senderNeighbors){
//			if(NodeUtilities.sameDC(kn.getNodeID(), sender.getID()) && !kn.getNodeID().equals(sender.getID()))
//				return false;
//		}
//		
//		return true;
//	}
	
	
	private void broadcastGlobalNeighbor(KoalaNeighbor newNeig) {
        Set<String> candidates = myNode.createRandomIDs(NodeUtilities.MAGIC);
        ArrayList<KoalaNeighbor> localNeigs = new ArrayList<KoalaNeighbor>(); 
        localNeigs.add(myNode.getRoutingTable().getLocalSucessor());
//        localNeigs.add(myNode.getRoutingTable().getLocalPredecessor());
        if(!myNode.getRoutingTable().getLocalPredecessor().getNodeID().equals(myNode.getRoutingTable().getLocalSucessor().getNodeID()))
        	localNeigs.add(myNode.getRoutingTable().getLocalPredecessor());
        
        KoalaNGNMsgContent msgContent = new KoalaNGNMsgContent(candidates.toArray(new String[candidates.size()]), newNeig); 
        KoalaMessage newMsg = new KoalaMessage(msgContent);        
        for( KoalaNeighbor n : localNeigs)
            if(!NodeUtilities.isDefault(n))
                send(n.getNodeID(), newMsg);
		
	}
	
	
	protected void onRoute(KoalaMessage msg){
        String nid = ((KoalaRouteMsgContent)msg.getContent()).getId();
        if(msg.getLastSender() != null && msg.getLastSender().length() > 0){
	        myNode.updateLatencyPerDC(msg.getLastSender(), msg.getLatency(), 3);
	        myNode.updateLatencies();
        }
        if(!nid.equals(myNode.getID()))
            send(myNode.getRoute(nid, msg), msg);
        else
        	onReceivedMsg(msg);
	}
	
	

//	public boolean hasJoined() {
//		return joined;
//	}

	@Override
	public void intializeMyNode(Node node) {
		super.intializeMyNode(node);
		myNode = (KoalaNode) (Linkable) node.getProtocol(linkPid);
		
	}

	@Override
	protected void checkPiggybacked(KoalaMessage msg) {
//		ArrayList<KoalaNeighbor> receivedNeighbors = new ArrayList<KoalaNeighbor>();
//		for(String p : msg.getPath())
//			receivedNeighbors.add(new KoalaNeighbor(p));
//		
//		
//		for(KoalaNeighbor recNeighbor: receivedNeighbors){
//			if(recNeighbor.getNodeID().equals(myNode.getID())) continue;
//			int res  = myNode.tryAddNeighbour(recNeighbor);
//			
//			if( res == 2){
//				KoalaMessage newMsg = new KoalaMessage(myNode.getID(), new KoalaRTMsgConent(myNode));
//				send(recNeighbor.getNodeID(), newMsg);
////				System.out.println("### " + myNode.getID() + " found " + recNeighbor.getNodeID());
//			}
//				
//		}
		
	}

	@Override
	protected String getProtocolName() {
		return "koala";
	}

//	@Override
//	protected void checkStatus() {
//		long age = myNode.getAge(); 
//		if(age >=  1000){
//			Set<String> localNeigs =  myNode.getRoutingTable().getNeighboursIDs(1);
//			if(localNeigs.size() < 2){
//				System.out.println("("+CommonState.getTime()+") "+ myNode.getID() + " is " +age +" cycles old and has "+ localNeigs.size() +" friends");
//				myNode.setJoined(false);
//				myNode.resetRoutingTable();
//				join();
//			}
//		}
//		
//	}
}
