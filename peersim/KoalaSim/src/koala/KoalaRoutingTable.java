package koala;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import utilities.NodeUtilities;


public class KoalaRoutingTable {
	

	private KoalaNeighbor[] localPredecessors;
	private KoalaNeighbor[] localSucessors;
	private KoalaNeighbor[] globalPredecessors;
	private KoalaNeighbor[] globalSucessors;
	private ArrayList<KoalaNeighbor> longLinks = new ArrayList<KoalaNeighbor>();
	
	
	/*these two are supposed to be used only when the object is transmitted*/
	private ArrayList<KoalaNeighbor> neighborsContainer = new ArrayList<KoalaNeighbor>();
	private ArrayList<KoalaNeighbor> oldNeighborsContainer = new ArrayList<KoalaNeighbor>();
	
	
	public KoalaRoutingTable(){
		KoalaNeighbor defaultNeighbor = new KoalaNeighbor(NodeUtilities.DEFAULTID);
		localPredecessors = new KoalaNeighbor[NodeUtilities.NEIGHBORS];
		localSucessors = new KoalaNeighbor[NodeUtilities.NEIGHBORS];
		globalPredecessors = new KoalaNeighbor[NodeUtilities.NEIGHBORS];
		globalSucessors = new KoalaNeighbor[NodeUtilities.NEIGHBORS];
		for(int i = 0; i < NodeUtilities.NEIGHBORS; i++){
			localPredecessors[i] = defaultNeighbor;
			localSucessors[i] = defaultNeighbor;
			globalPredecessors[i] = defaultNeighbor;
			globalSucessors[i] = defaultNeighbor;
		}
	}

	
	
	public KoalaNeighbor[] getLocalPredecessors() {
		return localPredecessors;
	}

	public KoalaNeighbor[] getLocalSucessors() {
		return localSucessors;
	}

	public KoalaNeighbor[] getGlobalPredecessors() {
		return globalPredecessors;
	}

	public KoalaNeighbor[] getGlobalSucessors() {
		return globalSucessors;
	}



	public boolean hasAllDefaultLocals(){
		for(int i = 0; i < NodeUtilities.NEIGHBORS; i++){
			if(!localSucessors[i].getNodeID().equals(NodeUtilities.DEFAULTID)
			|| !localPredecessors[i].getNodeID().equals(NodeUtilities.DEFAULTID)
				)
				return false;
		}
		return true;
	}
	
	public boolean hasAllDefaultGlobals(){
		for(int i = 0; i < NodeUtilities.NEIGHBORS; i++){
			if(!globalSucessors[i].getNodeID().equals(NodeUtilities.DEFAULTID)
			|| !globalPredecessors[i].getNodeID().equals(NodeUtilities.DEFAULTID)
				)
				return false;
		}
		return true;
	}
	
	public KoalaNeighbor getLocalPredecessor(int index) {
		return localPredecessors[index];
	}

	public KoalaNeighbor setLocalPredecessor(KoalaNeighbor kn, int index) {
		KoalaNeighbor oldEntry = null;
		
		if(localPredecessors[index].equals(kn)){
			//update
			localPredecessors[index].update(kn);
		}else{
//			oldEntry = localPredecessors[index]; 
//			localPredecessors[index] = kn;
			oldEntry = last(localPredecessors);
			setIndex(localPredecessors, index, kn);
		}
		return oldEntry;
	}
	
	public KoalaNeighbor getLocalSucessor(int index) {
		return localSucessors[index];
	}

	public KoalaNeighbor setLocalSucessor(KoalaNeighbor kn, int index) {
		KoalaNeighbor oldEntry = null;		
		if(localSucessors[index].equals(kn)){
			//update
			localSucessors[index].update(kn);
		}else{
//			oldEntry = localSucessors[index]; 
//			localSucessors[index] = kn;
			oldEntry = last(localSucessors);
			setIndex(localSucessors, index, kn);
		}
		return oldEntry;
	}

	public KoalaNeighbor getGlobalPredecessor(int index) {
		return globalPredecessors[index];
	}

