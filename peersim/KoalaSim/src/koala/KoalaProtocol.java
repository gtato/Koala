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
		int linkableID = FastConfig.getLinkable(protocolID);
        Linkable linkable = (Linkable) self.getProtocol(linkableID);
        KoalaNode selfRn = (KoalaNode) linkable;
		Node bootstrap = getBootstrap(linkableID);
		if (bootstrap == null)
			selfRn.setJoined(true);
		else{
			String bootstrapID = ((KoalaNode)bootstrap.getProtocol(linkableID)).getID();
			selfRn.setBootstrapID( bootstrapID );
			KoalaNeighbor first = new KoalaNeighbor(bootstrapID);
			selfRn.getRoutingTable().tryAddNeighbour(first);
			
			selfRn.setJoined(true);
			KoalaMessage km = new KoalaMessage(KoalaMessage.JOIN, selfRn.toJson());
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

}
