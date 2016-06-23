package koala.utility;

import koala.KoalaNeighbor;
import koala.RenaterNode;
import peersim.config.Configuration;
import peersim.core.Node;

public class NodeUtilities {
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
		return n == null || n.getNodeID() == NodeUtilities.DEFAULTID;
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

	public static double normalizeLatency(int totDistance, double latency) {
		double x1 = 1;
        double y1 = 1;
        double x2 = NodeUtilities.MAX_INTER_LATENCY;
        double y2 = (double) 1 / totDistance;

        double sl = (double) (y2-y1)/(x2 - x1);

        double y = (double) sl * (latency - x1) + y1;
        return y;
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
        distance = Math.round(distance * 100000.0) / 100.0; //in km
        return distance; 
    }
	
	public static double[] getCoordinatesBetweenTwoPoints(double[] p1, double[] p2){
		double[] cords = new double[2];
		double minX = Math.min(p1[0], p2[0]);
		double minY = Math.min(p1[1], p2[1]);
		cords[0] = minX + Math.abs(p1[0] - p2[0])/2;
		cords[1] = minY + Math.abs(p1[1] - p2[1])/2;
		return cords;
	}
	
}
