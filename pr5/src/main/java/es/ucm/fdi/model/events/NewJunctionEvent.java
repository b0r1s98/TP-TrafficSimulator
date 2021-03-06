package es.ucm.fdi.model.events;

import java.util.Map;

import es.ucm.fdi.control.RoadMap;
import es.ucm.fdi.model.exceptions.SimulatorException;
import es.ucm.fdi.model.simobject.Junction;

public class NewJunctionEvent extends Event {

	protected String id;

	public NewJunctionEvent(int time, String id) {
		super(time);
		this.id = id;
	}
	
	public void describe(Map<String, String> out) {
		super.describe(out);
		out.put("Type", "New Junction " + id);
	}

	@Override
	public void execute(RoadMap things) {
		if (things.getObject(id) != null) {
			throw new SimulatorException("Ups, " + id + " already exists");
		}
		things.addJunction(new Junction(id));
	}

	public static class Builder implements Event.Builder {
		
		public boolean canParse(String title, String type){
			return "new_junction".equals(title) && "".equals(type);
		}
		
		public Event parse(Map<String, String> map) {
			try {
				int time = checkNoNegativeIntOptional("time", map);
				
				String id = checkId(map);

				return new NewJunctionEvent(time, id);
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Incorrect arguments for new_junction", e);
			}
		}
	}
}
