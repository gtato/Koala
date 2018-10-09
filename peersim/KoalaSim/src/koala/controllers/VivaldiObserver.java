package koala.controllers;

import java.io.PrintStream;
import java.util.ArrayList;

import koala.KoalaNode;
import peersim.core.CommonState;
import peersim.core.Node;
import renater.RenaterGraph;
import topology.controllers.NodeObserver;
import utilities.NodeUtilities;

public class VivaldiObserver  extends NodeObserver{
	
	public static ArrayList<Double> errors = new ArrayList<Double>();
	public static ArrayList<Double> uncertainty = new ArrayList<Double>();
	public static ArrayList<String> out = new ArrayList<String>();
	boolean ended = false;
	
	public VivaldiObserver(String name) {
		super(name);
	}

	
	@Override
	public boolean execute() {
//		super.g = new RenaterGraph(pid,false);
		double sum = 0;
		double sumu = 0;
		for(double d : errors) 
			sum +=d;
		
		for(double d : uncertainty) 
			sumu +=d;
		
		
		out.add((double)sum/errors.size()+" " + (double)sumu/uncertainty.size());
//		ps.println((double)sum/errors.size());
//		errors = new ArrayList<Double>();
//		uncertainty = new ArrayList<Double>();
		errors.clear();
		uncertainty.clear();
		
		
		if(CommonState.getTime() == CommonState.getEndTime()-1 && !ended){
			graphToFile();
			ended = true;
		}
		
		return false;
	}

	@Override
	protected void printGraph(PrintStream ps, int psIndex) {
		if (psIndex != 0)
			return;
		
		for(String d : out) 
			ps.println(d);
	}

	
	@Override
	protected String getOutputFileBase() {
		return super.getOutputFileBase() +  "vivaldi/";
	}

	@Override
	protected String[] getOutputFileNames() {
		return new String[]{"vivaldi"};
	}
}
