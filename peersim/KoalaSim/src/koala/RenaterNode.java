package koala;

import peersim.core.Protocol;
import example.hot.InetCoordinates;

public class RenaterNode extends InetCoordinates implements Protocol {

	private int dcID;
	private boolean gateway;
	public RenaterNode(String prefix) {
		super(prefix);
	}
	
	public Object clone() {
		RenaterNode inp = null;
        inp = (RenaterNode) super.clone();
        gateway = false;
        return inp;
    }

	public int getDCID() {
        return dcID;
    }

    public void setDCID(int dcID) {
        this.dcID = dcID;
    }

    public boolean isGateway() {
        return gateway;
    }

    public void setGateway(boolean gateway) {
        this.gateway = gateway;
    }
    
}
