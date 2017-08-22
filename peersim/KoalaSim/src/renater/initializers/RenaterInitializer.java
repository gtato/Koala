package renater.initializers;

import java.awt.Toolkit;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import chord.ChordNode;
import koala.KoalaNode;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;
import renater.RenaterNode;
import utilities.KoalaJsonParser;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;

public class RenaterInitializer implements Control, NodeInitializer {

//	private static final String PAR_PROT = "protocol";
//	private static final String PAR_CHORD_PROT = "cprotocol";
	private static final String PAR_DC_FILE = "dc_file";
	private static final String PAR_DC_DISTANCE = "distance";
	private static final String PAR_RAND = "rand";
	
//	public static Map<String, Node> Nodes =  new HashMap<String, Node>(); 

//    private static int pid;
//    private static int phid;
//    private static int cpid;
    
    private final String dc_file;
    
    protected int nrDC;
//    protected int nrNodePerDC;
    protected double distance;
    boolean initializationMode;
    boolean randomize;
//    boolean nested;
    
    public RenaterInitializer(String prefix) {
      
        nrDC = NodeUtilities.NR_DC;
//        nrNodePerDC = Configuration.getInt("NR_NODE_PER_DC", 1);
        
        dc_file = Configuration.getString(prefix + "." + PAR_DC_FILE, "nofilewiththisname");
        distance = Configuration.getDouble(prefix + "." + PAR_DC_DISTANCE, 0.01);
//        nested = NodeUtilities.NESTED Configuration.getBoolean("koala.settings.nested", false);
        randomize = Configuration.getBoolean(prefix + "." + PAR_RAND, false);
    }

    	
	@Override
	public boolean execute() {
		if(Configuration.getBoolean("logging.file", false)){
        	String filename = "out/log."+System.currentTimeMillis();
        	System.setOut(outputFile(filename+".out"));
        	System.setErr(outputFile(filename+".err"));
        }
		
		PhysicalDataProvider.SimTime = System.currentTimeMillis();
//		double alpha = Configuration.getDouble("ALPHA", -2.0);
		System.out.println("ALPHA set to " + NodeUtilities.ALPHA);
		
		System.out.println("\nSetting up renater nodes, positioning and gateways ");
		
		List<String> lines = null;
        if(Files.exists(Paths.get(dc_file))){
        	 lines = getDcCordsFromFile();
        	 if(lines != null)
        		 nrDC = lines.size();
        }
		
		
		KoalaJsonParser.intitialize();
		NodeUtilities.initialize();
		//NodeUtilities.setNodePIDs(phid, pid, cpid);
		NodeUtilities.intializeDCCenters(lines);
		
        
//        if(Network.size() < nrDC)
//        	nrDC = Network.size();
        
        NodeUtilities.ACTUAL_NR_DC = Network.size();
        
        createIDs();
//        assigning nodes an id and setting their coordinates according to their data-center
        initializationMode = true;
        for (int i = 0; i < Network.size(); i++) {
            initialize(Network.get(i));
        }
        initializationMode = false;
        
//        System.exit(0);
//        System.setErr(new PrintStream(new OutputStream() {
//            public void write(int b) {
//                //DO NOTHING
//            }
//        }));
        
        
        
        return false;
	}
	
