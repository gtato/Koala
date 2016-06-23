package koala;

public class RenaterEdge {

	private double distance;
	private double bitrate;
	private double speed;
	
	private double latency;
	private String to;
	
	

	public RenaterEdge(double distance, double bitrate, double speed) {
		super();
		this.distance = distance;
		this.bitrate = bitrate;
		this.speed = speed;
		calculateLatency();
	}
	
	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getBitrate() {
		return bitrate;
	}

	public void setBitrate(double bitrate) {
		this.bitrate = bitrate;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getLatency() {
		return latency; 
	}

	public void calculateLatency(){
		int packetSize = 1562;
		double tranmissionTime = (double) ((packetSize * 8)/bitrate);
		double propagationTime = (double) ((distance * 1000)/speed);
		latency = tranmissionTime  + propagationTime;
//		latency *= 1000;
		latency = Math.round(latency * 100000.0) / 100.0; //in milliseconds
	}
	
	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}
}
