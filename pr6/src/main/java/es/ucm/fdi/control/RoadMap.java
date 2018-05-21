package es.ucm.fdi.control;

import java.util.*;

import es.ucm.fdi.model.simobject.*;

/**
 * 
 * Manages all the SimObjects in the simulation.
 *
 */
public class RoadMap {
	private List<Vehicle> vehicles;
	private List<Road> roads;
	private List<Junction> junctions;

	private Map<String, SimObject> map;
	
	/**
	 * Class constructor
	 */
	public RoadMap() {
		this.vehicles = new ArrayList<>();
		this.roads = new ArrayList<>();
		this.junctions = new ArrayList<>();
		this.map = new HashMap<>();
	}
	
	/**
	 * 
	 * @param id 	id of a vehicle to be looked for
	 * @return 		the vehicle with corresponding id
	 * 		   		or null if it wasn't found
	 */
	public Vehicle getVehicle(String id) {
		SimObject obj = map.get(id);
		if (obj instanceof Vehicle) {
			return (Vehicle) obj;
		} else {
			return null;
		}
	}
	
	/**
	 * 
	 * @param id	id of a road to be looked for
	 * @return 		the road with corresponding id
	 * 		   		or null if it wasn't found
	 */
	public Road getRoad(String id) {
		SimObject obj = map.get(id);
		if (obj instanceof Road) {
			return (Road) obj;
		} else {
			return null;
		}
	}
	
	/**
	 * 
	 * @param id	id of a junction to be looked for
	 * @return		the junction with corresponding id
	 * 		   		or null if it wasn't found
	 */
	public Junction getJunction(String id) {
		SimObject obj = map.get(id);
		if (obj instanceof Junction) {
			return (Junction) obj;
		} else {
			return null;
		}
	}
	
	/**
	 * 
	 * @param id 	id of a simobject to be looked for
	 * @return 		the simobject with corresponding id
	 * 		   		or null if it wasn't found
	 */
	public SimObject getObject(String id) {
		return map.get(id);
	}
	
	/**
	 * @return a list with all the vehicles in the simulation
	 */
	public List<Vehicle> getVehicles() {
		return vehicles;
	}
	
	/**
	 * @return a list with all the roads in the simulation
	 */
	public List<Road> getRoads() {
		return roads;
	}
	
	/**
	 * @return a list with all the junctions in the simulation
	 */
	public List<Junction> getJunctions() {
		return junctions;
	}
	
	/**
	 * Adds a vehicle to the simulation
	 * 
	 * @param v	vehicle to be added
	 */
	public void addVehicle(Vehicle v) {
		map.put(v.getId(), v);
		vehicles.add(v);
	}
	
	/**
	 * Adds a road to the simulation
	 * 
	 * @param r	road to be added
	 */
	public void addRoad(Road r) {
		map.put(r.getId(), r);
		roads.add(r);
	}
	
	/**
	 * Adds a junction to the simulation
	 * 
	 * @param j junction to be added
	 */
	public void addJunction(Junction j) {
		map.put(j.getId(), j);
		junctions.add(j);
	}
}
