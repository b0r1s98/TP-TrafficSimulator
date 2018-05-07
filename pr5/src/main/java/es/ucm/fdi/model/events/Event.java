package es.ucm.fdi.model.events;

import java.util.Map;

import es.ucm.fdi.control.RoadMap;
import es.ucm.fdi.model.Describable;

/**
 * 
 * Class to create and add a certain SimObject
 * at a certain time to the simulation.
 *
 */
public abstract class Event implements Describable{
	private int time;
	
	/**
	 * Class constructor
	 * 
	 * @param time	tick when the simobject will be added to simulation
	 */
	public Event(int time) {
		this.time = time;
	}

	/**
	 * @return tick when the simobject will be added to simulation
	 */
	public int getTime() {
		return time;
	}
	
	public void describe(Map<String, String> out) {
		out.put("Time", ""+time);
	}

	/**
	 * Creates the simobject and adds it to the simulation
	 * 
	 * @param things 	class containing all the simobjects
	 * 					currently in the simulation
	 */
	public abstract void execute(RoadMap things);

	/**
	 * 
	 * Checks if the arguments match this type of event.
	 *
	 */
	public interface Builder {

		/**
		 * Checks whether this event is the appropriate for that title and type
		 * 
		 * @param title	type of simobject that will be created
		 * @param type	subclass of the type that adds some changes
		 * @return	boolean telling if matches or not
		 */
		public abstract boolean canParse(String title, String type);

		/**
		 * Checks if all the data in the map is correct
		 * and creates the Event with it
		 * 
		 * @param map	data with whom the new Event will be filled
		 * @return 		event with all the data
		 */
		public abstract Event parse(Map<String, String> map);
		
		/**
		 * Checks the id doesn't have incorrect characters
		 * 
		 * @param id 	string to be checked
		 * @return		whether is correct or not.
		 */
		default boolean isValidId(String id) {
			return id.matches("[a-zA-Z0-9_]+");
		}

		/**
		 * Checks whether map contains a valid id
		 * 
		 * @param map	map with possibly an id
		 * @return		corresponding boolean
		 */
		default String checkId(Map<String, String> map) {
			String id = map.get("id");
			if (id == null) {
				throw new IllegalArgumentException("Missing id");
			}
			if (!isValidId(id)) {
				throw new IllegalArgumentException("Invalid id");
			}
			return id;
		}

		/**
		 * Checks whether map contains s and the value is a no negative int
		 * 
		 * @param s		the string to be checked
		 * @param map	the map optionally containing s
		 * @return		the int if it's no negative and s is contained in the map,
		 * 				or 0 if it's not
		 * @throws IllegalArgumentException	otherwise
		 */
		default int checkNoNegativeIntOptional(String s, Map<String, String> map) {
			int check = (map.containsKey(s) ? Integer.parseInt(map.get(s)) : 0);
			if (check < 0) {
				throw new IllegalArgumentException("Negative " + s);
			}
			return check;
		}

		/**
		 * Checks whether map contains s and the value is a positive int
		 * 
		 * @param s		the string to be checked
		 * @param map	the map optionally containing s
		 * @return		the int if it's positive and s is contained in the map
		 * @throws IllegalArgumentException otherwise
		 */
		default int checkPositiveInt(String s, Map<String, String> map) {
			String num = map.get(s);
			if (num == null) {
				throw new IllegalArgumentException("Missing " + s);
			}
			int check = Integer.parseInt(num);
			if (check <= 0) {
				throw new IllegalArgumentException("No positive " + s);
			}
			return check;
		}

		/**
		 * Checks whether map contains s
		 * 
		 * @param s		the string to be checked
		 * @param map	the map optionally containing s
		 * @return		the value given by the map 
		 * @throws IllegalArgumentException	if s isn't in the map
		 */
		default String checkContains(String s, Map<String, String> map) {
			if (!map.containsKey(s)) {
				throw new IllegalArgumentException("Missing " + s);
			}
			return map.get(s);
		}

	}
}