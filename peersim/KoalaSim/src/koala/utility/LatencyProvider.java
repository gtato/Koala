package koala.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class LatencyProvider {
	
	  
	private static ArrayList<String> gatewayIDs = new ArrayList<String>();
	private static HashMap<String, Double> latencies = new HashMap<String, Double>();
	
	public static void addLatency(String src, String dst, double latency){
		String id = getKeyID(src, dst);
		if(!latencies.containsKey(id))
			latencies.put(id, round(latency));
	}
	
	public static void addGatewayID(String gwID){
		gatewayIDs.add(gwID);
	}
	
	public static void printLatencies(){
		for(String srcdst : latencies.keySet()){
			System.out.println(srcdst + " " + latencies.get(srcdst) );
		}
	}
	
	public static double getLatency(String src, String dst){
		if(src.equals(dst))
			return 0;
		
		int srcDC = NodeUtilities.getDCID(src); 
		int dstDC = NodeUtilities.getDCID(dst);
		if(srcDC == dstDC){
			double intraLatency = round(getIntraDCLatency(srcDC)); 
			if(gatewayIDs.contains(src) || gatewayIDs.contains(dst))
				return intraLatency;
			return round(2 * intraLatency);
		}
		if(latencies.containsKey(getKeyID(src, dst)))
			return latencies.get(getKeyID(src, dst));
		else{
			String gwSrc = getGW(src);
			String gwDst = getGW(dst);
			return getLatency(src, gwSrc) +
				   getLatency(gwSrc, gwDst) +
				   getLatency(gwDst, dst);
					
		}
		
	}
	
	public static double getIntraDCLatency(int dcID){
		Random random = new Random(dcID);
		double min, max;
		max = 2.5;
		min = 0.2;
		return random.nextDouble() * (max - min) + min; 
	}
	
	private static String getKeyID(String src, String dst){
		if (NodeUtilities.compare(src, dst) < 0)
			return src+"|"+dst;
		else
			return dst+"|"+src;
	}
	
	public static double round(double tr){
		return Math.round(tr * 100.0 ) / 100.0;
	}

	
	private static String getGW(String id){
		for(String gw : gatewayIDs){
			if(NodeUtilities.getDCID(gw) == NodeUtilities.getDCID(id))
				return gw;
		}
		return null;
	}
}
