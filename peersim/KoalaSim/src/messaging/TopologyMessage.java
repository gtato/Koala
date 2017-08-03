package messaging;


import java.util.ArrayList;



import topology.TopologyPathNode;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;

public abstract class TopologyMessage {
	public static int CAT_LOCAL = 0;
	public static int CAT_CLOSE = 1;
	public static int CAT_GLOBAL = 2;
	public static int CAT_UNDEFINED = 3;
	
	protected ArrayList<Double> latencies = new ArrayList<Double>();
	protected ArrayList<TopologyPathNode> path = new ArrayList<TopologyPathNode>();
	protected int id;
	protected int type;
	protected int category;
	protected String label;
	protected TopologyMessageContent content;
	
	
	private long sentCycle;
	private long receivedCycle;

	public TopologyMessage(){setCategory(CAT_UNDEFINED);}
	
	public TopologyMessage(TopologyMessageContent content){
		setCategory(CAT_UNDEFINED);
		this.type = content.getMsgType();
		this.content = content;
		
	}
	
	public abstract Class<? extends TopologyMessageContent>  getContentClassFromType();
	public abstract String getTypeName();
	
	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}
	
	public TopologyMessageContent getContent() {
		return content;
	}

	public void setContent(TopologyMessageContent msgContent) {
		this.content = msgContent;
	}
	
	public int getType() {
		return type;
	}
	
	public boolean isType(int type){
		return this.type == type;
	}
	
	public void setType(int msgType) {
		this.type = msgType;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public int getCategory() {
		return category;
	}
	
	public void setCategory(int cat) {
		this.category = cat;
	}
	
	public double getLatency() {
		if(this.latencies.size() == 0)
			return -1;
		return this.latencies.get(this.latencies.size()-1);
	}
	
	public double getTotalLatency() {
		double tl = 0;
		for(Double l : this.latencies)
			tl += l;
		return PhysicalDataProvider.round(tl);
	}

	public void addLatency(double latency) {
		double l = PhysicalDataProvider.round(latency);
		this.latencies.add(l);
		
	}
	
	public ArrayList<Double> getLatencies() {
		return latencies;
	}
	
	public void setLatencies(ArrayList<Double> lats) {
		this.latencies = lats;
	}
	
	
	public void addToPath(TopologyPathNode dest){
		boolean alreadythere = false;
		if(path.size() > 0 && path.get(path.size()-1).getCID().equals(dest.getCID()))
			alreadythere = true;
		
		if (dest != null && !alreadythere)
			path.add(dest);
	}
	
	public ArrayList<TopologyPathNode> getPath(){
		return path;
	}
	
	public boolean pathContains(Object n){
		for(TopologyPathNode tpn : path)
			if(tpn.equals(n))
				return true;
		return false;
	}
	
	
//	public ArrayList<String> getChordPath(){
//		ArrayList<String> chordPath = new ArrayList<String>();
//		for(TopologyPathNode nid : path){
//			chordPath.add(NodeUtilities.getChordNode(nid).chordId.toString());
//		}
//		return chordPath;
//	}
//	
	
	public ArrayList<String> pathToStrArray(){
		ArrayList<String> specificPath = new ArrayList<String>();
		for(TopologyPathNode nid : path)
			specificPath.add(nid.getCID());
		
		return specificPath;
	}
	
	public ArrayList<String> specificPathToStrArray(){
		ArrayList<String> specificPath = new ArrayList<String>();
		for(TopologyPathNode nid : path)
			specificPath.add(nid.getSID());
		return specificPath;
	}
	
	public void setPath(ArrayList<TopologyPathNode> p){
		path = p;
	}
	
	public long getSentCycle() {
		return sentCycle;
	}

	public void setSentCycle(long sentCycle) {
		this.sentCycle = sentCycle;
	}

	public long getReceivedCycle() {
		return receivedCycle;
	}

	public void setReceivedCycle(long receivedCycle) {
		this.receivedCycle = receivedCycle;
	}
	
	
	public ArrayList<String> getPhysicalPathToStrArray(){
		ArrayList<String> fpList = new ArrayList<String>();
		if(path.size() < 2)
			return pathToStrArray();
		
		for(int i=0; i < path.size()-1;i++)
			fpList.addAll(PhysicalDataProvider.getPath(path.get(i).getCID(), path.get(i+1).getCID()));
		
		ArrayList<String> fullpath = new ArrayList<String>();
		for(int i = 0; i<fpList.size()-1; i++)
			if(!fpList.get(i).equals(fpList.get(i+1)))
				fullpath.add(fpList.get(i));
		fullpath.add(fpList.get(fpList.size()-1));
		
		return fullpath;
	}
	public TopologyPathNode getFirstSender() {
		if(path.size() > 0)
			return path.get(0);
		return null;
	}
	
	public TopologyPathNode getLastSender(){
		if(path.size()==0)
			return null;
		if(path.size()==1)
			return path.get(0);
		return path.get(path.size()-2);
	}
	
	
	public TopologyPathNode getReceiver() {
		if(path.size() > 0)
			return path.get(path.size()-1);
		return null;
	}
	
//	public int getHopCategory(){
//		int hops = NodeUtilities.distance(getFirstSender(), getReceiver());
//		
//		if(hops <= NodeUtilities.hopCategories[0])
//			return 1;
//		if(hops > NodeUtilities.hopCategories[NodeUtilities.hopCategories.length-1])
//			return NodeUtilities.hopCategories.length+1;
//		
//		for(int i = 1; i < NodeUtilities.hopCategories.length; i++){
//			if(NodeUtilities.hopCategories[i-1] < hops && hops <= NodeUtilities.hopCategories[i])
//				return i+1;
//		}
//		return 0;
//	}
//	
	public int getHops(){
		return this.getPath().size()-1;
	}
	
	public int getLocalHops(){
		TopologyPathNode lastpn = null;
		int lh = 0;
		for(TopologyPathNode pn : path){
			if(lastpn == null){lastpn = pn; continue;}
			if(NodeUtilities.getDCID(lastpn.getSID()) == NodeUtilities.getDCID(pn.getSID()))
				lh++;
			lastpn = pn;
		}
		return lh;
	}
	
	public int getGlobalHops(){
		return getHops()-getLocalHops();
	}
	
//	
//	public int getLatencyCategory(){
//		double lat = PhysicalDataProvider.getLatency(getFirstSender(), getReceiver());
//		if(lat <= NodeUtilities.latencyCategories[0])
//			return 1;
//		if(lat > NodeUtilities.latencyCategories[NodeUtilities.latencyCategories.length-1])
//			return NodeUtilities.latencyCategories.length+1;
//		
//		for(int i = 1; i < NodeUtilities.latencyCategories.length; i++){
//			if(NodeUtilities.latencyCategories[i-1] < lat && lat <= NodeUtilities.latencyCategories[i])
//				return i+1;
//		}
//		return 0;
//	}
//	
	

	
//	public JsonElement serialize(TopologyMessage src, Type typeOfSrc, JsonSerializationContext context) {
//		JsonArray pathEntries = new JsonArray();
//		ArrayList<TopologyPathNode> mpath = src.getPath();
//		for(TopologyPathNode neig : mpath){
//			pathEntries.add(neig);
//		}
//		
//		JsonArray latencyEntries = new JsonArray();
//		ArrayList<Double> mlatency= src.getLatencies();
//		for(Double lat : mlatency)
//			latencyEntries.add(lat);
//		
//		
//		JsonObject obj = new JsonObject();
//		obj.addProperty("id", src.getID());
//		obj.addProperty("type", src.getType());
//		obj.addProperty("label", src.getLabel());
//		obj.add("path", (JsonElement)pathEntries);
//		obj.add("latencies", (JsonElement)latencyEntries);
//		obj.add("content", KoalaJsonParser.toJsonTree(src.getContent()));
//		return obj;
//	}
//	
//	
//	public TopologyMessage deserialize(TopologyMessage km, JsonElement src, Type typeOfSrc, JsonDeserializationContext context) throws JsonParseException {
//		JsonObject srcJO = src.getAsJsonObject();
//		km.setID(srcJO.get("id").getAsInt());
//		km.setType(srcJO.get("type").getAsInt());
//		km.setLabel(srcJO.get("label").getAsString());
//		JsonArray jpath = srcJO.getAsJsonArray("path");
//		ArrayList<TopologyPathNode> path = new ArrayList<TopologyPathNode>();
//		for(JsonElement entry : jpath)
//			path.add(entry.getAsString());
//
//		km.setPath(path);
//		
//		JsonArray jlat = srcJO.getAsJsonArray("latencies");
//		ArrayList<Double> latencies = new ArrayList<Double>();
//		for(JsonElement entry : jlat)
//			latencies.add(entry.getAsDouble());
//		km.setLatencies(latencies);
//		
//		km.setContent(KoalaJsonParser.jsonTreeToObject(srcJO.get("content"), km.getContentClassFromType()));
//		
//		return km;
//	}

	
}
