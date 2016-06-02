package koala.utility;

import koala.KoalaNeighbor;
import koala.KoalaNode;
import koala.KoalaNode.KoalaNodeDeserializer;
import koala.KoalaNode.KoalaNodeSerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class KoalaJsonParser {
	private static Gson gson;
	
	public static void intitialize(KoalaNode sample){
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(KoalaNode.class, new KoalaNode.KoalaNodeSerializer());
		gsonBuilder.registerTypeAdapter(KoalaNode.class, new KoalaNode.KoalaNodeDeserializer(sample));
		gson = gsonBuilder.create();
	}
	
	public static String toJson(Object k){
		return gson.toJson(k);
	}
	
	public static <T> T jsonToObject(String jsonode, Class<T> c){
		return gson.fromJson(jsonode, c);
	}

	
	public static JsonElement neighborToJsonTree(KoalaNeighbor k){
		return gson.toJsonTree(k);
	}
	
	public static KoalaNeighbor jsonToNeighbor(JsonElement jsonNeighbor){
		return gson.fromJson(jsonNeighbor, KoalaNeighbor.class);
	}
}
