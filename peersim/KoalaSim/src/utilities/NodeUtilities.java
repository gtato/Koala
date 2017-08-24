package utilities;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import messaging.TopologyMessage;
import chord.ChordNode;
import chord.ChordProtocol;
import koala.KoalaNeighbor;
import koala.KoalaNode;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;
import renater.RenaterNode;
import topology.TopologyNode;

public class NodeUtilities {
	public static final String DEFAULTID = "xxx";

	public static int DijkstraRAM = 1;
	public static int DijkstraDB = 2;
	public static int DijkstraSPAAS = 3;
	public static int DijkstraHipster = 4;
	public static int DijkstraMethod;
	
	public static int MAGIC = 2;
	
	
//	public static double MAX_INTER_LATENCY = 2000;
//	public static double MAX_INTRA_LATENCY = 500;
	
	public static int CYCLES = Configuration.getInt("CYCLES", 100); 
	public static double ALPHA = Configuration.getDouble("ALPHA", 0.5);
	public static double BETA;
	
	public static int NR_NODE_PER_DC = Configuration.getInt("NR_NODE_PER_DC");
	public static int NR_DC = Configuration.getInt("NR_DC");
	public static int ACTUAL_NR_DC = 0;
	
	public static int RID = -1, KID = -1, CID = -1, FKID = -1, LKID = -1;
	public static int RPID = -1, KPID=-1, CPID=-1, FKPID=-1, LKPID=-1;
	public static int TRID = -1;
	public static int CURRENT_PID = -1;
	
	public static double[] hopCategories;
	public static double[] latencyCategories;
	
	public static int PiggybackLength = 0;
//	public static double WORLD_SIZE = 1.0;
	
	public static int SIZE = Configuration.getInt("SIZE", 0);
	public static int SUCC_SIZE = Configuration.getInt("SUCC_SIZE", 4);
	public static int M = 0;
	public static int C = Configuration.getInt("koala.settings.c", 1);
	public static int RAND_C = Configuration.getInt("koala.settings.random_c", 1);
	public static int NEIGHBORS = Configuration.getInt("koala.settings.neighbors", 2);
	public static int RAND_LINKS;
	public static int LONG_LINKS;
	public static int FLAT_LONG_LINKS;
	
	public static boolean COLLABORATE = Configuration.getBoolean("koala.settings.collaborate", false);
	public static int NR_COLLABORATORS = Configuration.getInt("koala.settings.collaborate_nr", 2);
	
	public static boolean NESTED = Configuration.getBoolean("koala.settings.nested", false);
	public static String LOCALITY = Configuration.getString("koala.settings.locality", "local");
	public static double CLOSE_LAT = Configuration.getDouble("koala.settings.close_latency_rate", 0.25);
	
	public static Map<String, Node> Nodes =  new HashMap<String, Node>();
	public static HashMap<String, Node> CHORD_NODES = new HashMap<String, Node>();
	public static Map<String, RenaterNode> Gateways =  new HashMap<String, RenaterNode>();
	public static double[][] CenterPerDC;

	public static HashMap<String, Node> UPS =  new HashMap<String, Node>();
	public static HashMap<String, Node> DOWNS =  new HashMap<String, Node>();
	public static HashMap<String, Node> ALT_DOWNS =  new HashMap<String, Node>();
//	public static HashMap<String, String> FlatMap =  new HashMap<String, String>();
	
