package koala.utility;

import java.util.ArrayList;

public class ErrorDetection {
	private static ArrayList<String[]> communication = new ArrayList<String[]>(4);
	
	
	public static boolean hasLoopCommunication(String src, String dst){
		
		int occurrences = 0;
		for(String[] com : communication){
			if((com[0].equals(src) && com[1].equals(dst)) || (com[0].equals(dst) && com[1].equals(src)))
				occurrences++;
		}
		if(occurrences > 3){
			return true;
		}
		if(communication.size() > 3)
			communication.remove(0);
		if(src != null && dst != null)
			communication.add(new String[]{src, dst});
		return false;
	}
	
}
