package es.ucm.fdi.model.simobject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.ucm.fdi.model.Describable;
import es.ucm.fdi.model.exceptions.SimulatorException;

public class Junction extends SimObject implements Describable {
	protected Map<Road, IncomingRoad> knowIncoming;
	protected List<IncomingRoad> incoming;
	protected int trafficLight;
	protected Map<Junction, Road> knowOutgoing;

	public Junction(String id) {
		super(id);
		this.knowIncoming = new HashMap<>();
		this.incoming = new ArrayList<>();
		this.trafficLight = 0;
		this.knowOutgoing = new HashMap<>();
	}

	public void newVehicle(Vehicle c) {
		knowIncoming.get(c.getRoad()).cola.add(c);
	}

	public void newOutgoing(Road r) {
		knowOutgoing.put(r.getEnd(), r);
	}

	public void newIncoming(Road r) {
		IncomingRoad ir = new IncomingRoad(r.getId());
		knowIncoming.put(r, ir);
		incoming.add(ir);
		trafficLight = incoming.size()-1;
	}
	
	public boolean isRoadGreen(Road r) {
		return knowIncoming.get(r).semaforoVerde;
	}

	public void moveVehicleToNextRoad(Vehicle v) {
		Junction nextJunction = v.getProxCruce();
		if (nextJunction != null) {
			Road r = knowOutgoing.get(nextJunction);
			if (r == null) {
				throw new SimulatorException("A vehicle goes over ghost roads");
			}
			v.moveToNextRoad(r);
		} else {
			v.arrived();
		}
	}

	public void advance() {
		if (!incoming.isEmpty()) {
			IncomingRoad roadGreen = incoming.get(trafficLight);
			if (!roadGreen.cola.isEmpty()) {
				Vehicle lucky = roadGreen.cola.getFirst();
				lucky.getRoad().removeVehicle(lucky);
				roadGreen.cola.pop();
				moveVehicleToNextRoad(lucky);
			}
			advanceTrafficLights();
		}
	}
	
	protected void advanceTrafficLights(){
		IncomingRoad roadGreen = incoming.get(trafficLight);
		roadGreen.semaforoVerde = false;
		trafficLight++;
		if (trafficLight == incoming.size()) {
			trafficLight = 0;
		}
		incoming.get(trafficLight).semaforoVerde = true;
	}

	protected void fillReportDetails(Map<String, String> out) {
		StringBuilder reportJunct = new StringBuilder();
		incoming.forEach(r -> reportJunct.append(r.generateReport() + ","));

		if (incoming.size() != 0) {
			reportJunct.delete(reportJunct.length() - 1, reportJunct.length());
		}

		out.put("queues", reportJunct.toString());
	}

	protected String getReportHeader() {
		return "junction_report";
	}

	protected class IncomingRoad {
		protected ArrayDeque<Vehicle> cola;
		protected String id;
		protected boolean semaforoVerde;

		public IncomingRoad(String r) {
			cola = new ArrayDeque<>();
			id = r;
			semaforoVerde = false;
		}

		protected String generateReport() {
			StringBuilder vehiculosCola = new StringBuilder();
			cola.forEach(v -> vehiculosCola.append(v.getId() + ","));
			if (cola.size() != 0) {
				vehiculosCola.delete(vehiculosCola.length() - 1, vehiculosCola.length());
			}

			StringBuilder r = new StringBuilder();
			r.append("(" + id + ",");
			r.append(semaforoReport());
			r.append(",[" + vehiculosCola + "])");

			return r.toString();
			//return "("+id+","+semaforoReport()+",["+String.join(",", cola)
		}
		
		protected String semaforoReport(){
			return semaforoVerde ? "green" : "red";
		}
	}

	@Override
	public void describe(Map<String, String> out) {
		out.put("ID", id);
		StringBuilder reportJunct = new StringBuilder();
		String green = "";
		for(IncomingRoad ir : incoming) {
			if(ir.semaforoVerde) {
				green = "[" + ir.generateReport() + "]";
			}
			else {
				reportJunct.append(ir.generateReport() + ",");
			}
		}
		if (reportJunct.length() != 0) {
			reportJunct.delete(reportJunct.length() - 1, reportJunct.length());
		}
		out.put("Green", green);
		out.put("Red", "[" + reportJunct.toString() + "]");
	}
}
