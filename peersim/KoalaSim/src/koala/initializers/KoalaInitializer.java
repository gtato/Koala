package koala.initializers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import com.google.gson.Gson;

import chord.ChordProtocol;
import koala.FlatKoalaProtocol;
import koala.KoalaNeighbor;
import koala.KoalaNode;
import koala.KoalaProtocol;
import koala.LeaderKoalaProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;
import renater.RenaterNode;
import renater.RenaterProtocol;
import topology.TopologyNode;
import topology.TopologyPathNode;
import topology.TopologyProtocol;
import utilities.KoalaJsonParser;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;


public class KoalaInitializer implements Control, NodeInitializer {
		
	private static final String PAR_KOALA_NR= "nr";
	private static final String PAR_KOALA_INIT= "initialize";

	private final int nr;
	private final boolean initialize;
	
	
//	private FileOutputStream[] foss = new FileOutputStream[10];
//	private PrintStream[] pss = new PrintStream[10];
	private boolean fromFile = false;
	
	public KoalaInitializer(String prefix) {
		nr = Configuration.getInt(prefix + "." + PAR_KOALA_NR, Network.size());
		initialize = Configuration.getBoolean(prefix + "." + PAR_KOALA_INIT, false);
	
		fromFile = Configuration.getBoolean(prefix + ".fromfile", false);
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
			if(NodeUtilities.LKPID > 0){
				LeaderKoalaProtocol fkp = (LeaderKoalaProtocol )n.getProtocol(NodeUtilities.LKPID);
				fkp.intializeMyNode(n, NodeUtilities.LKPID);
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
		if(NodeUtilities.LKPID > 0)
			initializeLeader(inx, NodeUtilities.LKPID);
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
		NodeUtilities.CURRENT_PID = pid;
		
		if(loadFromFile(pid)){ System.out.println("Loaded from the file."); return;}
		
		double perc,prevPerc=0;
		for (int i = 0; i < nr; i++) {
			Node n = Network.get(inx.get(i));
			KoalaNode kn = (KoalaNode )n.getProtocol(NodeUtilities.getLinkable(pid));
			if(initialize){ //if we are setting long links then why not set also the neighbors 
				setNextNNeighbors(kn);
				kn.setJoined(true);
			}
			ArrayList<KoalaNeighbor> lls = getLongLinksKleinsberg(kn);
			kn.getRoutingTable().setLongLinks(lls);
		}	
		
		if(!initialize){		
			for (int i = 0; i < nr; i++) {
				Node n = Network.get(inx.get(i));
				KoalaProtocol kp = (KoalaProtocol)n.getProtocol(pid);
				System.out.println(i+ ". joining " + kp.getMyNode().getCID());
				kp.join();
				
				perc = (double)100*(i+1)/nr;
				String txt = i < nr-1 ? perc + "%, " : perc + "%"; 
				if(perc - prevPerc > 10 || i == nr-1){
					System.out.print(txt);
					prevPerc = perc;
				}
			}
		}
		saveToFile(pid);
		System.out.println(" Done.");
		
	} 
	
	private void initializeLeader(ArrayList<Integer> inx, int pid){
		NodeUtilities.CURRENT_PID = pid;
		
		if(loadFromFile(pid)){ System.out.println("Loaded from the file."); return;}
		
		ArrayList<Integer> linx = new ArrayList<Integer>();
		ArrayList<Integer> nolinx = (ArrayList<Integer>)inx.clone();

		double perc,prevPerc=0;
		for (int i = 0; i < nr; i++) {
			Node n = Network.get(inx.get(i));
			KoalaNode kn = (KoalaNode )n.getProtocol(NodeUtilities.getLinkable(pid));
			RenaterNode rn = (RenaterNode )n.getProtocol(NodeUtilities.RID);
			if(initialize && rn.isGateway()){ //if we are setting long links then why not set also the neighbors 
				setNextNNeighbors(kn);
				kn.setJoined(true);
			}
			if(rn.isGateway()){
				ArrayList<KoalaNeighbor> lls = getLongLinksKleinsberg(kn);
				kn.getRoutingTable().setLongLinks(lls);
				linx.add(inx.get(i));
				
			}
		}	
		
		for(Integer i: linx)
			nolinx.remove(i);
		linx.addAll(nolinx);
		
		if(!initialize){
			for (int i = 0; i < nr; i++) {
				Node n = Network.get(linx.get(i));
				KoalaProtocol kp = (KoalaProtocol)n.getProtocol(pid);				
				System.out.println(i+ ". joining " + kp.getMyNode().getCID());
				kp.join();
				perc = (double)100*(i+1)/nr;
				String txt = i < nr-1 ? perc + "%, " : perc + "%"; 
				if(perc - prevPerc > 10 || i == nr-1){
					System.out.print(txt);
					prevPerc = perc;
				}
				
				
			}
			
		}
		
		saveToFile(pid);
		System.out.println(" Done.");
	} 
	
	
	private ArrayList<KoalaNeighbor> getLongLinksKleinsberg(KoalaNode kn){
		ArrayList<KoalaNeighbor> pll = new ArrayList<KoalaNeighbor>();
		int k = NodeUtilities.getLongLinks();
		int size = NodeUtilities.getSize(); 
		int n = size / 2;
		if(size <= k)
			k = size/4;
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
			String[] ids = NodeUtilities.getIDFromDistance(kn.getSID(), dist);
			String realId = CommonState.r.nextInt() % 2 == 0 ? ids[0] : ids[1]; 
			String id = initialize ? getFromNeighborhood(realId) : NodeUtilities.DEFAULTID;
			KoalaNeighbor kneigh = new KoalaNeighbor(new TopologyPathNode(id), PhysicalDataProvider.getDefaultInterLatency());
			kneigh.setIdealID(realId);
			pll.add(kneigh);
		}
		return pll;
		 
	}
	
