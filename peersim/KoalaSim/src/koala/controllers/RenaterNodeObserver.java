package koala.controllers;

import java.io.PrintStream;

import peersim.core.Node;
import koala.KoalaNode;
import koala.RenaterNode;

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
			RenaterNode current = (RenaterNode)((Node) g.getNode(i)).getProtocol(pid);
            double x_to = current.getX();
            double y_to = current.getY();
            for (int index : g.getNeighbours(i)) {
            	RenaterNode n = (RenaterNode) ((Node) g.getNode(index)).getProtocol(pid);
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