package koala;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import koala.utility.KoalaJsonParser;
import koala.utility.KoalaNodeUtilities;
import peersim.core.CommonState;
import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;


public class KoalaProtocol implements CDProtocol{

	KoalaNode me = null;
	ArrayDeque<KoalaMessage> queue;
	int koalaNodePid = -1;
	int koalaPid = -1;
	
	public KoalaProtocol(String prefix) {
	}
	
	public Object clone() {
		KoalaProtocol inp = null;
        try {
            inp = (KoalaProtocol) super.clone();
            inp.queue = new ArrayDeque<KoalaMessage>();
        } catch (CloneNotSupportedException e) {
        } // never happens
        return inp;
    }
	
	@Override
	public void nextCycle(Node node, int protocolID) {
		koalaPid = protocolID;
		koalaNodePid = FastConfig.getLinkable(protocolID);
		me = (KoalaNode) (Linkable) node.getProtocol(koalaNodePid);
		System.out.println("yoyo, I is " + me.getID() );
		
		//receive();
		if(!me.hasJoined())
			join(node);
				
	}
	

	private void join(Node self)
	{
		Node bootstrap = getBootstrap();
		
		if (bootstrap == null)
			me.setJoined(true);
		else{
			KoalaNode bootstrapRn = (KoalaNode)bootstrap.getProtocol(koalaNodePid);
			String bootstrapID = bootstrapRn.getID();
			me.setBootstrapID( bootstrapID );
			KoalaNeighbor first = new KoalaNeighbor(bootstrapID);
			me.tryAddNeighbour(first);
			
			me.setJoined(true);
			KoalaMessage km = new KoalaMessage(me.getID(), KoalaMessage.RT, KoalaJsonParser.toJson(me));
			send(bootstrapID, km);
		}
	}
	
	private Node getBootstrap()
	{
		KoalaNode each;
		ArrayList<Node> joined = new ArrayList<Node>();
		for (int i = 0; i < Network.size(); i++) {
            each = (KoalaNode) Network.get(i).getProtocol(koalaNodePid);
            if(each.hasJoined())
            	joined.add(Network.get(i));   	
		}
		if(joined.size() == 0)
			return null;
		
		return joined.get(CommonState.r.nextInt(joined.size()));
	}
	
	
	public void send(String destinationID, KoalaMessage msg)
	{
		Node each = null;
		for (int i = 0; i < Network.size(); i++) {
            each =  Network.get(i);
            if(((KoalaNode)each.getProtocol(koalaNodePid)).getID().equals(destinationID))
            	break;
		}
		if(each != null){
			msg.setRandomLatency(me.getID(), destinationID);
			msg.addToPath(destinationID);
			((KoalaProtocol)each.getProtocol(koalaPid)).registerMsg(msg);
			/*TODO: uncomment this later*/
			((KoalaProtocol)each.getProtocol(koalaPid)).receive();
		}
	}
	
	
	public void registerMsg(KoalaMessage msg){
		queue.add(msg);
	}

	
	public void receive()
	{
		if(queue.size() == 0)
			return;
		
		KoalaMessage msg = queue.remove();
		switch(msg.getMsgType()){
			case KoalaMessage.RT:
				updateRoutingTable(msg);
				break;
			case KoalaMessage.ROUTE:
				onRoute(msg);
				break;
			case KoalaMessage.NGN:
				onNewGlobalNeighbours(msg);
				break;
		}
	}

	private void onNewGlobalNeighbours(KoalaMessage msg) {
		GlobalNeighborMsgContent content = KoalaJsonParser.jsonToObject(msg.getMsgContent(), GlobalNeighborMsgContent.class);
		
        ArrayList<String> respees = new ArrayList<String>();
        for(String  c : content.getCandidates()){
            if(me.isResponsible(c))
                if(respees.size() == 0)
                {
                	KoalaMessage km = new KoalaMessage(me.getID(), KoalaMessage.RT, KoalaJsonParser.toJson(me), true);
                	send(content.getNeighbor().getNodeID(), km);
                }
 
                respees.add(c);
        }
        content.getNeighbor().setLatencyQuality(2);
        int rnes = me.tryAddNeighbour(content.getNeighbor());
        if(rnes != 2)
            return;
        
        List<String> cands = Arrays.asList(content.getCandidates());
        cands.removeAll(respees);
        
        Set<String> add_cands = me.createRandomIDs(respees.size() - 1);
        cands.addAll(add_cands);
        Set<String> new_cands = new HashSet<String>(cands);
        content.setCandidates(new_cands.toArray(new String[new_cands.size()]));
        msg.setMsgContent(KoalaJsonParser.toJson(content));
        
        String target = me.getRoutingTable().getLocalPredecessor().getNodeID();
        if (msg.getSource().equals(target))
            target = me.getRoutingTable().getLocalSucessor().getNodeID();

        send(target, msg);
	}

