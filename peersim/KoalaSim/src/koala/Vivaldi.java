package koala;

import java.util.ArrayList;

import koala.controllers.VivaldiObserver;
import peersim.core.CommonState;
import topology.TopologyPathNode;
import utilities.NodeUtilities;

public class Vivaldi {

	
	public static void update(KoalaNode kn, TopologyPathNode msgSender, double rtt) {
        
//		if(msgSender.getVivaldiUncertainty() == 1000)
//			System.out.println(msgSender.getSID() + " has still not updated uncertainty");
		
		ArrayList<Double> local_cords = kn.getVivaldiCoordinates();
	    double local_uncertainty = kn.getVivaldiUncertainty();
	    ArrayList<Double> remote_cords = msgSender.getVivaldiCoordinates();
	    double remote_uncertainty = msgSender.getVivaldiUncertainty();
	  
	    double estimate = getEuclideanDistance(local_cords, remote_cords);
//	    System.out.println("estimate: " + estimate);
//	    double err = rtt - estimate;
	    double err = estimate - rtt;
	    VivaldiObserver.errors.add(Math.abs(err));
//	  		  System.out.println("vivaldi error: " + err);
	  // console.log('error: %s', err)
	    double rel_error = (double)Math.abs(err)/rtt;
	  //		  double rel_error = (double)err/rtt;
	    double balance_uncertainty = (double) local_uncertainty / (local_uncertainty + remote_uncertainty);
	
	
	    kn.setVivaldiUncertainty(rel_error * NodeUtilities.VIV_UNCERTAINTY_FACTOR * balance_uncertainty
		        + local_uncertainty * (1 - NodeUtilities.VIV_UNCERTAINTY_FACTOR * balance_uncertainty));
	  
	    VivaldiObserver.uncertainty.add(kn.getVivaldiUncertainty());
	
	  //		  console.log('rtt: %s, error: %s, uncertainty: %s', rtt,err,myvivaldi.dynamic.uncertainty)
	
	    double sensitivity = NodeUtilities.VIV_CORRECTION_FACTOR * balance_uncertainty;
	    ArrayList<Double> force_vect = getForceVector(local_cords, remote_cords, err);
	  
	    for(int i = 0; i < NodeUtilities.VIV_DIMENSIONS; i++) 
		    kn.getVivaldiCoordinates().set(i, kn.getVivaldiCoordinates().get(i) + force_vect.get(i) * sensitivity);
//		System.out.println("Node: " + kn.getSID() + " updated coordinates");  
	    
	}
	
	public static ArrayList<Double> getForceVector(ArrayList<Double> cord1, ArrayList<Double> cord2, double err){
		ArrayList<Double> force_vect = new ArrayList<Double>();
		ArrayList<Double> zero_vect = new ArrayList<Double>();
		  boolean equal = true;
		  for(int i = 0; i < cord1.size(); i++){
		    force_vect.add(i, cord2.get(i) - cord1.get(i)); //compute difference
		    zero_vect.add(0.0);
		    if(force_vect.get(i) != 0)
		      equal = false;
		  }
		
		  while(equal){ //generate random vector 
		    for(int i = 0; i < force_vect.size(); i++) {
		      force_vect.set(i, CommonState.r.nextDouble());
		      if (force_vect.get(i) != 0) equal = false;
		    }
		  }
		
		  double length = getEuclideanDistance(zero_vect, force_vect);
		  for(int i = 0; i < force_vect.size(); i++){
		      force_vect.set(i, (double)force_vect.get(i)/length); //normalize
		      force_vect.set(i, (double)force_vect.get(i) * err); //apply error
		  }
		  return force_vect;
	}
	

	public static double getEuclideanDistance(ArrayList<Double> cord1, ArrayList<Double> cord2) {
		double sum = 0;
		for(int i = 0; i < cord1.size(); i++)
		    sum += Math.pow(cord2.get(i) - cord1.get(i), 2);
//		 System.out.println("sum: " + sum);
		 
		return Math.sqrt(sum);
	}
	
}
