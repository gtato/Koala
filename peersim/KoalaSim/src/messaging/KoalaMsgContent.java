package messaging;

public class KoalaMsgContent {
	
	int msgType;

	public KoalaMsgContent(int msgType) {
		super();
		this.msgType = msgType;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}
	
}
