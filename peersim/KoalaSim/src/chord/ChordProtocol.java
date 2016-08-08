/**
 * 
 */
package chord;

import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;
import topology.TopologyProtocol;

import java.math.*;
import java.util.ArrayList;

import koala.KoalaNeighbor;
import koala.KoalaNode;
import messaging.KoalaMessage;
import messaging.KoalaMsgContent;
import messaging.KoalaRouteMsgContent;

/**
 * @author Andrea
 * 
 */
public class ChordProtocol extends TopologyProtocol {

	ChordNode myNode;

	/**
	 * 
	 */
	public ChordProtocol(String prefix) {
		super(prefix);
//		this.prefix = prefix;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see peersim.edsim.EDProtocol#processEvent(peersim.core.Node, int,
	 *      java.lang.Object)
	 */
//	public void processEvent(Node node, int pid, Object event) {
//		// processare le richieste a seconda della routing table del nodo
//		p.pid = pid;
//		currentNode = node.getIndex();
//		if (event.getClass() == LookUpMessage.class) {
//			LookUpMessage message = (LookUpMessage) event;
//			message.increaseHopCounter();
//			BigInteger target = message.getTarget();
//			Transport t = (Transport) node.getProtocol(p.tid);
//			Node n = message.getSender();
//			if (target == ((ChordProtocol) node.getProtocol(pid)).chordId) {
//				// mandare mess di tipo final
//				t.send(node, n, new FinalMessage(message.getHopCounter()), pid);
//			}
//			if (target != ((ChordProtocol) node.getProtocol(pid)).chordId) {
//				// funzione lookup sulla fingertabable
//				Node dest = find_successor(target);
//				if (dest.isUp() == false) {
//					do {
//						varSuccList = 0;
//						stabilize(node);
//						stabilizations++;
//						fixFingers();
//						dest = find_successor(target);
//					} while (dest.isUp() == false);
//				}
//				if (dest.getID() == successorList[0].getID()
//						&& (target.compareTo(((ChordProtocol) dest
//								.getProtocol(p.pid)).chordId) < 0)) {
//					fails++;
//				} else {
//					t.send(message.getSender(), dest, message, pid);
//				}
//			}
//		}
//		if (event.getClass() == FinalMessage.class) {
//			FinalMessage message = (FinalMessage) event;
//			lookupMessage.add(message.getHopCounter());
//		}
//	}

	public Object clone() {
		ChordProtocol cp = (ChordProtocol) super.clone();
		return cp;
	}
	
	@Override
	public void intializeMyNode(Node node, int pid) {
		super.intializeMyNode(node, pid);
		myNode = (ChordNode) (Linkable) node.getProtocol(linkPid);
		
	}

	@Override
	public void join() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void checkPiggybacked(KoalaMessage msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getProtocolName() {
		return "chord";
	}

	@Override
	protected void onNewGlobalNeighbours(KoalaMessage msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onRoute(KoalaMessage msg) {
		String tid = ((KoalaRouteMsgContent)msg.getContent()).getId();
		BigInteger target = new BigInteger(tid);
		if (!tid.equals(myNode.chordId.toString())){
			ChordNode dest = myNode.findUpSuccessor(target);
			if(dest != null)
				send(dest.getID(), msg);
		} else{
			onReceivedMsg(msg);
		}
			
			
		
		
	}

	@Override
	protected void onRoutingTable(KoalaMessage msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onLongLink(KoalaMessage msg) {
		// TODO Auto-generated method stub
		
	}
}
