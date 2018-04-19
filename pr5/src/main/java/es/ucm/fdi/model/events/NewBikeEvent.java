package es.ucm.fdi.model.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.ucm.fdi.control.RoadMap;
import es.ucm.fdi.model.exceptions.SimulatorException;
import es.ucm.fdi.model.simobject.Bike;
import es.ucm.fdi.model.simobject.Junction;
import es.ucm.fdi.model.simobject.Vehicle;

public class NewBikeEvent extends NewVehicleEvent {

	public NewBikeEvent(int time, String id, int maxSpeed,
			String[] junctions) {
		super(time, id, maxSpeed, junctions);
	}
	
	public void describe(Map<String, String> out) {
		super.describe(out);
		out.put("Type", "New Bike " + id);
	}

	@Override
	public void execute(RoadMap things) {
		if (things.getObject(id) != null)
			throw new SimulatorException("Ups, " + id + " already exists");

		List<Junction> it = new ArrayList<>();
		for (String s : junctions) {
			Junction step = things.getJunction(s);
			if (step == null)
				throw new SimulatorException("A vehicle goes over ghost junctions");
			it.add(step);
		}
		
		//Esto ya no es comun:
		Vehicle v = new Bike(maxSpeed, it, id, "bike");
		//Esto vuelve a ser comun:
		
		things.addVehicle(v);
		things.getJunction(junctions[0]).moveVehicleToNextRoad(v);
	}

	public static class Builder implements Event.Builder {
		
		public boolean canParse(String title, String type){
			return "new_vehicle".equals(title) && "bike".equals(type);
		}

		public Event fill(Map<String, String> map) {
			try {
				String id = checkId(map);

				int time = checkNoNegativeIntOptional("time", map);
				
				int maxSpeed = checkPositiveInt("max_speed", map);

				String[] junctions = checkContains("itinerary", map).split(",");
				if (junctions.length < 2)
					throw new SimulatorException("Missing destination");
				
				//Hasta aqui es comun
				
				return new NewBikeEvent(time, id, maxSpeed, junctions);
			} catch (IllegalArgumentException e) {
				throw e;
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Incorrect arguments for new_vehicle");
			}
		}
	}
}
