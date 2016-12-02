package koala;



import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.ListSelectionEvent;

import messaging.KoalaMessage;
import peersim.core.CommonState;
import topology.TopologyNode;
import utilities.KoalaJsonParser;
import utilities.NodeUtilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class KoalaNode extends TopologyNode{

	/**
	 * 
	 */

	private int dcID;
	private int nodeID;
	private String bootstrapID;
	
//	these two are just for statistics purposes, not necessary  
	public int nrMsgRouted=0;
	public int nrMsgRoutedByLatency=0;
	
	
	

	private boolean isJoining;
	private KoalaRoutingTable routingTable;
	
	private HashMap<Integer, Double> latencyPerDC;
	
	public KoalaNode(String prefix) {
		super(prefix);
		resetRoutingTable();
		resetLatencyPerDC();
	}
	
	

	public Object clone() {
		KoalaNode inp = null;
        inp = (KoalaNode) super.clone();
        inp.resetRoutingTable();
        inp.resetLatencyPerDC();
        return inp;
    }

	public void resetLatencyPerDC(){
		this.latencyPerDC = new HashMap<Integer, Double>();
	}
	
	public int getDCID() {
        return dcID;
    }

    public void setDCID(int dcID) {
        this.dcID = dcID;
    }

    public int getNodeID() {
		return nodeID;
	}

	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	public void setID(int dcID, int nodeID) {
		this.dcID = dcID;
		this.nodeID = nodeID;
	}
	
	public void setID(String id) {
		this.dcID = Integer.parseInt(id.split("-")[0]);
		this.nodeID = Integer.parseInt(id.split("-")[1]);
	}
	
	public String getID() {
		return this.dcID + "-" + this.nodeID;
	}
	
	public String getBootstrapID() {
		return bootstrapID;
	}

	public void setBootstrapID(String bootstrapID) {
		this.bootstrapID = bootstrapID;
	}
	
	public KoalaRoutingTable getRoutingTable() {
		return routingTable;
	}
	
	public void resetRoutingTable() {
		routingTable = new KoalaRoutingTable();
	}
	

	public boolean getJoining(){
		return this.isJoining; 
	}
	
	public void setJoining(boolean isJoining) {
		this.isJoining = isJoining;
	}

	public String toString(){
		return getID();
	}
	
	
	/* The relevant methods start here */
	
		
	public int tryAddNeighbour(KoalaNeighbor n){
        ArrayList<KoalaNeighbor> oldNeighbors = new ArrayList<KoalaNeighbor>();
		boolean local = this.isLocal(n.getNodeID());
        int addedS, addedP;
        addedS = addedP = -1;
        KoalaNeighbor oldS, oldP;
        oldS = oldP = null;
        
        for(int i = 0; i < NodeUtilities.NEIGHBORS; i++){
		    if(this.isSuccessor(n.getNodeID(), i))
		        oldS = local ? getRoutingTable().setLocalSucessor(n,i) : getRoutingTable().setGlobalSucessor(n,i);
		        
		    if (this.isPredecessor(n.getNodeID(), i))
		        oldP = local ? getRoutingTable().setLocalPredecessor(n,i) : getRoutingTable().setGlobalPredecessor(n,i);
		    
		    if(i==0){
		    	addedS = oldS != null ? 1 : 0;
		    	addedP = oldP != null ? 1 : 0;
		    	if (!NodeUtilities.isDefault(oldS)) oldNeighbors.add(oldS);
		    	if (!NodeUtilities.isDefault(oldP)) oldNeighbors.add(oldP);
		    }
        }
        
//        updateNeighbors(n);
        
        int ret = Math.max(addedS, addedP);
         
        if( ret == 1)
            ret++;

        if( ret ==-1 && (canBePredecessor(n.getNodeID(),0) || canBeSuccessor(n.getNodeID(), 0)))
            ret = 1;

        getRoutingTable().setOldNeighborsContainer(oldNeighbors);
        // 2: added, 1: potential neighbor, 0: updated , -1:not neighbor
        return ret; // should return oldNeighbors as well
	}
	
	
	public int getLatencyQuality(boolean isSender, String sourceID, KoalaNeighbor kn){
		if(isSender)
			return 3;
		if(isLocal(sourceID) && kn.getLatencyQuality() > 1)
			return 2;
		return 1;
	}
	
	public boolean isJoining(){
		if(this.getBootstrapID() == null)
			return false;
		ArrayList<KoalaNeighbor> neighbors = getRoutingTable().getNeighbors();
		for(KoalaNeighbor kn : neighbors)
			if(!NodeUtilities.isDefault(kn) && kn.getLatency() != -1)
				return false;
		return true;
	}
	
	
//	private boolean isNeighbour(String nodeID){
//        if (isSuccessor(nodeID))
//            return true;
//        if (isPredecessor(nodeID))
//            return true;
//        return false;
//	}
	
	private boolean isSuccessor(String nodeID, int index){
        boolean local = this.isLocal(nodeID);
        KoalaNeighbor successor = local ? getRoutingTable().getLocalSucessor(index) : getRoutingTable().getGlobalSucessor(index);
        KoalaNeighbor predecessor = local ? getRoutingTable().getLocalPredecessor(index) : getRoutingTable().getGlobalPredecessor(index);
        String current = index == 0 ? this.getID() : getRoutingTable().getLocalSucessor(index-1).getNodeID();
        if(index > 0)
        	predecessor = local ? getRoutingTable().getLocalSucessor(index-1) : getRoutingTable().getGlobalSucessor(index-1);
        if (canBeSuccessor(nodeID, index)){
        	if( NodeUtilities.isDefault(successor)|| local || NodeUtilities.compareIDs(nodeID, successor.getNodeID(), false) != 0)
        		return true;
        	if(successor.equals(predecessor))
        		return true;
        	if(!nodeID.equals(predecessor.getNodeID())  &&  NodeUtilities.distance(current, nodeID, true) <= NodeUtilities.distance(current, successor.getNodeID(), true))
        		return true;
        }
        return false;
	}
	 
	private boolean isPredecessor(String nodeID, int index){
		boolean local = this.isLocal(nodeID);
        KoalaNeighbor successor = local ? getRoutingTable().getLocalSucessor(index) : getRoutingTable().getGlobalSucessor(index);
        KoalaNeighbor predecessor = local ? getRoutingTable().getLocalPredecessor(index) : getRoutingTable().getGlobalPredecessor(index);
        String current = index == 0 ? this.getID() : getRoutingTable().getLocalSucessor(index-1).getNodeID();
        if(index > 0)
        	successor = local ? getRoutingTable().getLocalPredecessor(index-1) : getRoutingTable().getGlobalPredecessor(index-1);
        if (canBePredecessor(nodeID, index)){
        	if(NodeUtilities.isDefault(predecessor) || local || NodeUtilities.compareIDs(nodeID, predecessor.getNodeID(), false) != 0)
        		return true;
        	if(successor.getNodeID().equals(predecessor.getNodeID()))
        		return true;
        	if(!nodeID.equals(successor.getNodeID())  &&  NodeUtilities.distance(current, nodeID, true) <= NodeUtilities.distance(current, predecessor.getNodeID(), true))
        		return true;
        }
        return false;
	}
	
	
//	private boolean canBeNeighbour(String nodeID){
//        if (canBeSuccessor(nodeID))
//            return true;
//        if (canBePredecessor(nodeID))
//            return true;
//        return false;
//	}
	
	private boolean canBeSuccessor(String nodeID, int index){
        boolean local = this.isLocal(nodeID);
        KoalaNeighbor successor = local ? getRoutingTable().getLocalSucessor(index) : getRoutingTable().getGlobalSucessor(index);
        String current;
        if(index == 0)
        	current = this.getID();
        else
        	current = local ? getRoutingTable().getLocalSucessor(index-1).getNodeID() : getRoutingTable().getGlobalSucessor(index-1).getNodeID(); 
        
        if (NodeUtilities.isDefault(successor) || successor.getNodeID().equals(current))
            return true;
        else{
            if((NodeUtilities.compareIDs(nodeID, successor.getNodeID(), local) <= 0 && NodeUtilities.compareIDs(successor.getNodeID(), current, local) < 0) || 
               (NodeUtilities.compareIDs(nodeID, successor.getNodeID(), local) >= 0 && NodeUtilities.compareIDs(successor.getNodeID(), current, local) < 0 && NodeUtilities.compareIDs(nodeID, current, local) > 0) || 
               (NodeUtilities.compareIDs(nodeID, successor.getNodeID(), local) <= 0 && NodeUtilities.compareIDs(nodeID, current, local) > 0) )
                return true;
        }
        return false;
	}
	
	 
	private boolean canBePredecessor(String nodeID, int index){
        boolean local = this.isLocal(nodeID);
        KoalaNeighbor predecessor = local ? getRoutingTable().getLocalPredecessor(index) : getRoutingTable().getGlobalPredecessor(index);
        String current;
        if(index == 0)
        	current = this.getID();
        else
        	current = local ? getRoutingTable().getLocalPredecessor(index-1).getNodeID() : getRoutingTable().getGlobalPredecessor(index-1).getNodeID(); 
        
        if (NodeUtilities.isDefault(predecessor) || predecessor.getNodeID().equals(current) )
            return true;
        else{
            if((NodeUtilities.compareIDs(nodeID, predecessor.getNodeID(), local) >= 0 && NodeUtilities.compareIDs(predecessor.getNodeID(), current, local) > 0) || 
               (NodeUtilities.compareIDs(nodeID, predecessor.getNodeID(), local) <= 0 && NodeUtilities.compareIDs(predecessor.getNodeID(), current, local) > 0 && NodeUtilities.compareIDs(nodeID, current, local) < 0) || 
               (NodeUtilities.compareIDs(nodeID, predecessor.getNodeID(), local) >= 0 && NodeUtilities.compareIDs(nodeID, current, local) < 0) )
                return true;
        }
        return false;
	}
    
    	
	public boolean isLocal(String id){
		return NodeUtilities.sameDC(getID(), id); 
	}

	public boolean isLocal(KoalaNeighbor n){
		return NodeUtilities.sameDC(getID(), n.getNodeID()); 
	}
	
	
    public void updateLatencyPerDC(String id, double l, int lq){    	
    	if(this.getID().equals(id))
    		return;
        if (lq > 1)
            latencyPerDC.put(NodeUtilities.getDCID(id), l);

        ArrayList<KoalaNeighbor> links = routingTable.getNeighbors();
        for(KoalaNeighbor ln : links){
            if(ln.getNodeID().equals(id) && lq >= ln.getLatencyQuality()){
                ln.setLatency(l);
                ln.setLatencyQuality(lq);
            }
        }
    }
    
    public void updateLatencies(){
    	ArrayList<KoalaNeighbor> neigs = routingTable.getNeighbors();
    	for(KoalaNeighbor n : neigs){
    		if(isLocal(n.getNodeID()))
    			continue;
    		int ndc = NodeUtilities.getDCID(n.getNodeID());
    		if(n.getLatencyQuality() < 2 &&  latencyPerDC.keySet().contains(ndc)){
    			n.setLatency(2);
    			n.setLatency(latencyPerDC.get(ndc));
    		}
    			
    	}
    }
    	
    
    public KoalaNeighbor getRoute(String dest, KoalaMessage msg) {
    	KoalaNeighbor normal = getRouteForAlpha(dest, msg, NodeUtilities.B);
    	KoalaNeighbor no_latency = getRouteForAlpha(dest, msg, 1);
    	if(normal!=null && no_latency!=null && !normal.equals(no_latency))
    		this.nrMsgRoutedByLatency++;
    	return normal;
    }
    
    public KoalaNeighbor getRouteForAlpha(String dest, KoalaMessage msg, double alpha) {
		AbstractMap.SimpleEntry<Double, KoalaNeighbor> mre;
		double v=0;
		ArrayList<KoalaNeighbor> rt = getRoutingTable().getNeighbors();
		ArrayList<AbstractMap.SimpleEntry<Double, KoalaNeighbor>> potentialDests = new ArrayList<AbstractMap.SimpleEntry<Double, KoalaNeighbor>>(); 
	
		for(KoalaNeighbor re : rt){
			v = getRouteValue(dest, re, alpha);
			mre = new AbstractMap.SimpleEntry<Double, KoalaNeighbor>(v, re);
			potentialDests.add(mre);
	
		}
		Collections.sort(potentialDests, Collections.reverseOrder(new Comparator<AbstractMap.SimpleEntry<Double, KoalaNeighbor>>() {
			@Override
			public int compare(AbstractMap.SimpleEntry<Double, KoalaNeighbor> o1, AbstractMap.SimpleEntry<Double, KoalaNeighbor> o2) {
				return o1.getKey().compareTo(o2.getKey());
				
			}
			}));
		
		KoalaNeighbor ret = null; 
		ArrayList<KoalaNeighbor> downEntries = new ArrayList<KoalaNeighbor>();
		for(AbstractMap.SimpleEntry<Double, KoalaNeighbor> entry : potentialDests){
			KoalaNeighbor rentry = entry.getValue();
			boolean isDown = !NodeUtilities.isUp(rentry.getNodeID()); 
			if(isDown)
				downEntries.add(rentry);
			if(rentry.getNodeID().equals(dest) && isDown)
				break;
			if(msg.getPath().contains(rentry.getNodeID()) || isDown)
				continue;
			
			ret = rentry;
			break;
		}
		
		for(KoalaNeighbor down : downEntries)
			down.reset();
			
		
		return ret;
    }

    
	private double getRouteValue(String dest, KoalaNeighbor re, double alpha) {
		double res = 0;

//		if( NodeUtilities.distance(this.getID(), dest) < NodeUtilities.distance(re.getNodeID(), dest))
//            res = -1;
//		
//		else
		if( NodeUtilities.getDCID(dest) == NodeUtilities.getDCID(re.getNodeID()))
            res = Double.MAX_VALUE - NodeUtilities.A * NodeUtilities.distance(re.getNodeID(), dest);
        
		else if( this.dcID == NodeUtilities.getDCID(re.getNodeID()))
            res = NodeUtilities.A * NodeUtilities.distance(this.getID(), re.getNodeID());
        
		else {//if( NodeUtilities.distance(this.getID(), dest) > NodeUtilities.distance(re.getNodeID(), dest)){
            int tot_distance = NodeUtilities.distance(this.getID(), dest);
            int rem_distance = NodeUtilities.distance(dest, re.getNodeID());
            
            double norm_dist =  1 - (double)rem_distance/tot_distance;
            double norm_latency = NodeUtilities.normalizeLatency(tot_distance, re.getLatency());
            res = 1 + alpha * norm_dist + (1-alpha) * norm_latency;
            
            if(alpha == -1)
            	res = 1+CommonState.r.nextInt(100);
        }
        
		
        
		return res;
	}
	
	
	public Set<String> createRandomIDs(int nr){
		Set<String> rids = new HashSet<String>();
		
		if (getRoutingTable().hasAllDefaultLocals())
			nr = 0;
		else if(getRoutingTable().getLocalSucessor(0).equals(getRoutingTable().getLocalPredecessor(0)))
			nr = 1;
		
        while( rids.size() < nr){
            String rand_id = this.dcID + "-" + CommonState.r.nextInt(NodeUtilities.NR_NODE_PER_DC);
            if( !this.isResponsible(rand_id))
                rids.add(rand_id);
            
        }
        return rids;
	}



	public boolean isResponsible(String id) {
		if(id.equals(getID()))
			return true;
		if (getRoutingTable().hasAllDefaultLocals())
            return false;
        return NodeUtilities.distance(getID(), id) < NodeUtilities.distance(getRoutingTable().getLocalSucessor(0).getNodeID(), id) 
                && NodeUtilities.distance(getID(), id) < NodeUtilities.distance(getRoutingTable().getLocalPredecessor(0).getNodeID(), id);
		
	}

	
	public boolean inNeighborsList(String id){
		Set<String> ids = isLocal(id) ?  getRoutingTable().getNeighboursIDs(1):
										 getRoutingTable().getNeighboursIDs(2); 
		return ids.contains(id);
	}
	
	public static class KoalaNodeSerializer implements JsonSerializer<KoalaNode> {

		@Override
		public JsonElement serialize(KoalaNode src, Type typeOfSrc, JsonSerializationContext context) {
			JsonArray neighbors = new JsonArray();
			ArrayList<KoalaNeighbor> neigs = src.getRoutingTable().getNeighbors();
			for(KoalaNeighbor neig : neigs){
				neighbors.add(KoalaJsonParser.toJsonTree(neig));
			}
			
			JsonArray oldNeighbors = new JsonArray();
			ArrayList<KoalaNeighbor> oldNeigs = src.getRoutingTable().getOldNeighborsContainer();
			for(KoalaNeighbor oldNeig : oldNeigs){
				oldNeighbors.add(KoalaJsonParser.toJsonTree(oldNeig));
			}
			
			JsonObject obj = new JsonObject();
			obj.addProperty("id", src.getID());
			obj.addProperty("joining", src.isJoining());
			
			obj.add("neighbors", (JsonElement)neighbors);
			obj.add("oldNeighbors", (JsonElement)oldNeighbors);
			return obj;
		}
		
	}
	
	public static class KoalaNodeDeserializer implements JsonDeserializer<KoalaNode> {
		private KoalaNode sample;
		public KoalaNodeDeserializer(KoalaNode sample){
			this.sample = sample;
		}
		
		@Override
		public KoalaNode deserialize(JsonElement src, Type typeOfSrc, JsonDeserializationContext context) throws JsonParseException {
			JsonObject srcJO = src.getAsJsonObject();
			KoalaNode kn = (KoalaNode) sample.clone();
			kn.setID(srcJO.get("id").getAsString());
			kn.setJoining(srcJO.get("joining").getAsBoolean());
		
			JsonArray neigs = srcJO.getAsJsonArray("neighbors");
			ArrayList<KoalaNeighbor> neighbors = new ArrayList<KoalaNeighbor>();
			for(JsonElement neig : neigs)
				neighbors.add(KoalaJsonParser.jsonTreeToObject(neig, KoalaNeighbor.class));
			
			JsonArray oldNeigs = srcJO.getAsJsonArray("oldNeighbors");
			ArrayList<KoalaNeighbor> oldNeighbors = new ArrayList<KoalaNeighbor>();
			for(JsonElement neig : oldNeigs)
				oldNeighbors.add(KoalaJsonParser.jsonTreeToObject(neig, KoalaNeighbor.class));
			
			kn.getRoutingTable().setNeighborsContainer(neighbors);
			kn.getRoutingTable().setOldNeighborsContainer(oldNeighbors); 
			return kn;
		}
		
	}

}
