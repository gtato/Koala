package koala.controllers;

import java.io.PrintStream;

import peersim.core.Node;
import koala.KoalaNode;

public class RenaterNodeObserver extends NodeObserver {



	public RenaterNodeObserver(String name) {
		super(name);
		plotScript = "gnuplot/plotRenater.plt";
	}

	@Override
	public boolean execute() {
		updateGraph();
		graphToFile();
		plotIt();
		return false;
	}

	@Override
	protected void printGraph(PrintStream ps) {
		for (int i = 0; i < g.size(); i++) {
			KoalaNode current = (KoalaNode)((Node) g.getNode(i)).getProtocol(coordPid);
            double x_to = current.getX();
            double y_to = current.getY();
            for (int index : g.getNeighbours(i)) {
            	KoalaNode n = (KoalaNode) ((Node) g.getNode(index)).getProtocol(coordPid);
                double x_from = n.getX();
                double y_from = n.getY();
                String label = true ? current.getID() : "";
                ps.println(x_from + " " + y_from);
                ps.println(x_to + " " + y_to + " " + label);
                ps.println();
            }
        }
	}
}