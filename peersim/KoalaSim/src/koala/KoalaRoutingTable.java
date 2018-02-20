package koala;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import peersim.core.CommonState;
import peersim.core.Node;
import topology.TopologyPathNode;
import utilities.NodeUtilities;
import utilities.PhysicalDataProvider;


public class KoalaRoutingTable {
	

	private KoalaNeighbor[] globalPredecessors;
	private KoalaNeighbor[] globalSucessors;
	private ArrayList<KoalaNeighbor> longLinks = new ArrayList<KoalaNeighbor>();
	private ArrayList<KoalaNeighbor> randLinks = new ArrayList<KoalaNeighbor>();
	private ArrayList<KoalaNeighbor> vicinityLinks = new ArrayList<KoalaNeighbor>();
	private ArrayList<KoalaNeighbor> applicationLinks = new ArrayList<KoalaNeighbor>();
	private ArrayList<KoalaNeighbor> locals = new ArrayList<KoalaNeighbor>();
	
	
	/*these two are supposed to be used only when the object is transmitted*/
	private ArrayList<KoalaNeighbor> neighborsContainer = new ArrayList<KoalaNeighbor>();
	private ArrayList<KoalaNeighbor> oldNeighborsContainer = new ArrayList<KoalaNeighbor>();
	private KoalaNode myNode;
	
	public KoalaRoutingTable(KoalaNode kn){
		myNode = kn;
		intializeGlobals();
	}
	
	public void intializeGlobals(){
		KoalaNeighbor defaultNeighbor = KoalaNeighbor.getDefaultNeighbor();
		globalPredecessors = new KoalaNeighbor[NodeUtilities.NEIGHBORS];
		globalSucessors = new KoalaNeighbor[NodeUtilities.NEIGHBORS];
		for(int i = 0; i < NodeUtilities.NEIGHBORS; i++){
			globalPredecessors[i] = defaultNeighbor;
			globalSucessors[i] = defaultNeighbor;
		}
	}
	
	public void initializeVicinities(){
		vicinityLinks = new ArrayList<KoalaNeighbor>();
		KoalaNeighbor defaultNeighbor;
		for(int i = 0; i < NodeUtilities.VICINITY_LINKS; i++){
			defaultNeighbor = KoalaNeighbor.getDefaultNeighbor();
			defaultNeighbor.setLatency(Double.MAX_VALUE);
			vicinityLinks.add(defaultNeighbor);
		}
	}
	
	public void intializeLongLinks(){
		longLinks = new ArrayList<KoalaNeighbor>();
		int k = NodeUtilities.getLongLinks();
		int size = NodeUtilities.getSize(); 
		int n = size / 2;
//		if(size <= k)
//			k = size/4;
		int limit = 50;
		int trials = 0;
		HashSet<Integer> nids = new LinkedHashSet<Integer>();
		while(nids.size() != k && trials < limit){
			int sizebefore = nids.size();
			int nid = (int) Math.round(Math.exp(Math.log(n) * (CommonState.r.nextDouble()-1.0))*n);
			if(nid >  NodeUtilities.NEIGHBORS) //skip neighbors, we already have them
				nids.add(nid);
			if(sizebefore == nids.size()) trials++;
			else trials = 0;
		}
		
		for(Integer dist : nids){
			String[] ids = NodeUtilities.getIDFromDistance(myNode.getSID(), dist);
			String realId = CommonState.r.nextInt() % 2 == 0 ? ids[0] : ids[1]; 
			KoalaNeighbor kneigh = new KoalaNeighbor(new TopologyPathNode(NodeUtilities.DEFAULTID), PhysicalDataProvider.getDefaultInterLatency());
			kneigh.setIdealID(realId);
			longLinks.add(kneigh);
		}
		nids.clear();
	}
	
	public void intializeRandomLinks(){
		randLinks = new ArrayList<KoalaNeighbor>();
		int k = NodeUtilities.RAND_LINKS;
		for(int i = 0; i < k; i++){
			int dc = CommonState.r.nextInt(NodeUtilities.NR_DC);
			int nid = CommonState.r.nextInt(NodeUtilities.NR_NODE_PER_DC);
			String id = dc+"-"+nid;
			KoalaNeighbor kneigh = new KoalaNeighbor(new TopologyPathNode(NodeUtilities.DEFAULTID), PhysicalDataProvider.getDefaultInterLatency());
			kneigh.setIdealID(id);
			randLinks.add(kneigh);
		}
	}
	
	
	
