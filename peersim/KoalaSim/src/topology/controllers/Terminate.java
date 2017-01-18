package topology.controllers;

import peersim.core.Control;
import utilities.KoaLite;
import utilities.PhysicalDataProvider;

public class Terminate implements Control{

	public Terminate(String prefix){
		
	}
	
	@Override
	public boolean execute() {
		KoaLite.close();
		
		PhysicalDataProvider.SimTime = System.currentTimeMillis() - PhysicalDataProvider.SimTime;
		System.out.println("Simulation lasted " + PhysicalDataProvider.SimTime + " ms");
		return false;
	}

}
