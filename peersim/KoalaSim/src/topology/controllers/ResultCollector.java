package topology.controllers;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import chord.ChordProtocol;
import koala.FlatKoalaProtocol;
import koala.KoalaProtocol;
import koala.LeaderKoalaProtocol;
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
	private static final String PAR_FILTER= "filter";
	
	
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
	private static int nrHelpMsg = 0;

	private static int local=0, close=0, global=0, undefined=0;
	
	private static int flush = 0;
	private boolean ended = false;
	private static String filter;
	private String[] outfilenames;
//	private ArrayList<ArrayList<Integer>> aggregated = new ArrayList<ArrayList<Integer>>(); 
//	ArrayList<String> toPrint = new ArrayList<String>();
	ArrayList<String> msgToPrint = new ArrayList<String>();
//	ArrayList<String> helpToPrint = new ArrayList<String>();
	PrintStream[] pss;
	
	int lastMsg, lastHelp, lastCFail, lastKFail, lastFKFail, lastLKFail;
	public ResultCollector(String prefix) {
		super(prefix);
//		renProtPid = Configuration.getPid(prefix + "." + PAR_RENATER_PROTOCOL);
//		koaProtPid = Configuration.getPid(prefix + "." + PAR_KOALA_PROTOCOL, -1);
//		chordProtPid = Configuration.getPid(prefix + "." + PAR_CHORD_PROTOCOL, -1);
//		
//		step  = Configuration.getInt(prefix + ".step", 1);
		flush = Configuration.getInt(prefix + "." +PAR_FLUSH, 100);
		filter = Configuration.getString(prefix + "."+ PAR_FILTER, "local close global");
//		plotScript = "gnuplot/plotResults.plt";
		String additional = NodeUtilities.FILENAME_ADDITIONAL.length() > 0 ? NodeUtilities.FILENAME_ADDITIONAL +".": "";
		
		
		outfilenames = new String[]{getFileName(additional) 
									};//, "help.C"+NodeUtilities.C+".RC"+NodeUtilities.RAND_C+"."+Configuration.getString("NR_DC")+"x"+Configuration.getString("NR_NODE_PER_DC")+".CCL"+NodeUtilities.getStringCycles()+".COL"+NodeUtilities.NR_COLLABORATORS+".T"+(int)NodeUtilities.COLABORATIVE_THRESHOLD+"."};
		deleteFiles();
		lastMsg = lastHelp = lastCFail = lastKFail = 0;
		msgToPrint.add("cycle\trlat\tclat\tklat\trhop\tchop\tkhop\tmsgs\trpath\tcpath\tkpath\tcfail\tkfail");
	}

	private String getFileName(String additional){
		String file = "results";
		if(NodeUtilities.C > 0)
			file += ".C"+NodeUtilities.C;
		if(NodeUtilities.RAND_C > 0)
			file += ".RC"+NodeUtilities.RAND_C;
		if(NodeUtilities.VICINITY_C > 0)
			file += ".VC"+NodeUtilities.VICINITY_C;
		if(NodeUtilities.APPLICATION_C > 0)
			file += ".AC"+NodeUtilities.APPLICATION_C;
		if(NodeUtilities.NR_COLLABORATORS > 0){
			file +=".COL"+NodeUtilities.NR_COLLABORATORS+".T"+(int)NodeUtilities.COLABORATIVE_THRESHOLD;
		}
		
		file += "."+Configuration.getString("NR_DC")+"x"+Configuration.getString("NR_NODE_PER_DC");
		file += ".CCL"+NodeUtilities.getStringCycles()+"."+additional;
		return file;
	}
	
	@Override
	public boolean execute() {
		updateGraph();
		
		
		if(NodeUtilities.KPID > 0 || NodeUtilities.CPID > 0 || NodeUtilities.FKPID > 0 || NodeUtilities.LKPID > 0)
			compare();
		else
			reportRenater();
//		System.out.println(CommonState.getTime() + " - " + CommonState.getEndTime());
		if(CommonState.getTime() == CommonState.getEndTime()-1 && !ended){
			System.out.println("Inter: " + nrInterDCMsg + " Intra: " + nrIntraDCMsg + " Total: " + (nrInterDCMsg + nrIntraDCMsg) + " Help: " + ((double)nrHelpMsg/nrInterDCMsg)*100 + " of interDCs");
			System.out.println("Local: " + local + " Close: " + close + " global: " + global);
			if(NodeUtilities.CPID >= 0) System.out.println("CHORD: success: " + ChordProtocol.SUCCESS + " fails: " + ChordProtocol.FAIL);
			if(NodeUtilities.KPID >= 0) System.out.println("KOALA: success: " + KoalaProtocol.SUCCESS + " fails: " + KoalaProtocol.FAIL + " internal fails: " + KoalaProtocol.INT_FAIL);
			if(NodeUtilities.FKPID >= 0) System.out.println("FLAT KOALA: success: " + FlatKoalaProtocol.SUCCESS + " fails: " + FlatKoalaProtocol.FAIL + " internal fails: " + FlatKoalaProtocol.INT_FAIL);
			if(NodeUtilities.LKPID >= 0) System.out.println("LEADER KOALA: success: " + LeaderKoalaProtocol.SUCCESS + " fails: " + LeaderKoalaProtocol.FAIL + " internal fails: " + FlatKoalaProtocol.INT_FAIL);
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
		TopologyMessage rm=null,km=null,fkm=null,lkm=null,cm=null;
		for(Map.Entry<Integer, Node> msg : sentMgs.entrySet()){
//			RenaterProtocol rp = (RenaterProtocol) msg.getValue().getProtocol(renProtPid);
//			KoalaProtocol kp = (KoalaProtocol) msg.getValue().getProtocol(koaProtPid);
//			ChordProtocol cp = (ChordProtocol) msg.getValue().getProtocol(chordProtPid);
			
			if((RenaterProtocol.REC_MSG.containsKey(msg.getKey()) || NodeUtilities.RPID < 0)
			&& (KoalaProtocol.REC_MSG.containsKey(msg.getKey()) || NodeUtilities.KPID < 0)
			&& (ChordProtocol.REC_MSG.containsKey(msg.getKey()) || NodeUtilities.CPID < 0)
			&& (FlatKoalaProtocol.REC_MSG.containsKey(msg.getKey()) || NodeUtilities.FKPID < 0)
			&& (LeaderKoalaProtocol.REC_MSG.containsKey(msg.getKey()) || NodeUtilities.LKPID < 0)
			){
			
				if(NodeUtilities.RPID >= 0) rm = RenaterProtocol.REC_MSG.get(msg.getKey());
				if(NodeUtilities.KPID >= 0) km = KoalaProtocol.REC_MSG.get(msg.getKey());
				if(NodeUtilities.FKPID >= 0) fkm = FlatKoalaProtocol.REC_MSG.get(msg.getKey());
				if(NodeUtilities.LKPID >= 0) lkm = LeaderKoalaProtocol.REC_MSG.get(msg.getKey());
				if(NodeUtilities.CPID >= 0) cm = ChordProtocol.REC_MSG.get(msg.getKey());
				
				
				if(rm.getCategory() == TopologyMessage.CAT_LOCAL) local++;
				if(rm.getCategory() == TopologyMessage.CAT_CLOSE) close++;
				if(rm.getCategory() == TopologyMessage.CAT_GLOBAL) global++;
				if(rm.getCategory() == TopologyMessage.CAT_UNDEFINED) undefined++;
				
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
				if(NodeUtilities.FKPID >= 0) FlatKoalaProtocol.REC_MSG.remove(msg.getKey());
				if(NodeUtilities.LKPID >= 0) LeaderKoalaProtocol.REC_MSG.remove(msg.getKey());
				if(NodeUtilities.CPID >= 0) ChordProtocol.REC_MSG.remove(msg.getKey());
				
				
				entriesToRemove.add(msg.getKey());
//				nr++;

				
				String printstr = rm.getSentCycle()+""; //0 
				printstr +=	NodeUtilities.RPID >= 0 ? "\t"+rm.getTotalLatency() : "\t0"; //1
				printstr += NodeUtilities.CPID >= 0 ? "\t"+cm.getTotalLatency() : "\t0"; //2
				printstr += NodeUtilities.KPID >= 0 ? "\t"+km.getTotalLatency() : "\t0"; //3
				printstr += NodeUtilities.FKPID >= 0 ? "\t"+fkm.getTotalLatency() : "\t0"; //4
				printstr += NodeUtilities.LKPID >= 0 ? "\t"+lkm.getTotalLatency() : "\t0"; //5
				
				printstr += NodeUtilities.RPID >= 0 ? "\t"+ rm.getLocalHops() : "\t0"; //6
				printstr += NodeUtilities.RPID >= 0 ? "\t"+ rm.getGlobalHops() : "\t0"; //7
				printstr += NodeUtilities.CPID >= 0 ? "\t"+ cm.getLocalHops() : "\t0"; //8
				printstr += NodeUtilities.CPID >= 0 ? "\t"+ cm.getGlobalHops() : "\t0"; //9
				printstr += NodeUtilities.KPID >= 0 ? "\t"+ km.getLocalHops() : "\t0"; //10
				printstr += NodeUtilities.KPID >= 0 ? "\t"+ km.getGlobalHops() : "\t0"; //11
				printstr += NodeUtilities.FKPID >= 0 ? "\t"+ fkm.getLocalHops() : "\t0"; //12
				printstr += NodeUtilities.FKPID >= 0 ? "\t"+ fkm.getGlobalHops() : "\t0"; //13
				printstr += NodeUtilities.LKPID >= 0 ? "\t"+ lkm.getLocalHops() : "\t0"; //14
				printstr += NodeUtilities.LKPID >= 0 ? "\t"+ lkm.getGlobalHops() : "\t0"; //15

//				printstr += renProtPid >= 0 ? "\t"+ rm.getHopCategory() + "\t"+ rm.getLatencyCategory(): "\t0\t0";
// 				printstr += NodeUtilities.RPID >= 0 ? "\t"+ rm.pathToStrArray(): "\t[]";
// 				printstr += NodeUtilities.CPID >= 0 ? "\t"+ cm.getChordPath(): "\t[]";
// 				printstr += NodeUtilities.KPID >= 0 ? "\t"+ km.pathToStrArray(): "\t[]";
// 				printstr += NodeUtilities.FKPID >= 0 ? "\t"+ fkm.pathToStrArray(): "\t[]";
// 				printstr += NodeUtilities.LKPID >= 0 ? "\t"+ lkm.pathToStrArray(): "\t[]";
// 				printstr += NodeUtilities.KPID >= 0 ? "\t"+ km.getPhysicalPathToStrArray(): "\t[]";
				
 				printstr += "\t"+ (nrInterDCMsg - lastMsg); //16
 				printstr += NodeUtilities.CPID >= 0 ? "\t"+ (ChordProtocol.FAIL - lastCFail): "\t0"; //17
 				printstr += NodeUtilities.KPID >= 0 ? "\t"+ (KoalaProtocol.FAIL - lastKFail): "\t0"; //18
 				printstr += NodeUtilities.FKPID >= 0 ? "\t"+ (FlatKoalaProtocol.FAIL - lastFKFail): "\t0"; //19
 				printstr += NodeUtilities.LKPID >= 0 ? "\t"+ (LeaderKoalaProtocol.FAIL - lastLKFail): "\t0"; //20
 				
 				
// 				helpToPrint.add((nrHelpMsg-lastHelp) + "\t" + (nrInterDCMsg - lastMsg));
 				
 				lastMsg = nrInterDCMsg;
 				lastHelp = nrHelpMsg;
 				lastCFail = ChordProtocol.FAIL;
 				lastKFail = KoalaProtocol.FAIL;
 				lastFKFail = FlatKoalaProtocol.FAIL;
 				lastLKFail = LeaderKoalaProtocol.FAIL;
 				
 				if(rm.getCategory() == TopologyMessage.CAT_LOCAL && !filter.contains("local")) continue;
				if(rm.getCategory() == TopologyMessage.CAT_CLOSE && !filter.contains("close")) continue;
				if(rm.getCategory() == TopologyMessage.CAT_GLOBAL && !filter.contains("global")) continue;
				
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
				String ok = rm.getPath().toString().equals(rm.getPhysicalPathToStrArray().toString()) ? " (ok) " : " (not ok) ";
				System.out.println("(R) "+rm.getID() + ": " + rm.getLatency() + " " + rm.getPath() + " " + rm.getPhysicalPathToStrArray() + ok);
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
	
	public static void countHelp(){
		nrHelpMsg++;
	}
	
	protected void flush(){
		pss = openFiles();
		for(String line : msgToPrint)
			pss[0].println(line);
//		for(String line : helpToPrint)
//			pss[1].println(line);
		msgToPrint.clear();
//		helpToPrint.clear();
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

//	@Override
//	protected String[] getOutputFileNames() {
//		return new String[]{"resultsC"+NodeUtilities.C+"CH"+Configuration.getString("CHURN")};
//	}
	@Override
	protected String[] getOutputFileNames() {
		return outfilenames;
	}
	
}
