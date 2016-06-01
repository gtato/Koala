package koala;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class Util {
	private static Gson gson;
	
	public static void intitialize(KoalaNode sample){
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(KoalaNode.class, new KoalaNode.KoalaNodeSerializer());
		gsonBuilder.registerTypeAdapter(KoalaNode.class, new KoalaNode.KoalaNodeDeserializer(sample));
		gson = gsonBuilder.create();
	}
	
	public static String nodeToJson(KoalaNode k){
		return gson.toJson(k);
	}
	
	public static KoalaNode jsonToNode(String jsonode){
		return gson.fromJson(jsonode, KoalaNode.class);
	}
	
	
	public static JsonElement neighborToJsonTree(KoalaNeighbor k){
		return gson.toJsonTree(k);
	}
	
	public static KoalaNeighbor jsonToNeighbor(JsonElement jsonNeighbor){
		return gson.fromJson(jsonNeighbor, KoalaNeighbor.class);
	}
}
