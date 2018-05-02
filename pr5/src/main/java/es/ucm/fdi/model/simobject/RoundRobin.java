package es.ucm.fdi.model.simobject;

import java.util.Map;

public class RoundRobin extends Junction {
	
	protected int minTime;
	protected int maxTime;
	protected String type;

	public RoundRobin(String id, int minTime, int maxTime, String type) {
		super(id);
		this.minTime = minTime;
		this.maxTime = maxTime;
		this.type = type;
	}
	
	public void newIncoming(Road r) {
		RoundRobin.IncomingRoad ir = new IncomingRoad(r.getId(),maxTime);
		knowIncoming.put(r, ir);
		incoming.add(ir);
		trafficLight = incoming.size()-1;
		incoming.get(trafficLight).semaforoVerde = true;
	}
	
	public void advance() {
		if (!incoming.isEmpty()) {
			IncomingRoad roadGreen = (IncomingRoad) incoming.get(trafficLight);
			if (!roadGreen.cola.isEmpty()) {
				Vehicle lucky = roadGreen.cola.getFirst();
				lucky.getRoad().removeVehicle(lucky);
				roadGreen.cola.pop();
				roadGreen.used++;
				moveVehicleToNextRoad(lucky);
			}
			
			roadGreen.timeUnitsUsed++;
			
			if(roadGreen.timeUnitsUsed == roadGreen.timeInterval) {
				advanceTrafficLights();
			}
		}
	}
	
	protected void advanceTrafficLights(){
		IncomingRoad roadGreen = (IncomingRoad) incoming.get(trafficLight);
		roadGreen.semaforoVerde = false;
		
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
		incoming.get(trafficLight).semaforoVerde = true;
	}
	
	protected void fillReportDetails(Map<String, String> out) {
		super.fillReportDetails(out);
		out.put("type", type);
	}
	
	protected class IncomingRoad extends Junction.IncomingRoad {
		
		protected int timeInterval;
		protected int timeUnitsUsed;
		protected int used;
		
		public IncomingRoad(String r, int maxTime) {
			super(r);
			this.timeInterval = maxTime;
			this.timeUnitsUsed = -1;
			this.used = 0;
		}
		
		protected String semaforoReport(){
			StringBuilder r = new StringBuilder();
			r.append(super.semaforoReport());
			if(semaforoVerde) {
				r.append(":" + (timeInterval - timeUnitsUsed));
			}
			return r.toString();
		}
	}
}
