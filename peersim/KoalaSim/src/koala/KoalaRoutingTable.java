package koala;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import koala.utility.NodeUtilities;


public class KoalaRoutingTable {
	
//	private String nodeID;
	private KoalaNeighbor localPredecessor;
	private KoalaNeighbor localSucessor;
	private KoalaNeighbor globalPredecessor;
	private KoalaNeighbor globalSucessor;
	
	/*these two are supposed to be used only when the object is transmitted*/
	private ArrayList<KoalaNeighbor> neighborsContainer = new ArrayList<KoalaNeighbor>();
	private ArrayList<KoalaNeighbor> oldNeighborsContainer = new ArrayList<KoalaNeighbor>();
	
	
	public KoalaRoutingTable(/*String nodeID*/){
//		this.nodeID = nodeID;
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
			if(kn.getLatencyQuality() >= this.localPredecessor.getLatencyQuality())
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
			if(kn.getLatencyQuality() >= this.localSucessor.getLatencyQuality())
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
			if(kn.getLatencyQuality() >= this.globalPredecessor.getLatencyQuality())
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
			if(kn.getLatencyQuality() >= this.globalSucessor.getLatencyQuality())
				this.globalSucessor.update(kn);
		}else{
			oldEntry = this.globalSucessor; 
			this.globalSucessor = kn;
		}
		return oldEntry;
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

	public Set<String> getNeighboursIDs(){
        KoalaNeighbor[] neighs = {this.localPredecessor, this.localSucessor, this.globalPredecessor, this.globalSucessor};
        Set<String> hs = new LinkedHashSet<String>();
        
        for(int i = 0; i < neighs.length; i++)
            if(!NodeUtilities.isDefault(neighs[i]))
            	hs.add(neighs[i].getNodeID());
                
        return hs;
	}
	
	public Set<KoalaNeighbor> getNeighbors(){
        KoalaNeighbor[] neighs = {this.localPredecessor, this.localSucessor, this.globalPredecessor, this.globalSucessor};
        Set<KoalaNeighbor> hs = new LinkedHashSet<KoalaNeighbor>();
        
        for(int i = 0; i < neighs.length; i++)
            if(!NodeUtilities.isDefault(neighs[i]))
            	hs.add(neighs[i]);
        
        for(int i = 0; i < neighborsContainer.size(); i++)
            if(!NodeUtilities.isDefault(neighborsContainer.get(i)))
            	hs.add(neighborsContainer.get(i));
                
        return hs;
	}
	
	

	
}
