package topology.controllers;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import chord.ChordProtocol;
import koala.KoalaProtocol;
import messaging.KoalaMessage;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import renater.RenaterProtocol;
import utilities.PhysicalDataProvider;

public class ResultCollector extends NodeObserver {

	private static final String PAR_CHORD_PROTOCOL= "cprotocol";
	private static final String PAR_KOALA_PROTOCOL= "kprotocol";
	private static final String PAR_RENATER_PROTOCOL= "protocol";
	
	
	private final int renProtPid;
	private final int koaProtPid;
	private final int chordProtPid;
	
	private static HashMap<Integer, Node> sentMgs = new HashMap<Integer, Node>(); 
	
	private double renaterTotalLatency = 0;
	private double koalaTotalLatency = 0;
	private double chordTotalLatency = 0;
	
	private static int nrInterDCMsg = 0;
	private static int nrIntraDCMsg = 0;
	private int step;
	private ArrayList<ArrayList<Integer>> aggregated = new ArrayList<ArrayList<Integer>>(); 
	ArrayList<String> toPrint = new ArrayList<String>();
	
	public ResultCollector(String prefix) {
		super(prefix);
		renProtPid = Configuration.getPid(prefix + "." + PAR_RENATER_PROTOCOL);
		koaProtPid = Configuration.getPid(prefix + "." + PAR_KOALA_PROTOCOL, -1);
		chordProtPid = Configuration.getPid(prefix + "." + PAR_CHORD_PROTOCOL, -1);
		
		step  = Configuration.getInt(prefix + ".step", 1);
		plotScript = "gnuplot/plotResults.plt";
	}

	@Override
	public boolean execute() {
		updateGraph();
		
		if(koaProtPid > 0)
			compare();
		else
			reportRenater();
		if(CommonState.getTime() == CommonState.getEndTime()-1){
			System.out.println("Inter: " + nrInterDCMsg + " Intra: " + nrIntraDCMsg + " Total: " + (nrInterDCMsg + nrIntraDCMsg));
		
			graphToFile();
			plotIt();
		}
		return false;
	}
	
	private void compare(){
		ArrayList<Integer> entriesToRemove = new ArrayList<Integer>();
		
		int rps, kps, cps, nr; rps = kps = cps = nr = 0;
		int rl, kl, cl; rl = kl = cl = 0;
		
		for(Map.Entry<Integer, Node> msg : sentMgs.entrySet()){
			RenaterProtocol rp = (RenaterProtocol) msg.getValue().getProtocol(renProtPid);
			KoalaProtocol kp = (KoalaProtocol) msg.getValue().getProtocol(koaProtPid);
			ChordProtocol cp = (ChordProtocol) msg.getValue().getProtocol(chordProtPid);
			
			if(rp.hasReceivedMsg(msg.getKey()) 
			&& kp.hasReceivedMsg(msg.getKey())
			&& cp.hasReceivedMsg(msg.getKey())
					){
				KoalaMessage rm = rp.getReceivedMsg(msg.getKey());
				KoalaMessage km = kp.getReceivedMsg(msg.getKey());
				KoalaMessage cm = cp.getReceivedMsg(msg.getKey());
				
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

				rps += rm.getPath().size(); cps += cm.getPath().size(); kps += km.getPath().size(); 
				rl += rm.getTotalLatency(); cl += cm.getTotalLatency(); kl += km.getTotalLatency();
				
				rp.removeReceivedMsg(msg.getKey());
				kp.removeReceivedMsg(msg.getKey());
				cp.removeReceivedMsg(msg.getKey());
				entriesToRemove.add(msg.getKey());
				nr++;
			}
				
		}
		
		if(nr > 0){
			if(toPrint.size() == 0)
				toPrint.add("Latency");
//				toPrint.add("Hops");
//			toPrint.add((double)kps/nr+" " + (double)rps/nr + " " + (double)cps/nr);
			toPrint.add((double)kl/nr+" " + (double)rl/nr + " " + (double)cl/nr);
		}
		
		for(Integer rem: entriesToRemove)
			sentMgs.remove(rem);
		
		
	}
	
	private void reportRenater(){
		ArrayList<Integer> entriesToRemove = new ArrayList<Integer>();
		for(Map.Entry<Integer, Node> msg : sentMgs.entrySet()){
			RenaterProtocol rp = (RenaterProtocol) msg.getValue().getProtocol(renProtPid);
			if(rp.hasReceivedMsg(msg.getKey())){
				KoalaMessage rm = rp.getReceivedMsg(msg.getKey());
				renaterTotalLatency += rm.getLatency();
				renaterTotalLatency = PhysicalDataProvider.round(renaterTotalLatency);
				
				//do stuff 
				String ok = rm.getPath().toString().equals(rm.getPhysicalPathToString().toString()) ? " (ok) " : " (not ok) ";
				System.out.println("(R) "+rm.getID() + ": " + rm.getLatency() + " " + rm.getPath() + " " + rm.getPhysicalPathToString() + ok);
				System.out.println();
				rp.removeReceivedMsg(msg.getKey());
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

	@Override
	protected void printGraph(PrintStream ps) {
		for(String line : toPrint){
			ps.println(line);
		}
			
		
	}
	
}
