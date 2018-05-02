package es.ucm.fdi.control;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import javax.swing.SwingUtilities;

import es.ucm.fdi.model.events.Event;
import es.ucm.fdi.model.exceptions.SimulatorException;
import es.ucm.fdi.ini.Ini;
import es.ucm.fdi.ini.IniSection;
import es.ucm.fdi.model.simobject.*;
import es.ucm.fdi.util.MultiTreeMap;

public class TrafficSimulator {
	private RoadMap objects;
	private MultiTreeMap<Integer, Event> events;
	private int timeCounter;

	public TrafficSimulator() {
		this.events = new MultiTreeMap<>();
		clear();
	}

	public void addEvent(Event e) {
		if (e.getTime() < timeCounter) {
			throw new IllegalArgumentException("We don't travel back in time!");
		}
		events.putValue(e.getTime(), e);
		fireUpdateEvent(EventType.NEW_EVENT, "");
	}
	
	public void removeEvents() {
		events.clear();
	}

	public void run(int numSteps, OutputStream out) throws IOException {
		int timeLimit = timeCounter + numSteps - 1;
		while (timeCounter <= timeLimit) {
			List<Event> nowEvents = events.get(timeCounter);
			if (nowEvents != null) {
				for (Event e : nowEvents) {
					e.execute(objects);
				}
			}
			
			for (Road r : objects.getRoads()) {
				r.advance();
			}
			
			for (Junction j :  objects.getJunctions()) {
				j.advance();
			}
			
			timeCounter++;
			fireUpdateEvent(EventType.ADVANCED, "");
			
			try {
				generateReport(out, objects.getJunctions(), objects.getRoads(), objects.getVehicles());
			}
			catch (SimulatorException e) {
				fireUpdateEvent(EventType.ERROR, e.getMessage());
			}
		}
	}
	
	public void generateReport(OutputStream out, List<Junction> junctions,
			List<Road> roads, List<Vehicle> vehicles) throws IOException {
		Ini report = new Ini();
		addSectionsFor(junctions, report);
		addSectionsFor(roads, report);
		addSectionsFor(vehicles, report);
		report.store(out);
	}

	private void addSectionsFor(List<? extends SimObject> it, Ini report) {
		for (SimObject j : it) {
			Map<String, String> map = j.report(timeCounter);
			IniSection section = new IniSection(map.get(""));
			map.remove("");
			map.forEach((k, v) -> section.setValue(k, v));
			report.addsection(section);
		}
	}

	public void reset(){
		clear();
		fireUpdateEvent(EventType.RESET, "");
	}
	
	private void clear(){
		this.objects = new RoadMap();
		this.timeCounter = 0;
	}

	private List<SimulatorListener> listeners = new ArrayList<>();

	public void addSimulatorListener(SimulatorListener l) {
		listeners.add(l);
		UpdateEvent ue = new UpdateEvent(EventType.REGISTERED);
		SwingUtilities.invokeLater(()->l.update(ue, ""));
	}

	public void removeListener(SimulatorListener l) {
		listeners.remove(l);
	}

	private void fireUpdateEvent(EventType type, String error) {
		UpdateEvent ue = new UpdateEvent(type);
		for(SimulatorListener l : listeners){
			l.update(ue, error);
		}
	}

	public interface SimulatorListener {
		void update(UpdateEvent ue, String error);
	}

	public enum EventType {
		REGISTERED, RESET, NEW_EVENT, ADVANCED, ERROR;
	}

	public class UpdateEvent {
		private EventType event;
		
		public UpdateEvent(EventType event) {
			this.event = event;
		}

		public EventType getEvent() {
			return event;
		}

		public RoadMap getRoadMap() {
			return objects;
		}

		public List<Event> getEvenQueue() {
			return events.valuesList();
		}

		public int getCurrentTime() {
			return timeCounter;
		}
	}
	
}
