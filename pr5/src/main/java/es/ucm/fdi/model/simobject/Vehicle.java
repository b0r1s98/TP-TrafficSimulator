package es.ucm.fdi.model.simobject;

import java.util.List;
import java.util.Map;

import es.ucm.fdi.model.Describable;

public class Vehicle extends SimObject implements Describable {
	protected int velMaxima;
	protected int velActual;
	protected Road actual;
	protected int localizacion;
	protected List<Junction> itinerario;
	protected int proxCruce;
	protected int tiempoAveria;
	protected int kilometrage;
	protected boolean haLlegado;

	public Vehicle(int velM, List<Junction> cruc, String ide) {
		super(ide);
		localizacion = 0;
		velMaxima = velM;
		velActual = 0;
		tiempoAveria = 0;
		itinerario = cruc;
		kilometrage = 0;
		proxCruce = 1;
		haLlegado = false;
	}

	public int getLocation() {
		return localizacion;
	}

	public void setTiempoAveria(int n) {
		tiempoAveria += n;
		velActual = 0;
	}

	public Junction getProxCruce() {
		if (proxCruce != itinerario.size())
			return itinerario.get(proxCruce);
		else
			return null;
	}

	public Road getRoad() {
		return actual;
	}

	public int getTiempoAveria() {
		return tiempoAveria;
	}

	public int getVelocidadActual() {
		return velActual;
	}

	public void setVelocidadActual(int vel) {
		if (vel > velMaxima)
			velActual = velMaxima;
		else
			velActual = vel;
	}

	public void avanza() {
		if (tiempoAveria > 0) {
			tiempoAveria--;
		} else {
			if (localizacion + velActual >= actual.getLongitud()) {
				kilometrage += actual.getLongitud() - localizacion;
				localizacion = actual.getLongitud();
				velActual = 0;

				actual.getFinal().newVehicle(this);
			} else {
				kilometrage += velActual;
				localizacion += velActual;
			}
		}
	}

	public void moveToNextRoad(Road r) {
		r.newVehicleR(this);
		proxCruce++;
		actual = r;
		localizacion = 0;
		velActual = 0;
	}

	public boolean getArrived() {
		return haLlegado;
	}

	public void arrived() {
		haLlegado = true;
	}

	protected void fillReportDetails(Map<String, String> out) {
		out.put("speed", "" + velActual);
		out.put("kilometrage", "" + kilometrage);
		out.put("faulty", "" + tiempoAveria);
		if (!haLlegado)
			out.put("location", "(" + actual.getId() + "," + localizacion + ")");
		else
			out.put("location", "arrived");
	}

	public String getFillVehiculo() {
		return ("(" + id + "," + localizacion + ")");

	}

	protected String getReportHeader() {
		return "vehicle_report";
	}

	@Override
	public Map<String, String> describe(Map<String, String> out) {
		out.put("ID", id);
		out.put("Road", actual.getId());
		out.put("Location", ""+localizacion);
		out.put("Speed", ""+velActual);
		out.put("Km", ""+kilometrage);
		out.put("Faulty Units", ""+tiempoAveria);
		StringBuilder sb = new StringBuilder();
		itinerario.forEach(j -> sb.append(j.getId() + ","));
		if(sb.length() != 0) sb.delete(sb.length() - 1, sb.length());
		out.put("Itinerary", "[" + sb.toString() + "]");
		return out;
	}
	
	
}