	private void updateRoutingTable(KoalaMessage msg) {
		KoalaNode sender = KoalaJsonParser.jsonToObject(msg.getMsgContent(), KoalaNode.class);
		System.out.println("content: " + sender.getID());
		ArrayList<KoalaNeighbor> senderOldNeighbors = sender.getRoutingTable().getOldNeighbors();
		ArrayList<KoalaNeighbor> newNeighbors = new ArrayList<KoalaNeighbor>();
		ArrayList<KoalaNeighbor> receivedNeighbors = sender.getRoutingTable().getNeighborsContainer();
		receivedNeighbors.addAll(senderOldNeighbors);
		
		receivedNeighbors.add(new KoalaNeighbor(sender.getID()));
		
		
		
		Set<String> neighborsBefore = me.getRoutingTable().getNeighboursIDs();
		Set<Integer> dcsBefore = new HashSet<Integer>(); 
		for(String neighID : neighborsBefore)
			dcsBefore.add(KoalaNodeUtilities.getDCID(neighID));
		dcsBefore.add(me.getDCID());
		
		boolean sourceJoining = sender.getJoining();
		boolean selfJoining = me.isJoining();
		ArrayList<KoalaNeighbor> myOldNeighbors = new ArrayList<KoalaNeighbor>();
		for(KoalaNeighbor recNeighbor: receivedNeighbors){
			boolean isSource = recNeighbor.getNodeID().equals(sender.getID());
			if(recNeighbor.getNodeID().equals(me.getID()))
				continue;
			if(selfJoining && me.isLocal(sender.getID()))
				dcsBefore.add(KoalaNodeUtilities.getDCID(recNeighbor.getNodeID()));

			int l = isSource ? msg.getLatency() : recNeighbor.getLatency();
			int lq = me.getLatencyQuality(isSource, sender.getID(), recNeighbor);
			
            int res  = me.tryAddNeighbour(new KoalaNeighbor(recNeighbor.getNodeID(), l, lq));
			ArrayList<KoalaNeighbor> oldies = me.getRoutingTable().getOldNeighbors();
			myOldNeighbors.addAll(oldies);

			me.updateLatencyPerDC(recNeighbor.getNodeID(), l, lq);
			
			if( res == 2 || (res == 1 && isSource && sourceJoining))
				newNeighbors.add(new KoalaNeighbor(recNeighbor.getNodeID(), l));
			else if (res < 0 && recNeighbor.getNodeID().equals(sender.getID())){				
				String dest = me.getRoute(sender.getID());
				msg.setConfidential(false);
				send(dest, msg);
			}


		}
		me.updateLatencies();

		Set<String> neighborsAfter = me.getRoutingTable().getNeighboursIDs();
		for(KoalaNeighbor newNeig : newNeighbors){
			if(neighborsAfter.contains(newNeig.getNodeID()) && !neighborsBefore.contains(newNeig.getNodeID()) || newNeig.getNodeID().equals(sender.getID()))
			{
				KoalaMessage newMsg = new KoalaMessage(me.getID(), KoalaMessage.RT, KoalaJsonParser.toJson(me));
				if(me.isLocal(newNeig.getNodeID())){
					send(newNeig.getNodeID(), newMsg);
				}else
				{
					boolean newDC = !dcsBefore.contains(KoalaNodeUtilities.getDCID(newNeig.getNodeID()));
					if(newDC && !selfJoining)
						broadcastGlobalNeighbor(newNeig);
					if(!me.isLocal(sender.getID())  && (!msg.isConfidential() || newDC) )
					{
						newMsg.setConfidential(true); 
						send(newNeig.getNodeID(), newMsg);
					}
				}
			}
		}
		
	}

	private void broadcastGlobalNeighbor(KoalaNeighbor newNeig) {
        Set<String> candidates = me.createRandomIDs(KoalaNodeUtilities.MAGIC);
        KoalaNeighbor[] localNeigs = {me.getRoutingTable().getLocalSucessor(), me.getRoutingTable().getLocalPredecessor()};
        String msgContent = KoalaJsonParser.toJson(new GlobalNeighborMsgContent(candidates.toArray(new String[candidates.size()]), newNeig));
        KoalaMessage newMsg = new KoalaMessage(me.getID(), KoalaMessage.NGN, msgContent);        
        for( KoalaNeighbor n : localNeigs)
            if(!KoalaNodeUtilities.isDefault(n))
                send(n.getNodeID(), newMsg);
		
	}
	
	
	private void onRoute(KoalaMessage msg){
        String nid = msg.getMsgContent();
        me.updateLatencyPerDC(msg.getSource(), msg.getLatency(), 3);
        me.updateLatencies();
        if(nid != me.getID())
            send(me.getRoute(nid), msg);
        
	}
}
