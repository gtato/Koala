package topology.controllers;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import chord.ChordProtocol;
import koala.KoalaProtocol;
import messaging.TopologyMessage;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import renater.RenaterProtocol;
import spaasclient.SPClient;
import utilities.KoaLite;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;

public class ResultCollector extends NodeObserver {

//	private static final String PAR_CHORD_PROTOCOL= "cprotocol";
//	private static final String PAR_KOALA_PROTOCOL= "kprotocol";
//	private static final String PAR_RENATER_PROTOCOL= "protocol";
//	
	private static final String PAR_FLUSH= "flush";
	
	
//	private final int renProtPid;
//	private final int koaProtPid;
//	private final int chordProtPid;
//	
	private static HashMap<Integer, Node> sentMgs = new HashMap<Integer, Node>(); 
	
	private double renaterTotalLatency = 0;
	private double koalaTotalLatency = 0;
	private double chordTotalLatency = 0;
	
	private static int nrInterDCMsg = 0;
	private static int nrIntraDCMsg = 0;

	private static int flush = 0;
	private boolean ended = false;
//	private ArrayList<ArrayList<Integer>> aggregated = new ArrayList<ArrayList<Integer>>(); 
//	ArrayList<String> toPrint = new ArrayList<String>();
	ArrayList<String> msgToPrint = new ArrayList<String>();
	PrintStream[] pss;
	
	int lastMsg;
	int lastCFail;
	int lastKFail;
	public ResultCollector(String prefix) {
		super(prefix);
//		renProtPid = Configuration.getPid(prefix + "." + PAR_RENATER_PROTOCOL);
//		koaProtPid = Configuration.getPid(prefix + "." + PAR_KOALA_PROTOCOL, -1);
//		chordProtPid = Configuration.getPid(prefix + "." + PAR_CHORD_PROTOCOL, -1);
//		
//		step  = Configuration.getInt(prefix + ".step", 1);
		flush = Configuration.getInt(prefix + PAR_FLUSH, 100);
//		plotScript = "gnuplot/plotResults.plt";
		deleteFiles();
		lastMsg = lastCFail = lastKFail = 0;
		msgToPrint.add("cycle\trlat\tclat\tklat\trhop\tchop\tkhop\tmsgs\trpath\tcpath\tkpath\tcfail\tkfail");
	}

	
	@Override
	public boolean execute() {
		updateGraph();
		
		if(NodeUtilities.KPID > 0)
			compare();
		else
			reportRenater();
//		System.out.println(CommonState.getTime() + " - " + CommonState.getEndTime());
		if(CommonState.getTime() == CommonState.getEndTime()-1 && !ended){
			System.out.println("Inter: " + nrInterDCMsg + " Intra: " + nrIntraDCMsg + " Total: " + (nrInterDCMsg + nrIntraDCMsg));
			System.out.println("CHORD: success: " + ChordProtocol.SUCCESS + " fails: " + ChordProtocol.FAIL);
			System.out.println("KOALA: success: " + KoalaProtocol.SUCCESS + " fails: " + KoalaProtocol.FAIL + " internal fails: " + KoalaProtocol.INT_FAIL);
			SPClient.printCacheStats();
			
			flush();
//			graphToFile();
//			closeFiles(pss);
			
//			plotIt();
			ended = true;
//			KoaLite.close();
		}
		
		if(CommonState.getTime()%flush==0)
			flush();
		
		return false;
	}
	
