package es.ucm.fdi.model.events;

import java.util.Map;

import es.ucm.fdi.control.RoadMap;
import es.ucm.fdi.model.exceptions.SimulatorException;
import es.ucm.fdi.model.simobject.MostCrowed;

public class NewMostCrowedEvent extends NewJunctionEvent {
	
	public NewMostCrowedEvent(int time, String id) {
		super(time, id);
	}
	
	public void describe(Map<String, String> out) {
		super.describe(out);
		out.put("Type", "New MC Junction " + id);
	}
	
	@Override
	public void execute(RoadMap things) {
		if (things.getObject(id) != null) {
			throw new SimulatorException("Ups, most crowed junction " + id + 
					" already exists at time " + time);
		}
		//Hasta aqui es igual
		things.addJunction(new MostCrowed(id,"mc"));
	}
	
	public static class Builder implements Event.Builder {
		
		public boolean canParse(String title, String type){
			return "new_junction".equals(title) && "mc".equals(type);
		}

		public Event parse(Map<String, String> map) {
			try {
				int time = checkNoNegativeIntOptional("time", map);
				
				String id = checkId(map);

				//Hasta aqui es igual

				return new NewMostCrowedEvent(time, id);
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Incorrect arguments for new_junction", e);
			}
		}
	}
}
