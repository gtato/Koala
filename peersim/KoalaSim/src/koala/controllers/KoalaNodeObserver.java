package koala.controllers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import koala.KoalaNeighbor;
import koala.KoalaNode;
import koala.utility.KoalaNodeUtilities;

import peersim.config.Configuration;
import peersim.reports.GraphObserver;
import peersim.util.FileNameGenerator;
import peersim.core.Node;
import peersim.graph.Graph;


public class KoalaNodeObserver extends GraphObserver {

    private static final String PAR_COORDINATES_PROT = "coord_protocol";
    private static final String PAR_FILENAME_BASE = "file_base";

    private final int coordPid;
    private final String graph_filename;
    private final FileNameGenerator fng;
    
    boolean dumpToStd = false;
    HashMap<Integer, double[]> cords = new HashMap<Integer, double[]>();
    ArrayList<KoalaNode> orderedGraph = new ArrayList<KoalaNode>(); 
    
	public KoalaNodeObserver(String prefix) {
		super(prefix);
		coordPid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PROT);
		graph_filename = Configuration.getString(prefix + "."
                + PAR_FILENAME_BASE, "graph_dump");
        if(graph_filename.equals("graph_dump"))
        	dumpToStd = true;
		fng = new FileNameGenerator(graph_filename, ".dat");
	}

	

	
	@Override
	public boolean execute() {
		updateGraph();
		//simpleReport();
		generateGraph();
		
		
		return false;
	}

	private void generateGraph(){
		for (int i = 0; i < g.size(); i++) {
			KoalaNode each = (KoalaNode) ((Node)g.getNode(i)).getProtocol(coordPid);
        	orderedGraph.add(each);
		}
		
		
		Collections.sort(orderedGraph, new Comparator<KoalaNode>(){

			@Override
			public int compare(KoalaNode arg0, KoalaNode arg1) {
				return KoalaNodeUtilities.compare(arg0.getID(), arg1.getID());
			}});
		
		for(KoalaNode each: orderedGraph){
	    	double[] coords = getNextCoordinate(each.getID());
			each.setX(coords[0]);
			each.setY(coords[1]);
		}
		

		try {
            
            String fname = fng.nextCounterName();
            FileOutputStream fos = new FileOutputStream(fname);
            PrintStream pstr = dumpToStd ? System.out : new PrintStream(fos);

            graphToFile(g, pstr, coordPid);

            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
                ps.println(x_from + " " + y_from);
                ps.println(x_to + " " + y_to + " " + label);
                ps.println();
            }
          
        }
    }
	
	private double[] getNextCoordinate(String id){
		int dc_id = koala.utility.KoalaNodeUtilities.getDCID(id);
		double radius = 0.5;
		double[] center = {radius, radius};
		double unitangle = 2*Math.PI/Configuration.getInt("NR_DC");
		double[] entry = new double[2];
		double angle = 0;
		if(cords.containsKey(dc_id)){
			double[] angleNcount = cords.get(dc_id);
			angle = angleNcount[0];
			radius = radius - angleNcount[1]*0.01; 
			angleNcount[1]++;
			cords.put(dc_id, angleNcount);
		}else{
			angle = unitangle*cords.size();
			cords.put(dc_id, new double[]{angle,1});
		}
		
		entry[0] = Math.cos(angle) * radius + center[0];
		entry[1] = Math.sin(angle) * radius + center[1];
		  
		return entry;
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
