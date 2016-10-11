package utilities;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import koala.KoalaNode;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import renater.RenaterNode;

public class PhysicalDataProvider {
	
	  
//	private static Set<String> gatewayIDs = new HashSet<String>();
	private static HashMap<String, Double> latencies = new HashMap<String, Double>();
	private static HashMap<String, String> paths= new HashMap<String, String>();
	private static double maxInterLatency = 0;
	private static double minInterLatency = Double.MAX_VALUE;
	private static double avgInterLatency = 0;
	private static double stdInterLatency = 0;
	
	private static double maxInraLatency = 0;
	
//	private static double defaultInterLatency = 0;
//	private static double defaultIntraLatency = 0;
	public static String DijsktraFile = "out/dijkstra/dijsktra"+Network.size()+".dat";
	
	
	public static void addLatency(String src, String dst, double latency){
//		gatewayIDs.add(src);
//		gatewayIDs.add(dst);
		String id = NodeUtilities.getKeyStrID(src, dst);
		if(!latencies.containsKey(id)){
			latencies.put(id, round(latency));
			if(latency > maxInterLatency)
				maxInterLatency = latency;
			if(!src.equals(dst) && latency < minInterLatency)
				minInterLatency = latency;
		}
	}
	
	public static void addPath(String srcdst, String strPath){
		if(strPath == null)
			return;
		if(!paths.containsKey(srcdst)){
			paths.put(srcdst, strPath);
		}
	}
	
