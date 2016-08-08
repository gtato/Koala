package chord.initializers;

import java.math.BigInteger;
import java.util.Random;

import chord.ChordNode;
import chord.ChordProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;
import utilities.ChordGlobalInfo;

public class ChordInitializer implements NodeInitializer {

	private static final String PAR_PROT = "protocol";

	private int pid = 0;

	private ChordNode cp;

	public ChordInitializer(String prefix) {
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
	}

	public void initialize(Node n) {
		cp = (ChordNode) n.getProtocol(pid);
		join(cp);
	}

	public void join(ChordNode myNode) {
		Random generator = new Random();
		cp.predecessor = null;
		// search a node to join
		Node n;
		do {
			n = Network.get(generator.nextInt(Network.size()));
		} while (n == null || n.isUp() == false);
		cp.m = ((ChordNode) n.getProtocol(pid)).m;
		cp.chordId = new BigInteger(cp.m, CommonState.r);
		ChordNode cpRemote = (ChordNode) n.getProtocol(pid);

		ChordNode successor = cpRemote.find_successor(cp.chordId);
		cp.fails = 0;
		cp.stabilizations = 0;
		cp.varSuccList = cpRemote.varSuccList;
		cp.varSuccList = 0;
		cp.succLSize = cpRemote.succLSize;
		cp.successorList = new ChordNode[cp.succLSize];
		cp.successorList[0] = successor;
		cp.fingerTable = new ChordNode[cp.m];
		long succId = 0;
		BigInteger lastId = ChordGlobalInfo.last().chordId;
		do {
			cp.stabilizations++;
			succId = cp.successorList[0].getSimNodeID();
			cp.stabilize(myNode);
			if (cp.successorList[0].chordId.compareTo(cp.chordId) < 0) {
				cp.successorList[0] = cp.successorList[0].find_successor(cp.chordId);
			}
			// controllo di non essere l'ultimo elemento della rete
			if (cp.chordId.compareTo(lastId) > 0) {
				cp.successorList[0] = ChordGlobalInfo.first();
				break;
			}
		} while (cp.successorList[0].getSimNodeID() != succId
				|| cp.successorList[0].chordId
						.compareTo(cp.chordId) < 0);
		cp.fixFingers();
	}
}
