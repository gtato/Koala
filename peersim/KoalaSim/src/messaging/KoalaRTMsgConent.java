package messaging;



import java.util.ArrayList;
import utilities.NodeUtilities;
import koala.KoalaNeighbor;
import koala.KoalaNode;

public class KoalaRTMsgConent extends KoalaMsgContent {

	String cid;
	String sid;
	boolean joining;
	boolean neighborsDown;
	String bootstrap;
	ArrayList<KoalaNeighbor> neighbors;
	ArrayList<KoalaNeighbor> oldNeighbors;
	
	
	
	public KoalaRTMsgConent(KoalaNode kn) {
		super(KoalaMessage.RT);
		initalize(kn);
	}

	private void initalize(KoalaNode kn) {
		if (kn == null)
			return;
		
		boolean predDown = NodeUtilities.isDefault(kn.getRoutingTable().getGlobalPredecessor(0)); 
 		boolean succDown = NodeUtilities.isDefault(kn.getRoutingTable().getGlobalSucessor(0));
		neighborsDown = predDown || succDown; 
		cid = kn.getCID();
		sid = kn.getSID();
		joining = kn.isJoining();
		bootstrap = kn.getBootstrapID();
		ArrayList<KoalaNeighbor> neigs = kn.getRoutingTable().getNeighbors();
		neighbors = new ArrayList<KoalaNeighbor>();
		
		for(KoalaNeighbor neig : neigs)
//			neighbors[i] = KoalaJsonParser.toJson(neig);
			neighbors.add(neig.copy());
		
		
		ArrayList<KoalaNeighbor> oldNeigs = kn.getRoutingTable().getOldNeighborsContainer();
		oldNeighbors = new ArrayList<KoalaNeighbor>();
		for(KoalaNeighbor oldNeig : oldNeigs)
//			oldNeighbors[i] = KoalaJsonParser.toJson(oldNeig);
			oldNeighbors.add(oldNeig.copy());
			
		
	}
	
	public KoalaNode getNode(){
		KoalaNode kn = new KoalaNode("");
		kn.setCID(cid);
		kn.setSID(sid);
		kn.setJoining(joining);
		kn.setBootstrapID(bootstrap);
		
		kn.getRoutingTable().setNeighborsContainer(neighbors);
		kn.getRoutingTable().setOldNeighborsContainer(oldNeighbors); 
		return kn;
	}
	
	public boolean getNeighborsDown(){
		return neighborsDown;
	}

}
