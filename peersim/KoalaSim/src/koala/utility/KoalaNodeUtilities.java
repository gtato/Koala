package koala.utility;

import koala.KoalaNeighbor;
import peersim.config.Configuration;
import peersim.core.Node;

public class KoalaNodeUtilities {
	public static final String DEFAULTID = "xxx";
	
	public static double A = 0;
	public static double B = 0;
	public static double C = 0;
	
	public static int MAGIC = 2;
	
	public static int MAX_INTER_LATENCY = 2000;
	public static int MAX_INTRA_LATENCY = 500;
	
	public static int NR_NODE_PER_DC = 0; //Configuration.getInt("NR_NODE_PER_DC")
	public static int NR_DC = 0; //Configuration.getInt("NR_DC")
	
	public static void initialize(){
		A = B = C = 1;
		NR_NODE_PER_DC = Configuration.getInt("NR_NODE_PER_DC");
		NR_DC = Configuration.getInt("NR_DC");
		A = (double) 1 / NR_DC;
	}
	
	public static int getDCID(String id){
		return Integer.parseInt(id.split("-")[0]);
	}
	
	public static int getNodeID(String id){
		return Integer.parseInt(id.split("-")[1]);
	}
	
	public static int distance(String srcID, String targetID){
		return distance(srcID, targetID, false);
	}
	
	public static int distance(String srcID, String targetID, boolean forceLocal){
        boolean local = false;
        if (getDCID(srcID) == getDCID(targetID) || forceLocal)
            local = true;

        int src_id = local ? getNodeID(srcID) : getDCID(srcID); 
        int target_id = local ? getNodeID(targetID) : getDCID(targetID);
        int a = src_id;
        int b = target_id;
        if (src_id > target_id){
            a = target_id; 
            b = src_id;
        }
        
        int size = local ? NR_NODE_PER_DC : NR_DC;
        int d1 = b - a;
        int d2 = (size - b + a) % size;

        return Math.min(d1, d2);
	}
	
	public static boolean isDefault(KoalaNeighbor n){
		return n == null || n.getNodeID() == KoalaNodeUtilities.DEFAULTID;
	}
	
	public static int compare(String id1, String id2){
    	int compareGlobal = compareIDs(id1,id2, false);
    	if (compareGlobal != 0)
    		return compareGlobal;
    	return compareIDs(id1,id2, true);
    }
	
	public static int compareIDs(String id1, String id2, boolean local){
    	if(local)
    		return new Integer(getNodeID(id1)).compareTo(new Integer(getNodeID(id2)));
    	return new Integer(getDCID(id1)).compareTo(new Integer(getDCID(id2)));
    }

	public static double normalizeLatency(int totDistance, int latency) {
		double x1 = 1;
        double y1 = 1;
        double x2 = KoalaNodeUtilities.MAX_INTER_LATENCY;
        double y2 = (double) 1 / totDistance;

        double sl = (double) (y2-y1)/(x2 - x1);

        double y = (double) sl * (latency - x1) + y1;
        return y;
	}
	
	
}
