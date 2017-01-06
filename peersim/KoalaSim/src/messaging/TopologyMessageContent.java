package messaging;

public class TopologyMessageContent {
	int msgType;

	public TopologyMessageContent(int msgType) {
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