	public static void initialize(){
//		SIZE = Configuration.getInt("SIZE", 0);
//		NR_NODE_PER_DC = Configuration.getInt("NR_NODE_PER_DC");
//		NR_DC = Configuration.getInt("NR_DC");
//		A = (double) 1 / NR_DC;
		BETA = (double) 1 / NR_NODE_PER_DC;
//		ALPHA = Configuration.getDouble("ALPHA", 0.5);
//		C = 1-B;
		
//		SUCC_SIZE = Configuration.getInt("SUCC_SIZE", 4);
		M = Configuration.getInt("M", getLog2(SIZE));
		LONG_LINKS = C * getLog2(NR_DC);
		FLAT_LONG_LINKS = C * M;
		RAND_LINKS = RAND_C * getLog2(NR_DC);
		
		PiggybackLength = Configuration.getInt("koala.settings.piggyback", 10);
		String dijktraStr = Configuration.getString("koala.settings.dijkstramethod", "ram");
		if(dijktraStr.equalsIgnoreCase("db"))
			DijkstraMethod = DijkstraDB;
		else if (dijktraStr.equalsIgnoreCase("spaas"))
			DijkstraMethod = DijkstraSPAAS;
		else if (dijktraStr.equalsIgnoreCase("hipster"))
			DijkstraMethod = DijkstraHipster;
		else
			DijkstraMethod = DijkstraRAM;
		
//		WORLD_SIZE = Configuration.getDouble("koala.settings.world_size", 1.0);
//		WORLD_SIZE = (double)NR_DC/1000;
//		WORLD_SIZE *= 5;
	}
	
//	public static void initializeCategories()
//	{
//		int nrCategories = Configuration.getInt("msg.categories",1);
//		double latDiff = PhysicalDataProvider.getMaxInterLatency() - PhysicalDataProvider.getMinInterLatency();
//		double latUnit = (double)latDiff/nrCategories;
//		
//		double hopUnit = (double)NR_DC/(2*nrCategories);
//		
//		hopCategories = new double[nrCategories-1];
//		latencyCategories = new double[nrCategories-1];
//		
//		for(int i=0; i < latencyCategories.length;i++){
//			if(i==0){
//				latencyCategories[i] = latUnit;
//				hopCategories[i] = hopUnit;
//			}else{
//				latencyCategories[i] = latencyCategories[i-1] + latUnit;
//				hopCategories[i] = hopCategories[i-1] + hopUnit;
//			}
//		}
//		System.out.println();
//	}
	
	
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
	
	
//	public static void setNodePIDs(int rid, int kid, int cid){
//		RID = rid;
//		KID = kid;
//		CID = cid;
//	}
//	
//	public static void setProtPIDs(int rid, int kid, int cid){
//		RPID = rid;
//		KPID = kid;
//		CPID = cid;
//	}
	
	public static int getLinkable(int pid){
		if(pid == RPID)
			return RID;
		if(pid == KPID)
			return KID;
		if(pid == CPID)
			return CID;
		if(pid == FKPID)
			return FKID;
		if(pid == LKPID)
			return LKID;
		return -1;
	}
	
	public static String getProtocolName(int pid){
		return Network.get(0).getProtocol(pid).getClass().getSimpleName();
	}
	
	private static int getLog2(int nr){
		double doubleLength = Math.log(nr) / Math.log(2);
		int lgNr = (int)(doubleLength); 
		if(doubleLength > lgNr)
			lgNr++;
		return lgNr;
	}
	
	public static RenaterNode getRenaterNode(String id){
		if(id==null) return null;
		return (RenaterNode) Nodes.get(id).getProtocol(RID);
	}
	
	public static KoalaNode getKoalaNode(String id){
		if(id==null) return null;
		return (KoalaNode) Nodes.get(id).getProtocol(KID);
	}
	
	
	public static ChordNode getChordNode(String id){
		if(id==null) return null;
		return (ChordNode) Nodes.get(id).getProtocol(CID);
	}
	
	public static ChordNode getChordNodeByCID(BigInteger id){
		if(id==null) return null;
		return (ChordNode) CHORD_NODES.get(id).getProtocol(CID);
	}
	
	public static ChordNode getChordFromNode(Node n){
		return (ChordNode) n.getProtocol(CID);
	}
	
	public static boolean isUp(String nodeid){
		if(nodeid == null) return false;
		Node n = Nodes.get(nodeid);
		if(n != null)
			return n.isUp();
		return false;
	}
	
	public static int getDCID(String id){
		if(id.equals(DEFAULTID))
			return -1;
		return Integer.parseInt(id.split("-")[0]);
	}
	
	public static String getStrDCID(String id){
		return id.split("-")[0];
	}
	
	public static int getNodeID(String id){
		return Integer.parseInt(id.split("-")[1]);
	}
	
	public static boolean sameDC(TopologyNode tp1, TopologyNode tp2){
		if(getDCID(tp1.getSID()) == getDCID(tp2.getSID()))
			return true;
		return false;
	}
	
	public static boolean sameDC(String nodeID1, String nodeID2){
		if(getDCID(nodeID1) == getDCID(nodeID2))
			return true;
		return false;
	}
	
	public static int distance(String srcID, String targetID){
		return distance(srcID, targetID, false, false);
	}
	
	public static int distanceGlobal(String srcID, String targetID){
		return distance(srcID, targetID, false, true);
	}
	
