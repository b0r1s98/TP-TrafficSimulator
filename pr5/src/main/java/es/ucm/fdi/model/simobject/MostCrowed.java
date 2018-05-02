package es.ucm.fdi.model.simobject;

import java.util.Map;

public class MostCrowed extends Junction {
	
	protected String type;

	public MostCrowed(String id, String type) {
		super(id);
		this.type = type;
	}
	
	public void newIncoming(Road r) {
		IncomingRoad ir = new IncomingRoad(r.getId());
		knowIncoming.put(r, ir);
		incoming.add(ir);
		advanceTrafficLights();
	}

	public void advance() {
		if (!incoming.isEmpty()) {
			IncomingRoad roadGreen = (IncomingRoad) incoming.get(trafficLight);
			if (!roadGreen.cola.isEmpty()) {
				Vehicle lucky = roadGreen.cola.getFirst();
				lucky.getRoad().removeVehicle(lucky);
				roadGreen.cola.pop();
				moveVehicleToNextRoad(lucky);
			}
			
			roadGreen.timeUnitsUsed++;
			
			if (roadGreen.timeUnitsUsed == roadGreen.timeInterval) {
				advanceTrafficLights();
			}
		}
	}
	
	protected void advanceTrafficLights(){
		IncomingRoad roadGreen = (IncomingRoad) incoming.get(trafficLight);
		roadGreen.semaforoVerde = false;
		
		IncomingRoad moreVehicles = roadGreen;
		int max = trafficLight;
		for(int i = 0; i<incoming.size(); i++) {
			IncomingRoad r = (IncomingRoad) incoming.get(i);
			if((r.cola.size() > moreVehicles.cola.size() && i!=trafficLight) || max == trafficLight){
				moreVehicles = r;
				max = i;
			}
		}
		
		trafficLight = max;
		moreVehicles.semaforoVerde = true;
		moreVehicles.timeInterval = Math.max(moreVehicles.cola.size() / 2, 1);
		moreVehicles.timeUnitsUsed = 0;
	}
	
	protected void fillReportDetails(Map<String, String> out) {
		super.fillReportDetails(out);
		out.put("type", type);
	}

	protected class IncomingRoad extends Junction.IncomingRoad {

		protected int timeInterval;
		protected int timeUnitsUsed;

		public IncomingRoad(String r) {
			super(r);
			this.timeInterval = 0;
			this.timeUnitsUsed = 0;
		}

		protected String semaforoReport() {
			StringBuilder r = new StringBuilder();
			r.append(super.semaforoReport());
			if (semaforoVerde) {
				r.append(":" + (timeInterval - timeUnitsUsed));
			}
			return r.toString();
		}
	}
}