	private void compare(){
		ArrayList<Integer> entriesToRemove = new ArrayList<Integer>();
		
//		int rps, kps, cps, nr; rps = kps = cps = nr = 0;
//		int rl, kl, cl; rl = kl = cl = 0;
//		int rtSize = 0;
		TopologyMessage rm=null,km=null,cm=null;
		for(Map.Entry<Integer, Node> msg : sentMgs.entrySet()){
//			RenaterProtocol rp = (RenaterProtocol) msg.getValue().getProtocol(renProtPid);
//			KoalaProtocol kp = (KoalaProtocol) msg.getValue().getProtocol(koaProtPid);
//			ChordProtocol cp = (ChordProtocol) msg.getValue().getProtocol(chordProtPid);
			
			if((RenaterProtocol.REC_MSG.containsKey(msg.getKey()) || NodeUtilities.RPID < 0)
			&& (KoalaProtocol.REC_MSG.containsKey(msg.getKey()) || NodeUtilities.KPID < 0)
			&& (ChordProtocol.REC_MSG.containsKey(msg.getKey()) || NodeUtilities.CPID < 0)
			){
			
				if(NodeUtilities.RPID >= 0)
					rm = RenaterProtocol.REC_MSG.get(msg.getKey());
				if(NodeUtilities.KPID >= 0)
					km = KoalaProtocol.REC_MSG.get(msg.getKey());
				if(NodeUtilities.CPID >= 0)
					cm = ChordProtocol.REC_MSG.get(msg.getKey());
				
//				renaterTotalLatency += rm.getLatency();
//				koalaTotalLatency += km.getLatency();
//				chordTotalLatency += cm.getLatency();
//				renaterTotalLatency = PhysicalDataProvider.round(renaterTotalLatency);
//				koalaTotalLatency = PhysicalDataProvider.round(koalaTotalLatency);
//				chordTotalLatency = PhysicalDataProvider.round(chordTotalLatency);

				
				//do stuff 
//				String ok = rm.getPath().toString().equals(rm.getPhysicalPathToString().toString()) ? " (ok) " : " (not ok) ";
//				System.out.println("(R) "+rm.getID() + ": " + rm.getTotalLatency() + " " + rm.getPath() + " " + rm.getPhysicalPathToString() + ok);
//				System.out.println("(K) "+km.getID() + ": " + km.getTotalLatency() + " " + km.getPath() + " " + km.getPhysicalPathToString());
//				System.out.println("(T) "+rm.getID() + ": " + ((double) km.getTotalLatency() / rm.getTotalLatency()) + 
//									" " + rm.getPath().size() + " " +km.getPath().size() +
//									" " +km.getPhysicalPathToString().size() + " " + PhysicalDataProvider.round((double) koalaTotalLatency / renaterTotalLatency));
//				
//				
//				System.out.println();
				
//				toPrint.add(km.getTotalLatency()+"");
//				toPrint.add(km.getTotalLatency()+" " + rm.getTotalLatency() + " " + cm.getTotalLatency());
//				toPrint.add(km.getPath().size()+" " + rm.getPath().size() + " " + cm.getPath().size());
//				toPrint.add(((double) km.getTotalLatency() / rm.getTotalLatency())+"");
				//toPrint.add(km.getPath().size()+"");

//				rps += rm.getPath().size(); 
//				if(chordProtPid > 0) cps += cm.getPath().size(); 
//				kps += km.getPath().size(); 
//				rl += rm.getTotalLatency(); 
//				if(chordProtPid > 0) cl += cm.getTotalLatency(); 
//				kl += km.getTotalLatency();
//				rtSize += ((KoalaNode)kp.getMyNode()).getRoutingTable().getSize();
				
				if(NodeUtilities.RPID >= 0) RenaterProtocol.REC_MSG.remove(msg.getKey());
				if(NodeUtilities.KPID >= 0) KoalaProtocol.REC_MSG.remove(msg.getKey());
				if(NodeUtilities.CPID >= 0) ChordProtocol.REC_MSG.remove(msg.getKey());
				
				
				entriesToRemove.add(msg.getKey());
//				nr++;

				
				String printstr = rm.getSentCycle()+""; 
				printstr +=	NodeUtilities.RPID >= 0 ? "\t"+rm.getTotalLatency() : "\t0";
				printstr += NodeUtilities.CPID >= 0 ? "\t"+cm.getTotalLatency() : "\t0";
				printstr += NodeUtilities.KPID >= 0 ? "\t"+km.getTotalLatency() : "\t0";
				
				printstr += NodeUtilities.RPID >= 0 ? "\t"+ rm.getHops() : "\t0"; 
				printstr += NodeUtilities.CPID >= 0 ? "\t"+ cm.getHops() : "\t0";
				printstr += NodeUtilities.KPID >= 0 ? "\t"+ km.getHops() : "\t0";
// 				printstr += renProtPid >= 0 ? "\t"+ rm.getHopCategory() + "\t"+ rm.getLatencyCategory(): "\t0\t0";
// 				printstr += renProtPid >= 0 ? "\t"+ rm.getPath(): "\t[]";
// 				printstr += chordProtPid >= 0 ? "\t"+ cm.getChordPath(): "\t[]";
// 				printstr += koaProtPid >= 0 ? "\t"+ km.getPath(): "\t[]";
				printstr += "\t"+ (nrInterDCMsg - lastMsg);
 				printstr += NodeUtilities.CPID >= 0 ? "\t"+ (ChordProtocol.FAIL - lastCFail): "\t0";
 				printstr += NodeUtilities.KPID >= 0 ? "\t"+ (KoalaProtocol.FAIL - lastKFail): "\t0";
 				
 				
 				lastMsg = nrInterDCMsg;
 				lastCFail = ChordProtocol.FAIL;
 				lastKFail = KoalaProtocol.FAIL;
 				msgToPrint.add(printstr);
 				
//				System.out.println(cm.getPath());
			}
				
		}


//		if(nr > 0){
//			if(toPrint.size() == 0){
//				toPrint.add("Size: " + Configuration.getInt("SIZE") + 
//						   ", DCs: " + Configuration.getInt("NR_DC") + 
//						   ", NodeXDC: " +Configuration.getInt("NR_NODE_PER_DC") + 
//						   ", Cycles: " + Configuration.getLong("simulation.endtime"));
//				toPrint.add("Latency");
////				toPrint.add("Hops");
//				
//			}
////			toPrint.add((double)kps/nr+" " + (double)rps/nr + " " + (double)cps/nr);
//			toPrint.add((double)kl/nr+" " + (double)rl/nr + " " + (double)cl/nr);
////			System.out.println(rtSize/nr);
//		}
		
		for(Integer rem: entriesToRemove)
			sentMgs.remove(rem);
		
		
	}
	
