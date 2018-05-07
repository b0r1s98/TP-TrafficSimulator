package es.ucm.fdi.model.simobject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import es.ucm.fdi.model.Describable;

/**
 * 
 * A SimObject called Vehicle.
 *
 */
public class Vehicle extends SimObject implements Describable {
	protected int speedMax;
	protected int speed;
	protected Road current;
	protected int location;
	protected List<Junction> itinerary;
	protected int nextJunction;
	protected int faultyTime;
	protected int kilometers;
	protected boolean arrived;

	/**
	 * Class constructor
	 * 
	 * @param speedMax	maximum speed
	 * @param itinerary	array of the junctions the vehicle must pass through
	 * @param id		name of the vehicle
	 */
	public Vehicle(int speedMax, List<Junction> itinerary, String id) {
		super(id);
		this.speedMax = speedMax;
		this.itinerary = itinerary;
		this.location = 0;
		this.speed = 0;
		this.faultyTime = 0;
		this.kilometers = 0;
		this.nextJunction = 1;
		this.arrived = false;
	}

	/**
	 * @return	position of the vehicle in the road
	 */
	public int getLocation() {
		return location;
	}

	/**
	 * Makes this vehicle faulty
	 * 
	 * @param ticks how many ticks will be faulty
	 */
	public void setFaultyTime(int ticks) {
		faultyTime += ticks;
		speed = 0;
	}

	/**
	 * @return next junction in the vehicle's itinerary
	 */
	public Junction getProxCruce() {
		if (nextJunction != itinerary.size()) {
			return itinerary.get(nextJunction);
		}
		else {
			return null;
		}
	}

	/**
	 * @return road where the vehicle is currently
	 */
	public Road getRoad() {
		return current;
	}

	/**
	 * @return current faulty ticks remaining
	 */
	public int getFaultyTime() {
		return faultyTime;
	}

	/**
	 * @return current speed
	 */
	public int getCurrentSpeed() {
		return speed;
	}

	/**
	 * Sets current speed to speed (or speedMax if speed is greater)
	 * 
	 * @param speed new speed
	 */
	public void setCurrentSpeed(int speed) {
		this.speed = (speed > speedMax) ? speedMax : speed;
	}

	/**
	 * Advances this vehicle a tick in time
	 */
	public void advance() {
		if (faultyTime > 0) {
			faultyTime--;
		} else {
			if (location + speed >= current.getLength()) {
				kilometers += current.getLength() - location;
				location = current.getLength();
				speed = 0;

				current.getEnd().newVehicle(this);
			} else {
				kilometers += speed;
				location += speed;
			}
		}
	}

	
	/**
	 * Moves this vehicle to road r
	 * 
	 * @param r	road where vehicle is going to be moved
	 */
	public void moveToNextRoad(Road r) {
		r.addVehicle(this);
		nextJunction++;
		current = r;
		location = 0;
		speed = 0;
	}

	/**
	 * @return whether it arrived
	 */
	public boolean getArrived() {
		return arrived;
	}

	/**
	 * Sets arrived to true
	 */
	public void arrived() {
		arrived = true;
	}

	@Override
	protected void fillReportDetails(Map<String, String> out) {
		out.put("speed", "" + speed);
		out.put("kilometrage", "" + kilometers);
		out.put("faulty", "" + faultyTime);
		if (!arrived) {
			out.put("location", "(" + current.getId() + "," + location + ")");
		}
		else {
			out.put("location", "arrived");
		}
	}
	
	/**
	 * @return string with id and location formatted
	 */
	public String getFillVehiculo() {
		return ("(" + id + "," + location + ")");
	}

	@Override
	protected String getReportHeader() {
		return "vehicle_report";
	}

	@Override
	public void describe(Map<String, String> out) {
		out.put("ID", id);
		out.put("Road", current.getId());
		out.put("Location", ""+location);
		out.put("Speed", ""+speed);
		out.put("Km", ""+kilometers);
		out.put("Faulty Units", ""+faultyTime);
		
		String ids = itinerary.stream()
				.map(Junction::getId)
				.collect(Collectors.joining(","));
		out.put("Itinerary", "[" + ids + "]");
	}
	
	
}
