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
		RenaterNode rn = (RenaterNode) linkable;
		System.out.println("yoyo, I is " + rn.getID() );
		join(node, protocolID);
				
	}
	

	private void join(Node self, int protocolID)
	{
		int linkableID = FastConfig.getLinkable(protocolID);
        Linkable linkable = (Linkable) self.getProtocol(linkableID);
        RenaterNode selfRn = (RenaterNode) linkable;
		Node bootstrap = getBootstrap(linkableID);
		if (bootstrap == null)
			selfRn.setJoined(true);
		else{
			selfRn.setBootstrapID( ((RenaterNode)bootstrap.getProtocol(linkableID)).getID());
			if (selfRn.addNeighbor(bootstrap))
				System.out.println("added");
			else 
				System.out.println("not added");
			selfRn.setJoined(true);
			KoalaMessage km = new KoalaMessage(KoalaMessage.JOIN, selfRn.toJson());
			((KoalaProtocol)(bootstrap).getProtocol(protocolID)).send(km);
		}
			
	}
	
	private Node getBootstrap(int linkableID)
	{
		RenaterNode each;
		ArrayList<Node> joined = new ArrayList<Node>();
		for (int i = 0; i < Network.size(); i++) {
            each = (RenaterNode) Network.get(i).getProtocol(linkableID);
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
