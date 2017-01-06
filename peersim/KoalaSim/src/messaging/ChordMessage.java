package messaging;

public class ChordMessage extends TopologyMessage{
	public static final int LOOK_UP=0;
	public static final int FINAL=1;
	public static final int SUCCESSOR=2;
	public static final int SUCCESSOR_FOUND=3;
	public static final int NOTIFY=4;
	
	public ChordMessage(ChordLookUpContent content){
		super(content);
	}
	
	@Override
	public Class<? extends TopologyMessageContent> getContentClassFromType() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getTypeName() {
		// TODO Auto-generated method stub
		return null;
	}
}
