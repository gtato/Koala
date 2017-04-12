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

	public FlatKoalaProtocol(String prefix) {
		super(prefix);
		nested = false;
	}

	@Override
	public void intializeMyNode(Node node, int pid) {
		super.intializeMyNode(node, pid);
		myNode = (KoalaNode) node.getProtocol(NodeUtilities.FKID);
		
	}
	
	
	protected Node getNodeFromID(String id)
	{
		return NodeUtilities.Nodes.get(NodeUtilities.FlatMap.get(id) );
	}
}
