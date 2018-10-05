package messaging;

import java.util.ArrayList;

import koala.KoalaNode;
import topology.TopologyNode;
import topology.TopologyPathNode;

public class KoalaRouteMsgContent extends KoalaMsgContent {

	TopologyPathNode node;
	private boolean updateLatency;
	private ArrayList<String> shortestPath;
	
	public TopologyPathNode getNode() {
		return node;
	}
	public void setNode(TopologyPathNode node) {
		this.node = node;
	}
	public KoalaRouteMsgContent(TopologyPathNode node) {
		super(KoalaMessage.ROUTE);
		this.node = node;
		this.updateLatency = false;
	}
	
	public KoalaRouteMsgContent(TopologyNode node) {
		super(KoalaMessage.ROUTE);
		this.node = new TopologyPathNode( node.getCID(), node.getSID());
		if( node instanceof KoalaNode) {
			KoalaNode kn = (KoalaNode) node;
			this.node.setVivaldiCoordinates(kn.vivaldiCoordinates);
			this.node.setVivaldiUncertainty(kn.vivaldiUncertainty);
		}
		this.updateLatency = false;
	}
	
	
	public KoalaRouteMsgContent(TopologyPathNode node, boolean updateLatency) {
		super(KoalaMessage.ROUTE);
		this.node = node;
		this.updateLatency = updateLatency;
	}
	
	public boolean wantsToUpdateLatency() {
		return updateLatency;
	}
	
	public void setUpdateLatency(boolean updateLatency) {
		this.updateLatency = updateLatency;
	}
	public ArrayList<String> getShortestPath() {
		return shortestPath;
	}
	public void setShortestPath(ArrayList<String> shortestPath) {
		this.shortestPath = shortestPath;
	}

}
