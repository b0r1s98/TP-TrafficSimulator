package es.ucm.fdi.model.simobject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import es.ucm.fdi.model.Describable;
import es.ucm.fdi.model.exceptions.SimulatorException;

/**
 * 
 * A junction that connects roads and manages
 * them using traffic lights.
 *
 */
public class Junction extends SimObject implements Describable {
	protected Map<Road, IncomingRoad> knowIncoming;
	protected List<IncomingRoad> incoming;
	protected int trafficLight;
	protected Map<Junction, Road> knowOutgoing;

	/**
	 * Class constructor
	 * 
	 * @param id	name of this junction
	 */
	public Junction(String id) {
		super(id);
		this.knowIncoming = new HashMap<>();
		this.incoming = new ArrayList<>();
		this.trafficLight = 0;
		this.knowOutgoing = new HashMap<>();
	}

	/**
	 * Adds vehicle to the corresponding incoming road queue
	 * 
	 * @param v	vehicle to be added
	 */
	public void newVehicle(Vehicle v) {
		knowIncoming.get(v.getRoad()).queue.add(v);
	}

	/**
	 * Adds a road to knowOutgoing, map that given a junction,
	 * gives the road that goes from this one to that junction
	 * 
	 * @param r road to be added
	 */
	public void addOutgoing(Road r) {
		knowOutgoing.put(r.getEnd(), r);
	}

	/**
	 * Adds a road to the junction
	 * 
	 * @param r road to be added
	 */
	public void newIncoming(Road r) {
		IncomingRoad ir = new IncomingRoad(r.getId());
		knowIncoming.put(r, ir);
		incoming.add(ir);
		trafficLight = incoming.size()-1;
	}
	
	/**
	 * Returns whether the corresponding incoming road
	 * of this road is green or not
	 * 
	 * @param r road to be checked
	 * @return corresponding boolean
	 */
	public boolean isRoadGreen(Road r) {
		return knowIncoming.get(r).trafficLightsGreen;
	}

	/**
	 * Moves vehicle to next road
	 * 
	 * @param v vehicle to be moved
	 */
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

	/**
	 * Allows one vehicle in an incoming road in green
	 *  to go to next road
	 */
	public void advance() {
		if (!incoming.isEmpty()) {
			IncomingRoad roadGreen = incoming.get(trafficLight);
			if (!roadGreen.queue.isEmpty()) {
				Vehicle lucky = roadGreen.queue.getFirst();
				lucky.getRoad().removeVehicle(lucky);
				roadGreen.queue.pop();
				moveVehicleToNextRoad(lucky);
			}
			advanceTrafficLights();
		}
	}
	
	/**
	 * Changes traffic lights color
	 */
	protected void advanceTrafficLights(){
		IncomingRoad roadGreen = incoming.get(trafficLight);
		roadGreen.trafficLightsGreen = false;
		trafficLight++;
		if (trafficLight == incoming.size()) {
			trafficLight = 0;
		}
		incoming.get(trafficLight).trafficLightsGreen = true;
	}

	@Override
	protected void fillReportDetails(Map<String, String> out) {
		String queues = incoming.stream()
				.map(IncomingRoad::generateReport)
				.collect(Collectors.joining(","));
		out.put("queues", queues);
	}

	@Override
	protected String getReportHeader() {
		return "junction_report";
	}

	/**
	 * 
	 * Manages the vehicles that are entering a junction
	 *
	 */
	protected class IncomingRoad {
		protected ArrayDeque<Vehicle> queue;
		protected String id;
		protected boolean trafficLightsGreen;

		/**
		 * Class constructor
		 * 
		 * @param r	name of the road
		 */
		public IncomingRoad(String r) {
			queue = new ArrayDeque<>();
			id = r;
			trafficLightsGreen = false;
		}

		/**
		 * Generates report of all the vehicles in the queue
		 * 
		 * @return this report
		 */
		protected String generateReport() {
			String vehiclesQueue = queue.stream()
					.map(Vehicle::getId)
					.collect(Collectors.joining(","));

			return "(" + id + "," + trafficLightsReport()
					+ ",[" + vehiclesQueue + "])";
		}
		
		/**
		 * @return whether the traffic lights are green or red
		 */
		protected String trafficLightsReport(){
			return trafficLightsGreen ? "green" : "red";
		}
	}

	@Override
	public void describe(Map<String, String> out) {
		out.put("ID", id);
		
		String red = incoming.stream()
				.filter(ir -> !ir.trafficLightsGreen)
				.map(IncomingRoad::generateReport)
				.collect(Collectors.joining(","));
		
		String green = incoming.stream()
				.filter(ir -> ir.trafficLightsGreen)
				.map(IncomingRoad::generateReport)
				.findAny()
				.orElse("[]");
		
		out.put("Green", green);
		out.put("Red", "[" + red + "]");
	}
}
