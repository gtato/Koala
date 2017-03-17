package messaging;



import java.util.ArrayList;
import utilities.KoalaJsonParser;
import utilities.NodeUtilities;
import koala.KoalaNeighbor;
import koala.KoalaNode;

public class KoalaRTMsgConent extends KoalaMsgContent {

	String id;
	boolean joining;
	boolean neighborsDown;
	String bootstrap;
	String[] neighbors;
	String[] oldNeighbors;
	
	
	
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
		id = kn.getID();
		joining = kn.isJoining();
		bootstrap = kn.getBootstrapID();
		ArrayList<KoalaNeighbor> neigs = kn.getRoutingTable().getNeighbors();
		neighbors = new String[neigs.size()];
		int i=0;
		for(KoalaNeighbor neig : neigs){
			neighbors[i] = KoalaJsonParser.toJson(neig);
			i++;
		}
		
		ArrayList<KoalaNeighbor> oldNeigs = kn.getRoutingTable().getOldNeighborsContainer();
		oldNeighbors = new String[oldNeigs.size()];
		i=0;
		for(KoalaNeighbor oldNeig : oldNeigs){
			oldNeighbors[i] = KoalaJsonParser.toJson(oldNeig);
			i++;
		}
	}
	
	public KoalaNode getNode(){
		KoalaNode kn = new KoalaNode("");
		
		kn.setID(id);
		kn.setJoining(joining);
		kn.setBootstrapID(bootstrap);
		ArrayList<KoalaNeighbor> neighs = new ArrayList<KoalaNeighbor>();
		for(String neig : neighbors)
			neighs.add(KoalaJsonParser.jsonToObject(neig, KoalaNeighbor.class));
		
		
		ArrayList<KoalaNeighbor> oldNeighs = new ArrayList<KoalaNeighbor>();
		for(String oldNeig : oldNeighbors)
			oldNeighs.add(KoalaJsonParser.jsonToObject(oldNeig, KoalaNeighbor.class));
		
		kn.getRoutingTable().setNeighborsContainer(neighs);
		kn.getRoutingTable().setOldNeighborsContainer(oldNeighs); 
		return kn;
	}
	
	public boolean getNeighborsDown(){
		return neighborsDown;
	}

}
