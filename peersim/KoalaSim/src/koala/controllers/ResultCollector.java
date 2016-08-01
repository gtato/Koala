package koala.controllers;

import java.awt.color.CMMException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import messaging.KoalaMessage;
import koala.KoalaProtocol;
import koala.RenaterProtocol;
import koala.TopologyProtocol;
import koala.utility.PhysicalDataProvider;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.reports.GraphObserver;

public class ResultCollector extends GraphObserver {

	private static final String PAR_KOALA_PROTOCOL= "kprotocol";
	private static final String PAR_RENATER_PROTOCOL= "protocol";
	
	private final int renProtPid;
	private final int koaProtPid;
	
	private static HashMap<Integer, Node> sentMgs = new HashMap<Integer, Node>(); 
	
	private double renaterTotalLatency = 0;
	private double koalaTotalLatency = 0;
	
	private static int nrInterDCMsg = 0;
	private static int nrIntraDCMsg = 0;
	
	public ResultCollector(String prefix) {
		super(prefix);
		renProtPid = Configuration.getPid(prefix + "." + PAR_RENATER_PROTOCOL);
		koaProtPid = Configuration.getPid(prefix + "." + PAR_KOALA_PROTOCOL, -1);
	}

	@Override
	public boolean execute() {
		updateGraph();
		if(koaProtPid > 0)
			compare();
		else
			reportRenater();
		if(CommonState.getTime() == CommonState.getEndTime()-1)
			System.out.println("Inter: " + nrInterDCMsg + " Intra: " + nrIntraDCMsg + " Total: " + (nrInterDCMsg + nrIntraDCMsg));
		return false;
	}
	
	private void compare(){
		ArrayList<Integer> entriesToRemove = new ArrayList<Integer>();
		for(Map.Entry<Integer, Node> msg : sentMgs.entrySet()){
			RenaterProtocol rp = (RenaterProtocol) msg.getValue().getProtocol(renProtPid);
			KoalaProtocol kp = (KoalaProtocol) msg.getValue().getProtocol(koaProtPid);
			
			if(rp.hasReceivedMsg(msg.getKey()) && kp.hasReceivedMsg(msg.getKey())){
				KoalaMessage rm = rp.getReceivedMsg(msg.getKey());
				KoalaMessage km = kp.getReceivedMsg(msg.getKey());
				
				renaterTotalLatency += rm.getLatency();
				koalaTotalLatency += km.getLatency();
				renaterTotalLatency = PhysicalDataProvider.round(renaterTotalLatency);
				koalaTotalLatency = PhysicalDataProvider.round(koalaTotalLatency);
				//do stuff 
				String ok = rm.getPath().toString().equals(rm.getPhysicalPathToString().toString()) ? " (ok) " : " (not ok) ";
				System.out.println("(R) "+rm.getID() + ": " + rm.getTotalLatency() + " " + rm.getPath() + " " + rm.getPhysicalPathToString() + ok);
				System.out.println("(K) "+km.getID() + ": " + km.getTotalLatency() + " " + km.getPath() + " " + km.getPhysicalPathToString());
				System.out.println("(T) "+rm.getID() + ": " + ((double) km.getTotalLatency() / rm.getTotalLatency()) + 
									" " + rm.getPath().size() + " " +km.getPath().size() +
									" " +km.getPhysicalPathToString().size() + " " + PhysicalDataProvider.round((double) koalaTotalLatency / renaterTotalLatency));
				System.out.println();
				rp.removeReceivedMsg(msg.getKey());
				kp.removeReceivedMsg(msg.getKey());
				entriesToRemove.add(msg.getKey());
			}
				
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
	
//	public static class MsgEntry{
//		Node dest;
//		int msgID;
//		
//		public MsgEntry(Node tp, int msgID){
//			this.dest = tp;
//			this.msgID = msgID;
//		}
//
//		public Node getDestination() {
//			return dest;
//		}
//
//		public void setDestination(Node dest) {
//			this.dest = dest;
//		}
//
//		public int getMsgID() {
//			return msgID;
//		}
//
//		public void setMsgID(int msg) {
//			this.msgID = msg;
//		}
//	}

}
