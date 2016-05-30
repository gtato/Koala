package koala;

public class KoalaMessage {
	public static final int JOIN = 0;
	public static final int ROUTE = 1;
	
	private int msgType;
	private String msgContent;
	
	public KoalaMessage(int type, String content){
		this.msgType = type;
		this.msgContent = content;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}
	 
}
