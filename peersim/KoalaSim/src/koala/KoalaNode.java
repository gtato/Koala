package koala;



import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Set;

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
	private static final long serialVersionUID = 1L;
	private int dcID;
	private int nodeID;
	private String bootstrapID;
	
	private boolean gateway;
	private boolean joined;
	private KoalaRoutingTable routingTable;
	        
	
	
	
	public KoalaNode(String prefix) {
		super(prefix);
	}
	
	public Object clone() {
		KoalaNode inp = null;
        inp = (KoalaNode) super.clone();
        inp.resetRoutingTable();
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
	
	public boolean hasJoined() {
		return joined;
	}

	public void setJoined(boolean joined) {
		this.joined = joined;
	}
	
	public KoalaRoutingTable getRoutingTable() {
		return routingTable;
	}
	
	public void resetRoutingTable() {
		routingTable = new KoalaRoutingTable(this.getID());
	}
	
	@Override
	public void onKill() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean addNeighbor(Node neighbour) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Node neighbor) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int degree() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Node getNeighbor(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void pack() {
		// TODO Auto-generated method stub
		
	}
	
	public String toString(){
		return getID();
	}
	
	
	public static class KoalaNodeSerializer implements JsonSerializer<KoalaNode> {

		@Override
		public JsonElement serialize(KoalaNode src, Type typeOfSrc, JsonSerializationContext context) {
			JsonArray neighbors = new JsonArray();
			Set<KoalaNeighbor> neigs = src.getRoutingTable().getNeighbours();
			for(KoalaNeighbor neig : neigs){
				neighbors.add(Util.neighborToJsonTree(neig));
			}
			
			JsonArray oldNeighbors = new JsonArray();
			ArrayList<KoalaNeighbor> oldNeigs = src.getRoutingTable().getOldNeighbors();
			for(KoalaNeighbor oldNeig : oldNeigs){
				oldNeighbors.add(Util.neighborToJsonTree(oldNeig));
			}
			
			JsonObject obj = new JsonObject();
			obj.addProperty("id", src.getID());
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
			JsonArray neigs = srcJO.getAsJsonArray("neighbors");
			ArrayList<KoalaNeighbor> neighbors = new ArrayList<KoalaNeighbor>();
			for(JsonElement neig : neigs)
				neighbors.add(Util.jsonToNeighbor(neig));
			
			JsonArray oldNeigs = srcJO.getAsJsonArray("oldNeighbors");
			ArrayList<KoalaNeighbor> oldNeighbors = new ArrayList<KoalaNeighbor>();
			for(JsonElement neig : oldNeigs)
				oldNeighbors.add(Util.jsonToNeighbor(neig));
			
			kn.getRoutingTable().setNeighbors(neighbors);
			kn.getRoutingTable().setOldNeighbors(oldNeighbors); 
			return kn;
		}
		
	}
	
}
