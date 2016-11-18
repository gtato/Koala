package messaging;

import java.lang.reflect.Type;
import java.util.ArrayList;

import koala.KoalaNeighbor;
import peersim.core.CommonState;
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

public class KoalaMessage extends TopologyMessage implements JsonSerializer< KoalaMessage>, JsonDeserializer<KoalaMessage>{
	public static final int RT = 0;
	public static final int NGN = 1;
	public static final int ROUTE = 2;
	public static final int JOIN = 3;
	public static final int LL = 4;
	
//	private static final int PiggybackLength = 10;
	

	/*if set to true, it means that you shouldn't share it with the rest of the DC*/
	private boolean confidential;
	
	private ArrayList<KoalaNeighbor> piggyback = new ArrayList<KoalaNeighbor>();
	
	
	public KoalaMessage(){
		super();
	}
	
	public KoalaMessage(KoalaMsgContent content){
		super(content);
	}

	public KoalaMessage(KoalaMsgContent content, boolean confidential){
		super(content);
		this.confidential = confidential;
	}
	
	public boolean isConfidential() {
		return confidential;
	}

	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
	}

	
	public ArrayList<KoalaNeighbor> getPiggyBack(){
		return piggyback;
	}

	
	public void setPiggyBack(ArrayList<KoalaNeighbor> p){
		piggyback = p;
	}
	
	
	public void setIdealPiggyBack(){
		for(int i = 0; i < NodeUtilities.PiggybackLength; i++){
			int dcID = CommonState.r.nextInt(NodeUtilities.NR_DC);
			KoalaNeighbor k = new KoalaNeighbor(NodeUtilities.DEFAULTID);
			k.setIdealID(dcID + "-0");
			piggyback.add(k);
			
		}
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
	
	public Class<? extends TopologyMessageContent> getContentClassFromType(){		
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
	
	
	@Override
	public JsonElement serialize(KoalaMessage src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject obj = (JsonObject) super.serialize(src, typeOfSrc, context);
		obj.addProperty("confidential", src.isConfidential());
		JsonArray piggyEntries = new JsonArray();
		ArrayList<KoalaNeighbor> piggies= src.getPiggyBack();
		for(KoalaNeighbor pigg : piggies)
			piggyEntries.add(KoalaJsonParser.toJsonTree(pigg));
		obj.add("piggyback", piggyEntries);
		return obj;
	}
	
	
	@Override
	public KoalaMessage deserialize(JsonElement src, Type typeOfSrc, JsonDeserializationContext context) throws JsonParseException {
		JsonObject srcJO = src.getAsJsonObject();
		KoalaMessage km = (KoalaMessage)super.deserialize(new KoalaMessage(), src, typeOfSrc, context); 
		
		km.setConfidential(srcJO.get("confidential").getAsBoolean());		
				
		
		JsonArray jpig = srcJO.getAsJsonArray("piggyback");
		ArrayList<KoalaNeighbor> piggies = new ArrayList<KoalaNeighbor>();
		for(JsonElement entry : jpig)
			piggies.add(KoalaJsonParser.jsonTreeToObject(entry, KoalaNeighbor.class));
		km.setPiggyBack(piggies);
		
		return km;
		
	}

}
