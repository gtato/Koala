package koala;

import peersim.core.Node;
import utilities.NodeUtilities;

public class FlatKoalaNode extends KoalaNode {

	private String commonID;
	
	public FlatKoalaNode(String prefix) {
		super(prefix);
	}

	public String getCommonID() {
		return commonID;
	}

	public void setCommonID(String commonID) {
		this.commonID = commonID;
	}
	
	protected Node getNode(String id){
		String cid = NodeUtilities.FlatMap.get(id);
		return NodeUtilities.Nodes.get(cid);
	}
	

}
