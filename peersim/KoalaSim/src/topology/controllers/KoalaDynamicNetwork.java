package topology.controllers;


import java.util.ArrayList;
import java.util.Collections;

import chord.ChordNode;
import koala.KoalaNode;
import koala.KoalaProtocol;
import koala.initializers.KoalaInitializer;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.DynamicNetwork;
import peersim.dynamics.NodeInitializer;
import renater.RenaterNode;
import topology.TopologyNode;
import utilities.NodeUtilities;

public class KoalaDynamicNetwork implements Control{

	public static final String MOD_RAND = "rand";
	public static final String MOD_STAND = "stand";
	
	private static final String PAR_INIT = "init";
	private static final String PAR_MIN = "minsize";
	private static final String PAR_ADD = "add";
	private static final String PAR_REM = "rem";
	private static final String PAR_MODE = "mode";
	private static final String PAR_ALT = "alt";
	protected final double add;
	protected final double rem;
	protected final int minsize;
	protected final String mode;
	protected final boolean alt;
	
	protected final NodeInitializer[] inits;
	
	public KoalaDynamicNetwork(String prefix) {
		add = Configuration.getDouble(prefix + "." + PAR_ADD,0);
		rem = Configuration.getDouble(prefix + "." + PAR_REM,0);
		minsize = Configuration.getInt(prefix + "." + PAR_MIN, 0);
		mode = Configuration.getString(prefix + "." + PAR_MODE, MOD_STAND);
		alt = Configuration.getBoolean(prefix + "." + PAR_ALT, false);
		Object[] tmp = Configuration.getInstanceArray(prefix + "." + PAR_INIT);
		inits = new NodeInitializer[tmp.length];
		for (int i = 0; i < tmp.length; ++i) {
			//System.out.println("Inits " + tmp[i]);
			inits[i] = (NodeInitializer) tmp[i];
		}
	}

//	protected void remove(int n){
//		if(n==0) return;
//		ArrayList<Integer> inx = new ArrayList<Integer>();
//		
//		for(int i = 0; i < Network.size(); i++)
//			if(Network.get(i).isUp())
//				inx.add(i);
//		Collections.shuffle(inx, CommonState.r);
//		
//		int realMinSize = (int) (Network.size() * (double)minsize/100);
//		int remove = (int) Math.min(inx.size(), Math.abs(add));
//		if(realMinSize > inx.size() + add)
//			remove = inx.size() - realMinSize;
//		
//		for(int i=0; i < remove; i++){
//			int index = inx.get(i);
//			Node node = Network.get(index); 
//			node.setFailState(Fallible.DOWN);
//			RenaterNode rn = (RenaterNode)node.getProtocol(NodeUtilities.RID);
//
//			System.out.println("Node " + rn.getID() + " died");
//		}
//	}


	
	
	@Override
	public boolean execute() {
		if (!isRand() && add == 0 && rem == 0) return false;
		
//		int nrUps = NodeUtilities.UPS.size();
//		int nrDowns = lat ? NodeUtilities.DOWNS.size();
		
		int realMinSize = (int) (Network.size() * (double)minsize/100);
				
		int toAdd = 0 , toRemove = 0;
		
		if(isRand()){
			toAdd = CommonState.r.nextInt(2);
			toRemove = 1-toAdd;
		}else{
			toAdd = add > 0 ? (int) add : 0;
//			toRemove = add < 0 ? (int) (int)-add : 0;
			toRemove = rem > 0 ? (int) rem : 0;
		}
		
		if(toAdd > 0 && toAdd > NodeUtilities.DOWNS.size())
			toAdd = NodeUtilities.DOWNS.size();
		
		if(toRemove > 0 && realMinSize > NodeUtilities.UPS.size() - toRemove)
			toRemove = NodeUtilities.UPS.size() - realMinSize;
		
		int upind = alt ? -1:0;
		ArrayList<Node> toUp = NodeUtilities.getUniformRandNodes(upind, toAdd);
		ArrayList<Node> toDown = NodeUtilities.getUniformRandNodes(1, toRemove);
			
		
		for(int i=0; i < toAdd; i++){
			Node node = toUp.get(i);
			NodeUtilities.up(node);
			TopologyNode tn;
			if(NodeUtilities.CID >= 0)
				tn = (ChordNode)node.getProtocol(NodeUtilities.CID);
			else
				tn = (KoalaNode)node.getProtocol(NodeUtilities.FKID);
			System.out.println("Node " + tn + " is in da house");
			
			for (int j = 0; j < inits.length; ++j) 
				inits[j].initialize(node);
		}
		
		
		for(int i=0; i < toRemove; i++){
			Node node = toDown.get(i);
			NodeUtilities.down(node);
			ChordNode cn = (ChordNode)node.getProtocol(NodeUtilities.CID);
			KoalaNode kn = (KoalaNode)node.getProtocol(NodeUtilities.KID);
			cn.reset();
			kn.reset();
			
			System.out.println("Node " + cn + " died... R.I.P.");			
		}
		
		return false;
	}
	
	private boolean isRand(){
		return mode.toLowerCase().equals(MOD_RAND.toLowerCase());
	}
}
