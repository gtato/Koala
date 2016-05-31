package koala;

import java.util.Set;

import peersim.config.Configuration;
import peersim.reports.GraphObserver;
import peersim.core.Node;
import example.hot.InetObserver;

public class KoalaNodeObserver extends GraphObserver {

    private static final String PAR_COORDINATES_PROT = "coord_protocol";
    private final int coordPid;

	
	public KoalaNodeObserver(String prefix) {
		super(prefix);
		coordPid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PROT);
	}

	

	
	@Override
	public boolean execute() {
		updateGraph();
		for (int i = 0; i < g.size(); i++) 
		{
			KoalaNode current = (KoalaNode) ((Node)g.getNode(i)).getProtocol(coordPid);
			System.out.println("ID: " + current.getID() + ", bootstrap: " + current.getBootstrapID());
			System.out.println("my neighbours are: ");
			Set<String> neigs = current.getRoutingTable().getNeighboursIDs(); 
			
			for(String n : neigs ){
				System.out.println("\t" + n);
			}
		}	
		return false;
	}

}
