package koala.initializers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import koala.KoalaNode;
import koala.utility.KoalaJsonParser;
import koala.utility.KoalaNodeUtilities;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class RenaterInitializer implements Control {

	private static final String PAR_PROT = "protocol";
	private static final String PAR_NR_DC = "nr_dc";
	private static final String PAR_NR_NODE_PER_DC = "nr_node_per_dc";
	
	private static final String PAR_DC_FILE = "dc_file";
	private static final String PAR_DC_DISTANCE = "distance";
	
	

    private static int pid;
    private final int nrDCVal;
    private final int nrNodePerDCVal;
    private final String dc_file;
    
    protected int nrDC;
    protected int nrNodePerDC;
    protected double distance;
    
    public RenaterInitializer(String prefix) {
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        
        nrDCVal = (Configuration.getInt(prefix + "." + PAR_NR_DC, 1));
        nrDC = nrDCVal;
        
        nrNodePerDCVal = (Configuration.getInt(prefix + "." + PAR_NR_NODE_PER_DC, 1));
        nrNodePerDC = nrNodePerDCVal;
        
        dc_file = (Configuration.getString(prefix + "." + PAR_DC_FILE, "nofilewiththisname"));
        distance = (Configuration.getDouble(prefix + "." + PAR_DC_DISTANCE, 0.01));
        
    }

    	
	@Override
	public boolean execute() {
		//initialize the gson parser
		KoalaJsonParser.intitialize((KoalaNode)Network.get(0).getProtocol(pid));
		KoalaNodeUtilities.initialize();
		
		List<String> lines = null;
		Node n; KoalaNode node; 
        
        if(Files.exists(Paths.get(dc_file))){
        	 lines = getDcCordsFromFile();
        	 if(lines != null)
        		 nrDC = lines.size();
        }
        
        int[] nodesPerDC = new int[nrDC];
        double[][] centerPerDC = new double[nrDC][2];
        int equalNodesPerDC = Network.size() / nrDC;
        int rem = Network.size() % nrDC;
        
//      splitting the nodes per data center and creating the coordinates per dc
        for (int i = 0; i < nodesPerDC.length; i++){
        	nodesPerDC[i] = equalNodesPerDC;
        	if(rem > 0){
        		nodesPerDC[i]++;
        		rem--;
        	}
        	
        	if(lines != null){
        		centerPerDC[i][0] = Double.parseDouble(lines.get(i).split(", ")[0]);
        		centerPerDC[i][1] = Double.parseDouble(lines.get(i).split(", ")[1]);
        	}else{
        		centerPerDC[i][0] = CommonState.r.nextDouble();
        		centerPerDC[i][1] = CommonState.r.nextDouble();
        	}
        }
        
        int j, k;
        j = k = 0;
        double[] cords;
        
//        assigning nodes an id and setting their coordinates according to their data-center
        for (int i = 0; i < Network.size(); i++) {
            n = Network.get(i);
            node = (KoalaNode) n.getProtocol(pid);
            
            nodesPerDC[j]--;
            node.setID(j, k);
            
            k++;
            if(nodesPerDC[j] == 0){
            	cords = new double[]{centerPerDC[j][0], centerPerDC[j][1]};
            	node.setGateway(true);
            	j++;
            	k=0;
            }else
            	cords = this.getRandomCirclePoint(centerPerDC[j][0], centerPerDC[j][1], distance);       
            node.setX(cords[0]);
            node.setY(cords[1]);
        }
        
        
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
}
