package koala;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;

import peersim.config.Configuration;
import peersim.reports.GraphObserver;
import peersim.util.FileNameGenerator;
import peersim.core.Node;
import peersim.graph.Graph;
import example.hot.InetObserver;
import example.hot.InetCoordinates;;

public class KoalaNodeObserver extends GraphObserver {

    private static final String PAR_COORDINATES_PROT = "coord_protocol";
    private static final String PAR_FILENAME_BASE = "file_base";

    private final int coordPid;
    private final String graph_filename;
    private final FileNameGenerator fng;
    int pointCounter;
    boolean dumpToStd = false;
	
	public KoalaNodeObserver(String prefix) {
		super(prefix);
		coordPid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PROT);
		graph_filename = Configuration.getString(prefix + "."
                + PAR_FILENAME_BASE, "graph_dump");
        if(graph_filename.equals("graph_dump"))
        	dumpToStd = true;
		fng = new FileNameGenerator(graph_filename, ".dat");
        pointCounter = 1;
	}

	

	
	@Override
	public boolean execute() {
		updateGraph();
		//simpleReport();
		
		
		KoalaNode each = (KoalaNode)((Node)g.getNode(0)).getProtocol(coordPid);
		String firstID = each.getID();
		do{
			
			double[] coords = getNextCoordinate();
			each.setX(coords[0]);
			each.setY(coords[1]);
			pointCounter++;
			each = getNodeFromID(each.getRoutingTable().getGlobalPredecessor().getNodeID());
		}while(!each.getID().equals(firstID));
		
		
		try {
            
            String fname = fng.nextCounterName();
            FileOutputStream fos = new FileOutputStream(fname);
            PrintStream pstr = dumpToStd ? System.out : new PrintStream(fos);

            graphToFile(g, pstr, coordPid);

            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

		
		
		return false;
	}




	private void simpleReport() {
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
	}
	
	private void graphToFile(Graph g, PrintStream ps, int coordPid) {
        for (int i = 0; i < g.size(); i++) {
        	KoalaNode current = (KoalaNode) ((Node)g.getNode(i)).getProtocol(coordPid);
            double x_to = current.getX();
            double y_to = current.getY();
            KoalaNeighbor[] gneigs = {current.getRoutingTable().getGlobalPredecessor(), current.getRoutingTable().getGlobalSucessor()};
            for(int j = 0; j < gneigs.length; j++){
            	KoalaNode n = getNodeFromID(gneigs[j].getNodeID());
            	double x_from = n.getX();
                double y_from = n.getY();
                String label = j==0 ? current.getID() : "";
                ps.println(x_from + " " + y_from + " " + label);
                ps.println(x_to + " " + y_to);
                ps.println();
            }
            
//            for (int index : g.getNeighbours(i)) {
//                Node n = (Node) g.getNode(index);
//                double x_from = ((KoalaNode) n.getProtocol(coordPid)).getX();
//                double y_from = ((KoalaNode) n.getProtocol(coordPid)).getY();
//                ps.println(x_from + " " + y_from);
//                ps.println(x_to + " " + y_to);
//                ps.println();
//            }
        }
    }
	
	private double[] getNextCoordinate(){
		double radius = 0.5;
		double[] ret = new double[2];
		//double[] center = {radius, radius};
		double unitangle = 2*Math.PI/g.size();
		ret[0] = Math.cos(unitangle*pointCounter) * radius + radius;
		ret[1] = Math.sin(unitangle*pointCounter) * radius + radius;
		return ret;
	}
	
	private KoalaNode getNodeFromID(String id)
	{
		for (int i = 0; i < g.size(); i++) 
		{
			KoalaNode current = (KoalaNode) ((Node)g.getNode(i)).getProtocol(coordPid);
			if(current.getID().equals(id))
				return current;
		}
		return null;
	}
}
