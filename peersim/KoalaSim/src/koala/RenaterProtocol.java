package koala;


import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;

public class RenaterProtocol implements CDProtocol {

	public RenaterProtocol(String prefix) {}
	
	public Object clone() {
		RenaterProtocol inp = null;
        try {
            inp = (RenaterProtocol) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        return inp;
    }
	
	@Override
	public void nextCycle(Node node, int protocolID) {
		KoalaNode me = (KoalaNode) (Linkable) node.getProtocol(FastConfig.getLinkable(protocolID));
//		System.out.println("yoyo, I is ");
		//TODO: implement physical routing here 

	}

}
