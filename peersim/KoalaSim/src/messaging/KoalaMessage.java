package messaging;

import java.lang.reflect.Type;
import java.util.ArrayList;

import koala.utility.KoalaJsonParser;
import koala.utility.PhysicalDataProvider;

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
	
	
	private int type;

	private KoalaMsgContent content;
	/*if set to true, it means that you shouldn't share it with the rest of the DC*/
	private boolean confidential;
	private double latency;
	private String source;
	private ArrayList<String> path = new ArrayList<String>();	
	private int id;
	
	
	
	public KoalaMessage(){}
	
	public KoalaMessage(String source, KoalaMsgContent content){
		this.source = source;
		this.type = content.getMsgType();
		this.content = content;
		latency = 0;
	}

	public KoalaMessage(String source, KoalaMsgContent content, boolean confidential){
		this.source = source;
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
		return PhysicalDataProvider.round(latency);
	}

	public void setLatency(double latency) {
		this.latency = latency;
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
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
				//fullpath += fpStr[i] + " "; 
		}
		fpList.add(fpStr[fpStr.length-1]);
		//fullpath += fpStr[fpStr.length-1];
		
		return fpList;
	}
	
	public String getLastSenderNode(){
		if(path.size()==0)
			return null;
		return path.get(path.size()-1);
	}
	
	public String getSecondLastSenderNode(){
		if(path.size()<=1)
			return null;
		return path.get(path.size()-2);
	}
	
//	public void setRandomLatency(String sourceID, String destID){
//        int sDC = NodeUtilities.getDCID(sourceID);
//        int dDC = NodeUtilities.getDCID(destID);
//
//        Random random = new Random(sDC*dDC);
//        int min, max;
//         
//        if (sDC == dDC){
//            min = 5; max = NodeUtilities.MAX_INTRA_LATENCY;
//        }else{
//        	min = NodeUtilities.MAX_INTRA_LATENCY; max = NodeUtilities.MAX_INTER_LATENCY;
//        }
//        
//        latency = random.nextInt((max - min) + 1) + min;
//        
//	}
	
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
			
			JsonObject obj = new JsonObject();
			obj.addProperty("id", src.getID());
			obj.addProperty("source", src.getSource());
			obj.addProperty("type", src.getType());
			obj.addProperty("confidential", src.isConfidential());
			obj.addProperty("latency", src.getLatency());
			obj.add("path", (JsonElement)pathEntries);
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
			km.setSource(srcJO.get("source").getAsString());
			km.setType(srcJO.get("type").getAsInt());
			km.setConfidential(srcJO.get("confidential").getAsBoolean());
			km.setLatency(srcJO.get("latency").getAsDouble());
			
			
			JsonArray jpath = srcJO.getAsJsonArray("path");
			ArrayList<String> path = new ArrayList<String>();
			for(JsonElement entry : jpath)
				path.add(entry.getAsString());

			km.setPath(path);
			km.setContent(KoalaJsonParser.jsonTreeToObject(srcJO.get("content"), km.getContentClassFromType()));
			
			return km;
		}
		
	}
	
}
