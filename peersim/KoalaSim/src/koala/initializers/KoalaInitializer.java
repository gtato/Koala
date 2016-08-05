package koala.initializers;

import java.util.ArrayList;
import java.util.Collections;

import koala.KoalaProtocol;
import koala.RenaterProtocol;
import koala.TopologyProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;


public class KoalaInitializer implements Control {
		
	private static final String PAR_KOALA_PROTOCOL= "kprotocol";
	private static final String PAR_RENATER_PROTOCOL= "protocol";
	private static final String PAR_KOALA_NR= "nr";
	
	private final int renProtPid;
	
	private final int koaProtPid;
	
	
	private final int nr;
	
	public KoalaInitializer(String prefix) {
		renProtPid = Configuration.getPid(prefix + "." + PAR_RENATER_PROTOCOL);
		koaProtPid = Configuration.getPid(prefix + "." + PAR_KOALA_PROTOCOL, -1);
		nr = Configuration.getInt(prefix + "." + PAR_KOALA_NR, Network.size());
	}
	
	@Override
	public boolean execute() {
		System.out.println("Building the koala ring. Depending on the size this might take also some time");
		TopologyProtocol.setInitializeMode(true);
		
		ArrayList<Integer> inx = new ArrayList<Integer>();
		for(int i = 0; i < nr; i++)
			inx.add(i);
		
		Collections.shuffle(inx, CommonState.r);
		
		
		for (int i = 0; i < nr; i++) {
			Node n = Network.get(inx.get(i));
			
			RenaterProtocol rp = (RenaterProtocol )n.getProtocol(renProtPid);
			rp.intializeMyNode(n, renProtPid);
			rp.join();
			
			if(koaProtPid > -1){
				KoalaProtocol kp = (KoalaProtocol )n.getProtocol(koaProtPid);
				kp.intializeMyNode(n, koaProtPid);
				kp.join();
			}
			
		}
		
		TopologyProtocol.setInitializeMode(false);
		
		return false;
	}

}
