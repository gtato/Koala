package chord;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

import peersim.core.Node;
import topology.TopologyNode;
import utilities.ChordGlobalInfo;
import utilities.NodeUtilities;

public class ChordNode extends TopologyNode implements Comparable<ChordNode>{

	public ChordNode predecessor;
	public ChordNode[] fingerTable, successorList;
	public BigInteger chordId;

	public int fingerToFix=0;

	public ChordNode(String prefix) {
		super(prefix);
	}
	
	public ChordNode(BigInteger chordId) {
		super("xxx");
		this.chordId = chordId;
	}
	
	
	public ChordNode closestPrecedingNode(BigInteger id) {

		ArrayList<ChordNode> fullTable = getFullTable();
		ChordNode found = null;
		for (int i = fullTable.size()-1; i >= 0; i--) {
			ChordNode entry = fullTable.get(i);
			if (entry != null && entry.isUp() && inAB(entry.chordId, this.chordId, id) ) {
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
		fullTable.sort(new Comparator<ChordNode>() {
			@Override
			public int compare(ChordNode arg0, ChordNode arg1) {
				int dist1 = ChordNode.distance(chordId, arg0.chordId);
				int dist2 = ChordNode.distance(chordId, arg1.chordId);
				return dist1 -dist2;
			}
		});
		
		fullTable.add(predecessor);
		return fullTable;
	}

	public static int distance(BigInteger a, BigInteger b){
		int ia = a.intValue(); 
		int ib = b.intValue();
		if(ib >= ia) return ib-ia;
		return ib+(int)Math.pow(2, NodeUtilities.M)-ia;
		
	}

	public static boolean inAB(BigInteger bid, BigInteger ba, BigInteger bb){
		long id = bid.longValue();
		long a = ba.longValue();
		long b = bb.longValue();
		if (id == a || id == b) return true;
		
		if(id > a && id < b)
			return true;
		if(id < a && a > b && id < b)
			return true;
		
		if(id > b && a > b && id > a)
			return true;
		
		
		return false;
	}
	
	
	public String toString(){
		return  getID() + " ("+chordId.toString()+")";
	}
	
	public boolean equals(Object arg0){
		if(arg0 == null || !arg0.getClass().equals(this.getClass()))
			return false;
		return this.compareTo((ChordNode)arg0) == 0;
		
	}
	
	@Override
	public int compareTo(ChordNode arg0) {
		if (arg0 == null) return 100; 
		return this.chordId.compareTo(arg0.chordId);
	}

}
