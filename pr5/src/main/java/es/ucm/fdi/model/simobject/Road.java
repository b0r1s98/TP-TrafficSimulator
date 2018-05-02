package es.ucm.fdi.model.simobject;

import java.util.List;
import java.util.Map;

import es.ucm.fdi.model.Describable;
import es.ucm.fdi.util.MultiTreeMap;

public class Road extends SimObject implements Describable{
	protected int length;
	protected int maxSpeed;
	protected Junction start;
	protected Junction end;
	protected MultiTreeMap<Integer, Vehicle> vehicles;
	
	public Road(String id, int length, int maxVel, Junction start, Junction end) {
		super(id);
		this.length = length;
		this.maxSpeed = maxVel;
		this.start = start;
		this.end = end;
		this.vehicles =  new MultiTreeMap<>((a, b) -> b - a);
	}

	public Junction getStart() {
		return start;
	}

	public Junction getEnd() {
		return end;
	}

	public int getLength() {
		return length;
	}
	
	public List<Vehicle> getVehicles() {
		return vehicles.valuesList();
	}

	public void addVehicle(Vehicle v) {
		vehicles.putValue(0, v);
	}

	public void removeVehicle(Vehicle v) {
		vehicles.removeValue(length, v);
	}

	public int calculateSpeedBase() {
		long n = vehicles.sizeOfValues();
		n = (n<1) ? 1 : n;
		if (maxSpeed < (maxSpeed / n) + 1) {
			return maxSpeed;
		}
		return (maxSpeed / (int) n) + 1;
	}

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

	protected void fillReportDetails(Map<String, String> out) {
		StringBuilder meter = new StringBuilder();
		for (Vehicle v : vehicles.innerValues()) {
			meter.append(v.getFillVehiculo() + ",");
		}
		
		if (!vehicles.isEmpty()) {
			meter.delete(meter.length() - 1, meter.length());
		}
		out.put("state", meter.toString());
	}

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
		StringBuilder sb = new StringBuilder();
		vehicles.valuesList().forEach(v -> sb.append(v.getId() + ","));
		if(sb.length() != 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		out.put("Vehicles", "[" + sb.toString() + "]");
	}
}