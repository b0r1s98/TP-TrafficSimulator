package es.ucm.fdi.simobject;

import java.util.Map;
import es.ucm.fdi.util.MultiTreeMap;

public class Road extends SimObject {
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

	public Junction getFinal() {
		return end;
	}

	public int getLongitud() {
		return longitud;
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
}