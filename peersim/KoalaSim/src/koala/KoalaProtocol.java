package koala;

import java.util.ArrayList;
import peersim.core.CommonState;
import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;


public class KoalaProtocol implements CDProtocol{

	
	
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
		
		RenaterNode each;
		ArrayList<RenaterNode> joined = new ArrayList<RenaterNode>();
		for (int i = 0; i < Network.size(); i++) {
            each = (RenaterNode) Network.get(i).getProtocol(linkableID);
            if(each.hasJoined())
            	joined.add(each);
            	
		}
		
		if(joined.size() == 0)
			rn.setJoined(true);
		else{
			RenaterNode bootstrap = joined.get(CommonState.r.nextInt(joined.size()));
			rn.setBootstrapID(bootstrap.getID());

			//now contact the bootstrap and do shit
			rn.setJoined(true);
		}
		
	}

}