	public KoalaNeighbor[] getGlobalPredecessors() {
		return globalPredecessors;
	}

	public KoalaNeighbor[] getGlobalSucessors() {
		return globalSucessors;
	}

	public boolean hasAllDefaultLocals(){
		for(KoalaNeighbor ln : locals){
			if(!ln.getSID().equals(NodeUtilities.DEFAULTID))
				return false;
		}
		return true;
	}
	
	public boolean hasAllDefaultGlobals(){
		for(int i = 0; i < NodeUtilities.NEIGHBORS; i++){
			if(!globalSucessors[i].getSID().equals(NodeUtilities.DEFAULTID)
			|| !globalPredecessors[i].getSID().equals(NodeUtilities.DEFAULTID)
				)
				return false;
		}
		return true;
	}
	
	public boolean addLocal(KoalaNeighbor kn){
		boolean add = true;
		for(KoalaNeighbor n : locals)
			if(n.equals(kn)){
				n.setLatency(kn.getLatency());
				n.setLatencyQuality(kn.getLatencyQuality());
				add = false;
			}
		if(add)
			locals.add(kn);
		return add;
	}
	
	public ArrayList<KoalaNeighbor> getLocals(){
		return locals;
	}
	
	public void setLocals(ArrayList<KoalaNeighbor> l){
		if(l!=null)
			locals = l;
	}
	
	
	public KoalaNeighbor getGlobalPredecessor(int index) {
		return globalPredecessors[index];
	}

	public KoalaNeighbor setGlobalPredecessor(KoalaNeighbor kn, int index) {
		KoalaNeighbor oldEntry = null;
		if(globalPredecessors[index].equals(kn)){
			//update
			globalPredecessors[index].updateLatency(kn);
		}else{
			oldEntry = last(globalPredecessors);
			setIndex(globalPredecessors, index, kn);
		}
		return oldEntry;
	}

	public KoalaNeighbor getGlobalSucessor(int index) {
		return globalSucessors[index];
	}

	public KoalaNeighbor setGlobalSucessor(KoalaNeighbor kn, int index) {
		KoalaNeighbor oldEntry = null;
		if(globalSucessors[index].equals(kn)){
			//update
			globalSucessors[index].updateLatency(kn);
		}else{ 
			oldEntry = last(globalSucessors);
			setIndex(globalSucessors, index, kn);

		}
		return oldEntry;
	}
	
	private void setIndex(KoalaNeighbor[] a, int index, KoalaNeighbor val){
		System.arraycopy(a, index, a, index+1, a.length-index-1);
		a[index] = val;
	}
	
	private KoalaNeighbor last(KoalaNeighbor[] a){
		return a[a.length-1];
	}
	
	public boolean updateLongLinks(KoalaNeighbor kn){
		return updateLinkList(kn, longLinks);
	}	
	
	public boolean updateRandomLinks(KoalaNeighbor kn){
		return updateLinkList(kn, randLinks);
	}
	
	private boolean updateLinkList(KoalaNeighbor kn, ArrayList<KoalaNeighbor> linkList){
		Node nn = NodeUtilities.Nodes.get(kn.getCID());
		if(nn==null || !nn.isUp()) return false;
		boolean added = false;
		for(int i = 0; i < linkList.size(); i++){
			KoalaNeighbor ll = linkList.get(i);
			if(ll.equals(kn)){ //update
				if(kn.getLatencyQuality() >= ll.getLatencyQuality()){
					ll.setLatency(kn.getLatency());
					ll.setLatencyQuality(kn.getLatencyQuality());
				}
				return false;
			}
			else{
				int dist = NodeUtilities.distance(ll.getIdealID(), kn.getSID());
				int currentDist = NodeUtilities.distance(ll.getIdealID(), ll.getSID());
				if(dist < currentDist){
					//here there might be a situation where we would have to chose between a better id or a better latency quality
					ll.copy(kn, false);
					added=true;
				}
			}			
		}
		return added;
	}
	
