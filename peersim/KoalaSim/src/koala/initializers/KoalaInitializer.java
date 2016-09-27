package koala.initializers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import chord.ChordNode;
import koala.KoalaNeighbor;
import koala.KoalaNode;
import koala.KoalaProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import renater.RenaterProtocol;
import topology.TopologyProtocol;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;


public class KoalaInitializer implements Control {
		
	private static final String PAR_KOALA_PROTOCOL= "kprotocol";
	private static final String PAR_RENATER_PROTOCOL= "protocol";
	private static final String PAR_CHORD_PROTOCOL= "cprotocol";
	private static final String PAR_KOALA_NR= "nr";
	private static final String PAR_KOALA_LL= "longlinks";
	
	private final int renProtPid;
	private final int koaProtPid;
	private final int cordProtPid;
	private final int nr;
	private final boolean longlinks;
	
	public KoalaInitializer(String prefix) {
		renProtPid = Configuration.getPid(prefix + "." + PAR_RENATER_PROTOCOL);
		koaProtPid = Configuration.getPid(prefix + "." + PAR_KOALA_PROTOCOL, -1);
		cordProtPid = Configuration.getPid(prefix + "." + PAR_CHORD_PROTOCOL, -1);
		nr = Configuration.getInt(prefix + "." + PAR_KOALA_NR, Network.size());
		longlinks = Configuration.getBoolean(prefix + "." + PAR_KOALA_LL, false);
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
			}
			
			perc = (double)100*i/nr;
			String txt = i < nr-1 ? perc + "%, " : perc + "%"; 
			if(perc - prevPerc > 10){
				System.out.print(txt);
				prevPerc = perc;
			}
			
		}
		
		if(longlinks){
			int totalNrLongLinks = 20;
			for (int i = 0; i < nr; i++) {
				Node n = Network.get(inx.get(i));
				if(koaProtPid > -1){
					KoalaNode kn = (KoalaNode )n.getProtocol(FastConfig.getLinkable( koaProtPid));
					setLongLinksKlein(totalNrLongLinks, kn);
//					setLongLinksRand(totalNrLongLinks, kn);
//					setLongLinks(100, kn);
				}
				
			}
			System.out.println("Average nr of long links: " + (double)totalNrLongLinks/nr);
		}
		
		
//		System.exit(0);
		System.out.println("Done.");
		
		TopologyProtocol.setInitializeMode(false);
		
		return false;
	}
	
	private int setLongLinks(int n, KoalaNode k){
		int nrLongLinks = 0;
		ArrayList<KoalaNeighbor> pll = new ArrayList<KoalaNeighbor>();
		for(int i =0; i < n; i++){
			Node node = Network.get(CommonState.r.nextInt(Network.size()));
			KoalaNode kn = (KoalaNode )node.getProtocol(FastConfig.getLinkable( koaProtPid));
//			KoalaNeighbor kneigh = new KoalaNeighbor(kn.getID(), PhysicalDataProvider.getDefaultInterLatency());
			KoalaNeighbor kneigh = new KoalaNeighbor(kn.getID(), PhysicalDataProvider.getLatency(k.getID(), kn.getID()));
			pll.add(kneigh);
		}
		
		double distance = 0;
		for(int i = 0; i < pll.size(); i++){
			distance = NodeUtilities.distance(k.getID(), pll.get(i).getNodeID());
			double prop = (1/distance) + 0.2;
			
			if(CommonState.r.nextDouble() < prop){
				k.getRoutingTable().addLongLink(pll.get(i));
				nrLongLinks++;
			}
		}
		return nrLongLinks;
	}
	
	
	private int setLongLinksRand(int n, KoalaNode k){
		int nrLongLinks = 0;
		ArrayList<KoalaNeighbor> pll = new ArrayList<KoalaNeighbor>();
		for(int i =0; i < n; i++){
			Node node = Network.get(CommonState.r.nextInt(Network.size()));
			KoalaNode kn = (KoalaNode )node.getProtocol(FastConfig.getLinkable( koaProtPid));
			KoalaNeighbor kneigh = new KoalaNeighbor(kn.getID(), PhysicalDataProvider.getDefaultInterLatency());
			pll.add(kneigh);
		}
		
		double distance = 0;
		for(int i = 0; i < pll.size(); i++){
			distance = NodeUtilities.distance(k.getID(), pll.get(i).getNodeID());
//			double prop = (1/distance) + 0.2;
			
//			if(CommonState.r.nextDouble() < prop){
				k.getRoutingTable().addLongLink(pll.get(i));
//				nrLongLinks++;
//			}
		}
		return nrLongLinks;
	}
	
	
	private void setLongLinksKlein(int k, KoalaNode kn){
		int n = NodeUtilities.NR_DC / 2;
//		int n = NodeUtilities.NR_DC;
		HashSet<Integer> nids = new HashSet<Integer>();
		while(nids.size() != k){
			int nid = (int)(Math.exp(Math.log(n) * (CommonState.r.nextDouble()-1.0))*n);
			nids.add(nid);
//			System.out.println(nids.size());
		}
		
		for(Integer dist : nids){
			String[] ids = NodeUtilities.getIDFromDistance(kn.getID(), dist, false);
			KoalaNeighbor kneigh1 = new KoalaNeighbor(ids[0], PhysicalDataProvider.getDefaultInterLatency());
			KoalaNeighbor kneigh2 = new KoalaNeighbor(ids[1], PhysicalDataProvider.getDefaultInterLatency());
			if(CommonState.r.nextInt() % 2 == 0)
				kn.getRoutingTable().addLongLink(kneigh1);
			else
				kn.getRoutingTable().addLongLink(kneigh2);
		}
		
		 
	}
	
	private void setLongLinksChord(KoalaNode kn){
		ChordNode cn = (ChordNode) kn.getNode().getProtocol(FastConfig.getLinkable(cordProtPid));
		for(int i =0; i < cn.fingerTable.length; i++){
			if(cn.fingerTable[i] != null){
				KoalaNeighbor kneigh = new KoalaNeighbor(cn.fingerTable[i].getID(), PhysicalDataProvider.getDefaultInterLatency());
				kn.getRoutingTable().addLongLink(kneigh);
			}
		}
	}

}
