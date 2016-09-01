package messaging;

import java.lang.reflect.Type;
import java.util.ArrayList;

import utilities.KoalaJsonParser;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class KoalaMessage {
	public static final int RT = 0;
	public static final int NGN = 1;
	public static final int ROUTE = 2;
	public static final int JOIN = 3;
	public static final int LL = 4;
	
	private int type;

	private KoalaMsgContent content;
	/*if set to true, it means that you shouldn't share it with the rest of the DC*/
	private boolean confidential;
	private ArrayList<Double> latencies = new ArrayList<Double>();
	private ArrayList<String> path = new ArrayList<String>();	
	private int id;
	
	private long sentCycle;
	private long receivedCycle;
	
	public KoalaMessage(){}
	
	public KoalaMessage( KoalaMsgContent content){
		this.type = content.getMsgType();
		this.content = content;
	}

	public KoalaMessage(KoalaMsgContent content, boolean confidential){
		this.type = content.getMsgType();
		this.content = content;
		this.confidential = confidential;
	}
	
	public boolean isConfidential() {
		return confidential;
	}

	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
	}

	public int getType() {
		return type;
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

	public void setType(int msgType) {
		this.type = msgType;
	}

	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public KoalaMsgContent getContent() {
		return content;
	}

	public void setContent(KoalaMsgContent msgContent) {
		this.content = msgContent;
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
	
	public void setPath(ArrayList<String> p){
		path = p;
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

	public String getTypeName(){
		switch(type){
		case RT:
			return "RT";
		case NGN:
			return "NGN";
		case ROUTE:
			return "ROUTE";
		case JOIN:
			return "JOIN";
		case LL:
			return "LL";
	}
	return null;
	}
	
	public Class<? extends KoalaMsgContent> getContentClassFromType(){		
		switch(type){
			case RT:
				return KoalaRTMsgConent.class;
			case NGN:
				return KoalaNGNMsgContent.class;
			case ROUTE:
				return KoalaRouteMsgContent.class;
			case JOIN:
				return KoalaMsgContent.class;
			case LL:
				return KoalaMsgContent.class;
		}
		return null;
	}

	public static class KoalaMessageSerializer implements JsonSerializer<KoalaMessage> {

		@Override
		public JsonElement serialize(KoalaMessage src, Type typeOfSrc, JsonSerializationContext context) {
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
			obj.addProperty("confidential", src.isConfidential());
			obj.add("path", (JsonElement)pathEntries);
			obj.add("latencies", (JsonElement)latencyEntries);
			obj.add("content", KoalaJsonParser.toJsonTree(src.getContent()));
			return obj;
		}
		
	}
	
	public static class KoalaMessageDeserializer implements JsonDeserializer<KoalaMessage> {
		@Override
		public KoalaMessage deserialize(JsonElement src, Type typeOfSrc, JsonDeserializationContext context) throws JsonParseException {
			JsonObject srcJO = src.getAsJsonObject();
			KoalaMessage km = new KoalaMessage();
			km.setID(srcJO.get("id").getAsInt());
			km.setType(srcJO.get("type").getAsInt());
			km.setConfidential(srcJO.get("confidential").getAsBoolean());
			
			
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
	
}
