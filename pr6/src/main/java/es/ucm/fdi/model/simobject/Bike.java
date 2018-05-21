package es.ucm.fdi.model.simobject;

import java.util.List;
import java.util.Map;

/**
 * 
 * A type of vehicle that gets faulty less times.
 *
 */
public class Bike extends Vehicle {

	protected String type;

	/**
	 * Class constructor
	 * 
	 * @param speedMax	maximum speed
	 * @param itinerary	array of the junctions the vehicle must pass through
	 * @param id		name of the vehicle
	 * @param type		string containing "bike"
	 */
	public Bike(int speedMax, List<Junction> itinerary, String id, String type) {
		super(speedMax, itinerary, id);
		this.type = type;
	}

	@Override
	public void setFaultyTime(int n) {
		if(speed > speedMax/2) {
			faultyTime += n;
			speed = 0;
		}
	}
	
	@Override
	public void fillReportDetails(Map<String, String> out) {
		out.put("type", type);
		super.fillReportDetails(out);
	}

}
