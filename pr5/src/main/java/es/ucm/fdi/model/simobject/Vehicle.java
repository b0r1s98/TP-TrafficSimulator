package es.ucm.fdi.model.simobject;

import java.util.List;
import java.util.Map;

import es.ucm.fdi.model.Describable;

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

	public int getLocation() {
		return location;
	}

	public void setFaultyTime(int ticks) {
		faultyTime += ticks;
		speed = 0;
	}

	public Junction getProxCruce() {
		if (nextJunction != itinerary.size()) {
			return itinerary.get(nextJunction);
		}
		else {
			return null;
		}
	}

	public Road getRoad() {
		return current;
	}

	public int getFaultyTime() {
		return faultyTime;
	}

	public int getCurrentSpeed() {
		return speed;
	}

	public void setCurrentSpeed(int speed) {
		this.speed = speed > speedMax ? speedMax : speed;
	}

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

	public void moveToNextRoad(Road r) {
		r.addVehicle(this);
		nextJunction++;
		current = r;
		location = 0;
		speed = 0;
	}

	public boolean getArrived() {
		return arrived;
	}

	public void arrived() {
		arrived = true;
	}

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

	public String getFillVehiculo() {
		return ("(" + id + "," + location + ")");
	}

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
		StringBuilder sb = new StringBuilder();
		itinerary.forEach(j -> sb.append(j.getId() + ","));
		if(sb.length() != 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		out.put("Itinerary", "[" + sb.toString() + "]");
	}
	
	
}
