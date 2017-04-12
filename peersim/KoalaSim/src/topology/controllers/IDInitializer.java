package topology.controllers;

import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Control;
import utilities.NodeUtilities;

public class IDInitializer implements Control{

	public IDInitializer(String prefix){
		NodeUtilities.RID = Configuration.getPid(prefix + ".rid", -1);
		NodeUtilities.KID = Configuration.getPid(prefix + ".kid", -1);
		NodeUtilities.FKID = Configuration.getPid(prefix + ".fkid", -1);
		NodeUtilities.CID = Configuration.getPid(prefix + ".cid", -1);
		
		NodeUtilities.RPID = Configuration.getPid(prefix + ".rpid", -1);
		NodeUtilities.KPID = Configuration.getPid(prefix + ".kpid", -1);
		NodeUtilities.FKPID = Configuration.getPid(prefix + ".fkpid", -1);
		NodeUtilities.CPID = Configuration.getPid(prefix + ".cpid", -1);
    
		NodeUtilities.TRID = Configuration.getPid(prefix + ".trid", -1);
	}
	
	
	@Override
	public boolean execute() {
		return false;
	}

}
