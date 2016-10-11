package messaging;

import java.util.ArrayList;

public class KoalaRouteMsgContent extends KoalaMsgContent {

	String id;
	private boolean updateLatency;
	private ArrayList<String> shortestPath;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public KoalaRouteMsgContent(String id) {
		super(KoalaMessage.ROUTE);
		this.id = id;
		this.updateLatency = false;
	}
	
	public KoalaRouteMsgContent(String id, boolean updateLatency) {
		super(KoalaMessage.ROUTE);
		this.id = id;
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
