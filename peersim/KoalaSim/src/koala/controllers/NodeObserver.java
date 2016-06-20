package koala.controllers;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import koala.KoalaNeighbor;
import koala.KoalaNode;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.reports.GraphObserver;
import peersim.util.FileNameGenerator;

public abstract class NodeObserver extends GraphObserver {

	private static final String PAR_PROT = "protocol";
    private static final String PAR_FILENAME_BASE = "file_base";

    protected final int pid;
    private final String graph_filename;
    private final FileNameGenerator fng;
    protected String plotScript;
    boolean dumpToStd = false;
    
	protected NodeObserver(String prefix) {
		super(prefix);
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
		graph_filename = Configuration.getString(prefix + "."+ PAR_FILENAME_BASE, "graph_dump");
        if(graph_filename.equals("graph_dump"))
        	dumpToStd = true;
		fng = new FileNameGenerator(graph_filename, ".dat");
	}

	protected void graphToFile() {
		try {
            
            String fname = fng.nextCounterName();
            FileOutputStream fos = new FileOutputStream(fname);
            PrintStream ps = dumpToStd ? System.out : new PrintStream(fos);

            printGraph(ps);
            
            fos.close();
            ps.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
	
	protected void plotIt(){
		try {
			Process p = new ProcessBuilder("gnuplot", "-persistent", plotScript).start();
//			BufferedReader reader = new BufferedReader (new InputStreamReader(p.getErrorStream()));
//			String line;
//			while ((line = reader.readLine ()) != null) {
//				System.out.println ("Stdout: " + line);
//			}

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected KoalaNode getNodeFromID(String id)
	{
		for (int i = 0; i < g.size(); i++) 
		{
			KoalaNode current = (KoalaNode) ((Node)g.getNode(i)).getProtocol(pid);
			if(current.getID().equals(id))
				return current;
		}
		return null;
	}

	protected abstract void printGraph(PrintStream ps);
}
