package koala;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import peersim.config.Configuration;

public class KoalaRoutingTable {
	public static final String DEFAULTID = "xxx";
	private String nodeID;
	private KoalaNeighbor localPredecessor;
	private KoalaNeighbor localSucessor;
	private KoalaNeighbor globalPredecessor;
	private KoalaNeighbor globalSucessor;
	private ArrayList<KoalaNeighbor> neighbors = new ArrayList<KoalaNeighbor>();
	
	public KoalaRoutingTable(String nodeID){
		this.nodeID = nodeID;
		KoalaNeighbor defaultNeighbor = new KoalaNeighbor(DEFAULTID);
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
	
	//true if added, false if just updated
	public boolean addNeighbor(KoalaNeighbor neighbor) {
		
		KoalaNeighbor each = null;
		boolean updated= false;
		for(int i = 0; i < neighbors.size(); i++)
		{
			each = neighbors.get(i);
			if (each.equals(neighbor)){
				each.update(neighbor);
				updated = true;
				break;
			}
			
		}
		
		if(!updated)
			neighbors.add(neighbor);
		
		return !updated;
		
	}
	
	
	public int tryAddNeighbour(KoalaNeighbor n){
        ArrayList<KoalaNeighbor> oldNeighbors = new ArrayList<>();
		boolean local = this.isLocal(n.getNodeID());
        int addedS, addedP;
        addedS = addedP = -1;
        KoalaNeighbor oldS, oldP;
        oldS = oldP = null;
        
        if(this.isSuccessor(n.getNodeID())){
            oldS = local ? setLocalSucessor(n) : setGlobalSucessor(n);
            addedS = oldS != null ? 1 : 0;
            if (!isDefault(oldS)) oldNeighbors.add(oldS);   
        }
        if (this.isPredecessor(n.getNodeID())){
            oldP = local ? setLocalPredecessor(n) : setGlobalPredecessor(n);
            addedP = oldP != null ? 1 : 0;
            if (!isDefault(oldP)) oldNeighbors.add(oldP);
        }
        int ret = Math.max(addedS, addedP);
         
        if( ret == 1)
            ret++;

        if( ret ==-1 && (canBePredecessor(n.getNodeID()) || canBeSuccessor(n.getNodeID())))
            ret = 1;

        
        // 2: added, 1: potential neighbor, 0: updated , -1:not neighbor
        return ret; // should return oldNeighbors as well
	}
	
		
	
	
	/*
	 * These are some simple utility functions
	 */
	
	
	public Set<String> getNeighboursIDs(){
        KoalaNeighbor[] neighs = {this.localPredecessor, this.localSucessor, this.globalPredecessor, this.globalSucessor};
        Set<String> hs = new HashSet<String>();
        
        for(int i = 0; i < neighs.length; i++)
            if(!isDefault(neighs[i]))
            	hs.add(neighs[i].getNodeID());
                
        return hs;
	}
	
	private boolean isNeighbour(String nodeID){
        if (isSuccessor(nodeID))
            return true;
        if (isPredecessor(nodeID))
            return true;
        return false;
	}
	
	private boolean isSuccessor(String nodeID){
        boolean local = this.isLocal(nodeID);
        KoalaNeighbor successor = local ? getLocalSucessor() : getGlobalSucessor();
        KoalaNeighbor predecessor = local ? getLocalPredecessor() : getGlobalPredecessor();
        if (canBeSuccessor(nodeID)){
        	if( isDefault(successor)|| local || compareIDs(nodeID, successor.getNodeID(), false) != 0)
        		return true;
        	if(successor.getNodeID().equals(predecessor.getNodeID()))
        		return true;
        	if(!nodeID.equals(predecessor.getNodeID())  &&  distance(this.nodeID, nodeID, true) <= distance(this.nodeID, successor.getNodeID(), true))
        		return true;
        }
        return false;
	}
	 
	private boolean isPredecessor(String nodeID){
		boolean local = this.isLocal(nodeID);
        KoalaNeighbor successor = local ? getLocalSucessor() : getGlobalSucessor();
        KoalaNeighbor predecessor = local ? getLocalPredecessor() : getGlobalPredecessor();
        if (canBePredecessor(nodeID)){
        	if(isDefault(predecessor) || local || compareIDs(nodeID, predecessor.getNodeID(), false) != 0)
        		return true;
        	if(successor.getNodeID().equals(predecessor.getNodeID()))
        		return true;
        	if(!nodeID.equals(successor.getNodeID())  &&  distance(this.nodeID, nodeID, true) <= distance(this.nodeID, predecessor.getNodeID(), true))
        		return true;
        }
        return false;
	}
	
	
	private boolean canBeNeighbour(String nodeID){
        if (canBeSuccessor(nodeID))
            return true;
        if (canBePredecessor(nodeID))
            return true;
        return false;
	}
	
	private boolean canBeSuccessor(String nodeID){
	        boolean local = this.isLocal(nodeID);
	        KoalaNeighbor successor = local ? getLocalSucessor() : getGlobalSucessor();
	        if (isDefault(successor))
	            return true;
	        else{
	            if((compareIDs(nodeID, successor.getNodeID(), local) <= 0 && compareIDs(successor.getNodeID(), this.nodeID, local) < 0) || 
	               (compareIDs(nodeID, successor.getNodeID(), local) >= 0 && compareIDs(successor.getNodeID(), this.nodeID, local) < 0 && compareIDs(nodeID, this.nodeID, local) > 0) || 
	               (compareIDs(nodeID, successor.getNodeID(), local) <= 0 && compareIDs(nodeID, this.nodeID, local) > 0) )
	                return true;
	        }
	        return false;
	}
	 
	private boolean canBePredecessor(String nodeID){
        boolean local = this.isLocal(nodeID);
        KoalaNeighbor predecessor = local ? getLocalPredecessor() : getGlobalPredecessor();
        if (isDefault(predecessor))
            return true;
        else{
            if((compareIDs(nodeID, predecessor.getNodeID(), local) >= 0 && compareIDs(predecessor.getNodeID(), this.nodeID, local) > 0) || 
               (compareIDs(nodeID, predecessor.getNodeID(), local) <= 0 && compareIDs(predecessor.getNodeID(), this.nodeID, local) > 0 && compareIDs(nodeID, this.nodeID, local) < 0) || 
               (compareIDs(nodeID, predecessor.getNodeID(), local) >= 0 && compareIDs(nodeID, this.nodeID, local) < 0) )
                return true;
        }
        return false;
	}
    
	private int compare(String id1, String id2){
    	int compareGlobal = compareIDs(id1,id2, false);
    	if (compareGlobal != 0)
    		return compareGlobal;
    	return compareIDs(id1,id2, true);
    }
	
    private int compareIDs(String id1, String id2, boolean local){
    	if(local)
    		return new Integer(getNodeID(id1)).compareTo(new Integer(getNodeID(id2)));
    	return new Integer(getDCID(id1)).compareTo(new Integer(getDCID(id2)));
    }

    	
	private boolean isLocal(String id){
		return getDCID(this.nodeID) == getDCID(id); 
	}
	
	private int getDCID(String id){
		return Integer.parseInt(id.split("-")[0]);
	}
	
	private int getNodeID(String id){
		return Integer.parseInt(id.split("-")[1]);
	}
	
	private int distance(String srcID, String targetID){
		return distance(srcID, targetID, false);
	}
	
	private int distance(String srcID, String targetID, boolean forceLocal){
        boolean local = false;
        if (getDCID(srcID) == getDCID(targetID) || forceLocal)
            local = true;

        int src_id = local ? getNodeID(srcID) : getDCID(srcID); 
        int target_id = local ? getNodeID(targetID) : getDCID(targetID);
        int a = src_id;
        int b = target_id;
        if (src_id > target_id){
            a = target_id; 
            b = src_id;
        }
        
        int size = local ? Configuration.getInt("NR_NODE_PER_DC") : Configuration.getInt("NR_DC");
        int d1 = b - a;
        int d2 = (size - b + a) % size;

        return Math.min(d1, d2);
	}
	
	private boolean isDefault(KoalaNeighbor n){
		return n == null || n.getNodeID() == DEFAULTID;
	}
}