	private String getFromNeighborhood(String realId) {
		if(NodeUtilities.isUp(realId))
			return realId;
		int size = NodeUtilities.getSize(); 
		for(int i= 0; i < size; i++){
			String[] ids = NodeUtilities.getIDFromDistance(realId, i);
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
			String succid = (NodeUtilities.getDCID(kn.getSID())+i+1)%NodeUtilities.NR_DC + "-0";
			if(NodeUtilities.isUp(succid)){
				KoalaNeighbor succ = new KoalaNeighbor(new TopologyPathNode(succid), PhysicalDataProvider.getDefaultInterLatency());
				kn.getRoutingTable().setGlobalSucessor(succ, succi);
				succi++;
			}
			i++;
		}
		
		int predi = 0; i=0; 
		while(predi < NodeUtilities.NEIGHBORS){
			String predid =  (NodeUtilities.getDCID(kn.getSID())-(i+1)+NodeUtilities.NR_DC)%NodeUtilities.NR_DC + "-0";
			if(NodeUtilities.isUp(predid)){
				KoalaNeighbor pred = new KoalaNeighbor(new TopologyPathNode(predid), PhysicalDataProvider.getDefaultInterLatency());
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
	

	private boolean checkNeighbors(int pid)
	{
		boolean isLeader = pid == NodeUtilities.LKPID;
		for (int i = 0; i < nr; i++) {
			Node n = Network.get(i);
			KoalaNode kn = (KoalaNode )n.getProtocol(NodeUtilities.getLinkable(pid));
			RenaterNode rn = (RenaterNode )n.getProtocol(NodeUtilities.RID);
			if(isLeader && !rn.isGateway()) continue;
			
			int dc = NodeUtilities.getDCID(kn.getSID());
			
			
			int idealSuccDC = (dc+1)%NodeUtilities.getSize();
			int idealPredDC = (dc-1+NodeUtilities.getSize())%NodeUtilities.getSize();
			
			int currentSuccDc = NodeUtilities.getDCID(kn.getRoutingTable().getGlobalSucessor(0).getSID());
			int currentPredDc = NodeUtilities.getDCID(kn.getRoutingTable().getGlobalPredecessor(0).getSID());
			
			if(idealSuccDC != currentSuccDc || idealPredDC != currentPredDc)
				return false;
			
		}	
		
		return true;
	}
	
	private void saveToFile(int pid){
		if(!fromFile) return;
		
		if(!checkNeighbors(pid)){
			System.out.println("WRONG ASSIGNEMNT OF NEIGHBORS");
			System.exit(1);
		}
		
		try {
			File file = new File("out/koala/init"+ pid+".dat");
			if(file.exists()) return;
			file.getParentFile().mkdirs();
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			PrintStream ps = new PrintStream(fos);
			for(int i = 0; i < Network.size(); i++){
				KoalaNode kn = (KoalaNode )Network.get(i).getProtocol(NodeUtilities.getLinkable(pid));
				ps.println(KoalaJsonParser.toJson(kn));
			}
			
			fos.close();
			ps.close();
			
		} catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
	private boolean loadFromFile(int pid){
		if(!fromFile) return false;
		
		try {
			File file = new File("out/koala/init"+ pid+".dat");
			if(!file.exists()) return false;
			
			
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				int i = 0;
				for(String line = br.readLine(); line != null; line = br.readLine()){
					KoalaNode kn = (KoalaNode )Network.get(i).getProtocol(NodeUtilities.getLinkable(pid));
					KoalaNode jsonkn = KoalaJsonParser.jsonToObject(line, KoalaNode.class);
					if(kn.getCID().equals(jsonkn.getCID()))
						kn.copyRTFrom(jsonkn);
					else{
						System.out.println("Inconsistent file");
						System.exit(1);
					}
					i++;
				}
				if(i!=Network.size()){
					System.out.println("Inconsistent file");
					System.exit(1);
				}
			}
			return true;
			
		} catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
	
//	private void openFile(int pid) {
//		if(!fromFile) return;
//		
//		try {
//			File file = new File("out/koala/init"+ pid+".dat");
//			file.getParentFile().mkdirs();
//			file.createNewFile();
//			foss[pid] = new FileOutputStream(file);
//            pss[pid] = new PrintStream(foss[pid]);
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//	
//	private void writeFile(int pid, String print){
//		if(!fromFile) return;
//		
//		pss[pid].println(print);
//	}
//	
//	
//	private void closeFile(int pid){
//		if(!fromFile) return;
//		
//		try {
//			foss[pid].close();
//			pss[pid].close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        
//	}
	

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
