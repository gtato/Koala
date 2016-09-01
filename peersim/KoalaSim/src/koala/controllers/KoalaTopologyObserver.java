package koala.controllers;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.ListSelectionEvent;

import koala.KoalaNode;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import topology.controllers.NodeObserver;
import utilities.NodeUtilities;


public class KoalaTopologyObserver extends NodeObserver {

    
    HashMap<Integer, double[]> cords = new HashMap<Integer, double[]>();
    ArrayList<KoalaNode> orderedGraph = new ArrayList<KoalaNode>();
    
    
	public KoalaTopologyObserver(String prefix) {
		super(prefix);		
	}

	@Override
	public boolean execute() {
		updateGraph();
		generateGraph();
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
	
	private double[] getNextCoordinate(String id){
		int dc_id = utilities.NodeUtilities.getDCID(id);
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
	protected void printGraph(PrintStream ps, int psIndex) {
		if (psIndex != 0)
			return;
		boolean first;
		for (int i = 0; i < g.size(); i++) {
        	KoalaNode current = (KoalaNode) ((Node)g.getNode(i)).getProtocol(pid);
            double x_to = current.getX();
            double y_to = current.getY();
            
            Set<String> gneigs = current.getRoutingTable().getNeighboursIDs(3);
            first = true;
            for(String gnID : gneigs){
            	if(NodeUtilities.isDefault(gnID)) continue;
            	KoalaNode n = getNodeFromID(gnID);
            	if(n == null) return;
            	double x_from =  n.getX();
                double y_from =   n.getY();
                String label = first ? current.getID() : "";
                ps.println(x_from + " " + y_from);
                ps.println(x_to + " " + y_to + " " + label);
                ps.println();
                first = false;
            }
            
        }
		
	}
	
	
	@Override
	protected String getOutputFileBase() {
		return super.getOutputFileBase() +  "koala/";
	}

	@Override
	protected String[] getOutputFileNames() {
		return new String[]{"topology"};
	}
	
		
	
}
