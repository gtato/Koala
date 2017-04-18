package renater.controllers;

import java.io.PrintStream;

import peersim.core.Node;
import renater.RenaterEdge;
import renater.RenaterGraph;
import renater.RenaterNode;
import topology.controllers.NodeObserver;
import utilities.NodeUtilities;

public class RenaterTopologyObserver extends NodeObserver {



	public RenaterTopologyObserver(String name) {
		super(name);
//		plotScript = "gnuplot/plotRenater.plt";
		
	}

	@Override
	public boolean execute() {
		//updateGraph();
		super.g = new RenaterGraph(pid,false);
		graphToFile();
//		plotIt();
		return false;
	}

	@Override
	protected void printGraph(PrintStream ps, int psIndex) {
		if (psIndex != 0)
			return;
		for (int i = 0; i < g.size(); i++) {
			RenaterNode current = (RenaterNode)((Node) g.getNode(i)).getProtocol(pid);
            double x_to = current.getX();
            double y_to = current.getY();
            for (int index : g.getNeighbours(i)) {
            	RenaterNode n = (RenaterNode) ((Node) g.getNode(index)).getProtocol(pid);
                double x_from = n.getX();
                double y_from = n.getY();
//                String label = true ? current.getID() : ".";
                String label = current.getCID();
                ps.println(x_from + " " + y_from);
                String secondLine = x_to + " " + y_to + " " + label; 
                if(current.isGateway() && n.isGateway()){
                	double[] p1 = new double[]{x_from,  y_from};
                	double[] p2 = new double[]{x_to,  y_to};
                	double[] middle = NodeUtilities.getCoordinatesBetweenTwoPoints(p1,p2); 
                	RenaterEdge re = (RenaterEdge) ((RenaterGraph)g).getEdge(i, index);
                	secondLine += " " + middle[0] + " " + middle[1] + " " + re.getLatency();
                }
                ps.println(secondLine);
                ps.println();
            }
        }
	}

	@Override
	protected String getOutputFileBase() {
		return super.getOutputFileBase() +  "renater/";
	}

	@Override
	protected String[] getOutputFileNames() {
		return new String[]{"topology"};
	}
	
	

}