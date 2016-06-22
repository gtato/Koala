package koala;


import peersim.core.Network;
import peersim.core.OverlayGraph;

public class RenaterGraph extends OverlayGraph {

	public RenaterGraph(int protocolID, boolean wireDirected) {
		super(protocolID, wireDirected);
	}
	
	public boolean setEdge( int i, int j, RenaterEdge edge ) {
		boolean ret = super.setEdge(i, j);
		((RenaterNode)Network.get(i).getProtocol(protocolID)).addEdge(j, edge);
		((RenaterNode)Network.get(j).getProtocol(protocolID)).addEdge(i, edge);
		return ret;
	}

	public Object getEdge(int i, int j) { 
		RenaterNode rn = (RenaterNode)Network.get(i).getProtocol(protocolID);
		return rn.getEdge(j);
	}
}
