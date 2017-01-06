package utilities;




import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;


public class DijkstraPlus
{
    public static void computePaths(Vertex source)
    {
        source.minDistance = 0.;
        PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
	    vertexQueue.add(source);
	
	    while (!vertexQueue.isEmpty()) {
	        Vertex u = vertexQueue.poll();
	
            // Visit each edge exiting u
            for (Edge e : u.adjacencies)
            {
                Vertex v = e.target;
                double weight = e.weight;
                double distanceThroughU = u.minDistance + weight;
		        if (distanceThroughU < v.minDistance) {
		            vertexQueue.remove(v);
		
		            v.minDistance = distanceThroughU ;
		            v.previous = u;
		            vertexQueue.add(v);
		        }
            }
        }
    }

    public static List<Vertex> getShortestPathTo(Vertex target)
    {
        List<Vertex> path = new ArrayList<Vertex>();
        for (Vertex vertex = target; vertex != null; vertex = vertex.previous)
            path.add(vertex);

        Collections.reverse(path);
        return path;
    }
    
    public static void resetVertexes(HashMap<String, Vertex> vertexes)
    {
    	for(Vertex v : vertexes.values()){
    		v.minDistance = Double.POSITIVE_INFINITY;
    		v.previous = null;
    	}
    }
    
    
    
    public static class Vertex implements Comparable<Vertex>
    {
        public final String name;
        public ArrayList<Edge> adjacencies;
        public double minDistance = Double.POSITIVE_INFINITY;
        public Vertex previous;
        public Vertex(String argName) { name = argName; }
        public String toString() { return name; }
        public int compareTo(Vertex other)
        {
            return Double.compare(minDistance, other.minDistance);
        }

    }


    public static class Edge
    {
        public final Vertex target;
        public final double weight;
        public Edge(Vertex argTarget, double argWeight)
        { target = argTarget; weight = argWeight; }
        public String toString() { return target + "(" + weight +")" ; }
        
    }

}



