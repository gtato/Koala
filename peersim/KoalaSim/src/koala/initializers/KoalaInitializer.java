package koala.initializers;

import java.util.ArrayList;
import java.util.Collections;

import koala.KoalaProtocol;
import koala.RenaterProtocol;
import koala.TopologyProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class KoalaInitializer implements Control {
	
	private static final String PAR_KOALA_PROTOCOL= "kprotocol";
	private static final String PAR_RENATER_PROTOCOL= "protocol";
	
	private final int renProtPid;
	private final int renNodePid;
	private final int koaProtPid;
	private final int koaNodePid;
	
	public KoalaInitializer(String prefix) {
		
		renProtPid = Configuration.getPid(prefix + "." + PAR_RENATER_PROTOCOL);
		renNodePid = FastConfig.getLinkable(renProtPid);
		
		koaProtPid = Configuration.getPid(prefix + "." + PAR_KOALA_PROTOCOL, -1);
		if(koaProtPid > -1)
			koaNodePid = FastConfig.getLinkable(koaProtPid);
		else
			koaNodePid = -1;
		
	}
	
	@Override
	public boolean execute() {
		System.out.println("Building the koala ring. Depending on the size this might take also some time");
		TopologyProtocol.setInitializeMode(true);
		
		ArrayList<Integer> inx = new ArrayList<Integer>();
		for(int i = 0; i < Network.size(); i++)
			inx.add(i);
		
		Collections.shuffle(inx, CommonState.r);
		
		
		for (int i = 0; i < Network.size(); i++) {
			Node n = Network.get(inx.get(i));
			
			RenaterProtocol rp = (RenaterProtocol )n.getProtocol(renProtPid);
			rp.setPids(renProtPid, renNodePid);
			rp.intializeMyNode(n);
			rp.join();
			
			if(koaProtPid > -1){
				KoalaProtocol kp = (KoalaProtocol )n.getProtocol(koaProtPid);
				kp.setPids(koaProtPid, koaNodePid);
				kp.intializeMyNode(n);
				kp.join();
			}
			
		}
		
		TopologyProtocol.setInitializeMode(false);
		
		return false;
	}

}
