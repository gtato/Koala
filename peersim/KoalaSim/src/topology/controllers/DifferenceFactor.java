package topology.controllers;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.PriorityQueue;

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
		PriorityQueue<Double> q = new PriorityQueue<Double>();
//		System.out.println(CommonState.getPhase()+ " - " +CommonState.POST_SIMULATION);
		
		if(CommonState.getTime() == CommonState.getEndTime()-1 && !ended){
			graphToFile();
			ended = true;
//			System.out.println(CommonState.getTime() + " - " + CommonState.getEndTime());
		}
		
		double minCoef = Double.MAX_VALUE, maxCoef=-1, totCoef=0;
		int nr = 0;
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
//				coef += (double) lld/myd;
				coef = (double) lld/myd;
//				coef = coef/current.getRoutingTable().getLongLinks().size();
				if(coef > maxCoef && coef < NodeUtilities.NR_DC) maxCoef = coef;
				if(coef < minCoef) minCoef = coef;
				nr++;
				totCoef += coef;
				q.add(coef);
			}
		}
		
		for(int i =0; i <= q.size()/2; i++)
			q.remove();
		
		msgToPrint.add(CommonState.getTime() + " " + minCoef + " " + maxCoef+ " " + (double)totCoef/nr + " " + q.remove());
		
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
