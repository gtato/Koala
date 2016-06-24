package koala.controllers;

import java.io.PrintStream;

import peersim.cdsim.CDState;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.reports.GraphObserver;
import koala.KoalaNode;
import koala.RenaterEdge;
import koala.RenaterGraph;
import koala.RenaterNode;
import koala.utility.LatencyProvider;
import koala.utility.NodeUtilities;

public class RenaterNodeObserver extends NodeObserver {



	public RenaterNodeObserver(String name) {
		super(name);
		plotScript = "gnuplot/plotRenater.plt";
		
	}

	@Override
	public boolean execute() {
		//updateGraph();
		super.g = new RenaterGraph(pid,false);
		graphToFile();
		plotIt();
//		LatencyProvider.printLatencies();
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
                String label = true ? current.getID() : ".";
                ps.println(x_from + " " + y_from);
                String secondLine = x_to + " " + y_to + " " + label; 
                if(current.isGateway() && n.isGateway()){
                	double[] p1 = new double[]{x_from,  y_from};
                	double[] p2 = new double[]{x_to,  y_to};
                	double[] middle = NodeUtilities.getCoordinatesBetweenTwoPoints(p1,p2); 
//                	double distance = NodeUtilities.getPhysicalDistance(current, n);
                	RenaterEdge re = (RenaterEdge) ((RenaterGraph)g).getEdge(i, index);
                	secondLine += " " + middle[0] + " " + middle[1] + " " + re.getLatency();
//                	secondLine += " " + middle[0] + " " + middle[1] + " " + distance;
                }
                ps.println(secondLine);
                ps.println();
            }
        }
	}
	
	
//	protected void updateGraph() {
//		
//		if( CommonState.getTime() != GraphObserver.time ||
//		    (CDState.isCD() && (CDState.getCycleT() != GraphObserver.ctime)) ||
//		    CommonState.getPhase() != GraphObserver.phase ||
//		    pid != GraphObserver.lastpid )
//		{
//			// we need to update the graphs
//			
//			GraphObserver.lastpid = pid;
//			GraphObserver.time = CommonState.getTime();
//			if( CDState.isCD() ) GraphObserver.ctime = CDState.getCycleT();
//			GraphObserver.phase = CommonState.getPhase();
//
//			GraphObserver.dirg = new OverlayGraph(pid);
//			if( GraphObserver.needUndir )
//			{
//				if( fast )
//					GraphObserver.undirg =
//					new FastUndirGraph(GraphObserver.dirg);
//				else
//					GraphObserver.undirg =
//					new ConstUndirGraph(GraphObserver.dirg);
//			}
//		}
//		
//		if( undir ) g = GraphObserver.undirg;
//		else g = GraphObserver.dirg;
//	}
//
//	}

}