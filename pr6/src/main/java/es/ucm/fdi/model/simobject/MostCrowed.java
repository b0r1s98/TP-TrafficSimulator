package es.ucm.fdi.model.simobject;

import java.util.Map;

/**
 * 
 * A type of junction that manages traffic lights giving 
 * priority to those incoming roads with more vehicles waiting.
 *
 */
public class MostCrowed extends Junction {
	
	protected String type;

	/**
	 * Class constructor
	 * 
	 * @param id	name of this junction
	 * @param type	string containing "mc"
	 */
	public MostCrowed(String id, String type) {
		super(id);
		this.type = type;
	}
	
	@Override
	public void newIncoming(Road r) {
		IncomingRoad ir = new IncomingRoad(r.getId());
		knowIncoming.put(r, ir);
		incoming.add(ir);
		advanceTrafficLights();
	}

	@Override
	public void advance() {
		if (!incoming.isEmpty()) {
			IncomingRoad roadGreen = (IncomingRoad) incoming.get(trafficLight);
			if (!roadGreen.queue.isEmpty()) {
				Vehicle lucky = roadGreen.queue.getFirst();
				lucky.getRoad().removeVehicle(lucky);
				roadGreen.queue.pop();
				moveVehicleToNextRoad(lucky);
			}
			
			roadGreen.timeUnitsUsed++;
			
			if (roadGreen.timeUnitsUsed == roadGreen.timeInterval) {
				advanceTrafficLights();
			}
		}
	}
	
	@Override
	protected void advanceTrafficLights(){
		IncomingRoad roadGreen = (IncomingRoad) incoming.get(trafficLight);
		roadGreen.trafficLightsGreen = false;
		
		IncomingRoad moreVehicles = roadGreen;
		int max = trafficLight;
		for(int i = 0; i<incoming.size(); i++) {
			IncomingRoad r = (IncomingRoad) incoming.get(i);
			if((r.queue.size() > moreVehicles.queue.size() && i!=trafficLight) || max == trafficLight){
				moreVehicles = r;
				max = i;
			}
		}
		
		trafficLight = max;
		moreVehicles.trafficLightsGreen = true;
		moreVehicles.timeInterval = Math.max(moreVehicles.queue.size() / 2, 1);
		moreVehicles.timeUnitsUsed = 0;
	}
	
	@Override
	protected void fillReportDetails(Map<String, String> out) {
		super.fillReportDetails(out);
		out.put("type", type);
	}

	/**
	 * 
	 * IncomingRoad for MostCrowed
	 *
	 */
	protected class IncomingRoad extends Junction.IncomingRoad {

		protected int timeInterval;
		protected int timeUnitsUsed;

		/**
		 * Class constructor
		 * 
		 * @param r name of the road
		 */
		public IncomingRoad(String r) {
			super(r);
			this.timeInterval = 0;
			this.timeUnitsUsed = 0;
		}

		@Override
		protected String trafficLightsReport() {
			String s = super.trafficLightsReport();
			if (trafficLightsGreen) {
				s += ":" + (timeInterval - timeUnitsUsed);
			}
			return s;
		}
	}
}
