package koala;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.EvictingQueue;

import es.usc.citius.hipster.model.function.impl.LazyActionStateTransitionFunction;
import messaging.KoalaMessage;
import messaging.KoalaMsgContent;
import messaging.KoalaNGNMsgContent;
import messaging.KoalaNLNMsgContent;
import messaging.KoalaRHMsgContent;
import messaging.KoalaRTMsgConent;
import messaging.KoalaRouteMsgContent;
import messaging.TopologyMessage;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import topology.TopologyPathNode;
import topology.TopologyProtocol;
import topology.controllers.ResultCollector;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;


public class KoalaProtocol extends TopologyProtocol{

	public static HashMap<Integer, TopologyMessage> REC_MSG = new HashMap<Integer, TopologyMessage>();
	public static int SUCCESS = 0;
	public static int FAIL = 0;
	public static int INT_FAIL = 0;
	
	
	protected HashMap<String, ArrayList<KoalaRHMsgContent>> advices = new HashMap<String, ArrayList<KoalaRHMsgContent>>();
	protected HashMap<String, KoalaMessage> drafts = new HashMap<String, KoalaMessage>();
	 
	
	private static final String PAR_LEARN= "learn";
	KoalaNode myNode;
	private final boolean learn;
	protected boolean nested;
	protected boolean helpSupported;
	public static int joinHops=0;
	
	public KoalaProtocol(String prefix) {
		super(prefix);
		learn = Configuration.getBoolean(prefix + "." +PAR_LEARN, true);
		nested = NodeUtilities.NESTED;
		helpSupported = true;
	}
	
	@Override
	public void handleMessage(TopologyMessage msg) {
		KoalaMessage kmsg = (KoalaMessage)msg;
//		msgPiggyBack = new ArrayList<KoalaNeighbor>();
//		for(int i = 0; i < kmsg.getPiggyBack().size(); i++)
//			msgPiggyBack.add(kmsg.getPiggyBack().get(i).copy());
		
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
			case KoalaMessage.RH:
				onHelpRequest(kmsg); 
				break;
		}
		
//		if(!initializeMode)
//			checkPiggybackedAfter(kmsg);
		
	}
	

	public void join()
	{
		joinHops = 0;
		if(myNode.hasJoined())
			return;
		
		Node bootstrap = getBootstrap();
		
		if (bootstrap == null){
			myNode.setJoined(true);
			addBootStrap();
//			System.out.println(myNode.getID()+ " is the first joining");
		}else{
			KoalaNode bootstrapRn = (KoalaNode)bootstrap.getProtocol(NodeUtilities.getLinkable(myPid));
//			System.out.println(myNode.getID()+ " is joining on " + bootstrapID);
			myNode.setBootstrapID( bootstrapRn.getSID() );
			KoalaNeighbor first = new KoalaNeighbor(bootstrapRn);
			myNode.tryAddNeighbour(first);
			
			myNode.setJoined(true);
			myNode.setJoining(true);
			
			KoalaMessage km = new KoalaMessage(new KoalaRTMsgConent(myNode));
			km.setIdealPiggyBack();
			joinHops++;
			send(new TopologyPathNode(bootstrapRn), km);
			addBootStrap();
		}
	}
	
	public void send(TopologyPathNode dest, TopologyMessage msg)
	{
		checkPiggybackedAfter();
		super.send(dest, msg);
	}
	
	
	

	
//	protected Node getBootstrap(){
//		String dcid = NodeUtilities.getStrDCID(myNode.getSID());
//		if(bootstraps.containsKey(dcid)){
//			ArrayList<Node> l = bootstraps.get(dcid); 
//			return l.get(CommonState.r.nextInt(l.size())); 
//		}else{
//			int trials = 20;
//			while(--trials != 0){ //try to see if you can guess something present 
//				dcid = CommonState.r.nextInt(NodeUtilities.NR_DC)+"";
//				if(bootstraps.containsKey(dcid)){
//					ArrayList<Node> l = bootstraps.get(dcid); 
//					return l.get(CommonState.r.nextInt(l.size())); 
//				}
//			}
//			if(lastBootstraps.size()==0) return null;
//			//we couldn't guess, so let's pick something we know it exists
//			return NodeUtilities.Nodes.get(lastBootstraps.get(CommonState.r.nextInt(lastBootstraps.size())));
//		}
//	}
	
	protected Node getBootstrap(){
		if(lastBootstraps.size()==0) return null;
//		Node closeBoot = getCloseBootstrap();
//		if(closeBoot!=null) return closeBoot;
		//we couldn't find smth nice 
		return NodeUtilities.Nodes.get(lastBootstraps.get(CommonState.r.nextInt(lastBootstraps.size())));		
	}
	
	
