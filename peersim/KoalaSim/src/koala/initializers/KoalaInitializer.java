package koala.initializers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import koala.KoalaNeighbor;
import koala.KoalaNode;
import koala.KoalaProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;
import renater.RenaterProtocol;
import topology.TopologyProtocol;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;


public class KoalaInitializer implements Control, NodeInitializer {
		
	private static final String PAR_KOALA_PROTOCOL= "kprotocol";
	private static final String PAR_RENATER_PROTOCOL= "protocol";
	private static final String PAR_CHORD_PROTOCOL= "cprotocol";
	private static final String PAR_KOALA_NR= "nr";
	private static final String PAR_KOALA_LL= "longlinks";
	private static final String PAR_KOALA_NLL= "nr_longlinks";
	
	private final int renProtPid;
	private final int koaProtPid;
	private final int cordProtPid;
	private final int nr;
	private final boolean longlinks;
	private final int nr_longlinks;
	
	public KoalaInitializer(String prefix) {
		renProtPid = Configuration.getPid(prefix + "." + PAR_RENATER_PROTOCOL);
		koaProtPid = Configuration.getPid(prefix + "." + PAR_KOALA_PROTOCOL, -1);
		cordProtPid = Configuration.getPid(prefix + "." + PAR_CHORD_PROTOCOL, -1);
		nr = Configuration.getInt(prefix + "." + PAR_KOALA_NR, Network.size());
		longlinks = Configuration.getBoolean(prefix + "." + PAR_KOALA_LL, false);
		nr_longlinks = Configuration.getInt(prefix + "." + PAR_KOALA_NLL, 0);
	}
	
	@Override
	public boolean execute() {
		System.out.println("Building the koala ring. Depending on the size this might take also some time");
		TopologyProtocol.setInitializeMode(true);
		
		ArrayList<Integer> inx = new ArrayList<Integer>();
		for(int i = 0; i < nr; i++)
			inx.add(i);
		
		Collections.shuffle(inx, CommonState.r);
		
		double perc,prevPerc;
		prevPerc = 0;
		for (int i = 0; i < nr; i++) {
			Node n = Network.get(inx.get(i));
			
			RenaterProtocol rp = (RenaterProtocol )n.getProtocol(renProtPid);
			rp.intializeMyNode(n, renProtPid);
			rp.join();
			
			if(koaProtPid > -1){
				
				KoalaProtocol kp = (KoalaProtocol )n.getProtocol(koaProtPid);
				kp.intializeMyNode(n, koaProtPid);
				kp.join();
				
				if(nr_longlinks > 0){
					KoalaNode kn = (KoalaNode )n.getProtocol(FastConfig.getLinkable( koaProtPid));
					ArrayList<KoalaNeighbor> lls = getLongLinksKleinsberg(nr_longlinks, kn);
//					ArrayList<KoalaNeighbor> lls = getLongLinksRand(nr_longlinks);
					kn.getRoutingTable().setLongLinks(lls);
				}
			}
			
			perc = (double)100*i/nr;
			String txt = i < nr-1 ? perc + "%, " : perc + "%"; 
			if(perc - prevPerc > 10){
				System.out.print(txt);
				prevPerc = perc;
			}
			
		}
		
		
		
		
//		System.exit(0);
		System.out.println("Done.");
		
		TopologyProtocol.setInitializeMode(false);
		
		return false;
	}
	

	
	private ArrayList<KoalaNeighbor> getLongLinksRand(int n){
		ArrayList<KoalaNeighbor> pll = new ArrayList<KoalaNeighbor>();
		for(int i =0; i < n; i++){
			Node node = Network.get(CommonState.r.nextInt(Network.size()));
			KoalaNode kn = (KoalaNode )node.getProtocol(FastConfig.getLinkable( koaProtPid));
			String id = longlinks ? kn.getID() : NodeUtilities.DEFAULTID;
			KoalaNeighbor kneigh = new KoalaNeighbor(id, PhysicalDataProvider.getDefaultInterLatency());
			kneigh.setIdealID(kn.getID());
			pll.add(kneigh);
		}
		

		return pll;
	}
	
	
	private ArrayList<KoalaNeighbor> getLongLinksKleinsberg(int k, KoalaNode kn){
		ArrayList<KoalaNeighbor> pll = new ArrayList<KoalaNeighbor>();
		int n = NodeUtilities.NR_DC / 2;
		if(NodeUtilities.NR_DC <= k)
			k = NodeUtilities.NR_DC/4;
//		int n = NodeUtilities.NR_DC;
		HashSet<Integer> nids = new HashSet<Integer>();
		while(nids.size() != k){
			int nid = (int)(Math.exp(Math.log(n) * (CommonState.r.nextDouble()-1.0))*n);
			if(nid > 1) //skip neighbors, we already have them
				nids.add(nid);
//			System.out.println(nids.size());
		}
		
		for(Integer dist : nids){
			String[] ids = NodeUtilities.getIDFromDistance(kn.getID(), dist, false);
			String realId = CommonState.r.nextInt() % 2 == 0 ? ids[0] : ids[1]; 
			String id = longlinks ? realId : NodeUtilities.DEFAULTID;
			KoalaNeighbor kneigh = new KoalaNeighbor(id, PhysicalDataProvider.getDefaultInterLatency());
			kneigh.setIdealID(realId);
			pll.add(kneigh);
		}
		return pll;
		 
	}
	
	

	@Override
	public void initialize(Node n) {
		System.out.println("node initialized");
		
	}

}
