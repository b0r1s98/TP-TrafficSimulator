package es.ucm.fdi.events;

import java.util.Map;

import es.ucm.fdi.controller.RoadMap;
import es.ucm.fdi.exceptions.SimulatorException;
import es.ucm.fdi.simobject.RoundRobin;

public class NewRoundRobinEvent extends NewJunctionEvent {
	
	protected int minTime;
	protected int maxTime;
	
	public NewRoundRobinEvent(int time, String id, int minTime, int maxTime) {
		super(time, id);
		this.minTime = minTime;
		this.maxTime = maxTime;
	}
	
	@Override
	public void execute(RoadMap things) {
		if (things.getObject(id) != null)
			throw new SimulatorException("Ups, " + id + " already exists");
		//Hasta aqui es igual
		things.addJunction(new RoundRobin(id,minTime,maxTime, "rr"));
	}
	
	public static class Builder implements Event.Builder {
		
		public boolean canParse(String title, String type){
			return "new_junction".equals(title) && "rr".equals(type);
		}

		public Event fill(Map<String, String> map) {
			try {
				String id = checkId(map);

				int time = checkNoNegativeIntOptional("time", map);
				
				//Hasta aqui es igual
				int maxTime = checkPositiveInt("max_time_slice", map);
				
				int minTime = checkPositiveInt("min_time_slice", map);
				
				if(minTime > maxTime) throw new IllegalArgumentException("max_time must be greater than min_time");

				return new NewRoundRobinEvent(time, id, minTime, maxTime);
			} catch (IllegalArgumentException e) {
				throw e;
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Incorrect arguments for new_junction");
			}
		}
	}
}
