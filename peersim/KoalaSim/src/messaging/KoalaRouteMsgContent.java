package messaging;

public class KoalaRouteMsgContent extends KoalaMsgContent {

	String id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public KoalaRouteMsgContent(String id) {
		super(KoalaMessage.ROUTE);
		this.id = id;
	}

}
