package koala;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import peersim.core.Node;
import utilities.NodeUtilities;


public class KoalaRoutingTable {
	

//	private KoalaNeighbor[] localPredecessors;
//	private KoalaNeighbor[] localSucessors;
	private KoalaNeighbor[] globalPredecessors;
	private KoalaNeighbor[] globalSucessors;
	private ArrayList<KoalaNeighbor> longLinks = new ArrayList<KoalaNeighbor>();
	private ArrayList<KoalaNeighbor> randLinks = new ArrayList<KoalaNeighbor>();
	private ArrayList<KoalaNeighbor> locals = new ArrayList<KoalaNeighbor>();
	
	
	/*these two are supposed to be used only when the object is transmitted*/
	private ArrayList<KoalaNeighbor> neighborsContainer = new ArrayList<KoalaNeighbor>();
	private ArrayList<KoalaNeighbor> oldNeighborsContainer = new ArrayList<KoalaNeighbor>();
	
	
	public KoalaRoutingTable(){
		resetGlobals();
	}
	
	
//	public KoalaNeighbor[] getLocalPredecessors() {
//		return localPredecessors;
//	}
//
//	public KoalaNeighbor[] getLocalSucessors() {
//		return localSucessors;
//	}

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
	
	public void resetGlobals(){
		KoalaNeighbor defaultNeighbor = KoalaNeighbor.getDefaultNeighbor();
		globalPredecessors = new KoalaNeighbor[NodeUtilities.NEIGHBORS];
		globalSucessors = new KoalaNeighbor[NodeUtilities.NEIGHBORS];
		for(int i = 0; i < NodeUtilities.NEIGHBORS; i++){
			globalPredecessors[i] = defaultNeighbor;
			globalSucessors[i] = defaultNeighbor;
		}
	}
	
//	public KoalaNeighbor getLocalPredecessor(int index) {
//		return localPredecessors[index];
//	}
//
//	public KoalaNeighbor setLocalPredecessor(KoalaNeighbor kn, int index) {
//		KoalaNeighbor oldEntry = null;
//		
//		if(localPredecessors[index].equals(kn)){
//			//update
//			localPredecessors[index].update(kn);
//		}else{
////			oldEntry = localPredecessors[index]; 
////			localPredecessors[index] = kn;
//			oldEntry = last(localPredecessors);
//			setIndex(localPredecessors, index, kn);
//		}
//		return oldEntry;
//	}
//	
//	public KoalaNeighbor getLocalSucessor(int index) {
//		return localSucessors[index];
//	}
//
//	public KoalaNeighbor setLocalSucessor(KoalaNeighbor kn, int index) {
//		KoalaNeighbor oldEntry = null;		
//		if(localSucessors[index].equals(kn)){
//			//update
//			localSucessors[index].update(kn);
//		}else{
////			oldEntry = localSucessors[index]; 
////			localSucessors[index] = kn;
//			oldEntry = last(localSucessors);
//			setIndex(localSucessors, index, kn);
//		}
//		return oldEntry;
//	}

	public KoalaNeighbor getGlobalPredecessor(int index) {
		return globalPredecessors[index];
	}

	public KoalaNeighbor setGlobalPredecessor(KoalaNeighbor kn, int index) {
		KoalaNeighbor oldEntry = null;
		if(globalPredecessors[index].equals(kn)){
			//update
			globalPredecessors[index].update(kn);
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
			globalSucessors[index].update(kn);
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
	
	public boolean addLongLink(KoalaNeighbor kn){
//		Discard down neighbors (like Lamport would do)
		Node nn = NodeUtilities.Nodes.get(kn.getCID());
		if(nn==null || !nn.isUp())
        	return false;
		
		boolean added = false;
		for(int i = 0; i < longLinks.size(); i++){
			KoalaNeighbor ll = longLinks.get(i);
			if(ll.equals(kn)){
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
					ll.setCID(kn.getCID());
					ll.setSID(kn.getSID());
					ll.setLatency(kn.getLatency());
					ll.setLatencyQuality(kn.getLatencyQuality());
					ll.setRecentlyAdded(kn.isRecentlyAdded());
					added=true;
				}
			}			
		}
		
		return added;
		
	}	
	
	/*
	 * These are some simple utility functions
	 */
	
	public void setLongLinks(ArrayList<KoalaNeighbor> longLinks){
		this.longLinks = longLinks;
	}
	
//	public void clearLongLinks(){
//		longLinks.clear();
//	}
	
	public ArrayList<KoalaNeighbor> getLongLinks(){
		return longLinks;
	}
	
	public void addRandLink(KoalaNeighbor randLink){
		if(NodeUtilities.isDefault(randLink)) return;
		for(KoalaNeighbor each : randLinks)
			if(NodeUtilities.getDCID(randLink.getSID()) == NodeUtilities.getDCID(each.getSID()))
				return;
		randLinks.add(0, randLink);
		if(randLinks.size() > NodeUtilities.RAND_LINKS)
			randLinks.remove(randLinks.size()-1);
	}

	
	public ArrayList<KoalaNeighbor> getRandLinks(){
		return randLinks;
	}
	
	
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
	}
	
	public ArrayList<KoalaNeighbor> getNeighbors(){
		ArrayList<KoalaNeighbor> neighs = new ArrayList<KoalaNeighbor>();
		ArrayList<KoalaNeighbor> hs = new ArrayList<KoalaNeighbor>();
		neighs.addAll(locals);
		neighs.addAll(Arrays.asList(globalPredecessors)); neighs.addAll(Arrays.asList(globalSucessors));
		neighs.addAll(longLinks);
		neighs.addAll(randLinks);
		
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
		
		Set<String> idhs = new LinkedHashSet<String>();
		
		for(KoalaNeighbor n: neighs)
            if(!NodeUtilities.isDefault(n) && !idhs.contains(n.getSID())){
            	hs.add(n); idhs.add(n.getSID());
            }
		
        return hs;
	}
	
}
