package utilities;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import messaging.TopologyMessage;
import chord.ChordNode;
import chord.ChordProtocol;
import koala.KoalaNeighbor;
import koala.KoalaNode;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import renater.RenaterNode;
import topology.TopologyNode;

public class NodeUtilities {
	public static final String DEFAULTID = "xxx";

	
	public static double A = 0;
	public static double B = 0; //a.k.a alpha
	public static double C = 0; //a.k.a beta 
	
	public static int DijkstraRAM = 1;
	public static int DijkstraDB = 2;
	public static int DijkstraSPAAS = 3;
	public static int DijkstraMethod;
	
	public static int MAGIC = 2;
	
	
	
//	public static double MAX_INTER_LATENCY = 2000;
//	public static double MAX_INTRA_LATENCY = 500;
	
	public static int NR_NODE_PER_DC = 0; //Configuration.getInt("NR_NODE_PER_DC")
	public static int NR_DC = 0; //Configuration.getInt("NR_DC")
	public static int ACTUAL_NR_DC = 0;
	
	public static int RID = 0;
	public static int KID = 0;
	public static int CID = 0;
	
	public static double[] hopCategories;
	public static double[] latencyCategories;
	
	public static int PiggybackLength = 0;
	public static double WORLD_SIZE = 1.0;
	
	public static int SUCC_SIZE = 0;
	public static int M = 0;
	public static int NEIGHBORS = Configuration.getInt("NEIGHBORS", 2);
	public static int LONG_LINKS = Configuration.getInt("LONG_LINKS", 3);
	
	public static Map<String, Node> Nodes =  new HashMap<String, Node>();
	public static HashMap<BigInteger, Node> CHORD_NODES = new HashMap<BigInteger, Node>();
	public static Map<String, RenaterNode> Gateways =  new HashMap<String, RenaterNode>();
	public static double[][] CenterPerDC;

	public static void initialize(){
		NR_NODE_PER_DC = Configuration.getInt("NR_NODE_PER_DC");
		NR_DC = Configuration.getInt("NR_DC");
//		A = (double) 1 / NR_DC;
		A = (double) 1 / NR_NODE_PER_DC;
		B = Configuration.getDouble("ALPHA", 0.5);
		C = 1-B;
		
		SUCC_SIZE = Configuration.getInt("SUCC_SIZE", 4);
		M = Configuration.getInt("M", 10);
		
//		NEIGHBORS= Configuration.getInt("NEIGHBORS", 2);
		
		PiggybackLength = Configuration.getInt("koala.settings.piggyback", 10);
		String dijktraStr = Configuration.getString("koala.settings.dijkstramethod", "ram");
		if(dijktraStr.equalsIgnoreCase("db"))
			DijkstraMethod = DijkstraDB;
		else if (dijktraStr.equalsIgnoreCase("spaas"))
			DijkstraMethod = DijkstraSPAAS;
		else 
			DijkstraMethod = DijkstraRAM;
		
		WORLD_SIZE = Configuration.getDouble("koala.settings.world_size", 1.0);
	}
	
	public static void initializeCategories()
	{
		int nrCategories = Configuration.getInt("msg.categories",1);
		double latDiff = PhysicalDataProvider.getMaxInterLatency() - PhysicalDataProvider.getMinInterLatency();
		double latUnit = (double)latDiff/nrCategories;
		
		double hopUnit = (double)NR_DC/(2*nrCategories);
		
		hopCategories = new double[nrCategories-1];
		latencyCategories = new double[nrCategories-1];
		
		for(int i=0; i < latencyCategories.length;i++){
			if(i==0){
				latencyCategories[i] = latUnit;
				hopCategories[i] = hopUnit;
			}else{
				latencyCategories[i] = latencyCategories[i-1] + latUnit;
				hopCategories[i] = hopCategories[i-1] + hopUnit;
			}
		}
		System.out.println();
	}
	
	
	public static void intializeDCCenters(List<String> lines){
		CenterPerDC = new double[NR_DC][2];
		
		for (int i = 0; i < NR_DC; i++){
          	if(lines != null){
          		CenterPerDC[i][0] = Double.parseDouble(lines.get(i).split(", ")[0]);
          		CenterPerDC[i][1] = Double.parseDouble(lines.get(i).split(", ")[1]);
        	}else{
        		CenterPerDC[i][0] = CommonState.r.nextDouble();
        		CenterPerDC[i][1] = CommonState.r.nextDouble();
        	}
        }
	}
	
	
	public static void setIDs(int rid, int kid, int cid){
		RID = rid;
		KID = kid;
		CID = cid;
	}
	
	public static RenaterNode getRenaterNode(String id){
		return (RenaterNode) Nodes.get(id).getProtocol(RID);
	}
	
	public static KoalaNode getKoalaNode(String id){
		return (KoalaNode) Nodes.get(id).getProtocol(KID);
	}
	
