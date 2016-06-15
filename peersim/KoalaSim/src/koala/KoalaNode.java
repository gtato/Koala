package koala;



import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import koala.utility.KoalaJsonParser;
import koala.utility.KoalaNodeUtilities;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import peersim.core.Linkable;
import peersim.core.Node;
import peersim.core.Protocol;
import example.hot.InetCoordinates;

public class KoalaNode extends InetCoordinates implements Protocol, Linkable {

	/**
	 * 
	 */
	double logicalX;
	double logicalY;
	private static final long serialVersionUID = 1L;
	private int dcID;
	private int nodeID;
	private String bootstrapID;
	
	private boolean gateway;
//	private boolean joined;
	private boolean isJoining;
	private KoalaRoutingTable routingTable;
	        
	private HashMap<Integer, Integer> latencyPerDC = new HashMap<Integer, Integer>(); 
	
	private ArrayList<Node> physicalNeighbors;
	
	public KoalaNode(String prefix) {
		super(prefix);
		resetRoutingTable();
		physicalNeighbors = new ArrayList<Node>();
	}
	
	

	public Object clone() {
		KoalaNode inp = null;
        inp = (KoalaNode) super.clone();
        inp.resetRoutingTable();
        physicalNeighbors = new ArrayList<Node>();
        return inp;
    }

	public int getDCID() {
        return dcID;
    }

    public void setDCID(int dcID) {
        this.dcID = dcID;
    }

    public boolean isGateway() {
        return gateway;
    }

    public void setGateway(boolean gateway) {
        this.gateway = gateway;
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
	
//	public boolean hasJoined() {
//		return joined;
//	}
//
//	public void setJoined(boolean joined) {
//		this.joined = joined;
//	}
	
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
	
	public double getLogicalX() {
		return logicalX;
	}



	public void setLogicalX(double logicalX) {
		this.logicalX = logicalX;
	}



	public double getLogicalY() {
		return logicalY;
	}



	public void setLogicalY(double logicalY) {
		this.logicalY = logicalY;
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
            if (!KoalaNodeUtilities.isDefault(oldS)) oldNeighbors.add(oldS);   
        }
        if (this.isPredecessor(n.getNodeID())){
            oldP = local ? getRoutingTable().setLocalPredecessor(n) : getRoutingTable().setGlobalPredecessor(n);
            addedP = oldP != null ? 1 : 0;
            if (!KoalaNodeUtilities.isDefault(oldP)) oldNeighbors.add(oldP);
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
			if(!KoalaNodeUtilities.isDefault(kn) && kn.getLatency() != -1)
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
        	if( KoalaNodeUtilities.isDefault(successor)|| local || KoalaNodeUtilities.compareIDs(nodeID, successor.getNodeID(), false) != 0)
        		return true;
        	if(successor.getNodeID().equals(predecessor.getNodeID()))
        		return true;
        	if(!nodeID.equals(predecessor.getNodeID())  &&  KoalaNodeUtilities.distance(this.getID(), nodeID, true) <= KoalaNodeUtilities.distance(this.getID(), successor.getNodeID(), true))
        		return true;
        }
        return false;
	}
	 
