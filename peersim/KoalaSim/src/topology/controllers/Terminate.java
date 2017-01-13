package topology.controllers;

import peersim.core.Control;
import utilities.KoaLite;

public class Terminate implements Control{

	public Terminate(String prefix){
		
	}
	
	@Override
	public boolean execute() {
		KoaLite.close();
		return false;
	}

}
