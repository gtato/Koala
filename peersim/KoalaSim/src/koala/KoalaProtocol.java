package koala;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import messaging.KoalaMessage;
import messaging.KoalaMsgContent;
import messaging.KoalaNGNMsgContent;
import messaging.KoalaNLNMsgContent;
import messaging.KoalaRTMsgConent;
import messaging.KoalaRouteMsgContent;
import messaging.TopologyMessage;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import topology.TopologyProtocol;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;


public class KoalaProtocol extends TopologyProtocol{

	public static HashMap<Integer, TopologyMessage> REC_MSG =  new HashMap<Integer, TopologyMessage>();
	public static int SUCCESS = 0;
	public static int FAIL = 0;
	public static int INT_FAIL = 0;
	
	private static final String PAR_LEARN= "learn";
	KoalaNode myNode;
	private final boolean learn;
	public KoalaProtocol(String prefix) {
		super(prefix);
		learn = Configuration.getBoolean(prefix + "." +PAR_LEARN, true); 
	}
	
	@Override
	public void handleMessage(TopologyMessage msg) {
		KoalaMessage kmsg = (KoalaMessage)msg;
		msgPiggyBack = new ArrayList<String>();
		for(int i = 0; i < kmsg.getPiggyBack().size(); i++)
			msgPiggyBack.add(kmsg.getPiggyBack().get(i).getNodeID());
		
//		if(!initializeMode)
			checkPiggybackedBefore(kmsg);
		
		
		
		
		switch(kmsg.getType()){
			case KoalaMessage.RT:
				onRoutingTable(kmsg);
				break;
			case KoalaMessage.ROUTE:
				onRoute(kmsg);
				break;
			case KoalaMessage.NGN:
//				logmsg += " " + ((KoalaNGNMsgContent )msg.getContent()).getNeighbor().getNodeID() ; 
				onNewGlobalNeighbours(kmsg);
				break;
			case KoalaMessage.NLN:
//				logmsg += " " + ((KoalaNGNMsgContent )msg.getContent()).getNeighbor().getNodeID() ; 
				onNewLocalNeighbour(kmsg);
				break;
			case KoalaMessage.JOIN:
				join();
				break;
			case KoalaMessage.LL:
				onLongLink(kmsg);
				break;
		}
		
//		if(!initializeMode)
			checkPiggybackedAfter(kmsg);
		
	}
	

