package koala;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;


public class KoalaProtocol extends TopologyProtocol{

	public static HashMap<Integer, TopologyMessage> REC_MSG =  new HashMap<Integer, TopologyMessage>();
	public static int SUCCESS = 0;
	public static int FAIL = 0;
	public static int INT_FAIL = 0;
	
	protected HashMap<Integer, ArrayList<Node>> bootstraps = new HashMap<Integer, ArrayList<Node>>();
	protected HashMap<String, ArrayList<KoalaRHMsgContent>> advices = new HashMap<String, ArrayList<KoalaRHMsgContent>>();
	protected HashMap<String, KoalaMessage> drafts = new HashMap<String, KoalaMessage>();
	ArrayList<String> lastBootstraps = new ArrayList<String>(); 
	
	private static final String PAR_LEARN= "learn";
	KoalaNode myNode;
	private final boolean learn;
	protected boolean nested;
	protected boolean helpSupported;
	
	public KoalaProtocol(String prefix) {
		super(prefix);
		learn = Configuration.getBoolean(prefix + "." +PAR_LEARN, true);
		nested = NodeUtilities.NESTED;
		helpSupported = true;
	}
	
	@Override
	public void handleMessage(TopologyMessage msg) {
		KoalaMessage kmsg = (KoalaMessage)msg;
		msgPiggyBack = new ArrayList<KoalaNeighbor>();
		for(int i = 0; i < kmsg.getPiggyBack().size(); i++)
			msgPiggyBack.add(kmsg.getPiggyBack().get(i).copy());
		
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
			checkPiggybackedAfter(kmsg);
		
	}
	

