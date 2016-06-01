package koala;

import java.util.ArrayDeque;
import java.util.ArrayList;

import peersim.core.CommonState;
import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;


public class KoalaProtocol implements CDProtocol{

	ArrayDeque<KoalaMessage> queue = new ArrayDeque<KoalaMessage>();
	
	public KoalaProtocol(String prefix) {
	}
	
	public Object clone() {
		KoalaProtocol inp = null;
        try {
            inp = (KoalaProtocol) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        return inp;
    }
	
	@Override
	public void nextCycle(Node node, int protocolID) {
		int linkableID = FastConfig.getLinkable(protocolID);
        Linkable linkable = (Linkable) node.getProtocol(linkableID);
		KoalaNode rn = (KoalaNode) linkable;
		System.out.println("yoyo, I is " + rn.getID() );
		join(node, protocolID);
				
	}
	

	private void join(Node self, int protocolID)
	{
		receive();
		
		int linkableID = FastConfig.getLinkable(protocolID);
        KoalaNode selfRn = (KoalaNode) (Linkable) self.getProtocol(linkableID);
		Node bootstrap = getBootstrap(linkableID);
		
		if (bootstrap == null)
			selfRn.setJoined(true);
		else{
			KoalaNode bootstrapRn = (KoalaNode)bootstrap.getProtocol(linkableID);
			String bootstrapID = bootstrapRn.getID();
			selfRn.setBootstrapID( bootstrapID );
			KoalaNeighbor first = new KoalaNeighbor(bootstrapID);
			selfRn.getRoutingTable().tryAddNeighbour(first);
			
			selfRn.setJoined(true);
			KoalaMessage km = new KoalaMessage(KoalaMessage.JOIN, Util.nodeToJson(selfRn));
			((KoalaProtocol)(bootstrap).getProtocol(protocolID)).send(km);
		}
			
	}
	
	private Node getBootstrap(int linkableID)
	{
		KoalaNode each;
		ArrayList<Node> joined = new ArrayList<Node>();
		for (int i = 0; i < Network.size(); i++) {
            each = (KoalaNode) Network.get(i).getProtocol(linkableID);
            if(each.hasJoined())
            	joined.add(Network.get(i));   	
		}
		if(joined.size() == 0)
			return null;
		
		return joined.get(CommonState.r.nextInt(joined.size()));
	}
	
	
	public void send(KoalaMessage msg)
	{
		queue.add(msg);
	}

	public void receive()
	{
		if(queue.size() == 0)
			return;
		
		KoalaMessage msg = queue.remove();
		switch(msg.getMsgType()){
			case KoalaMessage.JOIN:
				updateRoutingTable(msg);
				break;
		
		}
		
		
	}

	private void updateRoutingTable(KoalaMessage msg) {
		KoalaNode sender = Util.jsonToNode(msg.getMsgContent());
		System.out.println("content: " + sender.getID());
		
        
//        source_old_neig= msg.content.get('old_neighbors')
//
//        # then update my rt with the nodes i received
//        new_neighbours = []
//        rec_neighbours = rt.get_all_neighbours()
//        if source_old_neig:
//            rec_neighbours.extend(source_old_neig)
//        rec_neighbours.append(source)  # maybe source could be a potential neighbour
//
//        neigh_before = self.rt.get_neighbours_ids()
//        dc_before = [Node.dc_id(nb) for nb in neigh_before]
//        dc_before.append(self.dc_id)
//
//        source_joining = source.is_joining()
//        self_joining = self.is_joining()
//
//        old_neighbors = []
//        for rec_neighbour in rec_neighbours:
//            is_source = rec_neighbour.id == source.id
//            if rec_neighbour.id != self.id:
//
//                if self_joining and self.is_local(source.id):
//                    dc_before.append(Node.dc_id(rec_neighbour.id))
//
//                l = msg.latency if is_source else rec_neighbour.latency
//                lq = self.get_lq(is_source, source.id, rec_neighbour)
//                res, oldies = self.try_set_neighbour(rec_neighbour.id, l, lq)
//                old_neighbors.extend(oldies)
//                self.update_latency_x_dc(rec_neighbour.id, l, lq)
//
//                if res == 2 or (res == 1 and is_source and source_joining):
//                    new_neighbours.append(NeighborEntry(rec_neighbour.id, l))
//
//                elif res < 0 and rec_neighbour.id == source.id:
//                    dest = self.route_to(source.id, msg)
//                    msg.referrer = self
//                    msg.content['c'] = True
//                    source.send(dest, msg)
//
//        dc_before = list(set(dc_before))
//        self.update_latencies()
//
//        #  some neighbours might have been overwritten, we send only to the neighbors .
//        neigh_after = self.rt.get_neighbours_ids()
//        for new_n in new_neighbours:
//            if new_n.id in neigh_after and new_n.id not in neigh_before or new_n.id == source.id:
//                if self.is_local(new_n.id):
//                    self.send(new_n.id, Message('rt', {'c': True, 'rt': self.rt, 'old_neighbors': old_neighbors}))
//                else:
//                    new_dc = Node.dc_id(new_n.id) not in dc_before
//                    if new_dc and not self_joining:
//                        self.broadcast_global_neighbor(new_n)
//                    if not self.is_local(source.id) and (chain or new_dc):
//                        self.send(new_n.id, Message('rt', {'c': False, 'rt': self.rt, 'old_neighbors':old_neighbors}))
		
	}
	
}
