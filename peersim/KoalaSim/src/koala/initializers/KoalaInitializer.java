package koala.initializers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import chord.ChordProtocol;
import koala.FlatKoalaProtocol;
import koala.KoalaNeighbor;
import koala.KoalaNode;
import koala.KoalaProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;
import renater.RenaterProtocol;
import topology.TopologyProtocol;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;


public class KoalaInitializer implements Control, NodeInitializer {
		
	private static final String PAR_KOALA_NR= "nr";
	private static final String PAR_KOALA_INIT= "initialize";

	private final int nr;
	private final boolean initialize;
	private final boolean addFlat;
//	private final int nr_longlinks;
	
	public KoalaInitializer(String prefix) {
		nr = Configuration.getInt(prefix + "." + PAR_KOALA_NR, Network.size());
		initialize = Configuration.getBoolean(prefix + "." + PAR_KOALA_INIT, false);
//		nr_longlinks = Configuration.getInt(prefix + "." + PAR_KOALA_NLL, 0);
		addFlat = Configuration.getBoolean(prefix + ".addflat", false);
	}
	
	@Override
	public boolean execute() {
		System.out.println("Building the koala rings. Depending on the size this might take also some time");
		TopologyProtocol.setInitializeMode(true);
		
		for(int i = 0; i < Network.size(); i++){
			Node n = Network.get(i);
			RenaterProtocol rp = (RenaterProtocol )n.getProtocol(NodeUtilities.RPID);
			rp.intializeMyNode(n, NodeUtilities.RPID);
			rp.join();
			KoalaProtocol kp = (KoalaProtocol )n.getProtocol(NodeUtilities.KPID);
			kp.intializeMyNode(n, NodeUtilities.KPID);
			if(NodeUtilities.FKPID > 0){
				FlatKoalaProtocol fkp = (FlatKoalaProtocol )n.getProtocol(NodeUtilities.FKPID);
				fkp.intializeMyNode(n, NodeUtilities.FKPID);
			}
			
			NodeUtilities.down(Network.get(i));
			
		}
		
		ArrayList<Integer> inx = new ArrayList<Integer>();
		for(int i = 0; i < Network.size(); i++)
			inx.add(i);
		
		Collections.shuffle(inx, CommonState.r);
		
		
		
		for (int i = 0; i < nr; i++) {
			Node n = Network.get(inx.get(i));
			NodeUtilities.up(n);
		}
		
		NodeUtilities.copyAltDowns();
		
		
		if(NodeUtilities.KPID > 0)
			initialize(inx, NodeUtilities.KPID);
		if(NodeUtilities.FKPID > 0)
			initialize(inx, NodeUtilities.FKPID);
		
//		printing neighbors assignment
//		for (int i = 0; i < nr; i++) {
//			System.out.println("");
//			Node n = Network.get(inx.get(i));
//			KoalaNode kn = (KoalaNode )n.getProtocol(NodeUtilities.KID);
//			System.out.println(kn.getID());
//			System.out.print("succs: ");
//			for(KoalaNeighbor neig : kn.getRoutingTable().getGlobalSucessors())
//				System.out.print(neig.getNodeID() +  " ");
//			System.out.print("\npred: ");
//			for(KoalaNeighbor neig : kn.getRoutingTable().getGlobalPredecessors())
//				System.out.print(neig.getNodeID() +  " ");
//		}
		
//		System.exit(0);
		
		
		TopologyProtocol.setInitializeMode(false);
		return false;
	}
	

	private void initialize(ArrayList<Integer> inx, int pid){
		double perc,prevPerc=0;
		for (int i = 0; i < nr; i++) {
			Node n = Network.get(inx.get(i));
//			KoalaProtocol kp = (KoalaProtocol )n.getProtocol(pid);
			KoalaNode kn = (KoalaNode )n.getProtocol(NodeUtilities.getLinkable(pid));
//				kp.intializeMyNode(n, NodeUtilities.KPID);
//				kp.join();
//				System.out.println(kn.getID());
			if(initialize){ //if we are setting long links then why not set also th 
				setNextNNeighbors(kn);
				kn.setJoined(true);
			}
			ArrayList<KoalaNeighbor> lls = getLongLinksKleinsberg(kn);
			kn.getRoutingTable().setLongLinks(lls);
		}	
		
		if(!initialize){		
			for (int i = 0; i < nr; i++) {
				Node n = Network.get(inx.get(i));
				KoalaProtocol kp = (KoalaProtocol )n.getProtocol(pid);
				kp.join();
				perc = (double)100*i/nr;
				String txt = i < nr-1 ? perc + "%, " : perc + "%"; 
				if(perc - prevPerc > 10){
					System.out.print(txt);
					prevPerc = perc;
				}
			}
		}
		System.out.println(" Done.");
	} 
	
