package koala.initializers;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import koala.KoalaNode;
import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.WireGraph;
import peersim.graph.Graph;

public class WireRenater extends WireGraph {

	private static final String PAR_COORDINATES_PROT = "coord_protocol";
	private static final String PAR_K = "k";
	private final int coordPid;
	private final int k;
	
	public WireRenater(String prefix) {
		super(prefix);
		coordPid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PROT);
		k = Configuration.getInt(prefix + "." + PAR_K);
	}

	@Override
	public void wire(Graph g) {
		int centerIndex = -1;
		ArrayList<KoalaNode> gateways = new ArrayList<KoalaNode>();
		ArrayList<Integer> gateway_cords = new ArrayList<Integer>();
		for (int i = Network.size()-1; i >= 0; i--) {
            Node n = (Node) g.getNode(i);
            KoalaNode rd = (KoalaNode)n.getProtocol(coordPid);
            if(rd.isGateway()){
            	gateways.add(rd);
            	gateway_cords.add(i);
            	centerIndex = i;
            }else{
            	g.setEdge(i, centerIndex);
            }
		}
		
		ArrayList<ArrayList< AbstractMap.SimpleEntry<Integer, Double>>> distances = new ArrayList<ArrayList<AbstractMap.SimpleEntry<Integer, Double>>>(gateways.size());  
		for(int i = 0; i < gateways.size(); i++){
			ArrayList<AbstractMap.SimpleEntry<Integer, Double>> dists = new ArrayList<AbstractMap.SimpleEntry<Integer, Double>>();
			for(int j = 0; j < gateways.size(); j++){
				if(i != j)
					dists.add(new AbstractMap.SimpleEntry<Integer, Double>(gateway_cords.get(j), this.distance(gateways.get(i), gateways.get(j))));
			}
			distances.add(dists);
		}
		
		for(int i = 0; i < distances.size(); i++){
			ArrayList<AbstractMap.SimpleEntry<Integer, Double>> dists = distances.get(i);
			Collections.sort(dists, new Comparator<AbstractMap.SimpleEntry<Integer, Double>>() {
			@Override
			public int compare(AbstractMap.SimpleEntry<Integer, Double> o1, AbstractMap.SimpleEntry<Integer, Double> o2) {
				return o1.getValue().compareTo(o2.getValue());
				
			}
			});
			
			for(int j=0; j < dists.size() && j<k; j++)
			{
				g.setEdge(gateway_cords.get(i), dists.get(j).getKey());
				
			}
				
		}
		

	}
	
	
	
	private double distance(KoalaNode first, KoalaNode second) {
        double x1 = first.getX();
        double x2 = second.getX();
        double y1 = first.getY();
        double y2 = second.getY();
        if (x1 == -1 || x2 == -1 || y1 == -1 || y2 == -1)
        // NOTE: in release 1.0 the line above incorrectly contains
        // |-s instead of ||. Use latest CVS version, or fix it by hand.
            throw new RuntimeException(
                    "Found un-initialized coordinate. Use e.g.,InetInitializer class in the config file.");
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }
	

//	public static void main(String[] args){
//		ArrayList<AbstractMap.SimpleEntry<Integer, Double>> dists = new ArrayList<AbstractMap.SimpleEntry<Integer, Double>>();
//		dists.add(new AbstractMap.SimpleEntry<Integer, Double>(3, 5.0));
//		dists.add(new AbstractMap.SimpleEntry<Integer, Double>(5, 9.0));
//		dists.add(new AbstractMap.SimpleEntry<Integer, Double>(1, 2.0));
//		Collections.sort(dists, new Comparator<AbstractMap.SimpleEntry<Integer, Double>>() {
//			@Override
//			public int compare(AbstractMap.SimpleEntry<Integer, Double> o1, AbstractMap.SimpleEntry<Integer, Double> o2) {
//				return o1.getValue().compareTo(o2.getValue());
//				
//			}
//		});
//		
//		for(int i = 0; i < dists.size(); i++){
//			System.out.println(dists.get(i).getValue());
//		}
//	}

}
