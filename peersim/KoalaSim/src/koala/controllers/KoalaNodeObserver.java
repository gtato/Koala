package koala.controllers;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import koala.KoalaNeighbor;
import koala.KoalaNode;
import koala.utility.NodeUtilities;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;


public class KoalaNodeObserver extends NodeObserver {

    
    HashMap<Integer, double[]> cords = new HashMap<Integer, double[]>();
    ArrayList<KoalaNode> orderedGraph = new ArrayList<KoalaNode>(); 
    int logNodes;
	public KoalaNodeObserver(String prefix) {
		super(prefix);
		plotScript = "gnuplot/plotKoala.plt";
		logNodes = Configuration.getInt("logging.nodes");
	}

	

	
	@Override
	public boolean execute() {
		updateGraph();
		
		simpleReport();
		generateGraph();
		plotIt();
//		System.out.println("");
//		System.out.println("observer at " + CommonState.getTime());
		
		
		
		return false;
	}

	private void generateGraph(){
		for (int i = 0; i < g.size(); i++) {
			KoalaNode each = (KoalaNode) ((Node)g.getNode(i)).getProtocol(pid);
        	if (each.hasJoined())
				orderedGraph.add(each);
		}
		
		Collections.sort(orderedGraph, new Comparator<KoalaNode>(){
			@Override
			public int compare(KoalaNode arg0, KoalaNode arg1) {
				return NodeUtilities.compare(arg0.getID(), arg1.getID());
			}});
		
		for(KoalaNode each: orderedGraph){
	    	double[] coords = getNextCoordinate(each.getID());
			each.setX(coords[0]);
			each.setY(coords[1]);
		}
		
		graphToFile();

	}

	private void simpleReport() {
		ArrayList<String> withoutLocalNeighs = new ArrayList<String>();
		ArrayList<String> withOneLocalNeigh = new ArrayList<String>();
		ArrayList<String> withTwoLocalNeigh = new ArrayList<String>();
		ArrayList<String> unknown = new ArrayList<String>();
		for (int i = 0; i < g.size(); i++) 
		{
			boolean neighborToSomeone=false;
			KoalaNode current = (KoalaNode) ((Node)g.getNode(i)).getProtocol(pid);
			if(!current.hasJoined()) continue;
			String log = "ID: " + current.getID() + ", bootstrap: " + current.getBootstrapID();
			log += ", neighbours: ";
			
			Set<String> neigs = current.getRoutingTable().getNeighboursIDs(); 
			
			int ln=0;
			for(String n : neigs ){
				if(NodeUtilities.sameDC(current.getID(), n))
					ln++;
				log += "\t" + n;
			}
			if(ln == 0)
				withoutLocalNeighs.add(current.getID());
			else if (ln == 1)
				withOneLocalNeigh.add(current.getID());
			else if (ln == 2)
				withTwoLocalNeigh.add(current.getID());
			if (logNodes == 2)
				System.out.println(log);
			
			
			for (int j = 0; j < g.size(); j++) 
			{
				KoalaNode kn = (KoalaNode) ((Node)g.getNode(j)).getProtocol(pid);
				if (i == j || !kn.hasJoined()) continue;
				if (kn.getRoutingTable().getNeighboursIDs().contains(current.getID()))
					neighborToSomeone = true;
			}
			
			if(!neighborToSomeone)
				unknown.add(current.getID());
			
		}
		if(logNodes >= 1)
			System.out.println("Without local neighbors: ("+ withoutLocalNeighs.size()+ ") "  +withoutLocalNeighs +
				           "\nWith one local neighbor: ("+ withOneLocalNeigh.size()+ ") " +withOneLocalNeigh +
				           "\nWith two local neighbors: ("+ withTwoLocalNeigh.size()+ ") " +withTwoLocalNeigh +
				           "\nUnknown: (" +unknown.size() +  ") "+unknown           
				);
	}
	
	private double[] getNextCoordinate(String id){
		int dc_id = koala.utility.NodeUtilities.getDCID(id);
		double radius = 0.5;
		double[] center = {radius, radius};
		double unitangle = 2*Math.PI/getNrOnlineDC();
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


	public int getNrOnlineDC(){
		Set<Integer> onlineDCs = new HashSet<Integer>();
		for (int i = 0; i < g.size(); i++) 
		{
			KoalaNode current = (KoalaNode) ((Node)g.getNode(i)).getProtocol(pid);
			if(current.hasJoined())
				onlineDCs.add(current.getDCID());
		}
		return onlineDCs.size();
			
	}

	@Override
	protected void printGraph(PrintStream ps) {
		for (int i = 0; i < g.size(); i++) {
        	KoalaNode current = (KoalaNode) ((Node)g.getNode(i)).getProtocol(pid);
            double x_to = current.getX();
            double y_to = current.getY();
                        
            KoalaNeighbor[] gneigs = {current.getRoutingTable().getGlobalPredecessor(), current.getRoutingTable().getGlobalSucessor()};
            for(int j = 0; j < gneigs.length; j++){
            	if(NodeUtilities.isDefault(gneigs[j]))
            		continue;
            	KoalaNode n = getNodeFromID(gneigs[j].getNodeID());
            	if(n == null) return;
            	double x_from =  n.getX();
                double y_from =   n.getY();
                String label = j==0 ? current.getID() : "";
                ps.println(x_from + " " + y_from);
                ps.println(x_to + " " + y_to + " " + label);
                ps.println();
            }
        }
		
	}
	
	
	
}