	public boolean addVicinityLink(KoalaNeighbor vicinityLink){
		if(NodeUtilities.VICINITY_LINKS == 0 || NodeUtilities.isDefault(vicinityLink)) return false;
		if(vicinityLinks.size() == 0)
			initializeVicinities();
		int maxLatPos =-1, sameDCPos = -1; 
		double maxLat = -1; 
		for(int i = 0; i < vicinityLinks.size(); i ++){
			KoalaNeighbor each = vicinityLinks.get(i);
			if(each.getLatency() > maxLat){
				maxLat = each.getLatency();
				maxLatPos = i;  
			}
			if(NodeUtilities.sameDC(vicinityLink, each))
				sameDCPos = i;
		}
		if(vicinityLink.getLatency() > maxLat) return false;
		if(sameDCPos >= 0 && vicinityLinks.get(sameDCPos).getLatency() <= vicinityLink.getLatency()) return false;
		
		KoalaNeighbor oldie = sameDCPos >= 0? vicinityLinks.remove(sameDCPos) : vicinityLinks.remove(maxLatPos);
		oldie.copy(vicinityLink, true);
		vicinityLinks.add(0, oldie);
		return true;
	}
	
	public boolean addApplicationLink(KoalaNeighbor applicationLink){
		if(NodeUtilities.APPLICATION_LINKS == 0 || NodeUtilities.isDefault(applicationLink)) return false;
		KoalaNeighbor oldie = applicationLinks.size() >  NodeUtilities.APPLICATION_LINKS ?
				applicationLinks.remove(applicationLinks.size()-1) :
				KoalaNeighbor.getDefaultNeighbor();	
		oldie.copy(applicationLink, true);
		applicationLinks.add(0, oldie);
		return true;
	}
	
	
	/*
	 * These are some simple utility functions
	 */
	
	public void setLongLinks(ArrayList<KoalaNeighbor> longLinks){
		this.longLinks = longLinks;
	}
		
	public ArrayList<KoalaNeighbor> getLongLinks(){
		return longLinks;
	}
	
	public void setRandLinks(ArrayList<KoalaNeighbor> randLinks){
		this.randLinks = randLinks;
	}
	
	public ArrayList<KoalaNeighbor> getRandLinks(){
		return randLinks;
	}
	
	public void setVicinityLinks(ArrayList<KoalaNeighbor> vicinityLinks){
		this.vicinityLinks = vicinityLinks;
	}
	
	public ArrayList<KoalaNeighbor> getVicinityLinks(){
		return vicinityLinks;
	}
	
	public void setApplicationLinks(ArrayList<KoalaNeighbor> applicationLinks){
		this.applicationLinks = applicationLinks;
	}
	
	public ArrayList<KoalaNeighbor> getApplicationLinks(){
		return applicationLinks;
	}
	
//	public void resetRands(){
//		randLinks = new ArrayList<KoalaNeighbor>();
//		KoalaNeighbor defaultNeighbor;
//		for(int i = 0; i < NodeUtilities.RAND_LINKS; i++){
//			defaultNeighbor = KoalaNeighbor.getDefaultNeighbor();
//			randLinks.add(defaultNeighbor);
//		}
//	}
//	
//	public void addRandLink(KoalaNeighbor randLink){
//		if(NodeUtilities.RAND_LINKS == 0 || NodeUtilities.isDefault(randLink)) return;
//		if(randLinks.size() == 0)
//			resetRands();
//		for(KoalaNeighbor each : randLinks)
//			if(NodeUtilities.sameDC(randLink,each))
//				return;
//		KoalaNeighbor oldie = randLinks.remove(randLinks.size()-1);
//		oldie.copy(randLink, true);
//		randLinks.add(0, oldie);
//	}
	
	public ArrayList<KoalaNeighbor> getOldNeighborsContainer() {
		return oldNeighborsContainer;
	}


	public ArrayList<KoalaNeighbor> getNeighborsContainer() {
		return neighborsContainer;
	}
	
	public ArrayList<String> getNeighborsContainerIDs() {
		ArrayList<String> ids = new ArrayList<String>();
		for(KoalaNeighbor kn : neighborsContainer)
			ids.add(kn.getSID());
		return ids;
	}

	public void setNeighborsContainer(ArrayList<KoalaNeighbor> neighbors) {
		this.neighborsContainer = neighbors;
	}

	public void setOldNeighborsContainer(ArrayList<KoalaNeighbor> oldNeighbors) {
		this.oldNeighborsContainer = oldNeighbors;
	}

