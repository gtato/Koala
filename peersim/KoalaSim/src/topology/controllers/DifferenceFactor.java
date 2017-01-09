package topology.controllers;

import java.io.PrintStream;
import java.util.ArrayList;


import koala.KoalaNeighbor;
import koala.KoalaNode;
import peersim.core.CommonState;
import peersim.core.Node;
import utilities.NodeUtilities;

public class DifferenceFactor extends NodeObserver{

	ArrayList<String> msgToPrint = new ArrayList<String>();
	private boolean ended = false;
	
	
	public  DifferenceFactor(String prefix) {
		super(prefix);
		
	}

	@Override
	public boolean execute() {
		updateGraph();
		if(CommonState.getTime() == CommonState.getEndTime()-1 && !ended){
			graphToFile();
			ended = true;
		}
		
		double minCoef = Double.MAX_VALUE, maxCoef=-1;
		for (int i = 0; i < g.size(); i++) 
		{	
			KoalaNode current = (KoalaNode) ((Node)g.getNode(i)).getProtocol(pid);
			double coef=0;
			for(KoalaNeighbor ll: current.getRoutingTable().getLongLinks()){
				
				String nnid =  NodeUtilities.isDefault(ll.getNodeID())? ll.getNodeID() : NodeUtilities.getStrDCID(ll.getNodeID()) + "-0";
				String iid = NodeUtilities.isDefault(ll.getIdealID())? ll.getIdealID(): NodeUtilities.getStrDCID(ll.getIdealID())+ "-0";
				String nid = NodeUtilities.isDefault(current.getID())? current.getID(): NodeUtilities.getStrDCID(current.getID())+ "-0";
				int lld = NodeUtilities.distance(nnid, iid);
				int myd = NodeUtilities.distance(nid, iid);
				coef += (double) lld/myd;
			}
			
			coef = coef/current.getRoutingTable().getLongLinks().size();
			if(coef > maxCoef && coef < NodeUtilities.NR_DC) maxCoef = coef;
			if(coef < minCoef) minCoef = coef;
			
			
		}
		msgToPrint.add(CommonState.getTime() + " " + minCoef + " " + maxCoef);
		
		return false;
	}

	@Override
	protected void printGraph(PrintStream ps, int psIndex) {
		if(psIndex == 0) 
			for(String line : msgToPrint)
				ps.println(line);
		
	}
	
	@Override
	protected String[] getOutputFileNames() {
		return new String[]{"diff"};
	}

}