	private boolean isPredecessor(String nodeID){
		boolean local = this.isLocal(nodeID);
        KoalaNeighbor successor = local ? getRoutingTable().getLocalSucessor() : getRoutingTable().getGlobalSucessor();
        KoalaNeighbor predecessor = local ? getRoutingTable().getLocalPredecessor() : getRoutingTable().getGlobalPredecessor();
        if (canBePredecessor(nodeID)){
        	if(KoalaNodeUtilities.isDefault(predecessor) || local || KoalaNodeUtilities.compareIDs(nodeID, predecessor.getNodeID(), false) != 0)
        		return true;
        	if(successor.getNodeID().equals(predecessor.getNodeID()))
        		return true;
        	if(!nodeID.equals(successor.getNodeID())  &&  KoalaNodeUtilities.distance(this.getID(), nodeID, true) <= KoalaNodeUtilities.distance(this.getID(), predecessor.getNodeID(), true))
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
	        if (KoalaNodeUtilities.isDefault(successor))
	            return true;
	        else{
	            if((KoalaNodeUtilities.compareIDs(nodeID, successor.getNodeID(), local) <= 0 && KoalaNodeUtilities.compareIDs(successor.getNodeID(), this.getID(), local) < 0) || 
	               (KoalaNodeUtilities.compareIDs(nodeID, successor.getNodeID(), local) >= 0 && KoalaNodeUtilities.compareIDs(successor.getNodeID(), this.getID(), local) < 0 && KoalaNodeUtilities.compareIDs(nodeID, this.getID(), local) > 0) || 
	               (KoalaNodeUtilities.compareIDs(nodeID, successor.getNodeID(), local) <= 0 && KoalaNodeUtilities.compareIDs(nodeID, this.getID(), local) > 0) )
	                return true;
	        }
	        return false;
	}
	 
	private boolean canBePredecessor(String nodeID){
        boolean local = this.isLocal(nodeID);
        KoalaNeighbor predecessor = local ? getRoutingTable().getLocalPredecessor() : getRoutingTable().getGlobalPredecessor();
        if (KoalaNodeUtilities.isDefault(predecessor))
            return true;
        else{
            if((KoalaNodeUtilities.compareIDs(nodeID, predecessor.getNodeID(), local) >= 0 && KoalaNodeUtilities.compareIDs(predecessor.getNodeID(), this.getID(), local) > 0) || 
               (KoalaNodeUtilities.compareIDs(nodeID, predecessor.getNodeID(), local) <= 0 && KoalaNodeUtilities.compareIDs(predecessor.getNodeID(), this.getID(), local) > 0 && KoalaNodeUtilities.compareIDs(nodeID, this.getID(), local) < 0) || 
               (KoalaNodeUtilities.compareIDs(nodeID, predecessor.getNodeID(), local) >= 0 && KoalaNodeUtilities.compareIDs(nodeID, this.getID(), local) < 0) )
                return true;
        }
        return false;
	}
    
    	
	public boolean isLocal(String id){
		return KoalaNodeUtilities.getDCID(this.getID()) == KoalaNodeUtilities.getDCID(id); 
	}

	
	
    public void updateLatencyPerDC(String id, int l, int lq){    	
    
        if (lq > 1)
            latencyPerDC.put(KoalaNodeUtilities.getDCID(id), l);

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
    		int ndc = KoalaNodeUtilities.getDCID(n.getNodeID());
    		if(n.getLatencyQuality() < 2 &&  latencyPerDC.keySet().contains(ndc)){
    			n.setLatency(2);
    			n.setLatency(latencyPerDC.get(ndc));
    		}
    			
    	}
    }
    	
	
    public String getRoute(String dest) {
    	String ret = null;
    	double v, max = 0;
    	Set<KoalaNeighbor> rt = getRoutingTable().getNeighbors();
    	for(KoalaNeighbor re : rt){
    		v = getRouteValue(dest, re);
    	    if (v > max){
    	    	max = v;
    	    	ret = re.getNodeID();
    	    }
    	}
    	return ret;
	}

	private double getRouteValue(String dest, KoalaNeighbor re) {
		double res = 0;

        if( KoalaNodeUtilities.distance(this.getID(), dest) < KoalaNodeUtilities.distance(re.getNodeID(), dest))
            res = -1;

        if( this.dcID == KoalaNodeUtilities.getDCID(re.getNodeID()))
            res = KoalaNodeUtilities.A * KoalaNodeUtilities.distance(this.getID(), re.getNodeID());

        if( KoalaNodeUtilities.distance(this.getID(), dest) > KoalaNodeUtilities.distance(re.getNodeID(), dest)){
            int tot_distance = KoalaNodeUtilities.distance(this.getID(), dest);
            double distance = (double) KoalaNodeUtilities.distance(this.getID(), re.getNodeID()) / tot_distance;
            double norm_latency = KoalaNodeUtilities.normalizeLatency(tot_distance, re.getLatency());
            res = 1 + KoalaNodeUtilities.B * distance + KoalaNodeUtilities.C * norm_latency;
        }
        if( KoalaNodeUtilities.getDCID(dest) == KoalaNodeUtilities.getDCID(re.getNodeID()))
            res = Integer.MAX_VALUE - KoalaNodeUtilities.A * KoalaNodeUtilities.distance(re.getNodeID(), dest);
        return res;
	}
	
	public Set<String> createRandomIDs(int nr){
        Set<String> rids = new HashSet<String>();
        while( rids.size() < nr){
            String rand_id = this.dcID + "-" + new Random().nextInt(KoalaNodeUtilities.NR_NODE_PER_DC);
            if( !this.isResponsible(rand_id))
                rids.add(rand_id);
            
        }
        return rids;
	}



	public boolean isResponsible(String id) {
		
		if (KoalaNodeUtilities.isDefault(getRoutingTable().getLocalSucessor()))
            return false;
        return KoalaNodeUtilities.distance(getID(), id) < KoalaNodeUtilities.distance(getRoutingTable().getLocalSucessor().getNodeID(), id) 
                && KoalaNodeUtilities.distance(getID(), id) < KoalaNodeUtilities.distance(getRoutingTable().getLocalPredecessor().getNodeID(), id);
		
	}



	/* Methods which are there just to comply with the interfaces but not implemented */
	
	
	@Override
	public void onKill() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean addNeighbor(Node neighbour) {
		return physicalNeighbors.add(neighbour);
	}

	@Override
	public boolean contains(Node neighbor) {
		for(Node neigh : physicalNeighbors)
			if(neigh.equals(neighbor))
				return true;
		return false;
	}

	@Override
	public int degree() {
		return physicalNeighbors.size();
	}

	@Override
	public Node getNeighbor(int i) {
		return physicalNeighbors.get(i);
	}

	@Override
	public void pack() {
		// TODO Auto-generated method stub
		
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
