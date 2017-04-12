package topology;

import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.core.Protocol;
import example.hot.InetCoordinates;

public class TopologyNode extends InetCoordinates implements Protocol, Linkable{

	private String id;
//	private int pid;
	private long birthday;
	protected boolean joined;
	private Node node;
	boolean visited;
	
	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public long getBirthday() {
		return birthday;
	}

	public void setBirthday(long birthday) {
		this.birthday = birthday;
	}

	public boolean hasJoined() {
		return joined;
	}

	public void setJoined(boolean joined) {
		this.joined = joined;
		if(joined)
			this.setBirthday(CommonState.getTime());
	}
	
	public void reset(){
		setJoined(false);
	}
	
	public long getAge(){
		if(!joined)
			return -1;
		return CommonState.getTime() - getBirthday();
	}

	
	public TopologyNode(String prefix) {
		super(prefix);
		// TODO Auto-generated constructor stub
	}

	public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
	}

	@Override
	public void onKill() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean addNeighbor(Node neighbour) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Node neighbor) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int degree() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Node getNeighbor(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void pack() {
		// TODO Auto-generated method stub
		
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}
	
	public boolean isUp(){
		return this.node.isUp();
	}
	
	public long getSimNodeID(){
		return this.node.getID();
	}

//	public int getPid() {
//		return pid;
//	}
//
//	public void setPid(int pid) {
//		this.pid = pid;
//	}
}