	public void join()
	{
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
			
			KoalaMessage km = new KoalaMessage(new KoalaRTMsgConent(myNode));
			send(new TopologyPathNode(bootstrapRn), km);
			addBootStrap();
		}
	}
	
	private void addBootStrap(){
		int dcid = NodeUtilities.getDCID(myNode.getSID());
		lastBootstraps.add(0, myNode.getCID());  if(lastBootstraps.size()==11)lastBootstraps.remove(10);
		if(!bootstraps.containsKey(dcid)){
			ArrayList<Node> l = new ArrayList<Node>();
			l.add(myNode.getNode());
			bootstraps.put(dcid, l);
		}
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
		int dcid = NodeUtilities.getDCID(myNode.getSID());
		for(int i=0;i<50;i++){
			int p = dcid+i>=NodeUtilities.NR_DC? dcid+i-NodeUtilities.NR_DC:dcid+i;
			int m = dcid-i<0? NodeUtilities.NR_DC-dcid-i:dcid-i;
			ArrayList<Node> l = null;
			if(bootstraps.containsKey(p))
				l = bootstraps.get(p);
			if(bootstraps.containsKey(m))
				l = bootstraps.get(m);
			if(l!=null)
				return l.get(CommonState.r.nextInt(l.size())); 
		}
		
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

	


	protected void onNewGlobalNeighbours(KoalaMessage msg) {
		KoalaNGNMsgContent content = (KoalaNGNMsgContent )msg.getContent();
		
		content.getNeighbor().setLatencyQuality(2);
		myNode.tryAddNeighbour(content.getNeighbor());
        
//        if(CommonState.r.nextDouble() < 2/myNode.getRoutingTable().getLocals().size()){
		if(CommonState.r.nextDouble() < 2/NodeUtilities.NR_NODE_PER_DC){
		KoalaMessage km = new KoalaMessage(new KoalaRTMsgConent(myNode), true);
        	send(content.getNeighbor(), km);
        }

        
	}

	
	protected void onRoutingTable(KoalaMessage msg) {
		KoalaNode source = ((KoalaRTMsgConent)msg.getContent()).getNode();
		KoalaNeighbor firstLocal= null;
		boolean sourceJoining = source.getJoining();
		boolean selfJoining = myNode.isJoining();
		KoalaNeighbor fwd=null;
		ArrayList<KoalaNeighbor> senderOldNeighbors = source.getRoutingTable().getOldNeighborsContainer();
		ArrayList<KoalaNeighbor> newNeighbors = new ArrayList<KoalaNeighbor>();
		ArrayList<KoalaNeighbor> receivedNeighbors = new ArrayList<KoalaNeighbor>();
		receivedNeighbors.addAll(source.getRoutingTable().getNeighborsContainer());
		receivedNeighbors.addAll(senderOldNeighbors);
		
		receivedNeighbors.add(new KoalaNeighbor(source));
		
		
		
		Set<String> neighborsBefore = myNode.getRoutingTable().getFirstGlobalNeighboursIDs();
		int localsBefore = myNode.getRoutingTable().getLocals().size();
		Set<Integer> dcsBefore = new HashSet<Integer>(); 
		for(String neighID : neighborsBefore)
			dcsBefore.add(NodeUtilities.getDCID(neighID));
		dcsBefore.add(NodeUtilities.getDCID(myNode.getSID()));
		
//		myNode.updateNeighbors(receivedNeighbors, source.getID(), msgSender,  msg.getLatency());
		
		ArrayList<KoalaNeighbor> myOldNeighbors = new ArrayList<KoalaNeighbor>();
		for(KoalaNeighbor recNeighbor: receivedNeighbors){
			if(recNeighbor.equals(myNode)) continue;
			boolean isSource = recNeighbor.equals(source);
			boolean isSender = recNeighbor.equals(msgSender);
			
			if(localsBefore==0 && myNode.isLocal(recNeighbor))
				firstLocal = recNeighbor;
			if(selfJoining && myNode.isLocal(source.getSID()))
				dcsBefore.add(NodeUtilities.getDCID(recNeighbor.getSID()));

			double l = isSender ? msg.getLatency() : recNeighbor.getLatency();
			int lq = myNode.getLatencyQuality(isSender, source.getSID(), recNeighbor);
			
			KoalaNeighbor potentialKN = new KoalaNeighbor(recNeighbor, l, lq);
            int res  = myNode.tryAddNeighbour(potentialKN, false);
			ArrayList<KoalaNeighbor> oldies = myNode.getRoutingTable().getOldNeighborsContainer();
			myOldNeighbors.addAll(oldies);

			myNode.getRoutingTable().addLongLink(potentialKN);
			myNode.updateLatencyPerDC(recNeighbor.getSID(), l, lq);
			
			if( res == 2 || (res == 1 && isSource && sourceJoining))
				newNeighbors.add(potentialKN);
			else if (res < 0 && isSource){				
				fwd = getRoute(source, msg);
				
//				check if we are actually neighbors after cleaning
				res  = myNode.tryAddNeighbour(potentialKN, false);
				if(res >= 0){
					onRoutingTable(msg);
//					newNeighbors.add(new KoalaNeighbor(recNeighbor.getNodeID(), l));
					return;
				} 

				if(fwd != null){
					msg.setConfidential(false);
					send(fwd, msg);
				}else 
					onFail(msg);
			}


		}
		myNode.updateLatencies();

		if(firstLocal!=null){
			//if i find a first family member, i ask it first
			send(firstLocal, new KoalaMessage(new KoalaRTMsgConent(myNode)));
			return;
		}
		
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
					broadcastLocalNeighbor(newNeig);
					send(newNeig, newMsg);
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
	
	private void onNewLocalNeighbour(KoalaMessage kmsg) {
		KoalaNLNMsgContent content = (KoalaNLNMsgContent )kmsg.getContent();
		myNode.getRoutingTable().addLocal(content.getNeighbor());
	}
	
	protected void broadcastLocalNeighbor(KoalaNeighbor newNeig) {
		for(KoalaNeighbor kn : myNode.getRoutingTable().getLocals()){
			if(kn.equals(newNeig)) continue;
			KoalaNLNMsgContent msgContent = new KoalaNLNMsgContent(newNeig);
			send(kn, new KoalaMessage(msgContent));
		}
	}

	
	protected void broadcastGlobalNeighbor(KoalaNeighbor newNeig) {
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
                send(n, newMsg);
		
	}
	
	protected void askForHelp(KoalaMessage msg, TopologyPathNode dest, double score){
		KoalaRHMsgContent msgContent = new KoalaRHMsgContent(dest.getSID(), dest, score, true); 
        advices.put(dest.getSID(), new ArrayList<KoalaRHMsgContent>());
        drafts.put(dest.getSID(), msg);
        for(KoalaNeighbor n : myNode.getRoutingTable().getLocals()){
        	KoalaMessage newMsg = new KoalaMessage(msgContent);
        	if(!NodeUtilities.isDefault(n))
                send(n, newMsg);
        }
        
	}
	
	
	protected void onHelpRequest(KoalaMessage msg){
		KoalaRHMsgContent content = (KoalaRHMsgContent)msg.getContent();
		if(content.isRequest()){ //need to give help
			AbstractMap.SimpleEntry<Double, KoalaNeighbor> res = myNode.getRouteResult(new KoalaNode("",content.getNeighbor()), msg);
			KoalaRHMsgContent msgContent = new KoalaRHMsgContent(content.getLabel(), res.getValue(), res.getKey(), false);
			KoalaMessage newMsg = new KoalaMessage(msgContent);
			send(msgSender, newMsg);
		}else{ //someone is giving me help
			ArrayList<KoalaRHMsgContent> advs = advices.get(content.getLabel()); 
			advs.add(content);
			
			if(advs.size() == myNode.getRoutingTable().getLocals().size()){
				//everybody replied
				double max=-1; TopologyPathNode best=null;
				for(KoalaRHMsgContent ad : advs)
					if(ad.getScore() > max){max = ad.getScore(); best = ad.getNeighbor();}
				
				//send to the best
				if(best!= null){
					send(best, drafts.remove(content.getLabel()));
					advices.remove(content.getLabel());
				}else
					onFail(drafts.remove(content.getLabel()));
			}
			
		}
		
	}
	
	
	protected void onRoute(KoalaMessage msg){
        TopologyPathNode destNode = ((KoalaRouteMsgContent)msg.getContent()).getNode();
//        boolean wantsToUpdateLatency = ((KoalaRouteMsgContent)msg.getContent()).wantsToUpdateLatency();
        
        if(msgSender != null && !msgSender.equals(myNode)){
	        myNode.updateLatencyPerDC(msgSender.getSID(), msg.getLatency(), 3);
	        myNode.updateLatencies();
        }
        
        if(msgSender == null)
        	msg.setIdealPiggyBack();
        
        if(!destNode.equals(myNode)){
        	myNode.nrMsgRouted++;
            KoalaNeighbor kn = getRoute(new KoalaNode("", destNode.getCID(), destNode.getSID()), msg);
            if(NodeUtilities.CURRENT_PID == NodeUtilities.KPID && !kn.getCID().equals(kn.getSID())){
            	System.out.println("dafuq, there is smth wrong");
            }
            
            if(NodeUtilities.COLLABORATE
            	&& !NodeUtilities.sameDC(myNode.getSID(), destNode.getSID()) 
               //&& maybe take into account also the result of getroute 
            		&& helpSupported){
            	 askForHelp(msg, destNode, 0);
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
	
	protected KoalaNeighbor getRoute(KoalaNode kn, KoalaMessage msg){
		KoalaNeighbor ret;
		
		if(msg.getContent() instanceof KoalaRouteMsgContent)
			ret = myNode.getRoute(kn, msg);
		else{
			boolean neighDown = ((KoalaRTMsgConent)msg.getContent()).getNeighborsDown();
			boolean isJoining = ((KoalaRTMsgConent)msg.getContent()).getNode().isJoining();
		
			if(!isJoining && neighDown)
				ret = myNode.getClosestEntryBefore(kn.getSID());
			else
				ret = myNode.getRoute(kn, msg);
		}
		areNeighborsDown();
		
		if(NodeUtilities.sameDC(kn.getSID(), myNode.getSID())
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
		KoalaNeighbor ll = new KoalaNeighbor(msgSender, msg.getLatency(), 3);
		boolean added = myNode.getRoutingTable().addLongLink(ll);
		if(added)
			send(ll, msg);
			
	}


	@Override
	public void intializeMyNode(Node node, int pid) {
		super.intializeMyNode(node, pid);
		myNode = (KoalaNode) node.getProtocol(NodeUtilities.KID);
		
	}


	/**
	 * Check in the message if there is something interesting 
	 * @param msg
	 */
	protected void checkPiggybackedAfter(KoalaMessage msg) {
		if (msgSender == null || msgSender.equals(myNode)) return;
		
		Set<TopologyPathNode> someIds = new HashSet<TopologyPathNode>();
		someIds.addAll(getPath());
		someIds.addAll(msgPiggyBack);
		
		ArrayList<KoalaNeighbor> pathNodes = new ArrayList<KoalaNeighbor>(); double latency;
		for(TopologyPathNode p : someIds){
			if(p.equals(myNode) || p.getSID().equals(NodeUtilities.DEFAULTID)) continue;
			KoalaNeighbor kn = new KoalaNeighbor(p);
			latency = myNode.isLocal(p.getSID()) ?
					PhysicalDataProvider.getMaxIntraLatency() : PhysicalDataProvider.getDefaultInterLatency();
			if(p.equals(msgSender)){
				kn.setLatency(msg.getLatency());
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

	@Override
	protected String getProtocolName() {
		return "koala";
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

	
	protected ArrayList<TopologyPathNode> getPath(){
		return msgPath;
	} 
	
	
	
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
		
				
		System.out.println(failmsg);
		
	}

}