	protected PrintStream outputFile(String name) {
	    try {
			return new PrintStream(new BufferedOutputStream(new FileOutputStream(name)),true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private List<String> getDcCordsFromFile(){
		List<String> lines = null;
		try {
			lines = Files.readAllLines(Paths.get(dc_file), Charset.forName("UTF-8"));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}
	
	private double[] getRandomCirclePoint(double cX, double cY, double radius)
	{ 
	
		double x,y;
		if(CommonState.r.nextInt() %2==0){
			x = cX + CommonState.r.nextDouble() * 2 * radius - radius;
			y = Math.sqrt(Math.pow(radius, 2) - Math.pow(x-cX, 2));
			y = (CommonState.r.nextInt()%2 == 0) ? cY+y : cY-y;
		}else{
			y = cY + CommonState.r.nextDouble() * 2 * radius - radius;
			x = Math.sqrt(Math.pow(radius, 2) - Math.pow(y-cY, 2));
			x = (CommonState.r.nextInt()%2 == 0) ? cX+x : cX-x;
		}
		return new double[]{x,y};
		
	}
	
	//either equal or random
	private int[] getNodesPerDC(boolean equal, List<String> lines){
		boolean getNodesFromFile = false;
		if(lines != null && lines.size() > 0 && lines.get(0).split(", ").length > 2)
			getNodesFromFile = true;
		
		int equalNodesPerDC = Network.size() / nrDC;
	    int rem = Network.size() % nrDC;
	        
		int[] nodesPerDC = new int[nrDC];
		
		
	
		for (int i = 0; i < nodesPerDC.length; i++){
			if(getNodesFromFile){
				nodesPerDC[i] = Integer.parseInt(lines.get(i).split(", ")[2]);
				continue;
			}
			
			if(equal){
				nodesPerDC[i] = equalNodesPerDC;
	        	if(rem > 0){
	        		nodesPerDC[i]++;
	        		rem--;
	        	}
			}else{
	        	
			}
        }
		return nodesPerDC;
	}
	
	
	int dcid=0, nodeid=0;
	ArrayList<String> possibleIDs = new ArrayList<>();
	private void createIDs(){
		for(int i =0; i < Network.size(); i++){
			String id = dcid + "-" + nodeid;
			if(++nodeid == NodeUtilities.NR_NODE_PER_DC){nodeid=0; dcid++;}
			possibleIDs.add(id);
		}
		if(randomize)
			Collections.shuffle(possibleIDs, CommonState.r);
		
	}
	
	private String getID(){
		return possibleIDs.remove(0);
	}
	
	
//	private String getID(){
//		String id = dcid + "-" + nodeid;
//		
//		if(++nodeid == NodeUtilities.NR_NODE_PER_DC){
//			nodeid=0; dcid++;
//		}
//		return id;
//	}
	
//	private String getID(){
//		ArrayList<String> emptyDC = new ArrayList<String>();
//		for(int i = 0; i < NodeUtilities.NR_DC; i++){
//			if(!NodeUtilities.Gateways.containsKey(i+""))
//				emptyDC.add(i+"");
//		}
//		
//		if (emptyDC.size() != 0)
//			return emptyDC.get(CommonState.r.nextInt(emptyDC.size())) + "-" + CommonState.r.nextInt(NodeUtilities.NR_NODE_PER_DC);
//		
//		ArrayList<String> emptyNodeID = new ArrayList<String>();
//		for(int i = 0; i < NodeUtilities.NR_DC; i++){
//			for(int j = 0; j < NodeUtilities.NR_NODE_PER_DC; j++){
//				String id = i+"-"+j;
//				if(!NodeUtilities.Nodes.containsKey(id))
//					emptyNodeID.add(id);
//			}
//		}
//		
//		if(emptyNodeID.size() == 0)
//			return null;
//			
//		return emptyNodeID.get(CommonState.r.nextInt(emptyNodeID.size()));
//	}
	
	private String getFastID(Node node){
		return node.getIndex()+"-0";
	}
	
	@Override
	public void initialize(Node node) {
		KoalaNode koalaNode = (KoalaNode) node.getProtocol(NodeUtilities.KID);
		
		RenaterNode renaterNode = (RenaterNode) node.getProtocol(NodeUtilities.RID);
		ChordNode chordNode = null;
		if(NodeUtilities.CID >= 0)
			chordNode= (ChordNode) node.getProtocol(NodeUtilities.CID);
		KoalaNode flatKoalaNode = null, leadKoalaNode = null;
		if(NodeUtilities.FKID >= 0)
			flatKoalaNode = (KoalaNode) node.getProtocol(NodeUtilities.FKID);
		
		if(NodeUtilities.LKID >= 0)
			leadKoalaNode = (KoalaNode) node.getProtocol(NodeUtilities.LKID);
		
		String fid = getFastID(node);
        String id = NodeUtilities.NESTED ? getID() : getFastID(node);
//        System.out.println(id);
        int dcID = NodeUtilities.getDCID(id);
        
        if(NodeUtilities.RID >= 0){
        	renaterNode.setCID(id); renaterNode.setSID(id); renaterNode.setNode(node);
        }
        
        if(NodeUtilities.KID >= 0){
        	koalaNode.setCID(id); koalaNode.setSID(id); koalaNode.setNode(node);
        }
        if(NodeUtilities.CID >= 0){
        	chordNode.setCID(id); chordNode.setSID(fid.split("-")[0]); chordNode.setNode(node);
        }
        	
        if(NodeUtilities.FKID >= 0){
        	flatKoalaNode.setCID(id); flatKoalaNode.setSID(fid); flatKoalaNode.setNode(node);
        }
        
        if(NodeUtilities.LKID >= 0){
        	leadKoalaNode.setCID(id); leadKoalaNode.setSID(id); leadKoalaNode.setNode(node);
        }
        
        NodeUtilities.Nodes.put(id, node);
        
        double[] cords;
        if(NodeUtilities.NESTED && NodeUtilities.Gateways.containsKey(dcID+"")){
        	renaterNode.setGateway(NodeUtilities.Gateways.get(dcID+"").getCID());
        	cords = this.getRandomCirclePoint(NodeUtilities.CenterPerDC[dcID][0], NodeUtilities.CenterPerDC[dcID][1], distance);
//        	System.out.println(id + " is not gateway, its gateway is " +  NodeUtilities.Gateways.get(dcID+"").getID());
        }else{
        	NodeUtilities.Gateways.put(dcID+"", renaterNode);
        	cords = new double[]{NodeUtilities.CenterPerDC[dcID][0], NodeUtilities.CenterPerDC[dcID][1]};
        }
        
        renaterNode.setX(cords[0]);
        renaterNode.setY(cords[1]);
        renaterNode.setJoined(true);
		
	}
	
	
	
}