	private ArrayList<KoalaNeighbor> getLongLinksRand(int n){
		ArrayList<KoalaNeighbor> pll = new ArrayList<KoalaNeighbor>();
		for(int i =0; i < n; i++){
			Node node = Network.get(CommonState.r.nextInt(Network.size()));
			KoalaNode kn = (KoalaNode )node.getProtocol(NodeUtilities.KPID);
			String id = initialize ? kn.getID() : NodeUtilities.DEFAULTID;
			KoalaNeighbor kneigh = new KoalaNeighbor(id, PhysicalDataProvider.getDefaultInterLatency());
			kneigh.setIdealID(kn.getID());
			pll.add(kneigh);
		}
		

		return pll;
	}
	
	
	private ArrayList<KoalaNeighbor> getLongLinksKleinsberg(KoalaNode kn){
		ArrayList<KoalaNeighbor> pll = new ArrayList<KoalaNeighbor>();
		int k = NodeUtilities.LONG_LINKS;
		int n = NodeUtilities.NR_DC / 2;
		if(NodeUtilities.NR_DC <= k)
			k = NodeUtilities.NR_DC/4;
//		int n = NodeUtilities.NR_DC;
		HashSet<Integer> nids = new HashSet<Integer>();
		int limit = 50;
		int trials = 0;
		while(nids.size() != k && trials < limit){
			int sizebefore = nids.size();
			int nid = (int) Math.round(Math.exp(Math.log(n) * (CommonState.r.nextDouble()-1.0))*n);
			if(nid >  NodeUtilities.NEIGHBORS) //skip neighbors, we already have them
				nids.add(nid);
//			System.out.println(nids.size());
			if(sizebefore == nids.size())
				trials++;
			else
				trials = 0;
		}
		
		
		
		for(Integer dist : nids){
			String[] ids = NodeUtilities.getIDFromDistance(kn.getID(), dist, false);
			String realId = CommonState.r.nextInt() % 2 == 0 ? ids[0] : ids[1]; 
			String id = initialize ? getFromNeighborhood(realId) : NodeUtilities.DEFAULTID;
			KoalaNeighbor kneigh = new KoalaNeighbor(id, PhysicalDataProvider.getDefaultInterLatency());
			kneigh.setIdealID(realId);
			pll.add(kneigh);
		}
		return pll;
		 
	}
	
	private String getFromNeighborhood(String realId) {
		if(NodeUtilities.isUp(realId))
			return realId;
		for(int i= 0; i < NodeUtilities.NR_DC; i++){
			String[] ids = NodeUtilities.getIDFromDistance(realId, i, false);
			if(NodeUtilities.isUp(ids[0]))
				return ids[0];
			if(NodeUtilities.isUp(ids[1]))
				return ids[1];
		}
		
		return NodeUtilities.DEFAULTID;
	}

	private void setNextNNeighbors(KoalaNode kn){
		
		int succi = 0, i=0; 
		while(succi < NodeUtilities.NEIGHBORS){
			String succid = (NodeUtilities.getDCID(kn.getID())+i+1)%NodeUtilities.NR_DC + "-0";
			if(NodeUtilities.isUp(succid)){
				KoalaNeighbor succ = new KoalaNeighbor(succid, PhysicalDataProvider.getDefaultInterLatency());
				kn.getRoutingTable().setGlobalSucessor(succ, succi);
				succi++;
			}
			i++;
		}
		
		int predi = 0; i=0; 
		while(predi < NodeUtilities.NEIGHBORS){
			String predid =  (NodeUtilities.getDCID(kn.getID())-(i+1)+NodeUtilities.NR_DC)%NodeUtilities.NR_DC + "-0";
			if(NodeUtilities.isUp(predid)){
				KoalaNeighbor pred = new KoalaNeighbor(predid, PhysicalDataProvider.getDefaultInterLatency());
				kn.getRoutingTable().setGlobalPredecessor(pred, predi);
				predi++;
			}
			i++;
		}
		
//		for(int i=0; i < NodeUtilities.NEIGHBORS; i++){
//			
//			String succid = (kn.getDCID()+i+1)%NodeUtilities.NR_DC + "-0";
//			KoalaNeighbor succ = new KoalaNeighbor(succid, PhysicalDataProvider.getDefaultInterLatency());
//			kn.getRoutingTable().setGlobalSucessor(succ, i);
//			
//			int predDc = kn.getDCID()-(i+1);
//			
//			String predid = (predDc+NodeUtilities.NR_DC)%NodeUtilities.NR_DC + "-0";
//			KoalaNeighbor pred = new KoalaNeighbor(predid, PhysicalDataProvider.getDefaultInterLatency());
//			kn.getRoutingTable().setGlobalPredecessor(pred,i);
//		}
		
	}
	
	

	@Override
	public void initialize(Node n) {
		KoalaProtocol kp = (KoalaProtocol) n.getProtocol(NodeUtilities.KPID);
		kp.intializeMyNode(n, NodeUtilities.KPID);
		KoalaNode kn = (KoalaNode)kp.getMyNode();
		ArrayList<KoalaNeighbor> lls = getLongLinksKleinsberg(kn);
		kn.getRoutingTable().setLongLinks(lls);
		
		kp.join();
	}

}
