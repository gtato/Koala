package chord.controllers;


import chord.ChordNode;
import chord.ChordProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import utilities.NodeUtilities;



public class ChordMaintainer implements Control{

	private static final String PAR_PID= "protocol";
	
	private final int chPid;
	public ChordMaintainer(String prefix){
		chPid = Configuration.getPid(prefix + "." + PAR_PID);
	}
	
	@Override
	public boolean execute() {
		
		if(CommonState.getTime()==0) return false;
		System.out.println("Stabilizing chord");
		for(int i = 0; i < Network.size(); i++){
			ChordProtocol cp = (ChordProtocol) Network.get(i).getProtocol(chPid);
			if(Network.get(i).isUp() && cp.getMyNode().hasJoined()){
//			if(Network.get(i).isUp()){
				cp.stabilize();
				cp.fixFingers();
			}
		}
		return false;
	}

}
