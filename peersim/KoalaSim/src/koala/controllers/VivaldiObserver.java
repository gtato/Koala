package koala.controllers;

import java.io.PrintStream;

import koala.KoalaNode;
import peersim.core.Node;
import renater.RenaterEdge;
import renater.RenaterGraph;
import renater.RenaterNode;
import topology.controllers.NodeObserver;
import utilities.NodeUtilities;

public class VivaldiObserver extends NodeObserver{

	public VivaldiObserver(String name) {
		super(name);
	}

	
	@Override
	public boolean execute() {
		super.g = new RenaterGraph(pid,false);
		graphToFile();
		return false;
	}

	@Override
	protected void printGraph(PrintStream ps, int psIndex) {
		if (psIndex != 0)
			return;
		for (int i = 0; i < g.size(); i++) {
			KoalaNode current = (KoalaNode) ((Node)g.getNode(i)).getProtocol(NodeUtilities.KID);
            for(int j = 0; j < NodeUtilities.VIV_DIMENSIONS; j++)
            	ps.print(current.vivaldiCoordinates.get(j) + " ");
            ps.print(current.getSID());
            ps.println();
		}
		
	}

	
	@Override
	protected String getOutputFileBase() {
		return super.getOutputFileBase() +  "vivaldi/";
	}

	@Override
	protected String[] getOutputFileNames() {
		return new String[]{"vivaldi"};
	}
	
}
