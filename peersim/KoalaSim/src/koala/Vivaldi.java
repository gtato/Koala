package koala;

import java.util.ArrayList;

import topology.TopologyPathNode;
import utilities.NodeUtilities;

public class Vivaldi {

	
	public static void update(KoalaNode kn, TopologyPathNode msgSender, double rtt) {
		ArrayList<Double> local_cords = kn.vivaldiCoordinates;
		  double local_uncertainty = kn.vivaldiUncertainty;
		  ArrayList<Double> remote_cords = msgSender.getVivaldiCoordinates();
		  double remote_uncertainty = msgSender.getVivaldiUncertainty();
		  
		  double estimate = getEuclideanDistance(local_cords, remote_cords);
		  double err = estimate - rtt;
		  // console.log('error: %s', err)
		  double rel_error = (double)Math.abs(err)/rtt;
		  double balance_uncertainty = (double) local_uncertainty / (local_uncertainty + remote_uncertainty);


		  kn.vivaldiUncertainty = rel_error * NodeUtilities.VIV_UNCERTAINTY_FACTOR * balance_uncertainty
		        + local_uncertainty * (1 - NodeUtilities.VIV_UNCERTAINTY_FACTOR * balance_uncertainty);

//		  console.log('rtt: %s, error: %s, uncertainty: %s', rtt,err,myvivaldi.dynamic.uncertainty)

		  double sensitivity = NodeUtilities.VIV_CORRECTION_FACTOR * balance_uncertainty;
		  ArrayList<Double> force_vect = getForceVector(local_cords, remote_cords, err);
		  
		  for(int i = 0; i < NodeUtilities.VIV_DIMENSIONS; i++) 
			  kn.vivaldiCoordinates.set(i, kn.vivaldiCoordinates.get(i) + force_vect.get(i) * sensitivity);
		  
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
		      force_vect.set(i,  Math.random()*2-1);
		      if (force_vect.get(i) != 0) equal = false;
		    }
		  }
		
		  double length = getEuclideanDistance(zero_vect, force_vect);
		  for(int i = 0; i < force_vect.size(); i++){
		      force_vect.set(i, (double)force_vect.get(i)/length); //normalize
		      force_vect.set(i, force_vect.get(i) * err); //apply error
		  }
		  return force_vect;
	}
	

	public static double getEuclideanDistance(ArrayList<Double> cord1, ArrayList<Double> cord2) {
		double sum = 0;
		for(int i = 0; i < cord1.size(); i++)
		    sum += Math.pow(cord2.get(i) - cord1.get(i), 2);
		return Math.sqrt(sum);
	}
	
}
