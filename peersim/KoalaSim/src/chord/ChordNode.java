package chord;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import peersim.core.Node;
import topology.TopologyNode;
import utilities.ChordGlobalInfo;
import utilities.NodeUtilities;

public class ChordNode extends TopologyNode implements Comparable<ChordNode>{

	public ChordNode predecessor;
	public ChordNode[] fingerTable, successorList;
	
	public int fingerToFix=0;

//	public ChordNode(String prefix, String chordId) {
//		super(prefix);
//		this.setSID(chordId);
//	}
	
	public ChordNode(String chordId) {
		super("xxx");
		this.setSID(chordId);
	}
	
	public ChordNode closestPrecedingNode(String id, String joiningId ) {

		ArrayList<ChordNode> fullTable = getFullTable();
		ChordNode found = null;
		for (int i = fullTable.size()-1; i >= 0; i--) {
			ChordNode entry = fullTable.get(i);
			if(entry != null && joiningId != null && entry.getSID().equals(joiningId))
				continue;
			if (entry != null && entry.isUp() && inAB(entry.getSID(), this.getSID(), id) ) {
				found = entry;
				break;
			}
		}
		
		return found;
	}
	
	
	private ArrayList<ChordNode> getFullTable(){
		ArrayList<ChordNode> fullTable = new ArrayList<ChordNode>();
		HashSet<ChordNode> hs = new HashSet<ChordNode>();
		hs.addAll(Arrays.asList(fingerTable));
		hs.addAll(Arrays.asList(successorList));
		fullTable.addAll(hs);
		Collections.sort(fullTable,new Comparator<ChordNode>() {
			@Override
			public int compare(ChordNode arg0, ChordNode arg1) {
				int dist1 = ChordNode.distance(getSID(), arg0.getSID());
				int dist2 = ChordNode.distance(getSID(), arg1.getSID());
				return dist1 -dist2;
			}
		});
		
		fullTable.add(predecessor);
		return fullTable;
	}

	public static int distance(String a, String b){
		int ia = Integer.parseInt(a); 
		int ib = Integer.parseInt(b);
		if(ib >= ia) return ib-ia;
		return ib+(int)Math.pow(2, NodeUtilities.M)-ia;
		
	}

	public static boolean inAB(String bid, String ba, String bb){
		long id = Long.parseLong(bid);
		long a = Long.parseLong(ba);
		long b = Long.parseLong(bb);
		if (id == a || id == b) return true;
		
		if(id > a && id < b)
			return true;
		if(id < a && a > b && id < b)
			return true;
		
		if(id > b && a > b && id > a)
			return true;
		
		
		return false;
	}
	
	public long getLID(){
		return Long.parseLong(getSID());
	}
	
	public String toString(){
		if( getSID() == null)
			return getCID(); 
		return  getCID() + " ("+getSID()+")";
	}
	
	public boolean equals(Object arg0){
		if(arg0 == null || !arg0.getClass().equals(this.getClass()))
			return false;
		return this.compareTo((ChordNode)arg0) == 0;
		
	}
	
	@Override
	public int compareTo(ChordNode arg0) {
		if (arg0 == null) return 100; 
		return (int) (this.getLID() - arg0.getLID());
	}
	
	public int compareTo(String lid) {
		if (lid == null) return 100; 
		return (int) (this.getLID() - Long.parseLong(lid) );
	}
	
	public boolean isJoining(){
		return this.successorList[0] != null;
	}

}
