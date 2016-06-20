package koala.initializers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import koala.KoalaNode;
import koala.RenaterNode;
import koala.utility.KoalaJsonParser;
import koala.utility.KoalaNodeUtilities;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class RenaterInitializer implements Control {

	private static final String PAR_PROT = "protocol";
	private static final String PAR_DC_FILE = "dc_file";
	private static final String PAR_DC_DISTANCE = "distance";
	
	

    private static int pid;
    private static int phid;
    
    private final String dc_file;
    
    protected int nrDC;
    protected int nrNodePerDC;
    protected double distance;
    
    public RenaterInitializer(String prefix) {
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        phid  = FastConfig.getLinkable(pid);
        
        nrDC = Configuration.getInt("NR_DC", 1);
        nrNodePerDC = Configuration.getInt("NR_NODE_PER_DC", 1);
        
        dc_file = (Configuration.getString(prefix + "." + PAR_DC_FILE, "nofilewiththisname"));
        distance = (Configuration.getDouble(prefix + "." + PAR_DC_DISTANCE, 0.01));
        
    }

    	
	@Override
	public boolean execute() {
		//initialize the gson parser
		KoalaJsonParser.intitialize();
		KoalaNodeUtilities.initialize();
		
		List<String> lines = null;
		Node n; KoalaNode koalaNode; RenaterNode renaterNode; 
        
        if(Files.exists(Paths.get(dc_file))){
        	 lines = getDcCordsFromFile();
        	 if(lines != null)
        		 nrDC = lines.size();
        }
        
        
        double[][] centerPerDC = getCenterPerDC(lines);
        int[] nodesPerDC = getNodesPerDC(true, lines);
        
        ArrayList<RenaterNode> gateways = new ArrayList<RenaterNode>(); 
        int j, k;
        j = k = 0;
        double[] cords;
        
//        assigning nodes an id and setting their coordinates according to their data-center
        for (int i = 0; i < Network.size(); i++) {
            n = Network.get(i);
            koalaNode = (KoalaNode) n.getProtocol(pid);
            renaterNode = (RenaterNode) n.getProtocol(phid);
            
            nodesPerDC[j]--;
            koalaNode.setID(j, k);
            renaterNode.setID(j+"-"+k);
            k++;
            if(nodesPerDC[j] == 0){
            	cords = new double[]{centerPerDC[j][0], centerPerDC[j][1]};
            	//renaterNode.setGateway(true);
            	gateways.add(renaterNode);
            	j++;
            	k=0;
            }else
            	cords = this.getRandomCirclePoint(centerPerDC[j][0], centerPerDC[j][1], distance);       
            renaterNode.setX(cords[0]);
            renaterNode.setY(cords[1]);
        }
        
        for (int i = 0; i < Network.size(); i++) {
        	n = Network.get(i);
            renaterNode = (RenaterNode) n.getProtocol(phid);
            for(RenaterNode gateway: gateways){
            	if(!renaterNode.equals(gateway) && renaterNode.getID().split("-")[0].equals(gateway.getID().split("-")[0])){
            		renaterNode.setGateway(gateway.getID());
            		//gateways.remove(gateway);
            		break;
            	}
            }
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
	
	
	private double[][] getCenterPerDC(List<String> lines){
		double[][] centerPerDC = new double[nrDC][2];
		
		for (int i = 0; i < nrDC; i++){
          	if(lines != null){
        		centerPerDC[i][0] = Double.parseDouble(lines.get(i).split(", ")[0]);
        		centerPerDC[i][1] = Double.parseDouble(lines.get(i).split(", ")[1]);
        	}else{
        		centerPerDC[i][0] = CommonState.r.nextDouble();
        		centerPerDC[i][1] = CommonState.r.nextDouble();
        	}
        }
		
		return centerPerDC;
	}
	
	
	
}
