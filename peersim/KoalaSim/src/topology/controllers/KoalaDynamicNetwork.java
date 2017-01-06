package topology.controllers;


import java.util.ArrayList;
import java.util.Collections;

import chord.ChordNode;
import koala.KoalaNode;
import koala.KoalaProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.DynamicNetwork;
import peersim.dynamics.NodeInitializer;
import renater.RenaterNode;
import utilities.NodeUtilities;

public class KoalaDynamicNetwork implements Control{

	public static final String MOD_RAND = "rand";
	public static final String MOD_STAND = "stand";
	
	private static final String PAR_INIT = "init";
	private static final String PAR_MIN = "minsize";
	private static final String PAR_ADD = "add";
	private static final String PAR_MODE = "mode";
	protected final double add;
	protected final int minsize;
	protected final String mode;
	protected final NodeInitializer[] inits;
	
	public KoalaDynamicNetwork(String prefix) {
		add = Configuration.getDouble(prefix + "." + PAR_ADD);
		minsize = Configuration.getInt(prefix + "." + PAR_MIN, 0);
		mode = Configuration.getString(prefix + "." + PAR_MODE, MOD_STAND);
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
		if (!isRand() && add == 0) return false;
		
		int realMinSize = (int) (Network.size() * (double)minsize/100);
		
		ArrayList<Integer> upInx = new ArrayList<Integer>();
		ArrayList<Integer> downInx = new ArrayList<Integer>();
		
		for(int i = 0; i < Network.size(); i++){
			if(Network.get(i).isUp())
				upInx.add(i);
			else
				downInx.add(i);
		}
		
		Collections.shuffle(upInx, CommonState.r);
		Collections.shuffle(downInx, CommonState.r);
		
		int toAdd = 0 , toRemove = 0;
		
		if(isRand()){
			//TODO: why 5? because magic 
//			toAdd = CommonState.r.nextInt(5);
//			toRemove = CommonState.r.nextInt(5);
			toAdd = CommonState.r.nextInt(2);
			toRemove = 1-toAdd;
		}else{
			toAdd = add > 0 ? (int) add : 0;
			toRemove = add < 0 ? (int) (int)-add : 0;
		}
		
		if(toAdd > 0 && toAdd > downInx.size())
			toAdd = downInx.size();
		
		if(toRemove > 0 && realMinSize > upInx.size() - toRemove)
			toRemove = upInx.size() - realMinSize;
		
		
		for(int i=0; i < toAdd; i++){
			Node node = Network.get(downInx.get(i)); 
			node.setFailState(Fallible.OK);
			ChordNode cn = (ChordNode)node.getProtocol(NodeUtilities.CID);
			System.out.println("Node " + cn + " came back to save us all");
			
			for (int j = 0; j < inits.length; ++j) 
				inits[j].initialize(node);
		}
		
		
		for(int i=0; i < toRemove; i++){
			Node node = Network.get(upInx.get(i)); 
			node.setFailState(Fallible.DOWN);
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
