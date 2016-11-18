package chord.initializers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;

import chord.ChordNode;
import chord.ChordProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;
import utilities.ChordGlobalInfo;
import utilities.NodeUtilities;

public class ChordInitializer implements NodeInitializer, Control {

	private static final String PAR_PROT = "protocol";
	

	int pid = 0;

	

	public ChordInitializer(String prefix) {
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
	}

	
	public boolean execute() {
		ArrayList<BigInteger> ids = generateIDs(Network.size());
		
		for (int i = 0; i < Network.size(); i++) {
			Node node = (Node) Network.get(i);
			ChordNode cp = NodeUtilities.getChordFromNode(node);
			cp.setNode(node);
			cp.chordId = ids.get(i);
			NodeUtilities.CHORD_NODES.put(cp.chordId, node);
			cp.fingerTable = new ChordNode[NodeUtilities.M];
			cp.successorList = new ChordNode[NodeUtilities.SUCC_SIZE];
			ChordGlobalInfo.network.add(cp);
		}
		
		Collections.sort(ChordGlobalInfo.network, 
				new Comparator<ChordNode>() {

			@Override
			public int compare(ChordNode o1, ChordNode o2) {
				BigInteger one = o1.chordId;
				BigInteger two = o2.chordId;
				return one.compareTo(two);

			}
		});
		myCreateFingerTable();
//		printNeighs();
		return false;
	}
	
	
	
	public void initialize(Node n) {
		ChordProtocol cp = (ChordProtocol) n.getProtocol(pid);
		cp.intializeMyNode(n, pid);
		cp.join();
	}

	public ChordNode findNodeforId(BigInteger id) {
		for (int i = 0; i < Network.size(); i++) {
			ChordNode cp = NodeUtilities.getChordFromNode(Network.get(i));
			if(cp.chordId.compareTo(id) >= 0)
				return cp;
		}
		return NodeUtilities.getChordFromNode(Network.get(0));
	}
	
	
	
public void myCreateFingerTable() {
		
		for (int i = 0; i < ChordGlobalInfo.network.size(); i++) {
			
			ChordNode cp =  ChordGlobalInfo.network.get(i);
			for (int a = 0; a < NodeUtilities.SUCC_SIZE; a++) 
				cp.successorList[a] = ChordGlobalInfo.get((a + i + 1)%ChordGlobalInfo.network.size());
			if (i > 0)
				cp.predecessor = ChordGlobalInfo.get(i-1);
			else
				cp.predecessor = ChordGlobalInfo.last();
			
			for (int j = 0; j < cp.fingerTable.length; j++) {
				long a = (long) (cp.chordId.longValue() + Math.pow(2, j)) %(long)Math.pow(2, NodeUtilities.M);
				BigInteger id = new BigInteger(a+"");
				cp.fingerTable[j] = findNodeforId(id); 
			}
		}
		
	}
	
	
	public static ArrayList<BigInteger> generateIDs(int nr){
		HashSet<BigInteger> ids = new HashSet<BigInteger>();
		
		while(ids.size() != nr)		
			ids.add(new BigInteger(NodeUtilities.M, CommonState.r));
		
		return new ArrayList<BigInteger>(ids);
	}
	
}

