package koala;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import messaging.KoalaMessage;
import messaging.KoalaMsgContent;
import messaging.KoalaNGNMsgContent;
import messaging.KoalaNLNMsgContent;
import messaging.KoalaRHMsgContent;
import messaging.KoalaRTMsgConent;
import messaging.KoalaRouteMsgContent;
import messaging.TopologyMessage;
import peersim.config.Configuration;
import peersim.core.CommonState;

import peersim.core.Network;
import peersim.core.Node;
import topology.TopologyProtocol;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;


public class FlatKoalaProtocol extends KoalaProtocol{

	
	public static HashMap<Integer, TopologyMessage> REC_MSG =  new HashMap<Integer, TopologyMessage>();
	public static int SUCCESS = 0;
	public static int FAIL = 0;
	public static int INT_FAIL = 0;
	
	public FlatKoalaProtocol(String prefix) {
		super(prefix);
		nested = false;
		helpSupported = false;
	}

	@Override
	public void intializeMyNode(Node node, int pid) {
		super.intializeMyNode(node, pid);
		myNode = (KoalaNode) node.getProtocol(NodeUtilities.FKID);
	}

	
	
	protected void onSuccess(TopologyMessage msg) {
		msg.setReceivedCycle(CommonState.getTime());
		REC_MSG.put(msg.getID(), msg);
		SUCCESS++;
//		System.out.println(msg.getID()+  " ("+this.getClass().getName() +") "+ myNode.getID()+" got a message through: ["+msg.pathToString()+"] with latency: " +msg.getLatency());
	}
	
	protected void onFail(TopologyMessage msg){
		KoalaMessage kmsg = (KoalaMessage) msg;
		String failmsg = "FLAT failed to sent from " + kmsg.getFirstSender();
		if(msg.getContent() instanceof KoalaRouteMsgContent){
			String nid = ((KoalaRouteMsgContent)msg.getContent()).getNode().getSID();
			failmsg += " to " + nid;
			FAIL++;
		}else
			INT_FAIL++;
		
				
		System.out.println(failmsg);
		
	}
}
