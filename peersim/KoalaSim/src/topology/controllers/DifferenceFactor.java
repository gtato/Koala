package topology.controllers;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
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
//		PriorityQueue<Double> q = new PriorityQueue<Double>();
//		System.out.println(CommonState.getPhase()+ " - " +CommonState.POST_SIMULATION);
		
		if(CommonState.getTime() == CommonState.getEndTime()-1 && !ended){
			graphToFile();
			ended = true;
//			System.out.println(CommonState.getTime() + " - " + CommonState.getEndTime());
		}
		int dist[] = new int[1000];
		for(int j = 0; j < dist.length; j++) dist[j]=0;
		
		double minCoef = Double.MAX_VALUE, maxCoef=-1, totCoef=0, nodeTotCoef=0;
		
		int nr = 0;
		double gran = 0.001;
		ArrayList<Double> nodeDst = new ArrayList<Double>();
		ArrayList<Double> llDst = new ArrayList<Double>();
		for (int i = 0; i < g.size(); i++) 
		{	
			KoalaNode current = (KoalaNode) ((Node)g.getNode(i)).getProtocol(pid);
			double coef=0;nodeTotCoef=0;
			for(KoalaNeighbor ll: current.getRoutingTable().getLongLinks()){
				
				String nnid =  NodeUtilities.isDefault(ll.getSID())? ll.getSID() : NodeUtilities.getStrDCID(ll.getSID()) + "-0";
				String iid = NodeUtilities.isDefault(ll.getIdealID())? ll.getIdealID(): NodeUtilities.getStrDCID(ll.getIdealID())+ "-0";
				String nid = NodeUtilities.isDefault(current.getSID())? current.getSID(): NodeUtilities.getStrDCID(current.getSID())+ "-0";
				int lld = NodeUtilities.distance(nnid, iid);
				int myd = NodeUtilities.distance(nid, iid);
//				coef += (double) lld/myd;
				coef = (double) lld/myd;
//				coef = coef/current.getRoutingTable().getLongLinks().size();
				if(coef > maxCoef && coef < NodeUtilities.NR_DC) maxCoef = coef;
				if(coef < minCoef) minCoef = coef;
				nr++;
				totCoef += coef;
//				if(maxCoef != -1)
//				q.add(coef);
				
				nodeTotCoef += coef;
				for(int j = 0; j < dist.length; j++){
					if(j*gran <= coef && coef < (j+1)*gran)
						dist[j]++;
				}
				
				llDst.add(coef);
			}
			nodeDst.add(nodeTotCoef/current.getRoutingTable().getLongLinks().size());
//			msgToPrint.add((nodeTotCoef/current.getRoutingTable().getLongLinks().size())+"");
			
		}
		
//		Collections.sort(nodeDst);
		Collections.sort(llDst);
//		for(int i =0; i < nodeDst.size(); i++)
//			msgToPrint.add(nodeDst.get(i)+"");
		
		for(int i =0; i < llDst.size(); i++)
			msgToPrint.add(llDst.get(i)+"");
		
//		for(int i =0; i < q.size()/2; i++)
//			q.remove();
		
//		if(nr>0)
////			msgToPrint.add(CommonState.getTime() + " " + minCoef + " " + maxCoef+ " " + (double)totCoef/nr + " " + q.remove());
//			msgToPrint.add(CommonState.getTime() + " " + minCoef + " " + maxCoef+ " " + (double)totCoef/nr );
//		for(int j = 0; j < dist.length; j++)
//			msgToPrint.add((j*gran) + " " + dist[j]);
		
		
		
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