	public KoalaNeighbor setGlobalPredecessor(KoalaNeighbor kn, int index) {
		KoalaNeighbor oldEntry = null;
		if(globalPredecessors[index].equals(kn)){
			//update
			globalPredecessors[index].update(kn);
		}else{
//			oldEntry = globalPredecessors[index]; 
//			globalPredecessors[index] = kn;
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
//			oldEntry = globalSucessors[index]; 
//			globalSucessors[index] = kn;
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
		
//		if(this.getNeighboursIDs(1).contains(kn.getNodeID())
//		||this.getNeighboursIDs(2).contains(kn.getNodeID()))
//			return false;
		
		boolean added = false;
		for(int i = 0; i < longLinks.size(); i++){
			KoalaNeighbor ll = longLinks.get(i);
			if(ll.getNodeID().equals(kn.getNodeID())){
				if(kn.getLatencyQuality() >= ll.getLatencyQuality()){
					ll.setLatency(kn.getLatency());
					ll.setLatencyQuality(kn.getLatencyQuality());
				}
				return false;
			}
			else{
				int dist = NodeUtilities.distance(ll.getIdealID(), kn.getNodeID());
				int currentDist = NodeUtilities.distance(ll.getIdealID(), ll.getNodeID());
				if(dist < currentDist){
					//here there might be a situation where we would have to chose between a better id or a better latency quality
					ll.setNodeID(kn.getNodeID());
					ll.setLatency(kn.getLatency());
					ll.setLatencyQuality(kn.getLatencyQuality());
					added=true;
				}
			}
			
		}
		
		return added;
		
	}	
	
	public void setLongLinks(ArrayList<KoalaNeighbor> longLinks){
		this.longLinks = longLinks;
	}
	
	public void clearLongLinks(){
		longLinks.clear();
	}
	
	public ArrayList<KoalaNeighbor> getLongLinks(){
		return longLinks;
	}
	
	/*
	 * These are some simple utility functions
	 */
	
	
	public ArrayList<KoalaNeighbor> getOldNeighborsContainer() {
		return oldNeighborsContainer;
	}


	public ArrayList<KoalaNeighbor> getNeighborsContainer() {
		return neighborsContainer;
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
			neighs.addAll(Arrays.asList(localPredecessors)); neighs.addAll(Arrays.asList(localSucessors)); 
		}else if (which == 2){ //only globals
			neighs.addAll(Arrays.asList(globalPredecessors)); neighs.addAll(Arrays.asList(globalSucessors));
		}else if (which == 3){ //globals and long links
			neighs.addAll(Arrays.asList(globalPredecessors)); neighs.addAll(Arrays.asList(globalSucessors));
			for(KoalaNeighbor ll : longLinks)
				neighs.add(ll);
		}else{ //everything 
			neighs.addAll(Arrays.asList(localPredecessors)); neighs.addAll(Arrays.asList(localSucessors));
			neighs.addAll(Arrays.asList(globalPredecessors)); neighs.addAll(Arrays.asList(globalSucessors));
			
			for(KoalaNeighbor ll : longLinks)
				neighs.add(ll);
		}
		Set<String> hs = new LinkedHashSet<String>();
        
        for(int i = 0; i < neighs.size(); i++)
            if(!NodeUtilities.isDefault(neighs.get(i)))
            	hs.add(neighs.get(i).getNodeID());
                
        return hs;
	}
	
	
	public ArrayList<KoalaNeighbor> getNeighbors(){
		ArrayList<KoalaNeighbor> neighs = new ArrayList<KoalaNeighbor>();
		ArrayList<KoalaNeighbor> hs = new ArrayList<KoalaNeighbor>();
		neighs.addAll(Arrays.asList(localPredecessors)); neighs.addAll(Arrays.asList(localSucessors));
		neighs.addAll(Arrays.asList(globalPredecessors)); neighs.addAll(Arrays.asList(globalSucessors));
		
		for(KoalaNeighbor ll : longLinks)
			neighs.add(ll);
        
		for(int i = 0; i < neighs.size(); i++)
            if(!NodeUtilities.isDefault(neighs.get(i)))
            	hs.add(neighs.get(i));
		
        for(int i = 0; i < neighborsContainer.size(); i++)
        	if(!NodeUtilities.isDefault(neighs.get(i)))
        		hs.add(neighborsContainer.get(i));
                
        return hs;
	}
	
}
