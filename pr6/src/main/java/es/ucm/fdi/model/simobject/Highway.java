package es.ucm.fdi.model.simobject;

import java.util.Map;

import es.ucm.fdi.util.MultiTreeMap;

/**
 * 
 * A SimObject called Highway.
 *
 */
public class Highway extends Road {
	
	protected String type;
	protected int numLanes;
	
	/**
	 * Class constructor
	 * 
	 * @param id		name of this road
	 * @param length	length of this road
	 * @param maxVel	maximum speed allowed to vehicles in this road
	 * @param start		the junction at the beginning
	 * @param end		the junction at the end
	 * @param type		string containing "lanes"
	 * @param numLanes	number of lanes in this road
	 */
	public Highway(String id, int length, int maxVel, Junction start,
			Junction end, String type, int numLanes) {
		super(id, length, maxVel, start, end);
		this.type = type;
		this.numLanes = numLanes;
	}

	@Override
	public int calculateSpeedBase() {
		long n = vehicles.sizeOfValues();
		n = (n<1) ? 1 : n;
		int value = (maxSpeed*numLanes / (int) n) + 1;
		if (maxSpeed < value) {
			return maxSpeed;
		}
		return value;
	}
	
	@Override
	public void advance() {
		MultiTreeMap<Integer, Vehicle> newVehicles = new MultiTreeMap<>((a, b) -> b-a);
		int baseSpeed = calculateSpeedBase();
		int reductionFactor = 1;
		int numFaulties = 0;
		for (Vehicle v : vehicles.innerValues()) {
			if(reductionFactor == 1) {
				if(v.getFaultyTime() != 0) {
					numFaulties++;
					if(numFaulties >= numLanes) {
						reductionFactor = 2;
					}
				}
			}
			if(v.getLocation() < length){
				if (v.getFaultyTime() == 0) {
					v.setCurrentSpeed(baseSpeed / reductionFactor);
				}
				v.advance();
			}
			newVehicles.putValue(v.getLocation(), v);
		}
		vehicles = newVehicles;
	}
	
	@Override
	public void fillReportDetails(Map<String, String> out) {
		out.put("type", type);
		super.fillReportDetails(out);
	}
}
