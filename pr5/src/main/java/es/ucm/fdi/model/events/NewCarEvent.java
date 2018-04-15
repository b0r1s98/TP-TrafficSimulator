package es.ucm.fdi.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.ucm.fdi.controller.RoadMap;
import es.ucm.fdi.exceptions.SimulatorException;
import es.ucm.fdi.simobject.Car;
import es.ucm.fdi.simobject.Junction;
import es.ucm.fdi.simobject.Vehicle;

public class NewCarEvent extends NewVehicleEvent {

	protected int resistanceKm;
	protected double faultProbability;
	protected int maxFaultDuration;
	protected long seed;

	public NewCarEvent(int time, String id, int maxSpeed,
			String[] junctions, int resistanceKm, double faultProbability,
			int maxFaultDuration, long seed) {
		super(time, id, maxSpeed, junctions);
		this.resistanceKm = resistanceKm;
		this.faultProbability = faultProbability;
		this.maxFaultDuration = maxFaultDuration;
		this.seed = seed;
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
		Vehicle v = new Car(maxSpeed, it, id, "car", resistanceKm, faultProbability, maxFaultDuration, seed);
		//Esto vuelve a ser comun:
		
		things.addVehicle(v);
		things.getJunction(junctions[0]).moveVehicleToNextRoad(v);
	}

	public static class Builder implements Event.Builder {
		
		public boolean canParse(String title, String type){
			return "new_vehicle".equals(title) && "car".equals(type);
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
				int resistanceKm = checkPositiveInt("resistance", map);
				
				double faultProbability = Double.parseDouble(map.get("fault_probability"));
				if (faultProbability < 0)
					throw new IllegalArgumentException("Negative fault_probability");
				if (faultProbability > 1)
					throw new IllegalArgumentException("Imposible fault_probability");
				
				int maxFaultDuration = checkPositiveInt("max_fault_duration", map);
				
				long seed = System.currentTimeMillis();
				if(map.containsKey("seed"))
					seed = Long.parseLong(map.get("seed"));
				if (seed <= 0)
					throw new IllegalArgumentException("No positive seed");
				

				return new NewCarEvent(time, id, maxSpeed, junctions, resistanceKm, faultProbability, maxFaultDuration, seed);
			} catch (IllegalArgumentException e) {
				throw e;
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Incorrect arguments for new_vehicle");
			}
		}
	}
}
