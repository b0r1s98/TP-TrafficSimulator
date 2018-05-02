package es.ucm.fdi.model.simobject;

import java.util.Map;

import es.ucm.fdi.util.MultiTreeMap;

public class Dirt extends Road {
	
	protected String type;

	public Dirt(String id, int length, int maxVel, Junction start,
			Junction end, String type) {
		super(id, length, maxVel, start, end);
		this.type = type;
	}
	
	public int calculateSpeedBase() {
		return maxSpeed;
	}

	public void advance() {
		MultiTreeMap<Integer, Vehicle> newVehicles = new MultiTreeMap<>((a, b) -> b-a);
		int speedBase = calculateSpeedBase();
		int reductionFactor = 1;
		for (Vehicle v : vehicles.innerValues()) {
			if(v.getFaultyTime() != 0) {
				reductionFactor++;
			}
			if(v.getLocation() < length) {
				if (v.getFaultyTime() == 0) {
					v.setCurrentSpeed(speedBase / reductionFactor);
				}
				v.advance();
			}
			newVehicles.putValue(v.getLocation(), v);
		}
		vehicles = newVehicles;
	}
	
	public void fillReportDetails(Map<String, String> out) {
		out.put("type", type);
		super.fillReportDetails(out);
	}
}