	public int getSize(){
		return getNeighboursIDs(0).size();
	}
	
	public Set<String> getNeighboursIDs(){
		return getNeighboursIDs(0);
	}
	
	public Set<String> getNeighboursIDs(int which){
		
		ArrayList<KoalaNeighbor> neighs = new ArrayList<KoalaNeighbor>();

		if(which == 1){ //only locals 
			neighs.addAll(locals); 
		}else if (which == 2){ //only globals
			neighs.addAll(Arrays.asList(globalPredecessors)); neighs.addAll(Arrays.asList(globalSucessors));
		}else if (which == 3){ //globals and long links
			neighs.addAll(Arrays.asList(globalPredecessors)); neighs.addAll(Arrays.asList(globalSucessors));
			for(KoalaNeighbor ll : longLinks)
				neighs.add(ll);
		}else{ //everything 
			neighs.addAll(locals);
			neighs.addAll(Arrays.asList(globalPredecessors)); neighs.addAll(Arrays.asList(globalSucessors));
			neighs.addAll(longLinks);
			neighs.addAll(randLinks);
			neighs.addAll(vicinityLinks);
			neighs.addAll(applicationLinks);
//			neighs.addAll(temps);
		}
		Set<String> hs = new LinkedHashSet<String>();
        
        for(int i = 0; i < neighs.size(); i++)
            if(!NodeUtilities.isDefault(neighs.get(i)))
            	hs.add(neighs.get(i).getSID());
                
        return hs;
	}
	
	public Set<String> getFirstGlobalNeighboursIDs(){
		ArrayList<KoalaNeighbor> neighs = new ArrayList<KoalaNeighbor>();
		neighs.add(globalPredecessors[0]);
		neighs.add(globalSucessors[0]);
		Set<String> hs = new LinkedHashSet<String>();
        for(int i = 0; i < neighs.size(); i++)
            if(!NodeUtilities.isDefault(neighs.get(i)))
            	hs.add(neighs.get(i).getSID());
        return hs;
	}
	
	public void confirmNeighbors(){
		ArrayList<KoalaNeighbor> neighs = getNeighbors();
		for(KoalaNeighbor n: neighs)
			n.setRecentlyAdded(false);
//		temps.clear();
	}
	
	public ArrayList<KoalaNeighbor> getNeighbors(){
		ArrayList<KoalaNeighbor> neighs = new ArrayList<KoalaNeighbor>();
		ArrayList<KoalaNeighbor> hs = new ArrayList<KoalaNeighbor>();
		neighs.addAll(locals);
		neighs.addAll(Arrays.asList(globalPredecessors)); neighs.addAll(Arrays.asList(globalSucessors));
		neighs.addAll(longLinks);
		neighs.addAll(randLinks);
		neighs.addAll(vicinityLinks);
		neighs.addAll(applicationLinks);
		//		neighs.addAll(temps);
		
		Set<String> idhs = new LinkedHashSet<String>();
		
		for(KoalaNeighbor n: neighs)
            if(!NodeUtilities.isDefault(n) && !idhs.contains(n.getSID())){
            	hs.add(n); idhs.add(n.getSID());
            }
		
		
		for(KoalaNeighbor n: neighborsContainer)
            if(!NodeUtilities.isDefault(n) && !idhs.contains(n.getSID())){
            	hs.add(n); idhs.add(n.getSID());
            }
		
        return hs;
	}
	
	public ArrayList<KoalaNeighbor> getGlobalNeighbors(){
		ArrayList<KoalaNeighbor> neighs = new ArrayList<KoalaNeighbor>();
		ArrayList<KoalaNeighbor> hs = new ArrayList<KoalaNeighbor>();
		neighs.addAll(Arrays.asList(globalPredecessors)); neighs.addAll(Arrays.asList(globalSucessors));
		neighs.addAll(longLinks);
		neighs.addAll(randLinks);
		neighs.addAll(vicinityLinks);
		neighs.addAll(applicationLinks);
//		neighs.addAll(temps);
		
		Set<String> idhs = new LinkedHashSet<String>();
		
		for(KoalaNeighbor n: neighs)
            if(!NodeUtilities.isDefault(n) && !idhs.contains(n.getSID())){
            	hs.add(n); idhs.add(n.getSID());
            }
		
        return hs;
	}
	
}