	public static int distanceLocal(String srcID, String targetID){
		return distance(srcID, targetID, true, false);
	}
	
	public static int signDistance(String srcID, String targetID){
		int dist = distance(srcID, targetID);
		int size = getSize();
		String succ = (getDCID(srcID)+1)%size +"-"+getNodeID(srcID);
		String pred = (getDCID(srcID)+size-1)%size +"-"+getNodeID(srcID);
		int distsucc = distance(succ, targetID);
		int distpred = distance(pred, targetID);
		if(distsucc > distpred) 
			return -dist;
		else 
			return dist;
	}
	
	public static int getSize(){
		int size = CURRENT_PID == KPID || CURRENT_PID == LKPID ? NodeUtilities.NR_DC : NodeUtilities.SIZE;
		return size;
	}
	
	public static int getLongLinks(){
		int size = CURRENT_PID == KPID || CURRENT_PID == LKPID ? LONG_LINKS : FLAT_LONG_LINKS;
		return size;
	}
	
	
	public static int distance(String srcID, String targetID, boolean forceLocal, boolean forceGlobal){
		if(srcID.equals(NodeUtilities.DEFAULTID) || targetID.equals(NodeUtilities.DEFAULTID))
			return Integer.MAX_VALUE;
		
        boolean local = false;
        if (getDCID(srcID) == getDCID(targetID) || forceLocal)
            local = true;
        
        if(forceGlobal)
        	local = false;

        int src_id = local ? getNodeID(srcID) : getDCID(srcID); 
        int target_id = local ? getNodeID(targetID) : getDCID(targetID);
        int a = src_id;
        int b = target_id;
        if (src_id > target_id){
            a = target_id; 
            b = src_id;
        }
        
        int size = local ? NR_NODE_PER_DC : getSize();
        int d1 = b - a;
        int d2 = (size - b + a) % size;

        return Math.min(d1, d2);
	}
	
	public static boolean isDefault(KoalaNeighbor n){
		return n == null || n.getSID().equals(NodeUtilities.DEFAULTID);
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
	
	public static String[] getIDFromDistance(String id, int distance){
		String[] ids = new String[2];
		int size = getSize();
		int retid1 = (getDCID(id) + distance) % size;
		int retid2 = getDCID(id) - distance >= 0 ? getDCID(id) - distance : getDCID(id) - distance + size;
		ids[0] =  retid1  + "-" + getNodeID(id);
		ids[1] =  retid2  + "-" + getNodeID(id);
		return ids;
	}
	
	public static String generateNewChordID(){
		BigInteger newId;
		do
			newId= new BigInteger(M, CommonState.r);
		while(CHORD_NODES.containsKey(newId));
		
		return newId.toString();
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
	
	public static void up(Node n){
		KoalaNode kn = (KoalaNode) n.getProtocol(KID);
		n.setFailState(Fallible.OK);
		UPS.put(kn.getCID(), n);
		DOWNS.remove(kn.getCID());
	}
	
	public static void down(Node n){
		KoalaNode kn = (KoalaNode) n.getProtocol(KID);
		n.setFailState(Fallible.DOWN);
		DOWNS.put(kn.getCID(), n);
		UPS.remove(kn.getCID());
	}
	
	
	public static void copyAltDowns(){
		for(Entry<String,Node> e : DOWNS.entrySet()){
			ALT_DOWNS.put(e.getKey(), e.getValue());
		}
	}
	
	
	
	public static ArrayList<Node> getUniformRandNodes(int upOrDown, int nr){
		ArrayList<Node> ret = new ArrayList<Node>();
		if(nr < 1) return ret;
		
		HashMap<String, Node> upDowns = upOrDown==1 ? UPS : DOWNS;
		if(upOrDown==-1)
			upDowns = ALT_DOWNS;
		ArrayList<String> upDownKeys = new ArrayList<String>(upDowns.keySet());
			 
		Collections.shuffle(upDownKeys, CommonState.r);
		for(int i = 0; i < nr; i++)
			ret.add(upDowns.get(upDownKeys.get(i)));
		
		return ret;
	}
	
	
	public static void cleanAllVisited(){
		for(int i =0; i < Network.size();i++){
			RenaterNode rn = (RenaterNode) Network.get(i).getProtocol(NodeUtilities.RID);
			rn.setVisited(false);
		}
	}

	public static String getStringCycles() {
		return (CYCLES+"").replaceAll("000000", "M").replaceAll("000", "K");
	}
	
}