	public static void addPath(String src, String dst, LinkedList<RenaterNode> path){
		if(path == null)
			return;
		
		String id = NodeUtilities.getKeyStrID(src, dst);
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
	
	public static void addPath(String src, String dst, String path){
		if(path == null)
			return;
		
		String id = NodeUtilities.getKeyStrID(src, dst);
		if(!paths.containsKey(id)){
			paths.put(id, path.substring(1, path.length()-1).replaceAll(", ", " " ));
		}
	}
	
//	public static void addGatewayID(String gwID){
//		gatewayIDs.add(gwID);
//	}
	
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
			if(NodeUtilities.getRenaterNode(src).isGateway() || NodeUtilities.getRenaterNode(dst).isGateway())
				return intraLatency;
			return round(2 * intraLatency);
		}
		double dclat = getDCLatency(src, dst);
		if(dclat > 0)
			return dclat;
		else{
			String gwSrc = getGW(src);
			String gwDst = getGW(dst);
			return getLatency(src, gwSrc) +
				   getLatency(gwSrc, gwDst) +
				   getLatency(gwDst, dst);
					
		}
		
	}
	
	private static double getDCLatency(String src, String dst){
		Double d = -1.0;
		if (Configuration.getBoolean("dijkstraplus", false)){
			 d = KoaLite.getLatency(src, dst);
			 if(d==null)
				 return -1.0;
		}else{
			if(latencies.containsKey(NodeUtilities.getKeyStrID(src, dst)))
				return latencies.get(NodeUtilities.getKeyStrID(src, dst));
		}
		
		
		return d;
	}
	

	
	
	public static void setLatencyStats(){

		if (!Configuration.getBoolean("dijkstraplus", false)){
		//		double avg, std, tot;
			double tot = 0;
			int i = 0;
			ArrayList<Double> lats = new ArrayList<Double>(latencies.values());
			for(Double lat : latencies.values()){
				tot += lat;
				i++;
			}
			avgInterLatency = tot/i;
			
			double sum=0;
			for (Double lat : latencies.values()) 
				sum += Math.pow(lat - avgInterLatency, 2);
			
			stdInterLatency = Math.sqrt(sum/latencies.size());
		}else{
			avgInterLatency = KoaLite.getAverageLatency();
			stdInterLatency  = KoaLite.getStdLatency(avgInterLatency);
		}
	}
	
	public static void printLatencyStats(){
		double avg, std, tot;
		tot = 0;
		int i = 0;
		ArrayList<Double> lats = new ArrayList<Double>(latencies.values());
		for(Double lat : latencies.values()){
			tot += lat;
			i++;
		}
		avg = tot/i;
		
		double sum=0;
		for (Double lat : latencies.values()) 
			sum += Math.pow(lat - avg, 2);
		
		std = Math.sqrt(sum/latencies.size());
		
		int betweenStd = 0;
		for(Double lat : latencies.values()){
			if(lat < avg + 2*std && lat > avg -2*std)
				betweenStd++; 
		}
		
		
		System.out.println("min: "+ minInterLatency + ", avg: " + avg + ", max: "
		+ maxInterLatency + ", std: " + std + ", bettwen +-std: " + ((double)betweenStd/latencies.size())*100 + "%" );
		
//		Collections.sort(lats);
//		for(int j=0; j < lats.size(); j++){
//			System.out.println(j+" " + lats.get(j));
//		}
			
	}
	
	public static double getMaxInterLatency(){
		return maxInterLatency;
	}
	
	public static double getAvgInterLatency(){
		return avgInterLatency;
	}
	
	public static void setAvgInterLatency(Double avg){
		avgInterLatency = avg;
	}

	public static double getStdInterLatency(){
		return stdInterLatency;
	}
	
	public static double getMaxIntraLatency(){
		return maxInraLatency * NodeUtilities.NR_NODE_PER_DC;
	}
	
	public static double getMinInterLatency(){
		return minInterLatency;
	}
	
	public static double getDefaultInterLatency(){
		return getAvgInterLatency();
//		return getMaxIntraLatency()/32;
//		return getMaxIntraLatency()/64;
	}
	
	public static double getDefaultIntraLatency(){
		return getMaxIntraLatency()/16;
	}
	
	public static String getPath(String src, String dst){
		if(src.equals(dst))
			return src;
		
		int srcDC = NodeUtilities.getDCID(src); 
		int dstDC = NodeUtilities.getDCID(dst);
		if(srcDC == dstDC){
			if(NodeUtilities.getRenaterNode(src).isGateway() || NodeUtilities.getRenaterNode(dst).isGateway())
				return src + " " + dst; 
			return src +" " + getGW(src)  + " " + dst;
		}
		String path = getDCPath(src, dst);
		if(path != null){
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
	
	private static String getDCPath(String src, String dst){
		
		if (Configuration.getBoolean("dijkstraplus", false)){
			 return KoaLite.getPath(src, dst).toString().replace("[", "").replace("]", "").replace(",", "");
		}else{
			if(paths.containsKey(NodeUtilities.getKeyStrID(src, dst)))
				return paths.get(NodeUtilities.getKeyStrID(src, dst));
		}
		
		
		return null;
	}
	
	public static void saveRoutes(){
		try {
			FileOutputStream fos = new FileOutputStream(DijsktraFile);
            PrintStream ps =  new PrintStream(fos);
			for(String srcdst : latencies.keySet()){
				ps.println(srcdst + " " + latencies.get(srcdst) + " (" + paths.get(srcdst) + ")" );
			}
			fos.close();
            ps.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
	
	public static double getIntraDCLatency(int dcID){
		Random random = new Random(dcID);
		double latency;
		double min, max;
//		max = 0.5;
//		min = 0.05;
		max = 0.0005;
		min = 0.00005;
//		latency = 1.8;
		latency  = random.nextDouble() * (max - min) + min;
		if (latency > maxInraLatency)
			maxInraLatency = latency;
		return latency;
	}
	
	
	public static double round(double tr){
		for(double i=100.0; i <= 100000.0; i*=10){
			double ret = Math.round(tr * i ) / i;
			if(ret != 0)
				return ret;
		}
		return 0;
		
//		return Math.round(tr * 100.0 ) / 100.0;
		
//		return Math.round(tr * 100000.0 ) / 100000.0;
	}

	
	private static String getGW(String id){
		return NodeUtilities.getRenaterNode(id).getGateway();
	}
	
	public static double getPhysicalDistance(RenaterNode first, RenaterNode second, double worldSize) {
        double x1 = first.getX();
        double x2 = second.getX();
        double y1 = first.getY();
        double y2 = second.getY();
        if (x1 == -1 || x2 == -1 || y1 == -1 || y2 == -1)
        // NOTE: in release 1.0 the line above incorrectly contains
        // |-s instead of ||. Use latest CVS version, or fix it by hand.
            throw new RuntimeException(
                    "Found un-initialized coordinate. Use e.g.,InetInitializer class in the config file.");
        double distance = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        distance *= 1000 * worldSize;
        distance = Math.round(distance * 100.0) / 100.0; //in km
        return distance; 
    }

	public static void clearLists() {
		latencies.clear();
		paths.clear();
		
	}

	public static void loadRoutes() {
		try (BufferedReader br = Files.newBufferedReader(Paths.get(DijsktraFile), StandardCharsets.UTF_8)) {
		    for (String line = null; (line = br.readLine()) != null;) {
		    	int firstSpace = line.indexOf(" ");
		    	String ids = line.substring(0, firstSpace);
		    	String[] srcdst = NodeUtilities.getStrIDsFromKey(ids);
		    	
		    	int secondSpace = line.indexOf(" ",firstSpace+1);
		    	String lat = line.substring(firstSpace+1, secondSpace);
		    	double latency = Double.parseDouble(lat);
		    	
		    	String path = line.substring(secondSpace+2, line.length()-1);
		    	
		    	addLatency(srcdst[0], srcdst[1], latency);
		    	addPath(ids, path);
//			    System.out.println(srcdst[0] + "->" + srcdst[1] + " " + latency + " " + path);
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static double getBitRate(){
		double[] bitrates = {1e9, 2.5e9, 10e9};
		int rand = CommonState.r.nextInt(100);
		if(rand < 90)
			return bitrates[2];
		else if(rand >= 90 && rand < 97)
			return bitrates[1];
		else
			return bitrates[0];
	}
	
	public static double getSpeed(){
		double[] speeds = {2e8, 3e8};
		return speeds[1];
	}

}
