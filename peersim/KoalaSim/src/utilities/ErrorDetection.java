package utilities;

import java.util.ArrayList;

import messaging.KoalaMessage;

public class ErrorDetection {
	
	
	public static boolean hasLoopCommunication( KoalaMessage km, String dst){
		
		int occurrences = 0;
		for(String nodeId : km.getPath())
			if(nodeId.equals(dst))
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