	public void join()
	{
		if(myNode.hasJoined())
			return;
		
		Node bootstrap = getBootstrap();
		
		if (bootstrap == null){
			myNode.setJoined(true);
//			System.out.println(myNode.getID()+ " is the first joining");
		}else{
			KoalaNode bootstrapRn = (KoalaNode)bootstrap.getProtocol(linkPid);
			String bootstrapID = bootstrapRn.getID();
//			System.out.println(myNode.getID()+ " is joining on " + bootstrapID);
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
		
		//next two lines are only for initialization phase
		ArrayList<Node> joinedClosestToMyDC = new ArrayList<Node>();
		Node closestJoined; int minDist = NodeUtilities.NR_DC;
		
		for (int i = 0; i < Network.size(); i++) {
            each = (KoalaNode) Network.get(i).getProtocol(linkPid);
            if(each.isUp() && each.hasJoined()){
            	joined.add(Network.get(i));
            	if(myNode.isLocal(each.getID()))
            		joinedInMyDC.add(Network.get(i));
            	else{//this else is useful only for the initialization phase
            		int dist = NodeUtilities.distance(myNode.getID(), each.getID()); 
            		if(dist < minDist){
            			minDist = dist;
            			closestJoined = Network.get(i);
            			joinedClosestToMyDC = new ArrayList<Node>();
            			joinedClosestToMyDC.add(closestJoined);
            		}if(dist == minDist)
            			joinedClosestToMyDC.add(Network.get(i));
            	}	
            	
            }
		}
		if(joined.size() == 0)
			return null;
		
		if(joinedInMyDC.size() > 0)
			return joinedInMyDC.get(CommonState.r.nextInt(joinedInMyDC.size()));
		
//		if(initializeMode) //cheap boot, a bit cheating but it can be realistic
			if(joinedClosestToMyDC.size() > 0)
				return joinedClosestToMyDC.get(CommonState.r.nextInt(joinedClosestToMyDC.size()));
			
		return joined.get(CommonState.r.nextInt(joined.size()));
	}
	

	


	protected void onNewGlobalNeighbours(KoalaMessage msg) {
		KoalaNGNMsgContent content = (KoalaNGNMsgContent )msg.getContent();
		
		content.getNeighbor().setLatencyQuality(2);
        myNode.tryAddNeighbour(content.getNeighbor());
        
        if(CommonState.r.nextDouble() < 2/myNode.getRoutingTable().getLocals().size()){
        	KoalaMessage km = new KoalaMessage(new KoalaRTMsgConent(myNode), true);
        	send(content.getNeighbor().getNodeID(), km);
        }

        
	}

	protected void onRoutingTable(KoalaMessage msg) {
		KoalaNode source = ((KoalaRTMsgConent)msg.getContent()).getNode();

		boolean sourceJoining = source.getJoining();
		boolean selfJoining = myNode.isJoining();
		ArrayList<KoalaNeighbor> senderOldNeighbors = source.getRoutingTable().getOldNeighborsContainer();
		ArrayList<KoalaNeighbor> newNeighbors = new ArrayList<KoalaNeighbor>();
		ArrayList<KoalaNeighbor> receivedNeighbors = source.getRoutingTable().getNeighborsContainer();
		receivedNeighbors.addAll(senderOldNeighbors);
		
		receivedNeighbors.add(new KoalaNeighbor(source.getID()));
		
		
		
		Set<String> neighborsBefore = myNode.getRoutingTable().getFirstGlobalNeighboursIDs();
		Set<Integer> dcsBefore = new HashSet<Integer>(); 
		for(String neighID : neighborsBefore)
			dcsBefore.add(NodeUtilities.getDCID(neighID));
		dcsBefore.add(myNode.getDCID());
		
//		myNode.updateNeighbors(receivedNeighbors, source.getID(), msgSender,  msg.getLatency());
		
		ArrayList<KoalaNeighbor> myOldNeighbors = new ArrayList<KoalaNeighbor>();
		for(KoalaNeighbor recNeighbor: receivedNeighbors){
			boolean isSource = recNeighbor.getNodeID().equals(source.getID());
			boolean isSender = recNeighbor.getNodeID().equals(msgSender);
			if(recNeighbor.getNodeID().equals(myNode.getID()))
				continue;
			if(selfJoining && myNode.isLocal(source.getID()))
				dcsBefore.add(NodeUtilities.getDCID(recNeighbor.getNodeID()));

			double l = isSender ? msg.getLatency() : recNeighbor.getLatency();
			int lq = myNode.getLatencyQuality(isSender, source.getID(), recNeighbor);
			
			KoalaNeighbor potentialKN = new KoalaNeighbor(recNeighbor.getNodeID(), l, lq);
            int res  = myNode.tryAddNeighbour(potentialKN);
			ArrayList<KoalaNeighbor> oldies = myNode.getRoutingTable().getOldNeighborsContainer();
			myOldNeighbors.addAll(oldies);

			myNode.getRoutingTable().addLongLink(potentialKN);
			myNode.updateLatencyPerDC(recNeighbor.getNodeID(), l, lq);
			
			if( res == 2 || (res == 1 && isSource && sourceJoining))
				newNeighbors.add(new KoalaNeighbor(recNeighbor.getNodeID(), l));
			else if (res < 0 && recNeighbor.getNodeID().equals(source.getID())){				
				KoalaNeighbor fwd = getRoute(source, msg);
				
//				check if we are actually neighbors after cleaning
				res  = myNode.tryAddNeighbour(potentialKN);
				if(res >= 0){
					onRoutingTable(msg);
//					newNeighbors.add(new KoalaNeighbor(recNeighbor.getNodeID(), l));
					return;
				} 

				if(fwd != null){
					String dest = fwd.getNodeID();
					msg.setConfidential(false);
					send(dest, msg);
				}
				else 
					onFail(msg);
			}


		}
		myNode.updateLatencies();

		Set<String> neighborsAfter = myNode.getRoutingTable().getFirstGlobalNeighboursIDs();
		for(KoalaNeighbor newNeig : newNeighbors){
			if(neighborsAfter.contains(newNeig.getNodeID()) && !neighborsBefore.contains(newNeig.getNodeID()) || newNeig.getNodeID().equals(source.getID()))
			{
				KoalaMessage newMsg = new KoalaMessage(new KoalaRTMsgConent(myNode));
				if(myNode.isLocal(newNeig.getNodeID())){
					broadcastLocalNeighbor(newNeig);
					send(newNeig.getNodeID(), newMsg);
				}else
				{
					boolean newDC = !dcsBefore.contains(NodeUtilities.getDCID(newNeig.getNodeID()));
//					if(newDC && !selfJoining && NodeUtilities.NESTED)
					if(newDC && !selfJoining && NodeUtilities.NESTED)
						broadcastGlobalNeighbor(newNeig);
					if(!myNode.isLocal(source.getID())  && (!msg.isConfidential() || newDC) )
					{
						newMsg.setConfidential(true); 
						send(newNeig.getNodeID(), newMsg);
					}
				}
			}
		}
		

		
	}
	
	private void onNewLocalNeighbour(KoalaMessage kmsg) {
		KoalaNLNMsgContent content = (KoalaNLNMsgContent )kmsg.getContent();
		myNode.getRoutingTable().addLocal(content.getNeighbor());
	}
	
	private void broadcastLocalNeighbor(KoalaNeighbor newNeig) {
		for(KoalaNeighbor kn : myNode.getRoutingTable().getLocals()){
			if(kn.getNodeID().equals(newNeig.getNodeID())) continue;
			KoalaNLNMsgContent msgContent = new KoalaNLNMsgContent(newNeig);
			send(kn.getNodeID(), new KoalaMessage(msgContent));
		}
	}

	
	private void broadcastGlobalNeighbor(KoalaNeighbor newNeig) {
//        Set<String> candidates = myNode.createRandomIDs(NodeUtilities.MAGIC);
//        ArrayList<KoalaNeighbor> localNeigs = new ArrayList<KoalaNeighbor>(); 
//        localNeigs.add(myNode.getRoutingTable().getLocalSucessor(0));
////        localNeigs.add(myNode.getRoutingTable().getLocalPredecessor());
//        if(!myNode.getRoutingTable().getLocalPredecessor(0).equals(myNode.getRoutingTable().getLocalSucessor(0)))
//        	localNeigs.add(myNode.getRoutingTable().getLocalPredecessor(0));
        
        KoalaNGNMsgContent msgContent = new KoalaNGNMsgContent(null, newNeig); 
        KoalaMessage newMsg = new KoalaMessage(msgContent);        
        for( KoalaNeighbor n : myNode.getRoutingTable().getLocals())
            if(!NodeUtilities.isDefault(n))
                send(n.getNodeID(), newMsg);
		
	}
	
	
	protected void onRoute(KoalaMessage msg){
        String nid = ((KoalaRouteMsgContent)msg.getContent()).getId();
        boolean wantsToUpdateLatency = ((KoalaRouteMsgContent)msg.getContent()).wantsToUpdateLatency();
        if(msgSender != null && msgSender.length() > 0 && !msgSender.equals(myNode.getID())){
	        myNode.updateLatencyPerDC(msgSender, msg.getLatency(), 3);
	        myNode.updateLatencies();
        }
        
        if(msgSender == null)
        	msg.setIdealPiggyBack();
        
        if(!nid.equals(myNode.getID())){
        	myNode.nrMsgRouted++;
            KoalaNeighbor kn = getRoute(new KoalaNode("", nid), msg);
            if(kn != null){
	            //TODO: maybe ask to update latency even in other cases (for the moment ask only if quality is really bad)
	            boolean updateLat = kn.getLatencyQuality() <= 0? true:false;
	//            updateLat = false; //TODO:disabled for the moment 
	            ((KoalaRouteMsgContent)msg.getContent()).setUpdateLatency(updateLat);
	            addPiggybacked(msg,kn.getNodeID());
	            send(kn.getNodeID(), msg);
            }else
            	onFail(msg);
           
        }else{
        	onSuccess(msg);
        	
        	if(/*!initializeMode &&*/ learn){
	        	KoalaNeighbor ll = new KoalaNeighbor(msg.getFirstSender(), PhysicalDataProvider.getDefaultInterLatency(), 0);
	        	boolean added = myNode.getRoutingTable().addLongLink(ll);
	        	if(added){
	        		//here we are not forcing the long link to be added but maybe it is a good practice to force it give the 
	        		//fact that there was some interest shown (maybe start counting and force after a threshold) 
		        	KoalaMessage newMsg = new KoalaMessage(new KoalaMsgContent(KoalaMessage.LL));
		        	send(msg.getFirstSender(), newMsg);
	        	}
        	}        	
        }
        
        if(/*!initializeMode && */wantsToUpdateLatency){
        	KoalaMessage newMsg = new KoalaMessage(new KoalaMsgContent(KoalaMessage.LL));
        	send(msgSender, newMsg);
        }
        	
	}
	
	private KoalaNeighbor getRoute(KoalaNode kn, KoalaMessage msg){
		KoalaNeighbor ret;
		
		if(msg.getContent() instanceof KoalaRouteMsgContent)
			ret = myNode.getRoute(kn, msg);
		else{
			boolean neighDown = ((KoalaRTMsgConent)msg.getContent()).getNeighborsDown();
			boolean isJoining = ((KoalaRTMsgConent)msg.getContent()).getNode().isJoining();
		
			if(!isJoining && neighDown)
				ret = myNode.getClosestEntryBefore(kn.getID());
			else
				ret = myNode.getRoute(kn, msg);
		}
		areNeighborsDown();
		
		if(kn.getDCID() == myNode.getDCID() && NodeUtilities.getDCID(ret.getNodeID()) != myNode.getDCID()){
			System.out.println("I am calling foreigners to solve my problems, I am probably Albanian");
		}
			
		
		return ret;
	}
	
	private void areNeighborsDown() {
//		check first neighbors
 		boolean predDown = NodeUtilities.isDefault(myNode.getRoutingTable().getGlobalPredecessor(0)); 
 		boolean succDown = NodeUtilities.isDefault(myNode.getRoutingTable().getGlobalSucessor(0));
 		boolean allPredDown = true;
 		boolean allSuccDown = true;
 		
 		if(predDown){
 			KoalaMessage newMsg = new KoalaMessage(new KoalaRTMsgConent(myNode));
 			KoalaNeighbor pred = null;
 			for(KoalaNeighbor pr : myNode.getRoutingTable().getGlobalPredecessors()){
 				if(!NodeUtilities.isDefault(pr) && NodeUtilities.isUp(pr.getNodeID())){
 					allPredDown = false;
 					pred = pr;
 					break;
 				}
 			}
 			
 			if(allPredDown)
 				pred = myNode.getClosestEntryAfter(myNode.getRoutingTable().getGlobalPredecessor(0).getIdealID());
 			
 			if(pred != null){
 				myNode.tryAddNeighbour(pred);
 				send(pred.getNodeID(), newMsg);
 			}
 		}
		
 		if(succDown){
 			KoalaMessage newMsg = new KoalaMessage(new KoalaRTMsgConent(myNode));
 			KoalaNeighbor succ = null;
 			for(KoalaNeighbor su : myNode.getRoutingTable().getGlobalSucessors()){
 				if(!NodeUtilities.isDefault(su) && NodeUtilities.isUp(su.getNodeID())){
 					allSuccDown = false;
 					succ = su;
 					break;
 				}
 			}
 			
 			if(allSuccDown)
 				succ = myNode.getClosestEntryAfter(myNode.getRoutingTable().getGlobalSucessor(0).getIdealID());
 			
 			if(succ != null){
 				myNode.tryAddNeighbour(succ);
 				send(succ.getNodeID(), newMsg);
 			}
 		}
 		
 		
	}
	
	
	protected void onLongLink(KoalaMessage msg) {
		KoalaNeighbor ll = new KoalaNeighbor(msgSender, msg.getLatency(), 3);
		boolean added = myNode.getRoutingTable().addLongLink(ll);
		if(added)
			send(ll.getNodeID(), msg);
			
	}


	@Override
	public void intializeMyNode(Node node, int pid) {
		super.intializeMyNode(node, pid);
		myNode = (KoalaNode) (Linkable) node.getProtocol(linkPid);
		
	}

	
	protected void checkPiggybackedAfter(KoalaMessage msg) {
		if (msgSender == null || msgSender.equals(myNode.getID())) return;
		ArrayList<KoalaNeighbor> pathNodes = new ArrayList<KoalaNeighbor>();
		double latency = myNode.isLocal(msgSender) ?
			PhysicalDataProvider.getMaxIntraLatency() : PhysicalDataProvider.getDefaultInterLatency();
		
		Set<String> someIds = new HashSet<String>();
		someIds.addAll(msgPath);
		someIds.addAll(msgPiggyBack);
		
		for(String p : someIds){
			if(p.equals(myNode.getID()) || p.equals(NodeUtilities.DEFAULTID)) continue;
			KoalaNeighbor kn = new KoalaNeighbor(p);
			if(p.equals(msgSender)){
				kn.setLatency(msg.getLatency());
				kn.setLatencyQuality(3);
			}else{
				kn.setLatency(latency);
				kn.setLatencyQuality(0);
			}
				
			pathNodes.add(kn);
		}
		
		
		ArrayList<KoalaNeighbor> neighs = new ArrayList<KoalaNeighbor>();
		for(KoalaNeighbor recNeighbor: pathNodes){
			if(recNeighbor.getNodeID().equals(myNode.getID())) continue;
			int res  = myNode.tryAddNeighbour(recNeighbor);
			
			if(res == 2){
				neighs.add(recNeighbor);
//				KoalaMessage newMsg = new KoalaMessage(new KoalaRTMsgConent(myNode));
//				send(recNeighbor.getNodeID(), newMsg);
			}
			if(res == -1 /*&& !initializeMode*/ && learn)
				myNode.getRoutingTable().addLongLink(recNeighbor);
		}
		
//		if(neighs.size() > 0){
//			KoalaMessage newMsg = new KoalaMessage(new KoalaRTMsgConent(myNode));
//			send(neighs.get(neighs.size()-1).getNodeID(), newMsg);
//		}
		
	}

	@Override
	protected String getProtocolName() {
		return "koala";
	}

	
	/**
	 * Check if i have any neighbor which is closer to ideal IDs of the piggyback 
	 * @param msg
	 */
	protected void checkPiggybackedBefore(KoalaMessage msg) {
		Object[] ids = myNode.getRoutingTable().getNeighboursIDs().toArray();
		String[] idsIknow = Arrays.copyOf(ids, ids.length, String[].class);
		
		for(int i = 0; i < idsIknow.length; i++){
			String ll = idsIknow[i];
			for(int j = 0; j < msg.getPiggyBack().size(); j++){
				KoalaNeighbor kn = msg.getPiggyBack().get(j);
				if(kn.getIdealID() != null){
					int dist = NodeUtilities.distance(kn.getIdealID(), ll);
					int currentDist = NodeUtilities.distance(kn.getIdealID(), kn.getNodeID());
					if(dist < currentDist)
						kn.setNodeID(ll);
				}
			}
		}
	}
	
	private void addPiggybacked(KoalaMessage km, String dest){
		// if I am forwarding to a neighbor 
		if(myNode.inNeighborsList(dest)){
			if (myNode.isLocal(dest)){
//				km.getPiggyBack().addAll(Arrays.asList(myNode.getRoutingTable().getLocalSucessors()));
//				km.getPiggyBack().addAll(Arrays.asList(myNode.getRoutingTable().getLocalPredecessors()));
			}else{
				km.getPiggyBack().addAll(Arrays.asList(myNode.getRoutingTable().getGlobalSucessors()));
				km.getPiggyBack().addAll(Arrays.asList(myNode.getRoutingTable().getGlobalPredecessors()));
			}
		}
	}

	@Override
	protected void onReceiveLatency(String dest, double l) {
		 myNode.updateLatencyPerDC(dest, l, 3);
	     myNode.updateLatencies();
	}

	protected void onSuccess(TopologyMessage msg) {
		msg.setReceivedCycle(CommonState.getTime());
		REC_MSG.put(msg.getID(), msg);
		SUCCESS++;
//		System.out.println(msg.getID()+  " ("+this.getClass().getName() +") "+ myNode.getID()+" got a message through: ["+msg.pathToString()+"] with latency: " +msg.getLatency());
	}
	
	protected void onFail(TopologyMessage msg){
		KoalaMessage kmsg = (KoalaMessage) msg;
		String failmsg = "failed to sent from " + kmsg.getFirstSender();
		if(msg.getContent() instanceof KoalaRouteMsgContent){
			String nid = ((KoalaRouteMsgContent)msg.getContent()).getId();
			failmsg += " to " + nid;
			FAIL++;
		}else
			INT_FAIL++;
		
				
		System.out.println(failmsg);
		
	}

}
