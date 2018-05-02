package es.ucm.fdi.control;

import java.util.*;

import es.ucm.fdi.model.simobject.*;

public class RoadMap {
	private List<Vehicle> vehicles;
	private List<Road> roads;
	private List<Junction> junctions;

	private Map<String, SimObject> map;

	public RoadMap() {
		this.vehicles = new ArrayList<>();
		this.roads = new ArrayList<>();
		this.junctions = new ArrayList<>();
		this.map = new HashMap<>();
	}

	public Vehicle getVehicle(String id) {
		SimObject obj = map.get(id);
		if (obj instanceof Vehicle) {
			return (Vehicle) obj;
		}
		else {
			return null;
		}
	}

	public Road getRoad(String id) {
		SimObject obj = map.get(id);
		if (obj instanceof Road) {
			return (Road) obj;
		}
		else {
			return null;
		}
	}

	public Junction getJunction(String id) {
		SimObject obj = map.get(id);
		if (obj instanceof Junction) {
			return (Junction) obj;
		}
		else {
			return null;
		}
	}

	public SimObject getObject(String id) {
		return map.get(id);
	}

	public List<Vehicle> getVehicles() {
		return vehicles;
	}

	public List<Road> getRoads() {
		return roads;
	}

	public List<Junction> getJunctions() {
		return junctions;
	}

	public void addVehicle(Vehicle v) {
		map.put(v.getId(), v);
		vehicles.add(v);
	}

	public void addRoad(Road r) {
		map.put(r.getId(), r);
		roads.add(r);
	}

	public void addJunction(Junction j) {
		map.put(j.getId(), j);
		junctions.add(j);
	}
}
