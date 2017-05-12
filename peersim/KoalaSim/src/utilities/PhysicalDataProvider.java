package utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import com.google.common.collect.Lists;

import peersim.core.CommonState;
import renater.RenaterNode;
import spaasclient.SPClient;

public class PhysicalDataProvider {
	
	  

	private static HashMap<String, Double> latencies = new HashMap<String, Double>();
	private static HashMap<String, ArrayList<String>> paths= new HashMap<String, ArrayList<String>>();
	private static double maxInterLatency = 0;
	private static double minInterLatency = Double.MAX_VALUE;
	private static double avgInterLatency = 0;
	private static double stdInterLatency = 0;
	
	private static double maxInraLatency = 0;
	
	public static long SimTime;
	
	
	
	public static void addLatency(String src, String dst, double latency){
		String id = NodeUtilities.getKeyStrID(src, dst);
		if(!latencies.containsKey(id)){
			latencies.put(id, round(latency));
			if(latency > maxInterLatency)
				maxInterLatency = latency;
			if(!src.equals(dst) && latency < minInterLatency)
				minInterLatency = latency;
		}
	}
	
	public static void addPath(String srcdst, ArrayList<String> strPath){
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
			ArrayList<String> strPath = new ArrayList<String>();
			for(RenaterNode rn : path)
				strPath.add(rn.getCID());
			paths.put(id, strPath);
		}
	}
	
	public static void addPath(String src, String dst, ArrayList<String> path){
		if(path == null)
			return;
		
		String id = NodeUtilities.getKeyStrID(src, dst);
		if(!paths.containsKey(id)){
			paths.put(id, path);
		}
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
		if (NodeUtilities.DijkstraMethod == NodeUtilities.DijkstraSPAAS 
			|| NodeUtilities.DijkstraMethod == NodeUtilities.DijkstraHipster)
			return maxInterLatency;
		return avgInterLatency + 2*stdInterLatency;
//		return maxInterLatency;
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
		if (NodeUtilities.DijkstraMethod == NodeUtilities.DijkstraSPAAS
		 || NodeUtilities.DijkstraMethod == NodeUtilities.DijkstraHipster)
			return minInterLatency;
		
		return avgInterLatency - 2*stdInterLatency;
//		return minInterLatency;
	}
	
	public static double getDefaultInterLatency(){
		return getAvgInterLatency();
//		return getMaxIntraLatency()/32;
//		return getMaxIntraLatency()/64;
	}
	
	public static double getDefaultIntraLatency(){
		return getMaxIntraLatency()/16;
	}
	
	public static void setLatencyStats(){

		if (NodeUtilities.DijkstraMethod == NodeUtilities.DijkstraDB){
			avgInterLatency = KoaLite.getAverageLatency();
			stdInterLatency  = KoaLite.getStdLatency(avgInterLatency);
		
		}else if (NodeUtilities.DijkstraMethod == NodeUtilities.DijkstraSPAAS){
			double[] minmax = SPClient.getMinMax();
			minInterLatency = minmax[0];
			maxInterLatency = minmax[1];
		}else if (NodeUtilities.DijkstraMethod == NodeUtilities.DijkstraHipster){
			double[] minmax = MyHipster.getMinMax();
			minInterLatency = minmax[0];
			maxInterLatency = minmax[1];
		}
		else{
//			double avg, std, tot;
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
		if(dclat <= 0){
			String gwSrc = getGW(src);
			String gwDst = getGW(dst);
			dclat = getLatency(src, gwSrc) +
				   getLatency(gwSrc, gwDst) +
				   getLatency(gwDst, dst);
					
		}
		return dclat;
		
	}
	
	private static double getDCLatency(String src, String dst){
		Double d = -1.0;
		if (NodeUtilities.DijkstraMethod == NodeUtilities.DijkstraDB){
			 d = KoaLite.getLatency(src, dst);
			 if(d==null)
				 return -1.0;
		}else if(NodeUtilities.DijkstraMethod == NodeUtilities.DijkstraSPAAS)
			return SPClient.getSP(src, dst).getLatency();
		else if(NodeUtilities.DijkstraMethod == NodeUtilities.DijkstraHipster)
			return MyHipster.getSPLatency(src,dst);
		else{
			if(latencies.containsKey(NodeUtilities.getKeyStrID(src, dst)))
				return latencies.get(NodeUtilities.getKeyStrID(src, dst));
		}
		
		
		return d;
	}
	
	
	public static ArrayList<String> getPath(String src, String dst){
		ArrayList<String> path = new ArrayList<String>();
		if(src.equals(dst))
			path.add(src);
		else{
			int srcDC = NodeUtilities.getDCID(src); 
			int dstDC = NodeUtilities.getDCID(dst);
			if(srcDC == dstDC){
				if(NodeUtilities.getRenaterNode(src).isGateway() || NodeUtilities.getRenaterNode(dst).isGateway()){
					path.add(src); path.add(dst); 
				}else{
					path.add(src); path.add(getGW(src)); path.add(dst);
				}
			}else{
				path.addAll(getDCPath(src, dst));
				if(path.size() > 0){
					if(!path.get(0).equals(src))
						Collections.reverse(path);
				}else{
					String gwSrc = getGW(src);
					String gwDst = getGW(dst);
					if ((gwSrc+gwDst).equals(src+dst)){
						System.out.println("Something is wrong");
						System.exit(1);
					}
					ArrayList<String> path1 = getPath(src, gwSrc);
					ArrayList<String> path2 = getPath(gwSrc, gwDst);
					ArrayList<String> path3 = getPath(gwDst, dst);
					path2.remove(0);
					path3.remove(0);
					path.addAll(path1);
					path.addAll(path2);
					path.addAll(path3);
				}
			}
		}
		return path;
		
	}
	
	private static ArrayList<String> getDCPath(String src, String dst){
		
		if (NodeUtilities.DijkstraMethod == NodeUtilities.DijkstraDB){
			 return KoaLite.getPath(src, dst);
		}else if(NodeUtilities.DijkstraMethod == NodeUtilities.DijkstraSPAAS) {
			return SPClient.getSP(src, dst).getPath();
		}else if (NodeUtilities.DijkstraMethod == NodeUtilities.DijkstraHipster)
			return MyHipster.getSPPath(src, dst);
		else{
			if(paths.containsKey(NodeUtilities.getKeyStrID(src, dst)))
				return paths.get(NodeUtilities.getKeyStrID(src, dst));
		}
		
		
		return new ArrayList<String>();
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
		RenaterNode rn = NodeUtilities.getRenaterNode(id);
		if(rn.isGateway())
			return rn.getCID();
		return rn.getGateway();
	}
	
	public static double getPhysicalDistance(RenaterNode first, RenaterNode second) {
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
        distance = adjustDistanceValue(distance);
        return distance; 
    }

    /*We assume that the maximum RTT is 500 ms
     * that means sqrt(2) maps to 500
     * **/
	public static double adjustDistanceValue(double distance){
//		distance *= 1000 *  NodeUtilities.WORLD_SIZE;
//		distance *= NodeUtilities.WORLD_SIZE;
//        distance = round(distance);
		distance = round((distance*250) / Math.sqrt(2));
        return distance;
	}
	public static void clearLists() {
		latencies.clear();
		paths.clear();
		
	}

	public static double getTheoreticalMaxLatency(){
		return adjustDistanceValue(Math.sqrt(2));
	}
	
	public static double getCloseLatency(){
		return NodeUtilities.CLOSE_LAT * PhysicalDataProvider.getTheoreticalMaxLatency(); //of max latency
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
