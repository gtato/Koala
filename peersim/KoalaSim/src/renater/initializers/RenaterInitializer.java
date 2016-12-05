package renater.initializers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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

public class RenaterInitializer implements Control, NodeInitializer {

	private static final String PAR_PROT = "protocol";
	private static final String PAR_CHORD_PROT = "cprotocol";
	private static final String PAR_DC_FILE = "dc_file";
	private static final String PAR_DC_DISTANCE = "distance";
	
//	public static Map<String, Node> Nodes =  new HashMap<String, Node>(); 

    private static int pid;
    private static int phid;
    private static int cpid;
    
    private final String dc_file;
    
    protected int nrDC;
    protected int nrNodePerDC;
    protected double distance;
    boolean initializationMode;
    
    public RenaterInitializer(String prefix) {
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        phid  = FastConfig.getLinkable(pid);
        cpid = Configuration.getPid(prefix + "." + PAR_CHORD_PROT);
        
        nrDC = Configuration.getInt("NR_DC", 1);
        nrNodePerDC = Configuration.getInt("NR_NODE_PER_DC", 1);
        
        dc_file = (Configuration.getString(prefix + "." + PAR_DC_FILE, "nofilewiththisname"));
        distance = (Configuration.getDouble(prefix + "." + PAR_DC_DISTANCE, 0.01));
        
    }

    	
	@Override
	public boolean execute() {
		double alpha = Configuration.getDouble("ALPHA", -2.0);
		System.out.println("ALPHA set to " + alpha);
		
		System.out.println("\nSetting up renater nodes, positioning and gateways ");
		//initialize the gson parser
		List<String> lines = null;
		
        
        if(Files.exists(Paths.get(dc_file))){
        	 lines = getDcCordsFromFile();
        	 if(lines != null)
        		 nrDC = lines.size();
        }
		
		
		KoalaJsonParser.intitialize();
		NodeUtilities.initialize();
		NodeUtilities.setNodePIDs(phid, pid, cpid);
		NodeUtilities.intializeDCCenters(lines);
		
        
//        if(Network.size() < nrDC)
//        	nrDC = Network.size();
        
        NodeUtilities.ACTUAL_NR_DC = Network.size();
        
        
        
//        assigning nodes an id and setting their coordinates according to their data-center
        initializationMode = true;
        for (int i = 0; i < Network.size(); i++) {
            initialize(Network.get(i));
        }
        initializationMode = false;
        
        
//        System.setErr(new PrintStream(new OutputStream() {
//            public void write(int b) {
//                //DO NOTHING
//            }
//        }));
        
        return false;
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
	
	
	

	
	private String getID(){
		ArrayList<String> emptyDC = new ArrayList<String>();
		for(int i = 0; i < NodeUtilities.NR_DC; i++){
			if(!NodeUtilities.Gateways.containsKey(i+""))
				emptyDC.add(i+"");
		}
		
		if (emptyDC.size() != 0)
			return emptyDC.get(CommonState.r.nextInt(emptyDC.size())) + "-" + CommonState.r.nextInt(NodeUtilities.NR_NODE_PER_DC);
		
		ArrayList<String> emptyNodeID = new ArrayList<String>();
		for(int i = 0; i < NodeUtilities.NR_DC; i++){
			for(int j = 0; j < NodeUtilities.NR_NODE_PER_DC; j++){
				String id = i+"-"+j;
				if(!NodeUtilities.Nodes.containsKey(id))
					emptyNodeID.add(id);
			}
		}
		
		if(emptyNodeID.size() == 0)
			return null;
			
		return emptyNodeID.get(CommonState.r.nextInt(emptyNodeID.size()));
	}
	

	@Override
	public void initialize(Node node) {
		KoalaNode koalaNode = (KoalaNode) node.getProtocol(pid);
		RenaterNode renaterNode = (RenaterNode) node.getProtocol(phid);
		ChordNode chordNode = (ChordNode) node.getProtocol(cpid);
        
        String id = getID();
        int dcID = NodeUtilities.getDCID(id);
        
        koalaNode.setID(id);
        renaterNode.setID(id);
        chordNode.setID(id);
        
        koalaNode.setNode(node);
        renaterNode.setNode(node);
        
        NodeUtilities.Nodes.put(id, node);
        
        double[] cords;
        if(NodeUtilities.Gateways.containsKey(dcID+"")){
        	renaterNode.setGateway(NodeUtilities.Gateways.get(dcID+"").getID());
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
