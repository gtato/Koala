package renater;

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
	
	
	public RenaterEdge(double latency) {
		super();
		this.latency = latency;
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

//	public double calculateLatency_future(){
//		int packetSize = 1562;
//		double tranmissionTime = (double) ((packetSize * 8)/bitrate);
//		double propagationTime = (double) ((distance * 1000)/speed);
//		latency = tranmissionTime  + propagationTime;
//
//		//*1000 to convert in milliseconds and *10 is just to look more realistic
//		latency = (double)Math.round(latency *  100.0 *1000 *10) / 100.0; //in milliseconds
//		return latency; 
//	}
	
	public double calculateLatency(){
		latency = distance;
		return latency; 
	}
	
	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}
}
