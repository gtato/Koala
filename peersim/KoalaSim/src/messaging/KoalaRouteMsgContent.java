package messaging;

public class KoalaRouteMsgContent extends KoalaMsgContent {

	String id;
	private boolean updateLatency;
	
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

}
