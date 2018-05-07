package es.ucm.fdi.model.simobject;

import java.util.Map;

/**
 * 
 * A SimObject called RoundRobin.
 *
 */
public class RoundRobin extends Junction {
	
	protected int minTime;
	protected int maxTime;
	protected String type;

	/**
	 * Class constructor
	 * @param id		name of this junction
	 * @param minTime	minimum time an incoming road can be in green
	 * @param maxTime	maximum time an incoming road can be in green
	 * @param type		string containing "rr"
	 */
	public RoundRobin(String id, int minTime, int maxTime, String type) {
		super(id);
		this.minTime = minTime;
		this.maxTime = maxTime;
		this.type = type;
	}
	
	@Override
	public void newIncoming(Road r) {
		RoundRobin.IncomingRoad ir = new IncomingRoad(r.getId(),maxTime);
		knowIncoming.put(r, ir);
		incoming.add(ir);
		trafficLight = incoming.size()-1;
		incoming.get(trafficLight).trafficLightsGreen = true;
	}
	
	@Override
	public void advance() {
		if (!incoming.isEmpty()) {
			IncomingRoad roadGreen = (IncomingRoad) incoming.get(trafficLight);
			if (!roadGreen.queue.isEmpty()) {
				Vehicle lucky = roadGreen.queue.getFirst();
				lucky.getRoad().removeVehicle(lucky);
				roadGreen.queue.pop();
				roadGreen.used++;
				moveVehicleToNextRoad(lucky);
			}
			
			roadGreen.timeUnitsUsed++;
			
			if(roadGreen.timeUnitsUsed == roadGreen.timeInterval) {
				advanceTrafficLights();
			}
		}
	}
	
	@Override
	protected void advanceTrafficLights(){
		IncomingRoad roadGreen = (IncomingRoad) incoming.get(trafficLight);
		roadGreen.trafficLightsGreen = false;
		
		if(roadGreen.used == roadGreen.timeUnitsUsed) {
			roadGreen.timeInterval = Math.min(roadGreen.timeInterval + 1, maxTime);
		}
		
		if(roadGreen.used == 0){
			roadGreen.timeInterval = Math.max(roadGreen.timeInterval - 1, minTime);
		}
		
		
		roadGreen.timeUnitsUsed = 0;
		
		trafficLight++;
		if (trafficLight == incoming.size()) {
			trafficLight = 0;
		}
		incoming.get(trafficLight).trafficLightsGreen = true;
	}
	
	@Override
	protected void fillReportDetails(Map<String, String> out) {
		super.fillReportDetails(out);
		out.put("type", type);
	}
	
	/**
	 * 
	 * IncomingRoad for RoundRobin
	 *
	 */
	protected class IncomingRoad extends Junction.IncomingRoad {
		
		protected int timeInterval;
		protected int timeUnitsUsed;
		protected int used;
		
		/**
		 * Class constructor
		 * 
		 * @param r			name of the road
		 * @param maxTime	max time an incoming road can be in green
		 */
		public IncomingRoad(String r, int maxTime) {
			super(r);
			this.timeInterval = maxTime;
			this.timeUnitsUsed = -1;
			this.used = 0;
		}
		
		@Override
		protected String trafficLightsReport(){
			String s = super.trafficLightsReport();
			if(trafficLightsGreen) {
				s += ":" + (timeInterval - timeUnitsUsed);
			}
			return s;
		}
	}
}
