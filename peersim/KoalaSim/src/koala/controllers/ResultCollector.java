package koala.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import messaging.KoalaMessage;
import koala.KoalaProtocol;
import koala.RenaterProtocol;
import koala.TopologyProtocol;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.reports.GraphObserver;

public class ResultCollector extends GraphObserver {

	private static final String PAR_KOALA_PROTOCOL= "kprotocol";
	private static final String PAR_RENATER_PROTOCOL= "protocol";
	
	private final int renProtPid;
	private final int koaProtPid;
	
	private static HashMap<Integer, Node> sentMgs = new HashMap<Integer, Node>(); 
	
	public ResultCollector(String prefix) {
		super(prefix);
		renProtPid = Configuration.getPid(prefix + "." + PAR_RENATER_PROTOCOL);
		koaProtPid = Configuration.getPid(prefix + "." + PAR_KOALA_PROTOCOL, -1);
	}

	@Override
	public boolean execute() {
		updateGraph();
		ArrayList<Integer> entriesToRemove = new ArrayList<Integer>();
		for(Map.Entry<Integer, Node> msg : sentMgs.entrySet()){
			RenaterProtocol rp = (RenaterProtocol) msg.getValue().getProtocol(renProtPid);
			KoalaProtocol kp = (KoalaProtocol) msg.getValue().getProtocol(koaProtPid);
			
			if(rp.hasReceivedMsg(msg.getKey()) && kp.hasReceivedMsg(msg.getKey())){
				KoalaMessage rm = rp.getReceivedMsg(msg.getKey());
				KoalaMessage km = kp.getReceivedMsg(msg.getKey());
				
				//do stuff 
				System.out.println(rm.getID() + ": " + rm.getLatency() + " " + rm.getPath());
				System.out.println(km.getID() + ": " + km.getLatency() + " " + km.getPath());
				System.out.println();
				//rp.removeReceivedMsg(msg.getKey());
				//kp.removeReceivedMsg(msg.getKey());
				entriesToRemove.add(msg.getKey());
			}
				
		}
		
		
		for(Integer rem: entriesToRemove)
			sentMgs.remove(rem);
		
		return false;
	}
	
	public static void addSentMsg(int msgID, Node dest ){
		sentMgs.put(msgID, dest);
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
