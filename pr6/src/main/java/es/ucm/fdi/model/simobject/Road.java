package es.ucm.fdi.model.simobject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import es.ucm.fdi.model.Describable;
import es.ucm.fdi.util.MultiTreeMap;

/**
 * 
 * A road that has a length and connects two junctions,
 * so vehicles can go over it.
 *
 */
public class Road extends SimObject implements Describable{
	protected int length;
	protected int maxSpeed;
	protected Junction start;
	protected Junction end;
	protected MultiTreeMap<Integer, Vehicle> vehicles;
	
	/**
	 * Class constructor
	 * 
	 * @param id		name of this road
	 * @param length	length of this road
	 * @param maxVel	maximum speed allowed to vehicles in this road
	 * @param start		the junction at the beginning
	 * @param end		the junction at the end
	 */
	public Road(String id, int length, int maxVel,
			Junction start, Junction end) {
		super(id);
		this.length = length;
		this.maxSpeed = maxVel;
		this.start = start;
		this.end = end;
		this.vehicles =  new MultiTreeMap<>((a, b) -> b - a);
	}

	/**
	 * @return the junction at the beginning
	 */
	public Junction getStart() {
		return start;
	}

	/**
	 * @return the junction at the end
	 */
	public Junction getEnd() {
		return end;
	}

	/**
	 * @return the length of this road
	 */
	public int getLength() {
		return length;
	}
	
	/**
	 * @return a list with the vehicles inside this road
	 */
	public List<Vehicle> getVehicles() {
		return vehicles.valuesList();
	}

	/**
	 * Adds a vehicle at the beginning of the road
	 * 
	 * @param v vehicle to be added
	 */
	public void addVehicle(Vehicle v) {
		vehicles.putValue(0, v);
	}

	/**
	 * Removes a vehicle from the road
	 * 
	 * @param v vehicle to be removed
	 */
	public void removeVehicle(Vehicle v) {
		vehicles.removeValue(length, v);
	}

	/**
	 * @return the base speed of this road
	 */
	public int calculateSpeedBase() {
		long n = vehicles.sizeOfValues();
		n = (n<1) ? 1 : n;
		if (maxSpeed < (maxSpeed / n) + 1) {
			return maxSpeed;
		}
		return (maxSpeed / (int) n) + 1;
	}

	/**
	 * Advances all vehicles in this road
	 */
	public void advance() {
		MultiTreeMap<Integer, Vehicle> nuevos = new MultiTreeMap<>((a, b) -> b-a);
		int velocidadBase = calculateSpeedBase();
		int factorReduccion = 1;
		for (Vehicle v : vehicles.innerValues()) {
			if(factorReduccion == 1) {
				if(v.getFaultyTime() != 0) {
					factorReduccion=2;
				}
			}
			if(v.getLocation() < length){
				if (v.getFaultyTime() == 0) {
					v.setCurrentSpeed(velocidadBase / factorReduccion);
				}
				v.advance();
			}
			nuevos.putValue(v.getLocation(), v);
		}
		vehicles = nuevos;
	}

	@Override
	protected void fillReportDetails(Map<String, String> out) {
		String state = vehicles.valuesList().stream()
				.map(Vehicle::getFillVehiculo)
				.collect(Collectors.joining(","));
		out.put("state", state);
	}

	@Override
	protected String getReportHeader() {
		return "road_report";
	}

	@Override
	public void describe(Map<String, String> out) {
		out.put("ID", id);
		out.put("Source", start.getId());
		out.put("Target", end.getId());
		out.put("Length", ""+length);
		out.put("Max Speed", ""+maxSpeed);
		
		String listVehicles = vehicles.valuesList().stream()
				.map(Vehicle::getId)
				.collect(Collectors.joining(","));
		out.put("Vehicles", "[" + listVehicles + "]");
	}
}