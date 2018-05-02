package es.ucm.fdi.model.simobject;

import java.util.List;
import java.util.Map;

public class Bike extends Vehicle {

	protected String type;

	public Bike(int speedMax, List<Junction> itinerary, String id, String type) {
		super(speedMax, itinerary, id);
		this.type = type;
	}

	public void setFaultyTime(int n) {
		if(speed > speedMax/2) {
			faultyTime += n;
			speed = 0;
		}
	}
	
	public void fillReportDetails(Map<String, String> out) {
		out.put("type", type);
		super.fillReportDetails(out);
	}

}
