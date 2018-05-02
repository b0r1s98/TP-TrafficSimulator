package es.ucm.fdi.model.events;

import java.util.Map;

import es.ucm.fdi.control.RoadMap;
import es.ucm.fdi.model.exceptions.SimulatorException;
import es.ucm.fdi.model.simobject.Vehicle;

public class MakeVehicleFaultyEvent extends Event {
	
	protected int faultyTime;
	protected String vehicles;

	public MakeVehicleFaultyEvent(int time, int faultyTime, String vehicles) {
		super(time);
		this.faultyTime = faultyTime;
		this.vehicles = vehicles;
	}
	
	public void describe(Map<String, String> out) {
		super.describe(out);
		out.put("Type", "Break Vehicles [" + vehicles + "]");
	}

	@Override
	public void execute(RoadMap things) {
		String[] arrayVehicles = vehicles.split(",");

		for (String vehicle : arrayVehicles) {
			Vehicle unlucky = things.getVehicle(vehicle);
			if (unlucky == null) {
				throw new SimulatorException("Hitting the air");
			}
			unlucky.setFaultyTime(faultyTime);
		}
	}

	public static class Builder implements Event.Builder {
		
		public boolean canParse(String title, String type){
			return "make_vehicle_faulty".equals(title) && "".equals(type);
		}

		public Event parse(Map<String, String> map) {
			try {
				int time = checkNoNegativeIntOptional("time", map);
				
				int faultyTime = checkPositiveInt("duration", map);

				String idsList = checkContains("vehicles", map);

				return new MakeVehicleFaultyEvent(time, faultyTime, idsList);
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Incorrect arguments for make_vehicle_faulty", e);
			}
		}
	}
}
