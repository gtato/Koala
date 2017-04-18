package utilities;

import messaging.TopologyMessage;
import topology.TopologyPathNode;

public class ErrorDetection {
	
	
	public static boolean hasLoopCommunication( TopologyMessage km, TopologyPathNode dst){
		
		int occurrences = 0;
		for(TopologyPathNode nodeId : km.getPath())
			if(nodeId.getCID().equals(dst.getCID()))
				occurrences++;
			
			
		// this can give false positives in case the node is not very 
		// well connected , maybe give different values of danger depending
		// on the occurrences 
		if(occurrences > 3){
			return true;
		}
		
		return false;
	}
	
}
