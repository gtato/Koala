package koala;

import java.util.ArrayDeque;

import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import koala.utility.KoalaJsonParser;
import messaging.KoalaMessage;

public abstract class TopologyProtocol implements CDProtocol {
	protected ArrayDeque<String> queue;
	protected KoalaNode myNode = null;
//	protected TopologyProtocol me = null;
	
	protected int koalaNodePid = -1;
	protected int myPid = -1;

	protected boolean joined;
	
	public TopologyProtocol(String prefix) {}
	
	public Object clone() {
		TopologyProtocol inp = null;
        try {
            inp = (TopologyProtocol) super.clone();
            inp.queue = new ArrayDeque<String>();
        } catch (CloneNotSupportedException e) {
        } // never happens
        return inp;
    }
	
	public abstract boolean hasJoined();
	

	public void setJoined(boolean joined) {
		this.joined = joined;
	}

	
	public boolean hasEmptyQueue(){
		return queue.size() == 0;
	}
	
	public void registerMsg(KoalaMessage msg){
		String msgStr = KoalaJsonParser.toJson(msg);
		queue.add(msgStr);
	}

	public void receive()
	{
		if(queue.size() == 0)
			return;
		
		String msgStr = queue.remove();
		KoalaMessage msg = KoalaJsonParser.jsonToObject(msgStr, KoalaMessage.class);
		switch(msg.getType()){
			case KoalaMessage.RT:
				onRoutingTable(msg);
				break;
			case KoalaMessage.ROUTE:
				onRoute(msg);
				break;
			case KoalaMessage.NGN:
				onNewGlobalNeighbours(msg);
				break;
			case KoalaMessage.JOIN:
				join();
				break;
		}
	}
	protected abstract void join();

	protected abstract void onNewGlobalNeighbours(KoalaMessage msg);

	protected abstract void onRoute(KoalaMessage msg);

	protected abstract void onRoutingTable(KoalaMessage msg);

	@Override
	public void nextCycle(Node node, int protocolID) {
		myPid = protocolID;
		koalaNodePid = FastConfig.getLinkable(protocolID);
		myNode = (KoalaNode) (Linkable) node.getProtocol(koalaNodePid);
//		me = (TopologyProtocol) (Linkable) node.getProtocol(myPid);
		//System.out.print(me.getID() + "  ");
		receive();
	}
	


	
}
