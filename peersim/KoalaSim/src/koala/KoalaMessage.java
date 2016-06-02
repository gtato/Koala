package koala;

import java.util.ArrayList;
import java.util.Random;

import koala.utility.KoalaNodeUtilities;

public class KoalaMessage {
	public static final int RT = 0;
	public static final int NGN = 1;
	public static final int ROUTE = 2;
	
	
	private int msgType;

	private String msgContent;
	/*if set to true, it means that you shouldn't share it with the rest of the DC*/
	private boolean confidential;
	private int latency;
	private String source;
	private ArrayList<String> path = new ArrayList<String>();	
	
	public KoalaMessage(String source, int type, String content){
		this.source = source;
		this.msgType = type;
		this.msgContent = content;
		
	}

	public KoalaMessage(String source, int type, String content, boolean confidential){
		this.source = source;
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
	
	public int getLatency() {
		return latency;
	}

	public void setLatency(int latency) {
		this.latency = latency;
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void addToPath(String destID){
		path.add(destID);
	}
	
	public ArrayList<String> getPath(){
		return path;
	}
	
	public void setRandomLatency(String sourceID, String destID){
        int sDC = KoalaNodeUtilities.getDCID(sourceID);
        int dDC = KoalaNodeUtilities.getDCID(destID);

        Random random = new Random(sDC*dDC);
        int min, max;
         
        if (sDC == dDC){
            min = 5; max = 500;
        }else{
        	min = 500; max = 2000;
        }
        
        latency = random.nextInt((max - min) + 1) + min;
        
	}
	
	
}