//	protected Node getBootstrap()
//	{
//
//		KoalaNode each;
//		ArrayList<Node> joined = new ArrayList<Node>();
//		ArrayList<Node> joinedInMyDC = new ArrayList<Node>();
//		
//		//next two lines are only for initialization phase
//		ArrayList<Node> joinedClosestToMyDC = new ArrayList<Node>();
//		Node closestJoined; int minDist = NodeUtilities.getSize();
//		
//		for (int i = 0; i < Network.size(); i++) {
//            each = (KoalaNode) Network.get(i).getProtocol(NodeUtilities.getLinkable(myPid));
//            if(getBootCond(Network.get(i))){
//            	joined.add(Network.get(i));
//            	if(myNode.isLocal(each.getSID()))
//            		joinedInMyDC.add(Network.get(i));
//            	else{//this else is useful only for the initialization phase
//            		int dist = NodeUtilities.distance(myNode.getSID(), each.getSID()); 
//            		if(dist < minDist){
//            			minDist = dist;
//            			closestJoined = Network.get(i);
//            			joinedClosestToMyDC = new ArrayList<Node>();
//            			joinedClosestToMyDC.add(closestJoined);
//            		}if(dist == minDist)
//            			joinedClosestToMyDC.add(Network.get(i));
//            	}	
//            	
//            }
//		}
//		if(joined.size() == 0)
//			return null;
//		
//		if(joinedInMyDC.size() > 0)
//			return joinedInMyDC.get(CommonState.r.nextInt(joinedInMyDC.size()));
//		
////		if(initializeMode) //cheap boot, a bit cheating but it can be realistic
//			if(joinedClosestToMyDC.size() > 0)
//				return joinedClosestToMyDC.get(CommonState.r.nextInt(joinedClosestToMyDC.size()));
//			
//		return joined.get(CommonState.r.nextInt(joined.size()));
//	}
//	
//	protected boolean getBootCond(Node n){
//		KoalaNode each = (KoalaNode) n.getProtocol(NodeUtilities.getLinkable(myPid));
//		return each.isUp() && each.hasJoined();
//	}

	
	protected void onRoutingTable(KoalaMessage msg) {
		KoalaNode source = ((KoalaRTMsgConent)msg.getContent()).getNode();
		KoalaNeighbor sourceNeig = new KoalaNeighbor(source);
		TopologyPathNode msgSender = msg.getLastSender();
		boolean sourceJoining = source.getJoining();
		boolean selfJoining = myNode.isJoining();
		
		KoalaNeighbor fwd=null;
		ArrayList<KoalaNeighbor> senderOldNeighbors = source.getRoutingTable().getOldNeighborsContainer();
		ArrayList<KoalaNeighbor> newNeighbors = new ArrayList<KoalaNeighbor>();
		ArrayList<KoalaNeighbor> receivedNeighbors = new ArrayList<KoalaNeighbor>();
		receivedNeighbors.addAll(source.getRoutingTable().getNeighborsContainer());
		receivedNeighbors.addAll(senderOldNeighbors);
		receivedNeighbors.add(sourceNeig);
		
		joinHops++;
		Set<String> processed = new HashSet<String>(); 
		Set<String> neighborsBefore = myNode.getRoutingTable().getFirstGlobalNeighboursIDs();
		Set<Integer> dcsBefore = new HashSet<Integer>(); 
		for(String neighID : neighborsBefore)
			dcsBefore.add(NodeUtilities.getDCID(neighID));
		dcsBefore.add(NodeUtilities.getDCID(myNode.getSID()));
		
		ArrayList<KoalaNeighbor> myOldNeighbors = new ArrayList<KoalaNeighbor>();
		for(KoalaNeighbor recNeighbor: receivedNeighbors){
			if(!processed.add(recNeighbor.getSID())) continue;
			if(recNeighbor.equals(myNode)) continue;
			if(initializeMode && selfJoining && myNode.isLocal(recNeighbor) && myNode.getRoutingTable().getLocals().size()>0 ) continue;
			boolean isSource = recNeighbor.equals(source);
			boolean isSender = recNeighbor.equals(msgSender);
			
			if(selfJoining && myNode.isLocal(source.getSID()))
				dcsBefore.add(NodeUtilities.getDCID(recNeighbor.getSID()));

			double l = isSender ? msg.getLatency() : recNeighbor.getLatency();
			int lq = myNode.getLatencyQuality(isSender, source.getSID(), recNeighbor);
			
			KoalaNeighbor potentialKN = new KoalaNeighbor(recNeighbor, l, lq);
			potentialKN.setRecentlyAdded(true);
            int res  = myNode.tryAddNeighbour(potentialKN, false);
			ArrayList<KoalaNeighbor> oldies = myNode.getRoutingTable().getOldNeighborsContainer();
			myOldNeighbors.addAll(oldies);

			myNode.getRoutingTable().addLongLink(potentialKN);
			if(isSource) addRandomLink(potentialKN);
			myNode.updateLatencyPerDC(recNeighbor.getSID(), l, lq);
			
			if( res == 2)
				newNeighbors.add(potentialKN);
			else if ((res < 0 || (res==1 && sourceJoining))  && isSource){
				fwd = getRoute(source.getSID(), msg);
//				check if we are actually neighbors after cleaning
				if(res < 0){
					res  = myNode.tryAddNeighbour(potentialKN, false);
					if(res >= 0){
						onRoutingTable(msg);
						return;
					} 
				}
				if(fwd != null){
					msg.setConfidential(false);
					send(fwd, msg);
				}else 
					onFail(msg);
			}
		}
		myNode.updateLatencies();
		myNode.getRoutingTable().confirmNeighbors();
		myNode.setJoining(false);
		Set<String> neighborsAfter = myNode.getRoutingTable().getFirstGlobalNeighboursIDs();
		for(KoalaNeighbor newNeig : newNeighbors){
			boolean isSource = newNeig.equals(source);
			
			if(neighborsAfter.contains(newNeig.getSID()) && !neighborsBefore.contains(newNeig.getSID()) 
					|| isSource
					|| myNode.isLocal(newNeig)
			)
			{
				KoalaMessage newMsg = new KoalaMessage(new KoalaRTMsgConent(myNode));
				if(myNode.isLocal(newNeig)){
					if(isSource && !selfJoining){
						broadcastLocalNeighbor(newNeig);
						send(newNeig, newMsg);
					}
				}else
				{
					boolean newDC = !dcsBefore.contains(NodeUtilities.getDCID(newNeig.getSID()));
//					if(newDC && !selfJoining && NodeUtilities.NESTED)
					if(newDC && !selfJoining)
						broadcastGlobalNeighbor(newNeig);
					if(!myNode.isLocal(source.getSID())  && (!msg.isConfidential() || newDC) )
					{
						newMsg.setConfidential(true); 
						send(newNeig, newMsg);
					}
				}
			}
		}
	}
	
	
	protected void onRoute(KoalaMessage msg){
        TopologyPathNode destNode = ((KoalaRouteMsgContent)msg.getContent()).getNode();
//        boolean wantsToUpdateLatency = ((KoalaRouteMsgContent)msg.getContent()).wantsToUpdateLatency();
        TopologyPathNode msgSender = msg.getLastSender();
//        TopologyPathNode msgSender = receviedMsg.getSender();
        
        if(msgSender != null && !msgSender.equals(myNode)){
	        myNode.updateLatencyPerDC(msgSender.getSID(), msg.getLatency(), 3);
	        myNode.updateLatencies();
        }
        
        if(msgSender == null)
        	msg.setIdealPiggyBack();
        else
        	addRandomLink(new KoalaNeighbor(msg.getFirstSender()));
        
        if(!destNode.equals(myNode)){
        	myNode.nrMsgRouted++;
            KoalaNeighbor kn = getRoute(destNode.getSID(), msg);
            if(NodeUtilities.CURRENT_PID == NodeUtilities.KPID && !kn.getCID().equals(kn.getSID())){
            	System.out.println("dafuq, there is smth wrong");
            }
            
            if(NodeUtilities.NR_COLLABORATORS > 0
            	&& !NodeUtilities.sameDC(myNode.getSID(), destNode.getSID()) 
               //&& maybe take into account also the result of getroute
            	&& kn.isBelowThreshold()
            		&& helpSupported){
            	 askForHelp(msg,kn, destNode, 0);
            	 return;
            }
            if(kn != null){
	            //TODO: maybe ask to update latency even in other cases (for the moment ask only if quality is really bad)
	            boolean updateLat = kn.getLatencyQuality() <= 0? true:false;
	//            updateLat = false; //TODO:disabled for the moment 
	            ((KoalaRouteMsgContent)msg.getContent()).setUpdateLatency(updateLat);
	            addPiggybacked(msg,kn.getSID());
	            send(kn, msg);
            }else 
            	onFail(msg); 
           
        }else{
        	onSuccess(msg);
        	
//        	if(/*!initializeMode &&*/ learn){
//	        	KoalaNeighbor ll = new KoalaNeighbor(msg.getFirstSender(),PhysicalDataProvider.getDefaultInterLatency(), 0);
//	        	boolean added = myNode.getRoutingTable().addLongLink(ll);
//	        	if(added){
//	        		//here we are not forcing the long link to be added but maybe it is a good practice to force it give the 
//	        		//fact that there was some interest shown (maybe start counting and force after a threshold) 
//		        	KoalaMessage newMsg = new KoalaMessage(new KoalaMsgContent(KoalaMessage.LL));
//		        	send(msg.getFirstSender(), newMsg);
//	        	}
//        	}        	
        }
        
//        if(/*!initializeMode && */wantsToUpdateLatency){
//        	KoalaMessage newMsg = new KoalaMessage(new KoalaMsgContent(KoalaMessage.LL));
//        	send(msgSender, newMsg);
//        }
        	
	}
	
	
	private void onNewLocalNeighbour(KoalaMessage kmsg) {
		KoalaNLNMsgContent content = (KoalaNLNMsgContent )kmsg.getContent();
		myNode.getRoutingTable().addLocal(content.getNeighbor());
	}
	
	protected void broadcastLocalNeighbor(KoalaNeighbor newNeig) {
		if(initializeMode){
			//version 1 
//			for(KoalaNeighbor kn : myNode.getRoutingTable().getLocals()){
//				if(kn.equals(newNeig)) continue;
//				KoalaNode ln = (KoalaNode)NodeUtilities.Nodes.get(kn.getSID()).getProtocol(NodeUtilities.KID);
//				ln.getRoutingTable().addLocal(newNeig.copy());
//			}
			//version 2 (nothing just return)
			return;
		}
		
		
		for(KoalaNeighbor kn : myNode.getRoutingTable().getLocals()){
			if(kn.equals(newNeig)) continue;
			KoalaNLNMsgContent msgContent = new KoalaNLNMsgContent(newNeig);
			send(kn, new KoalaMessage(msgContent));
		}
	}

	protected void onNewGlobalNeighbours(KoalaMessage msg) {
		KoalaNGNMsgContent content = (KoalaNGNMsgContent )msg.getContent();
		
		content.getNeighbor().setLatencyQuality(2);
		myNode.tryAddNeighbour(content.getNeighbor());
        
		if(CommonState.r.nextDouble() < 2/NodeUtilities.NR_NODE_PER_DC){
			KoalaMessage km = new KoalaMessage(new KoalaRTMsgConent(myNode), true);
        	send(content.getNeighbor(), km);
        }        
	}
	
	protected void broadcastGlobalNeighbor(KoalaNeighbor newNeig) {
//        Set<String> candidates = myNode.createRandomIDs(NodeUtilities.MAGIC);
//        ArrayList<KoalaNeighbor> localNeigs = new ArrayList<KoalaNeighbor>(); 
//        localNeigs.add(myNode.getRoutingTable().getLocalSucessor(0));
////        localNeigs.add(myNode.getRoutingTable().getLocalPredecessor());
//        if(!myNode.getRoutingTable().getLocalPredecessor(0).equals(myNode.getRoutingTable().getLocalSucessor(0)))
//        	localNeigs.add(myNode.getRoutingTable().getLocalPredecessor(0));
        
		if(initializeMode){
			//version 1
//			for(KoalaNeighbor kn : myNode.getRoutingTable().getLocals()){
//				if(NodeUtilities.isDefault(kn)) continue;
//				KoalaNode ln = (KoalaNode)NodeUtilities.Nodes.get(kn.getSID()).getProtocol(NodeUtilities.KID);
//				ln.tryAddNeighbour(newNeig.copy());
//			}
			
			//version 2
			if (myNode.getRoutingTable().getLocals().size()==0)return;
			int dcid = NodeUtilities.getDCID(myNode.getSID());
			for(int i = 0; i < NodeUtilities.NR_NODE_PER_DC; i++){
				KoalaNode ln = (KoalaNode)NodeUtilities.Nodes.get(dcid+"-"+i).getProtocol(NodeUtilities.getLinkable(myPid));
				if(ln.hasJoined() && !ln.isJoining())
					ln.tryAddNeighbour(newNeig.cclone());
			}
				
			
			return;
		}
		
        KoalaNGNMsgContent msgContent = new KoalaNGNMsgContent(null, newNeig); 
        KoalaMessage newMsg = new KoalaMessage(msgContent);        
        for( KoalaNeighbor n : myNode.getRoutingTable().getLocals())
            if(!NodeUtilities.isDefault(n))
                send(n, newMsg);
		
	}
	
	protected void askForHelp(KoalaMessage msg, KoalaNeighbor myBest, TopologyPathNode dest, double score){
		KoalaRHMsgContent msgContent = new KoalaRHMsgContent(dest.getSID(), dest, score, true); 
		ArrayList<KoalaRHMsgContent> advs = new ArrayList<KoalaRHMsgContent>();
		advs.add(new KoalaRHMsgContent(dest.getSID(), myBest, myBest.getRating(), false));//my opinion
		advices.put(dest.getSID(),advs);
        drafts.put(dest.getSID(), msg);
        ArrayList<Integer> inx = new ArrayList<Integer>();
        for(int i = 0; i < myNode.getRoutingTable().getLocals().size(); i++) inx.add(i);
        Collections.shuffle(inx);
//        for(KoalaNeighbor n : myNode.getRoutingTable().getLocals()){
        for(int i =0; i < NodeUtilities.NR_COLLABORATORS; i++){
        	KoalaMessage newMsg = new KoalaMessage(msgContent);
        	KoalaNeighbor n = myNode.getRoutingTable().getLocals().get(inx.get(i));
        	if(!NodeUtilities.isDefault(n))
                send(n, newMsg);
        }
        
	}
	
	protected void onHelpRequest(KoalaMessage msg){
		KoalaRHMsgContent content = (KoalaRHMsgContent)msg.getContent();
		if(content.isRequest()){ //need to give help
			KoalaNeighbor res = myNode.getRoute(content.getNeighbor().getSID(), msg);
			KoalaRHMsgContent msgContent = new KoalaRHMsgContent(content.getLabel(), res, res.getRating(), false);
			KoalaMessage newMsg = new KoalaMessage(msgContent);
			send(msg.getLastSender(), newMsg);
		}else{ //someone is giving me help
			ArrayList<KoalaRHMsgContent> advs = advices.get(content.getLabel()); 
			advs.add(content);
			
//			if(advs.size() == myNode.getRoutingTable().getLocals().size()){
			if(advs.size() == NodeUtilities.NR_COLLABORATORS+1){
				//everybody replied
				double max=-1; TopologyPathNode best=null;
				for(KoalaRHMsgContent ad : advs)
					if(ad.getScore() > max){max = ad.getScore(); best = ad.getNeighbor();}
				
				//send to the best
				if(best!= null){
					ResultCollector.countHelp();
					send(best, drafts.remove(content.getLabel()));
					advices.remove(content.getLabel());
				}else
					onFail(drafts.remove(content.getLabel()));
			}
			
		}
		
	}
	

	protected KoalaNeighbor getRoute(String dest, KoalaMessage msg){
		KoalaNeighbor ret;
		
		if(msg.getContent() instanceof KoalaRouteMsgContent)
			ret = myNode.getRoute(dest, msg);
		else{
			boolean neighDown = ((KoalaRTMsgConent)msg.getContent()).getNeighborsDown();
			boolean isJoining = ((KoalaRTMsgConent)msg.getContent()).getNode().isJoining();
		
			if(!isJoining && neighDown)
				ret = myNode.getClosestEntryBefore(dest);
			else
				ret = myNode.getRoute(dest, msg);
		}
		areNeighborsDown();
		
		if(NodeUtilities.sameDC(dest, myNode.getSID())
		&&	!NodeUtilities.sameDC(ret.getSID(), myNode.getSID())){	
			System.out.println("I am calling outsiders to solve my problems");
			System.exit(1);
		}
			
		return ret;
	}
	
	protected void areNeighborsDown() {
//		check first neighbors
 		boolean predDown = NodeUtilities.isDefault(myNode.getRoutingTable().getGlobalPredecessor(0)); 
 		boolean succDown = NodeUtilities.isDefault(myNode.getRoutingTable().getGlobalSucessor(0));
 		boolean allPredDown = true;
 		boolean allSuccDown = true;
 		
 		if(predDown){
 			KoalaMessage newMsg = new KoalaMessage(new KoalaRTMsgConent(myNode));
 			KoalaNeighbor pred = null;
 			for(KoalaNeighbor pr : myNode.getRoutingTable().getGlobalPredecessors()){
 				if(!NodeUtilities.isDefault(pr) && NodeUtilities.isUp(pr.getCID())){
 					allPredDown = false;
 					pred = pr;
 					break;
 				}
 			}
 			
 			if(allPredDown)
 				pred = myNode.getClosestEntryAfter(myNode.getRoutingTable().getGlobalPredecessor(0).getIdealID());
 			
 			if(pred != null){
 				myNode.tryAddNeighbour(pred);
 				send(pred, newMsg);
 			}
 		}
		
 		if(succDown){
 			KoalaMessage newMsg = new KoalaMessage(new KoalaRTMsgConent(myNode));
 			KoalaNeighbor succ = null;
 			for(KoalaNeighbor su : myNode.getRoutingTable().getGlobalSucessors()){
 				if(!NodeUtilities.isDefault(su) && NodeUtilities.isUp(su.getCID())){
 					allSuccDown = false;
 					succ = su;
 					break;
 				}
 			}
 			
 			if(allSuccDown)
 				succ = myNode.getClosestEntryAfter(myNode.getRoutingTable().getGlobalSucessor(0).getIdealID());
 			
 			if(succ != null){
 				myNode.tryAddNeighbour(succ);
 				send(succ, newMsg);
 			}
 		}
 		
 		
	}
		
	protected void onLongLink(KoalaMessage msg) {
		KoalaNeighbor ll = new KoalaNeighbor(msg.getLastSender(), msg.getLatency(), 3);
		boolean added = myNode.getRoutingTable().addLongLink(ll);
		if(added)
			send(ll, msg);
	}


	/**
	 * Check if i have any neighbor which is closer to ideal IDs of the piggyback 
	 * @param msg
	 */
	protected void checkPiggybackedBefore(KoalaMessage msg) {
		ArrayList<KoalaNeighbor> myNeigs = getNeighbors();
		for(KoalaNeighbor neig : myNeigs){
			for(KoalaNeighbor pl : msg.getPiggyBack()){
				if(pl.getIdealID() == null) continue;
				int dist = NodeUtilities.distance(pl.getIdealID(), neig.getSID());
				int currentDist = NodeUtilities.distance(pl.getIdealID(), pl.getSID());
				if(dist < currentDist){
					pl.setCID(neig.getCID()); pl.setSID(neig.getSID());
				}
			}
		}
	}
	
	/**
	 * Check in the message if there is something interesting 
	 * @param msg
	 */
	protected void checkPiggybackedAfter() {
		if (receviedMsg == null || receviedMsg.getSender() == null || receviedMsg.getSender().equals(myNode)) return;
		
		Set<TopologyPathNode> someIds = new LinkedHashSet<TopologyPathNode>();
		someIds.addAll(receviedMsg.getPath());
		someIds.addAll(receviedMsg.getPiggyback());
		
		ArrayList<KoalaNeighbor> pathNodes = new ArrayList<KoalaNeighbor>(); double latency;
		for(TopologyPathNode p : someIds){
			if(p.equals(myNode) || p.getSID().equals(NodeUtilities.DEFAULTID)) continue;
			KoalaNeighbor kn = new KoalaNeighbor(p);
			latency = myNode.isLocal(p.getSID()) ?
					PhysicalDataProvider.getDefaultIntraLatency() : PhysicalDataProvider.getDefaultInterLatency();
			if(p.equals(receviedMsg.getSender())){
				kn.setLatency(receviedMsg.getLatency());
				kn.setLatencyQuality(3);
			}else{
				kn.setLatency(latency);
				kn.setLatencyQuality(0);
			}
				
			pathNodes.add(kn);
		}
		
//		ArrayList<KoalaNeighbor> neighs = new ArrayList<KoalaNeighbor>();
		for(KoalaNeighbor recNeighbor: pathNodes){
			if(recNeighbor.equals(myNode)) continue;
			int res  = myNode.tryAddNeighbour(recNeighbor);
			
//			if(res == 2){
//				neighs.add(recNeighbor);
//				KoalaMessage newMsg = new KoalaMessage(new KoalaRTMsgConent(myNode));
//				send(recNeighbor.getNodeID(), newMsg);
//			}
			if(res == -1 /*&& !initializeMode*/ && learn)
				myNode.getRoutingTable().addLongLink(recNeighbor);
		}
		
//		if(neighs.size() > 0){
//			KoalaMessage newMsg = new KoalaMessage(new KoalaRTMsgConent(myNode));
//			send(neighs.get(neighs.size()-1).getNodeID(), newMsg);
//		}
		
	}
	
	protected void addPiggybacked(KoalaMessage km, String dest){
		// if I am forwarding to a neighbor 
		if(myNode.inNeighborsList(dest)){
			if (myNode.isLocal(dest)){
//				km.getPiggyBack().addAll(Arrays.asList(myNode.getRoutingTable().getLocalSucessors()));
//				km.getPiggyBack().addAll(Arrays.asList(myNode.getRoutingTable().getLocalPredecessors()));
				km.getPiggyBack().add(myNode.getResponsibleLocalNeighbor(dest));
				km.getPiggyBack().addAll(myNode.getRoutingTable().getLongLinks());
				
			}else{
				km.getPiggyBack().addAll(Arrays.asList(myNode.getRoutingTable().getGlobalSucessors()));
				km.getPiggyBack().addAll(Arrays.asList(myNode.getRoutingTable().getGlobalPredecessors()));
			}
		}
	}

	protected void addRandomLink(KoalaNeighbor rl){
		myNode.getRoutingTable().addRandLink(rl);
	}
	
	@Override
	public void intializeMyNode(Node node, int pid) {
		super.intializeMyNode(node, pid);
		myNode = (KoalaNode) node.getProtocol(NodeUtilities.KID);
		
	}
	
	@Override
	protected String getProtocolName() {
		return "koala";
	}

	
//	protected ArrayList<TopologyPathNode> getPath(){
//		return msgPath;
//	} 
	
	
	
	protected ArrayList<KoalaNeighbor> getNeighbors(){
		return myNode.getRoutingTable().getNeighbors();
	}
	
	
	@Override
	protected void onReceiveLatency(TopologyPathNode dest, double l) {
		 myNode.updateLatencyPerDC(dest.getSID(), l, 3);
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
		String failmsg = "KOALA failed to sent from " + kmsg.getFirstSender();
		if(msg.getContent() instanceof KoalaRouteMsgContent){
			String nid = ((KoalaRouteMsgContent)msg.getContent()).getNode().getSID();
			failmsg += " to " + nid;
			FAIL++;
		}else
			INT_FAIL++;
		
		System.err.println(failmsg);		
		
	}
	

}
