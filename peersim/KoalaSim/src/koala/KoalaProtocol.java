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
	
	private static final String PAR_LEARN= "learn";
	KoalaNode myNode;
	private final boolean learn;
	public KoalaProtocol(String prefix) {
		super(prefix);
		learn = Configuration.getBoolean(prefix + "." +PAR_LEARN, true); 
	}
	
	@Override
	protected void handleMessage(TopologyMessage msg) {
		KoalaMessage kmsg = (KoalaMessage)msg;
		msgPiggyBack = new ArrayList<String>();
		for(int i = 0; i < kmsg.getPiggyBack().size(); i++)
			msgPiggyBack.add(kmsg.getPiggyBack().get(i).getNodeID());
		
		if(!initializeMode)
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
			case KoalaMessage.JOIN:
				join();
				break;
			case KoalaMessage.LL:
				onLongLink(kmsg);
				break;
		}
		
		if(!initializeMode)
			checkPiggybackedAfter(kmsg);
		
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
		
		//next two lines are only for initialization phase
		ArrayList<Node> joinedClosestToMyDC = new ArrayList<Node>();
		Node closestJoined; int minDist = NodeUtilities.NR_DC;
		
		for (int i = 0; i < Network.size(); i++) {
            each = (KoalaNode) Network.get(i).getProtocol(linkPid);
            if(each.hasJoined()){
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
		
		if(initializeMode)
			if(joinedClosestToMyDC.size() > 0)
				return joinedClosestToMyDC.get(CommonState.r.nextInt(joinedClosestToMyDC.size()));
			
		return joined.get(CommonState.r.nextInt(joined.size()));
	}
	

	


	protected void onNewGlobalNeighbours(KoalaMessage msg) {
		KoalaNGNMsgContent content = (KoalaNGNMsgContent )msg.getContent();
		
		content.getNeighbor().setLatencyQuality(2);
        int rnes = myNode.tryAddNeighbour(content.getNeighbor());
        
        ArrayList<String> respees = new ArrayList<String>();
        for(String  c : content.getCandidates()){
            if(myNode.isResponsible(c)){
                if(respees.size() == 0)
                {
                	KoalaMessage km = new KoalaMessage(new KoalaRTMsgConent(myNode), true);
                	send(content.getNeighbor().getNodeID(), km);
                }
 
                respees.add(c);
            }
        }
        
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
        
        KoalaNeighbor target = myNode.getRoutingTable().getLocalPredecessor(0);
        if (msgSender.equals(target))
            target = myNode.getRoutingTable().getLocalSucessor(0);
        if(!NodeUtilities.isDefault(target))
        	send(target.getNodeID(), msg);
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
		
		
		
		Set<String> neighborsBefore = myNode.getRoutingTable().getNeighboursIDs();
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
			
            int res  = myNode.tryAddNeighbour(new KoalaNeighbor(recNeighbor.getNodeID(), l, lq));
			ArrayList<KoalaNeighbor> oldies = myNode.getRoutingTable().getOldNeighborsContainer();
			myOldNeighbors.addAll(oldies);

			myNode.updateLatencyPerDC(recNeighbor.getNodeID(), l, lq);
			
			if( res == 2 || (res == 1 && isSource && sourceJoining))
				newNeighbors.add(new KoalaNeighbor(recNeighbor.getNodeID(), l));
			else if (res < 0 && recNeighbor.getNodeID().equals(source.getID())){				
				String dest = myNode.getRoute(source.getID(), msg).getNodeID();
				msg.setConfidential(false);
				send(dest, msg);
			}


		}
		myNode.updateLatencies();

		Set<String> neighborsAfter = myNode.getRoutingTable().getNeighboursIDs();
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
		

		
	}

	
	private void broadcastGlobalNeighbor(KoalaNeighbor newNeig) {
        Set<String> candidates = myNode.createRandomIDs(NodeUtilities.MAGIC);
        ArrayList<KoalaNeighbor> localNeigs = new ArrayList<KoalaNeighbor>(); 
        localNeigs.add(myNode.getRoutingTable().getLocalSucessor(0));
//        localNeigs.add(myNode.getRoutingTable().getLocalPredecessor());
        if(!myNode.getRoutingTable().getLocalPredecessor(0).equals(myNode.getRoutingTable().getLocalSucessor(0)))
        	localNeigs.add(myNode.getRoutingTable().getLocalPredecessor(0));
        
        KoalaNGNMsgContent msgContent = new KoalaNGNMsgContent(candidates.toArray(new String[candidates.size()]), newNeig); 
        KoalaMessage newMsg = new KoalaMessage(msgContent);        
        for( KoalaNeighbor n : localNeigs)
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
            KoalaNeighbor kn = myNode.getRoute(nid, msg);
            if(kn != null){
	            //TODO: maybe ask to update latency even in other cases (for the moment ask only if quality is really bad)
	            boolean updateLat = kn.getLatencyQuality() <= 0? true:false;
	//            updateLat = false; //TODO:disabled for the moment 
	            ((KoalaRouteMsgContent)msg.getContent()).setUpdateLatency(updateLat);
	        	send(kn.getNodeID(), msg);
            }else
            	onFail();
           
        }else{
        	onSuccess(msg);
        	
        	if(!initializeMode && learn){
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
        
        if(!initializeMode && wantsToUpdateLatency){
        	KoalaMessage newMsg = new KoalaMessage(new KoalaMsgContent(KoalaMessage.LL));
        	send(msgSender, newMsg);
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
		
		for(KoalaNeighbor recNeighbor: pathNodes){
			if(recNeighbor.getNodeID().equals(myNode.getID())) continue;
			int res  = myNode.tryAddNeighbour(recNeighbor);
			
			if( res == 2){
				KoalaMessage newMsg = new KoalaMessage(new KoalaRTMsgConent(myNode));
				send(recNeighbor.getNodeID(), newMsg);
			}
				
			if(res == -1 && !initializeMode && learn)
				myNode.getRoutingTable().addLongLink(recNeighbor);
		}
		
	}

	@Override
	protected String getProtocolName() {
		return "koala";
	}

	
	protected void checkPiggybackedBefore(KoalaMessage msg) {
		Object[] ids = myNode.getRoutingTable().getNeighboursIDs().toArray();
		String[] idsIknow = Arrays.copyOf(ids, ids.length, String[].class);
		
		for(int i = 0; i < idsIknow.length; i++){
			String ll = idsIknow[i];
			for(int j = 0; j < msg.getPiggyBack().size(); j++){
				KoalaNeighbor kn = msg.getPiggyBack().get(j);
				int dist = NodeUtilities.distance(kn.getIdealID(), ll);
				int currentDist = NodeUtilities.distance(kn.getIdealID(), kn.getNodeID());
				if(dist < currentDist){
					kn.setNodeID(ll);
				}
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
	
	protected void onFail(){
		FAIL++;
	}

}