	private void reportRenater(){
		ArrayList<Integer> entriesToRemove = new ArrayList<Integer>();
		for(Map.Entry<Integer, Node> msg : sentMgs.entrySet()){
			if(RenaterProtocol.REC_MSG.containsKey(msg.getKey())){
				TopologyMessage rm = RenaterProtocol.REC_MSG.get(msg.getKey());
				renaterTotalLatency += rm.getLatency();
				renaterTotalLatency = PhysicalDataProvider.round(renaterTotalLatency);
				
				//do stuff 
				String ok = rm.getPath().toString().equals(rm.getPhysicalPathToString().toString()) ? " (ok) " : " (not ok) ";
				System.out.println("(R) "+rm.getID() + ": " + rm.getLatency() + " " + rm.getPath() + " " + rm.getPhysicalPathToString() + ok);
				System.out.println();
				RenaterProtocol.REC_MSG.remove(msg.getKey());
				entriesToRemove.add(msg.getKey());
			}
				
		}
		
		for(Integer rem: entriesToRemove)
			sentMgs.remove(rem);
	}
	
	
	public static void addSentMsg(int msgID, Node dest ){
		sentMgs.put(msgID, dest);
	}
	
	public static void countInter(){
		nrInterDCMsg++;
	}
	
	public static void countIntra(){
		nrIntraDCMsg++;
	}
	
	protected void flush(){
		pss = openFiles();
		for(String line : msgToPrint)
			pss[0].println(line);
		msgToPrint.clear();
		closeFiles(pss);
	}

	@Override
	protected void printGraph(PrintStream ps, int psIndex) {
		if(psIndex == 0) 
			for(String line : msgToPrint)
				ps.println(line);
		
	}
	
	@Override
	protected String getOutputFileBase() {
		return super.getOutputFileBase() +  "results/";
	}

	@Override
	protected String[] getOutputFileNames() {
		return new String[]{"resultsC"+Configuration.getString("CC")+"CH"+Configuration.getString("CHURN")};
	}
	
	
	
}
