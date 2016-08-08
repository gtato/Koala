package utilities;

import java.util.ArrayList;

import chord.ChordNode;

public class ChordGlobalInfo {
	public static ArrayList<ChordNode> network = new ArrayList<ChordNode>();
	
	public static ChordNode first(){
		if(network.size() > 0)
			return network.get(0);
		return null;
	}
	
	public static ChordNode last(){
		if(network.size() > 0)
			return network.get(network.size()-1);
		return null;
	}
	
	public static ChordNode get(int i){
		if(network.size() > i)
			return network.get(i);
		return null;
	}
	
}
