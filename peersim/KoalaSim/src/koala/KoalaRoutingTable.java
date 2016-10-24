package koala;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import utilities.NodeUtilities;


public class KoalaRoutingTable {
	

	private KoalaNeighbor localPredecessor;
	private KoalaNeighbor localSucessor;
	private KoalaNeighbor globalPredecessor;
	private KoalaNeighbor globalSucessor;
	private ArrayList<KoalaNeighbor> longLinks = new ArrayList<KoalaNeighbor>();
	
	
	/*these two are supposed to be used only when the object is transmitted*/
	private ArrayList<KoalaNeighbor> neighborsContainer = new ArrayList<KoalaNeighbor>();
	private ArrayList<KoalaNeighbor> oldNeighborsContainer = new ArrayList<KoalaNeighbor>();
	
	
	public KoalaRoutingTable(){
		KoalaNeighbor defaultNeighbor = new KoalaNeighbor(NodeUtilities.DEFAULTID);
		localPredecessor = defaultNeighbor;
		localSucessor = defaultNeighbor;
		globalPredecessor = defaultNeighbor;
		globalSucessor = defaultNeighbor;
	}

	public KoalaNeighbor getLocalPredecessor() {
		return localPredecessor;
	}

	public KoalaNeighbor setLocalPredecessor(KoalaNeighbor kn) {
		KoalaNeighbor oldEntry = null;
		
		if(this.localPredecessor.equals(kn)){
			//update
			this.localPredecessor.update(kn);
		}else{
			oldEntry = this.localPredecessor; 
			this.localPredecessor = kn;
		}
		return oldEntry;
	}
	
	public KoalaNeighbor getLocalSucessor() {
		return localSucessor;
	}

	public KoalaNeighbor setLocalSucessor(KoalaNeighbor kn) {
		KoalaNeighbor oldEntry = null;		
		if(this.localSucessor.equals(kn)){
			//update
			this.localSucessor.update(kn);
		}else{
			oldEntry = this.localSucessor; 
			this.localSucessor = kn;
		}
		return oldEntry;
	}

	public KoalaNeighbor getGlobalPredecessor() {
		return globalPredecessor;
	}

	public KoalaNeighbor setGlobalPredecessor(KoalaNeighbor kn) {
		KoalaNeighbor oldEntry = null;
		if(this.globalPredecessor.equals(kn)){
			//update
			this.globalPredecessor.update(kn);
		}else{
			oldEntry = this.globalPredecessor; 
			this.globalPredecessor = kn;
		}
		return oldEntry;
	}

	public KoalaNeighbor getGlobalSucessor() {
		return globalSucessor;
	}

	public KoalaNeighbor setGlobalSucessor(KoalaNeighbor kn) {
		KoalaNeighbor oldEntry = null;
		if(this.globalSucessor.equals(kn)){
			//update
			this.globalSucessor.update(kn);
		}else{
			oldEntry = this.globalSucessor; 
			this.globalSucessor = kn;
		}
		return oldEntry;
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
			neighs.add(this.localPredecessor); neighs.add(this.localSucessor); 
		}else if (which == 2){ //only globals
			neighs.add(this.globalPredecessor); neighs.add(this.globalSucessor);
		}else if (which == 3){ //globals and long links
			neighs.add(this.globalPredecessor); neighs.add(this.globalSucessor);
			for(KoalaNeighbor ll : longLinks)
				neighs.add(ll);
		}else{ //everything 
			neighs.add(this.localPredecessor); neighs.add(this.localSucessor);
			neighs.add(this.globalPredecessor); neighs.add(this.globalSucessor);
			
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
		neighs.add(this.localPredecessor); neighs.add(this.localSucessor);
		neighs.add(this.globalPredecessor); neighs.add(this.globalSucessor);
		
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