	public static ChordNode getChordNode(String id){
		return (ChordNode) Nodes.get(id).getProtocol(CID);
	}
	
	public static ChordNode getChordNodeByCID(BigInteger id){
		return (ChordNode) CHORD_NODES.get(id).getProtocol(CID);
	}
	
	public static ChordNode getChordFromNode(Node n){
		return (ChordNode) n.getProtocol(CID);
	}
	
	public static boolean isUp(String nodeid){
		Node n = Nodes.get(nodeid);
		if(n != null)
			return n.isUp();
		return false;
	}
	
	public static int getDCID(String id){
		return Integer.parseInt(id.split("-")[0]);
	}
	
	public static String getStrDCID(String id){
		return id.split("-")[0];
	}
	
	public static int getNodeID(String id){
		return Integer.parseInt(id.split("-")[1]);
	}
	
	public static boolean sameDC(TopologyNode tp1, TopologyNode tp2){
		if(getDCID(tp1.getID()) == getDCID(tp2.getID()))
			return true;
		return false;
	}
	
	public static boolean sameDC(String nodeID1, String nodeID2){
		if(getDCID(nodeID1) == getDCID(nodeID2))
			return true;
		return false;
	}
	
	public static int distance(String srcID, String targetID){
		return distance(srcID, targetID, false);
	}
	
	public static int distance(String srcID, String targetID, boolean forceLocal){
		if(srcID.equals(NodeUtilities.DEFAULTID) || targetID.equals(NodeUtilities.DEFAULTID))
			return Integer.MAX_VALUE;
		
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
		return n == null || n.getNodeID().equals(NodeUtilities.DEFAULTID);
	}
	
	public static boolean isDefault(String n){
		return n == null || n.equals(NodeUtilities.DEFAULTID);
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
	
	public static String getKeyStrID(String src, String dst){
		if (NodeUtilities.compare(src, dst) < 0)
			return src+"|"+dst;
		else
			return dst+"|"+src;
	}
	
	public static String[] getStrIDsFromKey(String key){
		String[] sp = key.split("\\|");
		return new String[]{sp[0], sp[1]};
	}
	
	public static String getKeyID(int src, int dst){
		if (src < dst)
			return src+"|"+dst;
		else
			return dst+"|"+src;
	}
	
	public static int[] getIDsFromKey(String key){
		String[] sp = key.split("\\|");
		return new int[]{Integer.parseInt(sp[0]), Integer.parseInt(sp[1])};
	}
	
	public static String[] getIDFromDistance(String id, int distance, boolean local){
		String[] ids = new String[2];
		if(local){
			int retid1 = (getNodeID(id) + distance) % NR_NODE_PER_DC;
			int retid2 = getNodeID(id) - distance >= 0 ? getNodeID(id) - distance : getNodeID(id) - distance + NR_NODE_PER_DC;
			ids[0] =  getDCID(id) + "-" + retid1;
			ids[1] =  getDCID(id) + "-" + retid2;
		}else{
			int retid1 = (getDCID(id) + distance) % NR_DC;
			int retid2 = getDCID(id) - distance >= 0 ? getDCID(id) - distance : getDCID(id) - distance + NR_DC;
			ids[0] =  retid1  + "-" + getNodeID(id);
			ids[1] =  retid2  + "-" + getNodeID(id);
		}
		
		return ids;
	}
	
	public static BigInteger generateNewChordID(){
		BigInteger newId;
		do
			newId= new BigInteger(M, CommonState.r);
		while(CHORD_NODES.containsKey(newId));
		
		return newId;
	}
	
//	public static double normalizeLatency(int totDistance, double latency) {
//		double x1 = PhysicalDataProvider.getMinInterLatency();
//        double y1 = 1;
//        double x2 = PhysicalDataProvider.getMaxInterLatency();
//        double y2 = (double) 1 / totDistance;
//
//        double sl = (double) (y2-y1)/(x2 - x1);
//
//        double y = (double) sl * (latency - x1) + y1;
//        return y;
//	}
	
	
//	marin's normalization
//	public static double normalizeLatency(int totDistance, double latency) {
//		return 1 - (latency/PhysicalDataProvider.getMaxInterLatency()); 
//	}
	
	public static double normalizeLatency(int totDistance, double latency) {
//		double x1 = PhysicalDataProvider.getAvgInterLatency() - 2*PhysicalDataProvider.getStdInterLatency();
		double x1 = PhysicalDataProvider.getMinInterLatency();
        double y1 = 1;
//        double x2 = PhysicalDataProvider.getAvgInterLatency() + 2*PhysicalDataProvider.getStdInterLatency();
        double x2 = PhysicalDataProvider.getMaxInterLatency();
        
        double y2 = (double) 1 / totDistance;

        if(latency > x2)
        	latency = x2;
        
        if(latency < x1)
        	latency = x1;
        
        double sl = (double) (y2-y1)/(x2 - x1);

        double y = (double) sl * (latency - x1) + y1;
        return y;
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
