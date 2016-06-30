package koala.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;


import peersim.core.CommonState;
import koala.RenaterNode;

public class PhysicalDataProvider {
	
	  
	private static ArrayList<String> gatewayIDs = new ArrayList<String>();
	private static HashMap<String, Double> latencies = new HashMap<String, Double>();
	private static HashMap<String, String> paths= new HashMap<String, String>();
	
	
	public static void addLatency(String src, String dst, double latency){
		String id = getKeyID(src, dst);
		if(!latencies.containsKey(id))
			latencies.put(id, round(latency));
	}
	
	public static void addPath(String src, String dst, LinkedList<RenaterNode> path){
		String id = getKeyID(src, dst);
		if(!paths.containsKey(id)){
			String strPath = "";
			for(RenaterNode rn : path){
				strPath += rn.getID() + " ";
			}
			strPath = strPath.trim();
//			strPath = strPath.trim().replace(" ", ", ");
			paths.put(id, strPath);
		}
	}
	
	public static void addGatewayID(String gwID){
		gatewayIDs.add(gwID);
	}
	
	public static void printLatencies(){
		for(String srcdst : latencies.keySet()){
			System.out.println(srcdst + " " + latencies.get(srcdst) );
		}
	}
	
	public static void printPaths(){
		for(String srcdst : paths.keySet()){
			System.out.println(srcdst + " " + paths.get(srcdst) );
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
	
	
	public static String getPath(String src, String dst){
		if(src.equals(dst))
			return src;
		
		int srcDC = NodeUtilities.getDCID(src); 
		int dstDC = NodeUtilities.getDCID(dst);
		if(srcDC == dstDC){
			if(gatewayIDs.contains(src) || gatewayIDs.contains(dst))
				return src + " " + dst; 
			return src +" " + getGW(src)  + " " + dst;
		}
		if(paths.containsKey(getKeyID(src, dst))){
			String path = paths.get(getKeyID(src, dst));
			if(path.startsWith(src))
				return path;
			String reversePath = "";
			String[] splitPath = path.split(" ");
			for(int i = splitPath.length-1; i >= 0; i--)
				reversePath += splitPath[i] + " ";
			return reversePath.trim();
		}else{
			String gwSrc = getGW(src);
			String gwDst = getGW(dst);
			String path1 = getPath(src, gwSrc)+ " ";
			String path2 = getPath(gwSrc, gwDst)+ " ";
			path2 = path2.substring(path2.indexOf(" ")+1);
			String path3 = getPath(gwDst, dst);
			path3 = path3.substring(path3.indexOf(" ")+1);
			return path1 + path2 + path3; 
		}
		
	}
	
	
	
	
	public static double getIntraDCLatency(int dcID){
		//Random random = new Random(dcID);
		
		double min, max;
		max = 2.5;
		min = 0.2;
		return 1.8;
//		return random.nextDouble() * (max - min) + min;
//		return CommonState.r.nextDouble() * (max - min) + min;
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
