package koala.initializers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

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
		
	private static final String PAR_PROT = "protocol";
	private static final String PAR_KOALA_NR= "nr";
	private static final String PAR_KOALA_INIT= "initialize";

	private final int nr;
	private int pid;
	private final boolean initialize;
	private final boolean clearlinks;
	private final int flushload;
	
	
//	private FileOutputStream[] foss = new FileOutputStream[10];
//	private PrintStream[] pss = new PrintStream[10];
	private boolean fromFile = false;
	
	protected HashMap<Integer, ArrayList<KoalaNeighbor>> locals = new HashMap<Integer, ArrayList<KoalaNeighbor>>();
	
	public KoalaInitializer(String prefix) {
		nr = Configuration.getInt(prefix + "." + PAR_KOALA_NR, Network.size());
		initialize = Configuration.getBoolean(prefix + "." + PAR_KOALA_INIT, false);
		clearlinks = Configuration.getBoolean(prefix + ".clearlinks", false);
		pid = Configuration.getPid(prefix + "." + PAR_PROT, -1);
		fromFile = Configuration.getBoolean(prefix + ".fromfile", false);
		flushload = Configuration.getInt(prefix + ".flushload", 50000);
	}
	
	@Override
	public boolean execute() {
		System.out.println("Building the koala rings. Depending on the size this might take also some time");
		TopologyProtocol.setInitializeMode(true);
		
		
		for(int i = 0; i < Network.size(); i++){
			Node n = Network.get(i);
			addLocal(n);
			RenaterProtocol rp = (RenaterProtocol )n.getProtocol(NodeUtilities.RPID);
			rp.intializeMyNode(n, NodeUtilities.RPID);
			rp.join();
			if(NodeUtilities.KPID > 0){
				KoalaProtocol kp = (KoalaProtocol )n.getProtocol(NodeUtilities.KPID);
				kp.intializeMyNode(n, NodeUtilities.KPID);
			}
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
		for(int i = 0; i < nr; i++)
			inx.add(i);
		
		Collections.shuffle(inx, CommonState.r);
		
		
		
		for (int i = 0; i < nr; i++) {
			Node n = Network.get(inx.get(i));
			NodeUtilities.up(n);
		}
		
		NodeUtilities.copyAltDowns();
		
		if(NodeUtilities.FKPID > 0)
			initialize(inx, NodeUtilities.FKPID);
//		System.exit(0);
		if(NodeUtilities.KPID > 0)
			initialize(inx, NodeUtilities.KPID);		
		if(NodeUtilities.LKPID > 0)
			initializeLeader(inx, NodeUtilities.LKPID);		
		

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
		
		System.out.println("Initialization lasted " + (System.currentTimeMillis() - PhysicalDataProvider.SimTime)/1000 + " seconds");
		TopologyProtocol.setInitializeMode(false);
//		System.exit(0);
		return false;
	}
	

	private void initialize(ArrayList<Integer> inx, int pid){
		NodeUtilities.CURRENT_PID = pid;
		
		if(loadFromFile(pid)) return;
		double oldAlpha = NodeUtilities.ALPHA;
		NodeUtilities.ALPHA = 0.5;
		for (int i = 0; i < nr; i++) {
			Node n = Network.get(inx.get(i));
			KoalaNode kn = (KoalaNode )n.getProtocol(NodeUtilities.getLinkable(pid));
			if(initialize){ //if we are setting long links then why not set also the neighbors 
				setNextNNeighbors(kn);
				kn.setJoined(true);
			}
//			System.out.println("generating kleinberg for the " + i + "th node");
			kn.getRoutingTable().intializeLongLinks();
			if(NodeUtilities.VIV_TEST)
				setAllAsRandom(kn, inx);
			else
				kn.getRoutingTable().intializeRandomLinks();
		}	
		
		joinList(inx, pid);
		NodeUtilities.ALPHA = oldAlpha;
	} 
	
	private void setAllAsRandom(KoalaNode mykn, ArrayList<Integer> inx) {
		ArrayList<KoalaNeighbor> randLinks = new ArrayList<KoalaNeighbor>();
		for (int i = 0; i < nr; i++) {
			Node n = Network.get(inx.get(i));
			KoalaNode kn = (KoalaNode )n.getProtocol(NodeUtilities.getLinkable(pid));
			if(kn.equals(mykn)) continue;
			randLinks.add( new KoalaNeighbor(kn));
		}	
		mykn.getRoutingTable().setRandLinks(randLinks);
	}
	
	private void initializeLeader(ArrayList<Integer> inx, int pid){
		NodeUtilities.CURRENT_PID = pid;
		
		if(loadFromFile(pid)) {
//			for (int i = 0; i < nr; i++) {
//				Node n = Network.get(inx.get(i));
//				KoalaNode kn = (KoalaNode )n.getProtocol(NodeUtilities.getLinkable(pid));
//				ArrayList<KoalaNeighbor> neigs  = kn.getRoutingTable().getNeighbors();
//				for(KoalaNeighbor ni : neigs){
//					for(KoalaNeighbor nj : neigs){
//						if(ni.equals(nj)) continue;
//						if(ni.getDCID() == nj.getDCID() && ni.getDCID() != kn.getDCID()) 
//							System.out.println("asdfasfasdfasdfasd");
//						
//					}
//					
//				}
//			}
			
		return;}
		
		ArrayList<Integer> linx = new ArrayList<Integer>();
//		ArrayList<Integer> nolinx = (ArrayList<Integer>)inx.clone();

		for (int i = 0; i < nr; i++) {
			Node n = Network.get(inx.get(i));
			KoalaNode kn = (KoalaNode )n.getProtocol(NodeUtilities.getLinkable(pid));
			RenaterNode rn = (RenaterNode )n.getProtocol(NodeUtilities.RID);
			if(initialize && rn.isGateway()){ //if we are setting long links then why not set also the neighbors 
				setNextNNeighbors(kn);
				kn.setJoined(true);
			}
			if(rn.isGateway()){
//				ArrayList<KoalaNeighbor> lls = getLongLinksKleinsberg(kn);
//				kn.getRoutingTable().setLongLinks(lls);
				kn.getRoutingTable().intializeLongLinks();
				kn.getRoutingTable().intializeRandomLinks();
				linx.add(inx.get(i));
				
			}
		}	
		
//		for(Integer i: linx)
//			nolinx.remove(i);
//		linx.addAll(nolinx);
		
		joinList(linx, pid);
	} 
	
	
	private void joinList(ArrayList<Integer> inx, int pid){
		int s = inx.size();
		if(s==0) return;
		double perc,prevPerc=0;
		System.out.println("Joining " + NodeUtilities.getProtocolName(pid));
//		int s = nr;
		
		if(!initialize){
			for (int i = 0; i < s; i++) {
				Node n = Network.get(inx.get(i));
				KoalaProtocol kp = (KoalaProtocol)n.getProtocol(pid);				
				System.out.print(i+ ". joining " + kp.getMyNode().getSID());
				kp.join();
				System.out.println(" in " + KoalaProtocol.joinHops);
				perc = (double)100*(i+1)/s;
				String txt = i < s-1 ? perc + "%, " : perc + "%"; 
				if(perc - prevPerc > 10 || i == s-1){
					System.out.print(txt);
					prevPerc = perc;
				}
			}
			
		}
		
		saveToFile(pid);
		clearLinks(pid);
		System.out.println("\nDone joining "+ NodeUtilities.getProtocolName(pid));
//		System.exit(0);
		
		assignLocals(pid);
	}
	
	private void addLocal(Node n){
		KoalaNode kn = (KoalaNode)n.getProtocol(NodeUtilities.KID);
		int dcid = NodeUtilities.getDCID(kn.getSID());
		KoalaNeighbor kng = new KoalaNeighbor(kn, PhysicalDataProvider.getDefaultIntraLatency(),3);
		if(!locals.containsKey(dcid)){
			ArrayList<KoalaNeighbor> l = new ArrayList<KoalaNeighbor>();
			l.add(kng);
			locals.put(dcid, l);
		}else
			locals.get(dcid).add(kng);
	}
	
	private void assignLocals(int pid){
		if(pid==NodeUtilities.FKPID) return;
		System.out.print("Assigning locals...");
		for(int i = 0; i < nr; i++){
			KoalaNode kn = (KoalaNode)Network.get(i).getProtocol(NodeUtilities.getLinkable(pid));
			int dcid = NodeUtilities.getDCID(kn.getSID());
			kn.getRoutingTable().setLocals(locals.get(dcid));
		}
		
//		if(pid==NodeUtilities.KPID){
//			System.out.print("Assigning responsabilities...");
//			for(int i = 0; i < nr; i+=NodeUtilities.NR_NODE_PER_DC){
//				for(int j = i; j < NodeUtilities.NR_NODE_PER_DC; j++){
//					KoalaNode kn = (KoalaNode)Network.get(j).getProtocol(NodeUtilities.KID);
//					kn.getRoutingTable().setRandpLinks(getRespLinksKleinsberg(j*NodeUtilities.NR_DC/NodeUtilities.NR_NODE_PER_DC));
//				}
//			}
//		}
		
		System.out.println("Done!");
	}
	
	
//	private ArrayList<KoalaNeighbor> getRespLinksKleinsberg(int low){
//		ArrayList<KoalaNeighbor> prl = new ArrayList<KoalaNeighbor>();
//		int k = NodeUtilities.RESP_LINKS;
//		int high = low + NodeUtilities.NR_DC/NodeUtilities.NR_NODE_PER_DC; 
//		int n = (high-low) / 2;
//		int center = (high+low) / 2;
//		HashSet<Integer> nids = new LinkedHashSet<Integer>();
//		int limit = 50;
//		int trials = 0;
//		while(nids.size() != k && trials < limit){
//			int sizebefore = nids.size();
//			int nid = (int) Math.round(Math.exp(Math.log(n) * (CommonState.r.nextDouble()-1.0))*n);
//			nids.add(nid);
//			if(sizebefore == nids.size())
//				trials++;
//			else
//				trials = 0;
//		}
//		
//		for(Integer dist : nids){
//			String[] ids = NodeUtilities.getIDFromDistance(center+"-"+0, dist);
//			String realId = CommonState.r.nextInt() % 2 == 0 ? ids[0] : ids[1]; 
//			String id = initialize ? getFromNeighborhood(realId) : NodeUtilities.DEFAULTID;
//			KoalaNeighbor kneigh = new KoalaNeighbor(new TopologyPathNode(id), PhysicalDataProvider.getDefaultInterLatency());
//			kneigh.setIdealID(realId);
//			prl.add(kneigh);
//		}
//		return prl;
//		 
//	}
	
	
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
		int fails = 0;
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
				fails++;
			
		}	
		
		return fails == 0;
	}
	
	private void saveToFile(int pid){
		
		
		if(!checkNeighbors(pid)){
			System.out.println("WRONG NEIGHBOR ASSIGNEMNT");
			System.exit(1);
		}else
			System.out.println("CORRECT NEIGHBOR ASSIGNEMNT");
		
		if(!fromFile) return;
		
		try {
			File file = new File(getFilename(pid));
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
	
	private void clearLinks(int pid){
		if(!clearlinks) return;
		for(int i = 0; i < Network.size(); i++){
			KoalaNode kn = (KoalaNode )Network.get(i).getProtocol(NodeUtilities.getLinkable(pid));
			kn.getRoutingTable().intializeLongLinks();
			kn.getRoutingTable().initializeVicinities();
			kn.getRoutingTable().intializeRandomLinks();
			kn.getRoutingTable().setApplicationLinks(new ArrayList<KoalaNeighbor>());
		}
	} 
	
	private String getFilename(int pid){
		String file = "out/koala/init"; 
		if(NodeUtilities.C > 0)
			file += ".C"+NodeUtilities.C;
		if(NodeUtilities.RAND_C > 0)
			file += ".RC"+NodeUtilities.RAND_C;
		if(NodeUtilities.VICINITY_C > 0)
			file += ".VC"+NodeUtilities.VICINITY_C;
		if(NodeUtilities.APPLICATION_C > 0)
			file += ".AC"+NodeUtilities.APPLICATION_C;
		file += "."+ NodeUtilities.getProtocolName(pid)+"."+NodeUtilities.NR_DC+"x"+NodeUtilities.NR_NODE_PER_DC +".dat"; 
		
		return file;
//		return "out/koala/init.C"+NodeUtilities.C +".RC0.VC0.0" +"."+ NodeUtilities.getProtocolName(pid)+"."+NodeUtilities.NR_DC+"x"+NodeUtilities.NR_NODE_PER_DC +".dat";
	}
	
	private boolean loadFromFile(int pid){
		if(!fromFile || nr==0) return false;
		
		
		try {
			File file = new File(getFilename(pid));
			if(!file.exists()) return false;
			System.out.println("Loading "+NodeUtilities.getProtocolName(pid)+" from the file.");
			double perc,prevPerc=0;
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
					
					perc = (double)100*(i+1)/nr;
					String txt = i < nr-1 ? (int)perc + "%, " : (int)perc + "%"; 
					if(perc - prevPerc > 10 || i == nr-1){
						System.out.print(txt);
						prevPerc = perc;
					}
				}
				if(i!=Network.size()){
					System.out.println("Inconsistent file");
					System.exit(1);
				}
				
				System.out.println(" Done!");
			}
			
			assignLocals(pid);
			clearLinks(pid);
				
			
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
		int p = pid != -1? pid : NodeUtilities.KPID;
		KoalaProtocol kp = (KoalaProtocol) n.getProtocol(p);
		kp.intializeMyNode(n, p);
		KoalaNode kn = (KoalaNode)kp.getMyNode();
//		ArrayList<KoalaNeighbor> lls = getLongLinksKleinsberg(kn);
//		kn.getRoutingTable().setLongLinks(lls);
		kp.join();
		System.out.println("Joined in " + KoalaProtocol.joinHops);
		
	}

}
