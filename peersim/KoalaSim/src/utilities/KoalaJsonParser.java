package utilities;

import messaging.KoalaMessage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class KoalaJsonParser {
	private static Gson gson;
	
	public static void intitialize(){
		GsonBuilder gsonBuilder = new GsonBuilder();
		
		gsonBuilder.registerTypeAdapter(KoalaMessage.class, new KoalaMessage.KoalaMessageSerializer());
		gsonBuilder.registerTypeAdapter(KoalaMessage.class, new KoalaMessage.KoalaMessageDeserializer());
		
		gson = gsonBuilder.create();
	}
	
	public static String toJson(Object k){
		return gson.toJson(k);
	}
	
	public static <T> T jsonToObject(String jsonode, Class<T> c){
		return gson.fromJson(jsonode, c);
	}

	
	public static JsonElement toJsonTree(Object k){
		return gson.toJsonTree(k);
	}
	
	public static <T> T jsonTreeToObject(JsonElement json, Class<T> c){
		return gson.fromJson(json, c);
	}
	
	
}
