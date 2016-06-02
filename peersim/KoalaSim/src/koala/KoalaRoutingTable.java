package koala;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import koala.utility.KoalaNodeUtilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import peersim.config.Configuration;

public class KoalaRoutingTable {
	
//	private String nodeID;
	private KoalaNeighbor localPredecessor;
	private KoalaNeighbor localSucessor;
	private KoalaNeighbor globalPredecessor;
	private KoalaNeighbor globalSucessor;
	
	/*these two are supposed to be used only when the object is transmitted*/
	private ArrayList<KoalaNeighbor> neighbors = new ArrayList<KoalaNeighbor>();
	private ArrayList<KoalaNeighbor> oldNeighbors = new ArrayList<KoalaNeighbor>();
	
	
	public KoalaRoutingTable(/*String nodeID*/){
//		this.nodeID = nodeID;
		KoalaNeighbor defaultNeighbor = new KoalaNeighbor(KoalaNodeUtilities.DEFAULTID);
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
//	
//	//true if added, false if just updated
//	public boolean addNeighbor(KoalaNeighbor neighbor) {
//		
//		KoalaNeighbor each = null;
//		boolean updated= false;
//		for(int i = 0; i < neighbors.size(); i++)
//		{
//			each = neighbors.get(i);
//			if (each.equals(neighbor)){
//				each.update(neighbor);
//				updated = true;
//				break;
//			}
//			
//		}
//		
//		if(!updated)
//			neighbors.add(neighbor);
//		
//		return !updated;
//		
//	}
//	
	
	
	
		
	
	
	/*
	 * These are some simple utility functions
	 */
	
	
	public ArrayList<KoalaNeighbor> getOldNeighbors() {
		return oldNeighbors;
	}


	public ArrayList<KoalaNeighbor> getNeighborsContainer() {
		return neighbors;
	}

	public void setNeighborsContainer(ArrayList<KoalaNeighbor> neighbors) {
		this.neighbors = neighbors;
	}

	public void setOldNeighbors(ArrayList<KoalaNeighbor> oldNeighbors) {
		this.oldNeighbors = oldNeighbors;
	}

	public Set<String> getNeighboursIDs(){
        KoalaNeighbor[] neighs = {this.localPredecessor, this.localSucessor, this.globalPredecessor, this.globalSucessor};
        Set<String> hs = new HashSet<String>();
        
        for(int i = 0; i < neighs.length; i++)
            if(!KoalaNodeUtilities.isDefault(neighs[i]))
            	hs.add(neighs[i].getNodeID());
                
        return hs;
	}
	
	public Set<KoalaNeighbor> getNeighbors(){
        KoalaNeighbor[] neighs = {this.localPredecessor, this.localSucessor, this.globalPredecessor, this.globalSucessor};
        Set<KoalaNeighbor> hs = new HashSet<KoalaNeighbor>();
        
        for(int i = 0; i < neighs.length; i++)
            if(!KoalaNodeUtilities.isDefault(neighs[i]))
            	hs.add(neighs[i]);
        
        for(int i = 0; i < neighbors.size(); i++)
            if(!KoalaNodeUtilities.isDefault(neighbors.get(i)))
            	hs.add(neighbors.get(i));
                
        return hs;
	}
	
	

	
}
