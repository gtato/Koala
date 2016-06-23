package koala.utility;

import java.util.HashMap;
import java.util.LinkedList;

import koala.RenaterNode;

public class LatencyProvider {
	private static HashMap<String, HashMap<String, LinkedList<RenaterNode>>> paths
	= new HashMap<String, HashMap<String, LinkedList<RenaterNode>>>();
	
	public static void addPath(String src, String dst, LinkedList<RenaterNode> path){
		HashMap<String, LinkedList<RenaterNode>> pathToDst = new HashMap<String, LinkedList<RenaterNode>>();
		pathToDst.put(dst, path);
		paths.put(src, pathToDst);
	}
}
