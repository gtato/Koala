package messaging;

import java.util.ArrayList;

import koala.KoalaNeighbor;
import topology.TopologyPathNode;

public class TopologyMessageReplica {
	private TopologyPathNode sender;
	private ArrayList<TopologyPathNode> path = new ArrayList<TopologyPathNode>();
	private ArrayList<KoalaNeighbor> piggyback = new ArrayList<KoalaNeighbor>();
	private double latency;
	public TopologyMessageReplica(TopologyMessage msg){
		sender = msg.getLastSender();
		for(TopologyPathNode tpn : msg.getPath())
			path.add(tpn.copy());
		
		if(msg instanceof KoalaMessage){
			KoalaMessage kmsg = (KoalaMessage)msg;
			for(int i = 0; i < kmsg.getPiggyBack().size(); i++)
				piggyback.add(kmsg.getPiggyBack().get(i).copy());
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
