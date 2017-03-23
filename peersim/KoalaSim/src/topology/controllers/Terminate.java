package topology.controllers;

import koala.KoalaNeighbor;
import koala.KoalaNode;
import peersim.core.Control;
import peersim.core.Network;
import utilities.KoaLite;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;

public class Terminate implements Control{

	public Terminate(String prefix){
		
	}
	
	@Override
	public boolean execute() {
		KoaLite.close();
		
		PhysicalDataProvider.SimTime = System.currentTimeMillis() - PhysicalDataProvider.SimTime;
		System.out.println("Simulation lasted " + PhysicalDataProvider.SimTime + " ms");
		
		for(int i =0; i < Network.size(); i++){
			KoalaNode kn = (KoalaNode)Network.get(i).getProtocol(NodeUtilities.KID);
			System.out.print(i + " " + kn.getID() + ": ");
//			for(KoalaNeighbor kng : kn.getRoutingTable().getLocals())
//				System.out.print(kng.getNodeID() + " ");
			
			System.out.print(kn.getRoutingTable().getGlobalPredecessor(0).getNodeID() + " " + kn.getRoutingTable().getGlobalSucessor(0).getNodeID());
			System.out.println();
		}
		return false;
	}

}
