package spaasclient;

import java.util.ArrayList;
import java.util.List;

public class ShortestPath {
	private ArrayList<String> path;
	private double latency;
	public ShortestPath(ArrayList<String> path, double latency) {
		super();
		this.path = path;
		this.latency = latency;
	}
	public ArrayList<String> getPath() {
		return path;
	}
	public void setPath(ArrayList<String> path) {
		this.path = path;
	}
	public double getLatency() {
		return latency;
	}
	public void setLatency(double latency) {
		this.latency = latency;
	}
	
}
