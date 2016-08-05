package koala;



import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import koala.utility.KoalaJsonParser;
import koala.utility.NodeUtilities;
import messaging.KoalaMessage;
import peersim.core.CommonState;

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
        ArrayList<KoalaNeighbor> oldNeighbors = new ArrayList<>();
		boolean local = this.isLocal(n.getNodeID());
        int addedS, addedP;
        addedS = addedP = -1;
        KoalaNeighbor oldS, oldP;
        oldS = oldP = null;
        
        if(this.isSuccessor(n.getNodeID())){
            oldS = local ? getRoutingTable().setLocalSucessor(n) : getRoutingTable().setGlobalSucessor(n);
            addedS = oldS != null ? 1 : 0;
            if (!NodeUtilities.isDefault(oldS)) oldNeighbors.add(oldS);   
        }
        if (this.isPredecessor(n.getNodeID())){
            oldP = local ? getRoutingTable().setLocalPredecessor(n) : getRoutingTable().setGlobalPredecessor(n);
            addedP = oldP != null ? 1 : 0;
            if (!NodeUtilities.isDefault(oldP)) oldNeighbors.add(oldP);
        }
        int ret = Math.max(addedS, addedP);
         
        if( ret == 1)
            ret++;

        if( ret ==-1 && (canBePredecessor(n.getNodeID()) || canBeSuccessor(n.getNodeID())))
            ret = 1;

        getRoutingTable().setOldNeighborsContainer(oldNeighbors);
        // 2: added, 1: potential neighbor, 0: updated , -1:not neighbor
        return ret; // should return oldNeighbors as well
	}
	
	
	
	
	public int getLatencyQuality(boolean isSource, String sourceID, KoalaNeighbor kn){
		if(isSource)
			return 3;
		if(isLocal(sourceID) && kn.getLatencyQuality() > 1)
			return 2;
		return 1;
	}
	
	public boolean isJoining(){
		if(this.getBootstrapID() == null)
			return false;
		Set<KoalaNeighbor> neighbors = getRoutingTable().getNeighbors();
		for(KoalaNeighbor kn : neighbors)
			if(!NodeUtilities.isDefault(kn) && kn.getLatency() != -1)
				return false;
		return true;
	}
	
	
	private boolean isNeighbour(String nodeID){
        if (isSuccessor(nodeID))
            return true;
        if (isPredecessor(nodeID))
            return true;
        return false;
	}
	
	private boolean isSuccessor(String nodeID){
        boolean local = this.isLocal(nodeID);
        KoalaNeighbor successor = local ? getRoutingTable().getLocalSucessor() : getRoutingTable().getGlobalSucessor();
        KoalaNeighbor predecessor = local ? getRoutingTable().getLocalPredecessor() : getRoutingTable().getGlobalPredecessor();
        if (canBeSuccessor(nodeID)){
        	if( NodeUtilities.isDefault(successor)|| local || NodeUtilities.compareIDs(nodeID, successor.getNodeID(), false) != 0)
        		return true;
        	if(successor.getNodeID().equals(predecessor.getNodeID()))
        		return true;
        	if(!nodeID.equals(predecessor.getNodeID())  &&  NodeUtilities.distance(this.getID(), nodeID, true) <= NodeUtilities.distance(this.getID(), successor.getNodeID(), true))
        		return true;
        }
        return false;
	}
	 
	private boolean isPredecessor(String nodeID){
		boolean local = this.isLocal(nodeID);
        KoalaNeighbor successor = local ? getRoutingTable().getLocalSucessor() : getRoutingTable().getGlobalSucessor();
        KoalaNeighbor predecessor = local ? getRoutingTable().getLocalPredecessor() : getRoutingTable().getGlobalPredecessor();
        if (canBePredecessor(nodeID)){
        	if(NodeUtilities.isDefault(predecessor) || local || NodeUtilities.compareIDs(nodeID, predecessor.getNodeID(), false) != 0)
        		return true;
        	if(successor.getNodeID().equals(predecessor.getNodeID()))
        		return true;
        	if(!nodeID.equals(successor.getNodeID())  &&  NodeUtilities.distance(this.getID(), nodeID, true) <= NodeUtilities.distance(this.getID(), predecessor.getNodeID(), true))
        		return true;
        }
        return false;
	}
	
	
	private boolean canBeNeighbour(String nodeID){
        if (canBeSuccessor(nodeID))
            return true;
        if (canBePredecessor(nodeID))
            return true;
        return false;
	}
	
	private boolean canBeSuccessor(String nodeID){
	        boolean local = this.isLocal(nodeID);
	        KoalaNeighbor successor = local ? getRoutingTable().getLocalSucessor() : getRoutingTable().getGlobalSucessor();
	        if (NodeUtilities.isDefault(successor))
	            return true;
	        else{
	            if((NodeUtilities.compareIDs(nodeID, successor.getNodeID(), local) <= 0 && NodeUtilities.compareIDs(successor.getNodeID(), this.getID(), local) < 0) || 
	               (NodeUtilities.compareIDs(nodeID, successor.getNodeID(), local) >= 0 && NodeUtilities.compareIDs(successor.getNodeID(), this.getID(), local) < 0 && NodeUtilities.compareIDs(nodeID, this.getID(), local) > 0) || 
	               (NodeUtilities.compareIDs(nodeID, successor.getNodeID(), local) <= 0 && NodeUtilities.compareIDs(nodeID, this.getID(), local) > 0) )
	                return true;
	        }
	        return false;
	}
	 
	private boolean canBePredecessor(String nodeID){
        boolean local = this.isLocal(nodeID);
        KoalaNeighbor predecessor = local ? getRoutingTable().getLocalPredecessor() : getRoutingTable().getGlobalPredecessor();
        if (NodeUtilities.isDefault(predecessor))
            return true;
        else{
            if((NodeUtilities.compareIDs(nodeID, predecessor.getNodeID(), local) >= 0 && NodeUtilities.compareIDs(predecessor.getNodeID(), this.getID(), local) > 0) || 
               (NodeUtilities.compareIDs(nodeID, predecessor.getNodeID(), local) <= 0 && NodeUtilities.compareIDs(predecessor.getNodeID(), this.getID(), local) > 0 && NodeUtilities.compareIDs(nodeID, this.getID(), local) < 0) || 
               (NodeUtilities.compareIDs(nodeID, predecessor.getNodeID(), local) >= 0 && NodeUtilities.compareIDs(nodeID, this.getID(), local) < 0) )
                return true;
        }
        return false;
	}
    
    	
	public boolean isLocal(String id){
		return NodeUtilities.getDCID(this.getID()) == NodeUtilities.getDCID(id); 
	}

	
	
    public void updateLatencyPerDC(String id, double l, int lq){    	
    	if(this.getID().equals(id))
    		return;
        if (lq > 1)
            latencyPerDC.put(NodeUtilities.getDCID(id), l);

        Set<KoalaNeighbor> links = routingTable.getNeighbors();
        for(KoalaNeighbor ln : links){
            if(ln.getNodeID().equals(id) && lq >= ln.getLatencyQuality()){
                ln.setLatency(l);
                ln.setLatencyQuality(lq);
            }
        }
    }
    
    public void updateLatencies(){
    	Set<KoalaNeighbor> neigs = routingTable.getNeighbors();
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
    	
	
    public String getRoute(String dest, KoalaMessage msg) {
    	String ret = null;
    	AbstractMap.SimpleEntry<Double, KoalaNeighbor> mre;
    	double v=0;
    	Set<KoalaNeighbor> rt = getRoutingTable().getNeighbors();
    	ArrayList<AbstractMap.SimpleEntry<Double, KoalaNeighbor>> potentialDests = new ArrayList<AbstractMap.SimpleEntry<Double, KoalaNeighbor>>(); 

    	for(KoalaNeighbor re : rt){
    		v = getRouteValue(dest, re);
    		mre = new AbstractMap.SimpleEntry<Double, KoalaNeighbor>(v, re);
    		potentialDests.add(mre);

    	}
    	Collections.sort(potentialDests, Collections.reverseOrder(new Comparator<AbstractMap.SimpleEntry<Double, KoalaNeighbor>>() {
			@Override
			public int compare(AbstractMap.SimpleEntry<Double, KoalaNeighbor> o1, AbstractMap.SimpleEntry<Double, KoalaNeighbor> o2) {
				return o1.getKey().compareTo(o2.getKey());
				
			}
			}));
    	
    	if(potentialDests.size() > 1 && potentialDests.get(0).getValue().getNodeID().equals(msg.getLastSender()))
    		ret = potentialDests.get(1).getValue().getNodeID(); 
    	else
    		ret = potentialDests.get(0).getValue().getNodeID();

    	return ret;
	}
    
    //old
    public String getRoute_old(String dest, KoalaMessage msg) {
    	String ret = null;
    	double v, max = 0;
    	Set<KoalaNeighbor> rt = getRoutingTable().getNeighbors();
 
    	KoalaNeighbor lastRE = null;
    	for(KoalaNeighbor re : rt){
    		
    		lastRE = re;
    		v = getRouteValue(dest, re);

    		if (v > max){
    	    	max = v;
    	    	ret = re.getNodeID();
    	    }
    	}

    	//if the node does not have yet enough contacts
    	//send to the one it has with the hope that that one will know better
    	if(ret == null && lastRE != null)
    		ret = lastRE.getNodeID();
    	return ret;
	}
    

	private double getRouteValue(String dest, KoalaNeighbor re) {
		double res = 0;

        if( NodeUtilities.distance(this.getID(), dest) < NodeUtilities.distance(re.getNodeID(), dest))
            res = -1;

        if( this.dcID == NodeUtilities.getDCID(re.getNodeID()))
            res = NodeUtilities.A * NodeUtilities.distance(this.getID(), re.getNodeID());

        if( NodeUtilities.distance(this.getID(), dest) > NodeUtilities.distance(re.getNodeID(), dest)){
            int tot_distance = NodeUtilities.distance(this.getID(), dest);
            double distance = (double) NodeUtilities.distance(this.getID(), re.getNodeID()) / tot_distance;
            double norm_latency = NodeUtilities.normalizeLatency(tot_distance, re.getLatency());
            res = 1 + NodeUtilities.B * distance + NodeUtilities.C * norm_latency;
        }
        if( NodeUtilities.getDCID(dest) == NodeUtilities.getDCID(re.getNodeID()))
            res = Double.MAX_VALUE - NodeUtilities.A * NodeUtilities.distance(re.getNodeID(), dest);
        return res;
	}
	
	public Set<String> createRandomIDs(int nr){
        Set<String> rids = new HashSet<String>();
        while( rids.size() < nr){
            String rand_id = this.dcID + "-" + CommonState.r.nextInt(NodeUtilities.NR_NODE_PER_DC);
//            String rand_id = this.dcID + "-" + new Random().nextInt(NodeUtilities.NR_NODE_PER_DC);
            if( !this.isResponsible(rand_id))
                rids.add(rand_id);
            
        }
        return rids;
	}



	public boolean isResponsible(String id) {
		
		if (NodeUtilities.isDefault(getRoutingTable().getLocalSucessor()))
            return false;
        return NodeUtilities.distance(getID(), id) < NodeUtilities.distance(getRoutingTable().getLocalSucessor().getNodeID(), id) 
                && NodeUtilities.distance(getID(), id) < NodeUtilities.distance(getRoutingTable().getLocalPredecessor().getNodeID(), id);
		
	}

	


	public static class KoalaNodeSerializer implements JsonSerializer<KoalaNode> {

		@Override
		public JsonElement serialize(KoalaNode src, Type typeOfSrc, JsonSerializationContext context) {
			JsonArray neighbors = new JsonArray();
			Set<KoalaNeighbor> neigs = src.getRoutingTable().getNeighbors();
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
