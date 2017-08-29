package messaging;

import java.util.ArrayList;

import koala.KoalaNeighbor;
import topology.TopologyPathNode;

public class TopologyMessageReplica {
	private TopologyPathNode sender;
	private ArrayList<TopologyPathNode> path = new ArrayList<TopologyPathNode>();
	private ArrayList<KoalaNeighbor> piggyback = new ArrayList<KoalaNeighbor>();
	private double latency;
	
//	private static TopologyMessageReplica replica = new TopologyMessageReplica();
//	
//	public static TopologyMessageReplica getReplica(TopologyMessage msg){
//		replica.sender = msg.getLastSender();
//		for(TopologyPathNode tpn : msg.getPath())
//			replica.path.add(tpn.copy());
//		
//		if(msg instanceof KoalaMessage){
//			KoalaMessage kmsg = (KoalaMessage)msg;
//			for(int i = 0; i < kmsg.getPiggyBack().size(); i++)
//				replica.piggyback.add(kmsg.getPiggyBack().get(i).copy());
//		}
//		replica.latency = msg.getLatency();
//		return replica;
//	}
	
	public void updateReplica(TopologyMessage msg){
		sender = msg.getLastSender();
		path.clear(); piggyback.clear();
		for(TopologyPathNode tpn : msg.getPath())
			path.add(tpn.cclone());
		
		if(msg instanceof KoalaMessage){
			KoalaMessage kmsg = (KoalaMessage)msg;
			for(int i = 0; i < kmsg.getPiggyBack().size(); i++)
				piggyback.add(kmsg.getPiggyBack().get(i).cclone());
		}
		latency = msg.getLatency();
	}

	public TopologyMessageReplica(){}
	public TopologyMessageReplica(TopologyMessage msg){
		sender = msg.getLastSender();
		for(TopologyPathNode tpn : msg.getPath())
			path.add(tpn.cclone());
		
		if(msg instanceof KoalaMessage){
			KoalaMessage kmsg = (KoalaMessage)msg;
			for(int i = 0; i < kmsg.getPiggyBack().size(); i++)
				piggyback.add(kmsg.getPiggyBack().get(i).cclone());
		}
		latency = msg.getLatency();
	}
	
	
	public TopologyPathNode getSender() {
		return sender;
	}

	
	public ArrayList<TopologyPathNode> getPath() {
		return path;
	}

	
	public ArrayList<KoalaNeighbor> getPiggyback() {
		return piggyback;
	}

	public double getLatency() {
		return latency;
	}
}
