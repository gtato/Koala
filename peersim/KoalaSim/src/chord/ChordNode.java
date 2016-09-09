package chord;

import java.math.BigInteger;
import java.util.ArrayList;

import peersim.core.Network;
import peersim.core.Node;
import topology.TopologyNode;
import utilities.ChordGlobalInfo;

public class ChordNode extends TopologyNode{

private ArrayList<Integer> lookupMessage;
	
	public ChordNode predecessor;

	public ChordNode[] fingerTable;

	public ChordNode[] successorList;

	public BigInteger chordId;

	public int m;

	public int succLSize;

//	public String prefix;

	private int next = 0;

	// campo x debug
	private int currentNode = 0;

	public int varSuccList = 0;

	public int stabilizations = 0;

	public int fails = 0;
	
	public ChordNode(String prefix) {
		super(prefix);
		String val = BigInteger.ZERO.toString();
		chordId = new BigInteger(val);
		fingerTable = new ChordNode[m];
		successorList = new ChordNode[succLSize];
		currentNode = 0;
	}
	
	
	public Object clone() {
		ChordNode cp = (ChordNode) super.clone();
		String val = BigInteger.ZERO.toString();
		cp.chordId = new BigInteger(val);
		cp.fingerTable = new ChordNode[m];
		cp.successorList = new ChordNode[succLSize];
		cp.currentNode = 0;
		return cp;
	}

	public String toString(){
		return chordId.toString() + " " + getID();
	}
	
	public ArrayList<Integer> getLookupMessage() {
		return lookupMessage;
	}

	public void stabilize(ChordNode myNode) {
		try {
			ChordNode node = successorList[0].predecessor;
			if (node != null) {
				if (this.chordId == node.chordId)
					return;
				BigInteger remoteID = node.chordId;
				if (idInab(remoteID, chordId, successorList[0].chordId))
					successorList[0] = node;
				successorList[0].notify(myNode);
			}
			updateSuccessorList();
		} catch (Exception e1) {
			e1.printStackTrace();
			updateSuccessor();
		}
	}

	private void updateSuccessorList() throws Exception {
		try {
			while (successorList[0] == null || successorList[0].isUp() == false) {
				updateSuccessor();
			}
			System.arraycopy(successorList[0].successorList, 0, successorList, 1,
					succLSize - 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void notify(ChordNode node) throws Exception {
		BigInteger nodeId = node.chordId;
		if ((predecessor == null)
				|| (idInab(nodeId, predecessor.chordId, this.chordId))) {
			predecessor = node;
		}
	}

	private void updateSuccessor() {
		boolean searching = true;
		while (searching) {
			try {
				ChordNode node = successorList[varSuccList];
				varSuccList++;
				successorList[0] = node;
				if (successorList[0] == null
						|| successorList[0].isUp() == false) {
					if (varSuccList >= succLSize - 1) {
						searching = false;
						varSuccList = 0;
					} else
						updateSuccessor();
				}
				updateSuccessorList();
				searching = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private boolean idInab(BigInteger id, BigInteger a, BigInteger b) {
		if ((a.compareTo(id) == -1) && (id.compareTo(b) == -1)) {
			return true;
		}
		return false;
	}

	public ChordNode findUpSuccessor(BigInteger target){
		ChordNode dest = find_successor(target);
		if (dest.isUp() == false) {
			do {
				varSuccList = 0;
				stabilize(this);
				stabilizations++;
				fixFingers();
				dest = find_successor(target);
			} while (dest.isUp() == false);
		}
		if (dest.getSimNodeID() == successorList[0].getSimNodeID() 
				&& target.compareTo(dest.chordId) < 0
				&& this.chordId.compareTo(target) < 0		
			) {
			fails++;
			return null;
		}
		
		return dest;
	}
	
	public ChordNode find_successor(BigInteger id) {
		try {
			if (successorList[0] == null || successorList[0].isUp() == false) {
				updateSuccessor();
			}
			if (idInab(id, this.chordId, successorList[0].chordId)) {
				return successorList[0];
			} else {
				ChordNode tmp = closest_preceding_node(id);
				return tmp;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return successorList[0];
	}

	private ChordNode closest_preceding_node(BigInteger id) {
		for (int i = m; i > 0; i--) {
			try {
				if (fingerTable[i - 1] == null
						|| fingerTable[i - 1].isUp() == false) {
					continue;
				}
				BigInteger fingerId = fingerTable[i - 1].chordId;
				if ((idInab(fingerId, this.chordId, id))
						|| (id.compareTo(fingerId) == 0)) {
					return fingerTable[i - 1];
				}
				if (fingerId.compareTo(this.chordId) == -1) {
					// sono nel caso in cui ho fatto un giro della rete
					// circolare
					if (idInab(id, fingerId, this.chordId)) {
						return fingerTable[i - 1];
					}
				}
				if ((id.compareTo(fingerId) == -1)
						&& (id.compareTo(this.chordId) == -1)) {
					if (i == 1)
						return successorList[0];
					BigInteger lowId = fingerTable[i - 2].chordId;
					if (idInab(id, lowId, fingerId))
						return fingerTable[i - 2];
					else if (fingerId.compareTo(this.chordId) == -1)
						continue;
					else if (fingerId.compareTo(this.chordId) == 1)
						return fingerTable[i - 1];
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (fingerTable[m - 1] == null)
			return successorList[0];
		return successorList[0];
	}

	// debug function
	private void printFingers() {
		for (int i = fingerTable.length - 1; i > 0; i--) {
			if (fingerTable[i] == null) {
				System.out.println("Finger " + i + " is null");
				continue;
			}
			if ((fingerTable[i].chordId).compareTo(this.chordId) == 0)
				break;
			System.out
					.println("Finger["
							+ i
							+ "] = "
//							+ fingerTable[i].getIndex()
							+ " chordId "
							+ fingerTable[i].chordId);
		}
	}

	public void fixFingers() {
		if (next >= m - 1)
			next = 0;
		if (fingerTable[next] != null && fingerTable[next].isUp()) {
			next++;
			return;
		}
		BigInteger base;
		if (next == 0)
			base = BigInteger.ONE;
		else {
			base = BigInteger.valueOf(2);
			for (int exp = 1; exp < next; exp++) {
				base = base.multiply(BigInteger.valueOf(2));
			}
		}
		BigInteger pot = this.chordId.add(base);
		BigInteger idFirst = ChordGlobalInfo.first().chordId;
		BigInteger idLast = ChordGlobalInfo.last().chordId;
		if (pot.compareTo(idLast) == 1) {
			pot = (pot.mod(idLast));
			if (pot.compareTo(this.chordId) != -1) {
				next++;
				return;
			}
			if (pot.compareTo(idFirst) == -1) {
				this.fingerTable[next] = ChordGlobalInfo.last();
				next++;
				return;
			}
		}
		do {
			fingerTable[next] = successorList[0].find_successor(pot);
			pot = pot.subtract(BigInteger.ONE);
			successorList[0].fixFingers();
		} while (fingerTable[next] == null || fingerTable[next].isUp() == false);
		next++;
	}
}
