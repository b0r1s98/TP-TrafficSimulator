package es.ucm.fdi.model.simobject;

import java.util.List;
import java.util.Map;

import es.ucm.fdi.model.Describable;
import es.ucm.fdi.util.MultiTreeMap;

public class Road extends SimObject implements Describable{
	protected int longitud;
	protected int maxVel;
	protected Junction start;
	protected Junction end;
	protected MultiTreeMap<Integer, Vehicle> vehicles;

	public Road(String ide, int lon, int maxv, Junction princ, Junction fin) {
		super(ide);
		longitud = lon;
		maxVel = maxv;
		vehicles = new MultiTreeMap<>((a, b) -> b - a);
		start = princ;
		end = fin;
	}
	
	public Junction getInicio() {
		return start;
	}

	public Junction getFinal() {
		return end;
	}

	public int getLongitud() {
		return longitud;
	}
	
	public List<Vehicle> getVehicles() {
		return vehicles.valuesList();
	}

	public void newVehicleR(Vehicle v) {
		vehicles.putValue(0, v);
	}

	public void removeVehicle(Vehicle v) {
		vehicles.removeValue(longitud, v);
	}

	public int calcularVelBase() {
		long n = vehicles.sizeOfValues();
		if (n < 1)
			n = 1;
		if (maxVel < (maxVel / n) + 1)
			return maxVel;
		return (maxVel / (int) n) + 1;
	}

	public void avanza() {
		MultiTreeMap<Integer, Vehicle> nuevos = new MultiTreeMap<>((a, b) -> b-a);
		int velocidadBase = calcularVelBase();
		int factorReduccion = 1;
		for (Vehicle v : vehicles.innerValues()) {
			if(factorReduccion == 1)
				if(v.getTiempoAveria() != 0)
					factorReduccion=2;
			if(v.getLocation() < longitud){
				if (v.getTiempoAveria() == 0)
					v.setVelocidadActual(velocidadBase / factorReduccion);
				v.avanza();
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
		
		if (!vehicles.isEmpty())
			meter.delete(meter.length() - 1, meter.length());
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
		out.put("Length", ""+longitud);
		out.put("Max Speed", ""+maxVel);
		StringBuilder sb = new StringBuilder();
		vehicles.valuesList().forEach(v -> sb.append(v.getId() + ","));
		if(sb.length() != 0) sb.delete(sb.length() - 1, sb.length());
		out.put("Vehicles", "[" + sb.toString() + "]");
	}
}