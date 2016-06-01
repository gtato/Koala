package koala;

public class KoalaMessage {
	public static final int JOIN = 0;
	public static final int ROUTE = 1;
	
	private int msgType;

	private String msgContent;
	/*if set to true, it means that you shouldn't share it with the rest of the DC*/
	private boolean confidential;
	
	public KoalaMessage(int type, String content){
		this.msgType = type;
		this.msgContent = content;
		
	}

	public KoalaMessage(int type, String content, boolean confidential){
		this.msgType = type;
		this.msgContent = content;
		this.confidential = confidential;
	}
	
	public boolean isConfidential() {
		return confidential;
	}

	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
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
