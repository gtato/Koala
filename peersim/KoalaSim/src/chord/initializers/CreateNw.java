/**
 * 
 */
package chord.initializers;

import chord.ChordNode;
import peersim.core.*;
import peersim.config.Configuration;
import utilities.ChordGlobalInfo;

import java.math.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

/**
 * @author Andrea
 * 
 */
public class CreateNw implements Control {

	private int pid = 0;

//	private static final String PAR_IDLENGTH = "idLength";

	private static final String PAR_PROT = "protocol";

	private static final String PAR_SUCCSIZE = "succListSize";

	int idLength = 0;

	int successorLsize = 0;

	int fingSize = 0;
	//campo x debug
	boolean verbose = false;

	/**
	 * 
	 */
	public CreateNw(String prefix) {
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
//		idLength = Configuration.getInt(prefix + "." + PAR_IDLENGTH); 
		successorLsize = Configuration.getInt(prefix + "." + PAR_SUCCSIZE);
		double doubleLength = Math.log(Network.size()) / Math.log(2);
		idLength = (int)(doubleLength); 
		if(doubleLength > idLength)
			idLength++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see peersim.core.Control#execute()
	 */

	public boolean execute() {
		ArrayList<BigInteger> ids = generateIDs();
		ChordGlobalInfo.network.clear();
		for (int i = 0; i < Network.size(); i++) {
			Node node = (Node) Network.get(i);
			ChordNode cp = (ChordNode) node.getProtocol(pid);
			cp.setNode(node);
			cp.m = idLength;
			cp.succLSize = successorLsize;
			cp.varSuccList = 0;
//			cp.chordId = new BigInteger(idLength, CommonState.r);
			cp.chordId = ids.get(i);
//			cp.chordId = new BigInteger(i+"");
			cp.fingerTable = new ChordNode[idLength];
			cp.successorList = new ChordNode[successorLsize];
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

//		createFingerTable();
		myCreateFingerTable();
		return false;
	}

//	public ChordNode findId(BigInteger id, int nodeOne, int nodeTwo) {
//		if (nodeOne >= (nodeTwo - 1)) 
//			return ChordGlobalInfo.get(nodeOne);
//		int middle = (nodeOne + nodeTwo) / 2;
//		if (((middle) >= Network.size() - 1))
//			System.out.print("ERROR: Middle is bigger than Network.size");
//		if (((middle) <= 0))
//			return ChordGlobalInfo.get(0);
//		try {
//			BigInteger newId = ChordGlobalInfo.get(middle).chordId;
//			BigInteger lowId;
//			if (middle > 0)
//				lowId = ChordGlobalInfo.get(middle-1).chordId;
//			else
//				lowId = newId;
//			BigInteger highId = ChordGlobalInfo.get(middle+1).chordId;
//			if (id.compareTo(newId) == 0
//					|| ((id.compareTo(newId) == 1) && (id.compareTo(highId) == -1))) {
//				return ChordGlobalInfo.get(middle);
//			}
//			if ((id.compareTo(newId) == -1) && (id.compareTo(lowId) == 1)) {
//				if (middle > 0)
//					return ChordGlobalInfo.get(middle-1);
//				else
//					return ChordGlobalInfo.get(0);
//			}
//			if (id.compareTo(newId) == -1) {
//				return findId(id, nodeOne, middle);
//			} else if (id.compareTo(newId) == 1) {
//				return findId(id, middle, nodeTwo);
//			}
//			return null;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//	}

//	public void createFingerTable() {
//		BigInteger idFirst = ChordGlobalInfo.first().chordId;
//		BigInteger idLast = ChordGlobalInfo.last().chordId;
//		for (int i = 0; i < Network.size(); i++) {
//			Node node = (Node) Network.get(i);
//			ChordNode cp = (ChordNode) node.getProtocol(pid);
//			cp.setJoined(true);
//			for (int a = 0; a < successorLsize; a++) {
//				if (a + i < (Network.size() - 1))
//					cp.successorList[a] = ChordGlobalInfo.get(a + i + 1);
//				else
//					cp.successorList[a] = ChordGlobalInfo.get(a);
//			}
//			if (i > 0)
//				cp.predecessor = ChordGlobalInfo.get(i-1);
//			else
//				cp.predecessor = ChordGlobalInfo.last();
//			int j = 0;
//			for (j = 0; j < idLength; j++) {
//				BigInteger base;
//				if (j == 0)
//					base = BigInteger.ONE;
//				else {
//					base = BigInteger.valueOf(2);
//					for (int exp = 1; exp < j; exp++) {
//						base = base.multiply(BigInteger.valueOf(2));
//					}
//				}
//				BigInteger pot = cp.chordId.add(base);
//				if (pot.compareTo(idLast) == 1) {
//					pot = (pot.mod(idLast));
//					if (pot.compareTo(cp.chordId) != -1) {
//						break;
//					}
//					if (pot.compareTo(idFirst) == -1) {
//						cp.fingerTable[j] = ChordGlobalInfo.last();
//						continue;
//					}
//				}
//				cp.fingerTable[j] = findId(pot, 0, Network.size() - 1);
//			}
//		}
//	}
	
	public void myCreateFingerTable() {
		
		for (int i = 0; i < ChordGlobalInfo.network.size(); i++) {
			
			ChordNode cp =  ChordGlobalInfo.network.get(i);
			for (int a = 0; a < successorLsize; a++) 
				cp.successorList[a] = ChordGlobalInfo.get((a + i + 1)%ChordGlobalInfo.network.size());
			if (i > 0)
				cp.predecessor = ChordGlobalInfo.get(i-1);
			else
				cp.predecessor = ChordGlobalInfo.last();
			int j = 0;
			for (j = 0; j < cp.fingerTable.length; j++) {
				
				long a = (long) (cp.chordId.longValue() + Math.pow(2, j)) %(long)Math.pow(2, idLength);
//				long a = (long) (Math.pow(2, j)%(long)Math.pow(2, idLength));
//				BigInteger id = cp.chordId.add(new BigInteger(a+""));
				BigInteger id = new BigInteger(a+"");
				cp.fingerTable[j] = findNodeforId(id); 
				
			}
		}
		
	}
	
	
	public ChordNode findNodeforId(BigInteger id) {
		for (int i = 0; i < Network.size(); i++) {
			Node node = (Node) Network.get(i);
			ChordNode cp = (ChordNode) node.getProtocol(pid);
			if(cp.chordId.compareTo(id) >= 0)
				return cp;
		}
		return null;
	}
	
	private ArrayList<BigInteger> generateIDs(){
		HashSet<BigInteger> ids = new HashSet<BigInteger>();
		
		while(ids.size() != Network.size())
		{
			ids.add(new BigInteger(idLength, CommonState.r));
		}
		
		return new ArrayList<BigInteger>(ids);
	}
}
