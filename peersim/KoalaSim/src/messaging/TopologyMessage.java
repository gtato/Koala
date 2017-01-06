package messaging;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import utilities.KoalaJsonParser;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;

public abstract class TopologyMessage {
	protected ArrayList<Double> latencies = new ArrayList<Double>();
	protected ArrayList<String> path = new ArrayList<String>();
	protected int id;
	protected int type;
	protected String label;
	protected TopologyMessageContent content;
	
	
	private long sentCycle;
	private long receivedCycle;

	public TopologyMessage(){}
	
	public TopologyMessage(TopologyMessageContent content){
		this.type = content.getMsgType();
		this.content = content;
		
	}
	
	public abstract Class<? extends TopologyMessageContent>  getContentClassFromType();
	public abstract String getTypeName();
	
	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}
	
	public TopologyMessageContent getContent() {
		return content;
	}

	public void setContent(TopologyMessageContent msgContent) {
		this.content = msgContent;
	}
	
	public int getType() {
		return type;
	}
	
	public boolean isType(int type){
		return this.type == type;
	}
	
	public void setType(int msgType) {
		this.type = msgType;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public double getLatency() {
		if(this.latencies.size() == 0)
			return -1;
		return this.latencies.get(this.latencies.size()-1);
	}
	
	public double getTotalLatency() {
		double tl = 0;
		for(Double l : this.latencies)
			tl += l;
		return PhysicalDataProvider.round(tl);
	}

	public void addLatency(double latency) {
		double l = PhysicalDataProvider.round(latency);
		this.latencies.add(l);
		
	}
	
	public ArrayList<Double> getLatencies() {
		return latencies;
	}
	
	public void setLatencies(ArrayList<Double> lats) {
		this.latencies = lats;
	}
	
	
	public void addToPath(String destID){
		boolean alreadythere = false;
		if(path.size() > 0 && path.get(path.size()-1).equals(destID))
			alreadythere = true;
		
		if (destID != null && !alreadythere)
			path.add(destID);
	}
	
	public ArrayList<String> getPath(){
		return path;
	}
	
	public ArrayList<String> getChordPath(){
		ArrayList<String> chordPath = new ArrayList<String>();
		for(String nid : path){
			chordPath.add(NodeUtilities.getChordNode(nid).chordId.toString());
		}
		return chordPath;
	}
	
	public void setPath(ArrayList<String> p){
		path = p;
	}
	
	public long getSentCycle() {
		return sentCycle;
	}

	public void setSentCycle(long sentCycle) {
		this.sentCycle = sentCycle;
	}

	public long getReceivedCycle() {
		return receivedCycle;
	}

	public void setReceivedCycle(long receivedCycle) {
		this.receivedCycle = receivedCycle;
	}
	
	public String pathToString(){
		String pathstr = "";
		for(String p : path)
			pathstr += p+" ";
		pathstr = pathstr.trim().replace(" ", ", ");
		
		return pathstr;
	}
	
	public ArrayList<String> getPhysicalPathToString(){
		ArrayList<String> fpList = new ArrayList<String>();
		if(path.size() < 2)
			return path;
		
		String fullpath = "";
		for(int i=0; i < path.size()-1;i++){
			fullpath += PhysicalDataProvider.getPath(path.get(i), path.get(i+1)) + " ";
		}
		fullpath = fullpath.trim();
		String[] fpStr = fullpath.split(" ");
		
		fullpath = "";
		for(int i = 0; i<fpStr.length-1; i++){
			if(!fpStr[i].equals(fpStr[i+1]))
				fpList.add(fpStr[i]);
				
		}
		fpList.add(fpStr[fpStr.length-1]);
		
		
		return fpList;
	}
	public String getFirstSender() {
		if(path.size() > 0)
			return path.get(0);
		return null;
	}
	
	public String getLastSender(){
		if(path.size()==0)
			return null;
		if(path.size()==1)
			return path.get(0);
		return path.get(path.size()-2);
	}
	
	
	public String getReceiver() {
		if(path.size() > 0)
			return path.get(path.size()-1);
		return null;
	}
	
	public int getHopCategory(){
		int hops = NodeUtilities.distance(getFirstSender(), getReceiver());
		
		if(hops <= NodeUtilities.hopCategories[0])
			return 1;
		if(hops > NodeUtilities.hopCategories[NodeUtilities.hopCategories.length-1])
			return NodeUtilities.hopCategories.length+1;
		
		for(int i = 1; i < NodeUtilities.hopCategories.length; i++){
			if(NodeUtilities.hopCategories[i-1] < hops && hops <= NodeUtilities.hopCategories[i])
				return i+1;
		}
		return 0;
	}
	
	public int getHops(){
		return this.getPath().size()-1;
	}
	
	public int getLatencyCategory(){
		double lat = PhysicalDataProvider.getLatency(getFirstSender(), getReceiver());
		if(lat <= NodeUtilities.latencyCategories[0])
			return 1;
		if(lat > NodeUtilities.latencyCategories[NodeUtilities.latencyCategories.length-1])
			return NodeUtilities.latencyCategories.length+1;
		
		for(int i = 1; i < NodeUtilities.latencyCategories.length; i++){
			if(NodeUtilities.latencyCategories[i-1] < lat && lat <= NodeUtilities.latencyCategories[i])
				return i+1;
		}
		return 0;
	}
	
	

	
	public JsonElement serialize(TopologyMessage src, Type typeOfSrc, JsonSerializationContext context) {
		JsonArray pathEntries = new JsonArray();
		ArrayList<String> mpath = src.getPath();
		for(String neig : mpath){
			pathEntries.add(neig);
		}
		
		JsonArray latencyEntries = new JsonArray();
		ArrayList<Double> mlatency= src.getLatencies();
		for(Double lat : mlatency)
			latencyEntries.add(lat);
		
		
		JsonObject obj = new JsonObject();
		obj.addProperty("id", src.getID());
		obj.addProperty("type", src.getType());
		obj.addProperty("label", src.getLabel());
		obj.add("path", (JsonElement)pathEntries);
		obj.add("latencies", (JsonElement)latencyEntries);
		obj.add("content", KoalaJsonParser.toJsonTree(src.getContent()));
		return obj;
	}
	
	
	public TopologyMessage deserialize(TopologyMessage km, JsonElement src, Type typeOfSrc, JsonDeserializationContext context) throws JsonParseException {
		JsonObject srcJO = src.getAsJsonObject();
		km.setID(srcJO.get("id").getAsInt());
		km.setType(srcJO.get("type").getAsInt());
		km.setLabel(srcJO.get("label").getAsString());
		JsonArray jpath = srcJO.getAsJsonArray("path");
		ArrayList<String> path = new ArrayList<String>();
		for(JsonElement entry : jpath)
			path.add(entry.getAsString());

		km.setPath(path);
		
		JsonArray jlat = srcJO.getAsJsonArray("latencies");
		ArrayList<Double> latencies = new ArrayList<Double>();
		for(JsonElement entry : jlat)
			latencies.add(entry.getAsDouble());
		km.setLatencies(latencies);
		
		km.setContent(KoalaJsonParser.jsonTreeToObject(srcJO.get("content"), km.getContentClassFromType()));
		
		return km;
	}

	
}
