package es.ucm.fdi.model.events;

import java.util.Map;

import es.ucm.fdi.control.RoadMap;
import es.ucm.fdi.model.exceptions.SimulatorException;
import es.ucm.fdi.model.simobject.Dirt;
import es.ucm.fdi.model.simobject.Junction;
import es.ucm.fdi.model.simobject.Road;

public class NewDirtEvent extends NewRoadEvent {

	public NewDirtEvent(int time, String id, int maxSpeed, int length,
			String src, String dest) {
		super(time, id, maxSpeed, length, src, dest);
	}
	
	public void describe(Map<String, String> out) {
		super.describe(out);
		out.put("Type", "New Dirt Road " + id);
	}

	@Override
	public void execute(RoadMap things) {
		if (things.getObject(id) != null) {
			throw new SimulatorException("Ups, dirt road " + id + 
					" already exists at time " + time);
		}

		Junction a = things.getJunction(src);
		Junction b = things.getJunction(dest);
		if (a == null || b == null) {
			throw new SimulatorException("A junction is missing: dirt road " + id 
					+ "=(" + src + "," + dest + ") at time " + time);
		}

		// Hasta aqui es comun
		Road r = new Dirt(id, length, maxSpeed, a, b, "dirt");
		// Esto vuelve a ser comun

		a.addOutgoing(r);
		b.newIncoming(r);
		things.addRoad(r);
	}

	public static class Builder implements Event.Builder {
		
		public boolean canParse(String title, String type){
			return "new_road".equals(title) && "dirt".equals(type);
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
				return new NewDirtEvent(time, id, maxSpeed, length,
						ideJunctionSurc, ideJunctionDest);
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Incorrect arguments for new_road", e);
			}
		}
	}
}
