package es.ucm.fdi.model.events;

import java.util.Map;

import es.ucm.fdi.control.RoadMap;
import es.ucm.fdi.model.exceptions.SimulatorException;
import es.ucm.fdi.model.simobject.Highway;
import es.ucm.fdi.model.simobject.Junction;
import es.ucm.fdi.model.simobject.Road;

public class NewHighwayEvent extends NewRoadEvent {

	protected int lanes;

	public NewHighwayEvent(int time, String id, int maxSpeed, int length,
			String src, String dest, int lanes) {
		super(time, id, maxSpeed, length, src, dest);
		this.lanes = lanes;
	}
	
	public void describe(Map<String, String> out) {
		super.describe(out);
		out.put("Type", "New Lanes Road " + id);
	}

	@Override
	public void execute(RoadMap things) {
		if (things.getObject(id) != null) {
			throw new SimulatorException("Ups, " + id + " already exists");
		}

		Junction a = things.getJunction(src);
		Junction b = things.getJunction(dest);
		if (a == null || b == null) {
			throw new SimulatorException("A junction is missing: " + id + "=("
					+ src + "," + dest + ")");
		}

		// Hasta aqui es comun
		Road r = new Highway(id, length, maxSpeed, a, b, "lanes", lanes);
		// Esto vuelve a ser comun

		a.addOutgoing(r);
		b.newIncoming(r);
		things.addRoad(r);
	}

	public static class Builder implements Event.Builder {
		
		public boolean canParse(String title, String type){
			return "new_road".equals(title) && "lanes".equals(type);
		}

		public Event parse(Map<String, String> map) {
			try {
				int time = checkNoNegativeIntOptional("time", map);
				
				String id = checkId(map);

				String ideJunctionSurc = checkContains("src",map);

				String ideJunctionDest = checkContains("dest",map);

				int maxSpeed = checkPositiveInt("max_speed", map);
				
				int length = checkPositiveInt("length", map);

				// A partir de aqui cambia
				int lanes = checkPositiveInt("lanes", map);

				return new NewHighwayEvent(time, id, maxSpeed, length,
						ideJunctionSurc, ideJunctionDest, lanes);
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Incorrect arguments for new_road", e);
			}
		}
	}
}
