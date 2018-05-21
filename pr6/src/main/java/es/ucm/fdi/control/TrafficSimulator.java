package es.ucm.fdi.control;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import es.ucm.fdi.model.events.Event;
import es.ucm.fdi.model.exceptions.SimulatorException;
import es.ucm.fdi.ini.Ini;
import es.ucm.fdi.ini.IniSection;
import es.ucm.fdi.model.simobject.*;
import es.ucm.fdi.util.MultiTreeMap;

/**
 * 
 * The Simulator itself: can add events, run x ticks and generate reports.
 *
 */
public class TrafficSimulator {
	private RoadMap objects;
	private MultiTreeMap<Integer, Event> events;
	private int timeCounter;
	
	private List<SimulatorListener> listeners = new ArrayList<>();
	
	private static final Logger logger =
			Logger.getLogger(TrafficSimulator.class.getName());

	/**
	 * Class constructor
	 */
	public TrafficSimulator() {
		this.events = new MultiTreeMap<>();
		clear();
	}

	/**
	 * Adds an event to be executed at a time
	 * @param e	the event to be added
	 */
	public void addEvent(Event e) {
		if (e.getTime() < timeCounter) {
			throw new IllegalArgumentException("We don't travel back in time!");
		}
		events.putValue(e.getTime(), e);
		logger.info("Event added");
		fireUpdateEvent(EventType.NEW_EVENT, "");
	}
	
	/**
	 * Removes all events in the queue
	 */
	public void removeEvents() {
		events.clear();
		logger.info("Events removed");
	}

	/**
	 * Runs the simulator for numSteps times and generates all reports
	 * 
	 * @param numSteps		how many steps the simulator will run
	 * @param out			the outputstream where the reports will be stored
	 * @throws IOException	if an output exception ocurred
	 */
	public void run(int numSteps, OutputStream out) throws IOException {
		logger.info("Running simulator");
		int timeLimit = timeCounter + numSteps - 1;
		try {
			while (timeCounter <= timeLimit) {
				List<Event> nowEvents = events.get(timeCounter);
				if (nowEvents != null) {
					logger.info("Executing events at time "+timeCounter);
					for (Event e : nowEvents) {
						e.execute(objects);
					}
				}
				
				logger.info("Advancing roads");
				for (Road r : objects.getRoads()) {
					r.advance();
				}
				
				logger.info("Advancing junctions");
				for (Junction j :  objects.getJunctions()) {
					j.advance();
				}
				
				timeCounter++;
				logger.info("Time advanced");
				fireUpdateEvent(EventType.ADVANCED, "");
				
				generateReport(out, null);
			}
		} catch (SimulatorException e) {
			logger.log(Level.WARNING, "Simulation error", e);
			fireUpdateEvent(EventType.ERROR, e.getMessage());
		}
	}
	
	/**
	 * Generates the reports of the simobjects contained on ids,
	 * or every report if ids is null
	 * 
	 * @param out			the outputstream where the reports will be stored
	 * @param ids			set containing the ids of the simobjects to be reported
	 * @throws IOException	if an output exception ocurred
	 */
	public void generateReport(OutputStream out, Set<String> ids) throws IOException {	
		Ini report = new Ini();
		
		if(ids == null || ids.isEmpty()) {
			logger.info("Generating all reports");
			addSectionsFor(objects.getJunctions(), report);
			addSectionsFor(objects.getRoads(), report);
			addSectionsFor(objects.getVehicles(), report);
		} else {
			logger.info("Generating selected reports");
			List<SimObject> objs = new ArrayList<>();
			addIdsFromSet(objects.getJunctions(), objs, ids);
			addIdsFromSet(objects.getRoads(), objs, ids);
			addIdsFromSet(objects.getVehicles(), objs, ids);
			addSectionsFor(objs, report);
		}
		
		logger.info("Saving reports");
		report.store(out);
	}
	
	/**
	 * Adds into destiny the simobjects from source whose ids are into ids
	 * 
	 * @param source	list containing the simbobjects to be looked for
	 * @param destiny	list containing the simobjects whose ids are in ids
	 * @param ids		set with the ids of the simobjects that can be in destiny
	 */
	private void addIdsFromSet(List<? extends SimObject> source,
			List<SimObject> destiny, Set<String> ids) {
		
		for(SimObject o : source) {
			if(ids.contains(o.getId())) {
				destiny.add(o);
			}
		}
	}

	/**
	 * Adds to report a section with the report of each simobject in list
	 * 
	 * @param list		the list of simobjects to be reported
	 * @param report	the ini to be filled
	 */
	private void addSectionsFor(List<? extends SimObject> list, Ini report) {
		for (SimObject o : list) {
			Map<String, String> map = o.report(timeCounter);
			IniSection section = new IniSection(map.get(""));
			map.remove("");
			map.forEach((k, v) -> section.setValue(k, v));
			report.addsection(section);
		}
	}

	/**
	 * Clears simulator and notifies of the reset
	 */
	public void reset(){
		clear();
		logger.info("Simulator reseted");
		fireUpdateEvent(EventType.RESET, "");
	}
	
	/**
	 * Resets simulator without removing events
	 */
	private void clear(){
		this.objects = new RoadMap();
		this.timeCounter = 0;
	}
	
	/**
	 * Adds a SimulatorListener to the listeners list and notifies all listeners
	 * 
	 * @param l	the SimulatorListener to be added
	 */
	public void addSimulatorListener(SimulatorListener l) {
		listeners.add(l);
		logger.info("SimulatorListener added");
		UpdateEvent ue = new UpdateEvent(EventType.REGISTERED);
		SwingUtilities.invokeLater(()->l.update(ue, ""));
	}

	/**
	 * Removes a SimulatorListener from the listeners list
	 * 
	 * @param l the SimulatorListener to be removed
	 */
	public void removeListener(SimulatorListener l) {
		listeners.remove(l);
		logger.info("SimulatorListener removed");
	}

	/**
	 * Notifies all SimulatorListeners in listeners of an event
	 * 
	 * @param type		the type of the event that happened
	 * @param error		a string with the error if type is ERROR,
	 * 					"" otherwise
	 */
	private void fireUpdateEvent(EventType type, String error) {
		UpdateEvent ue = new UpdateEvent(type);
		for(SimulatorListener l : listeners){
			l.update(ue, error);
		}
	}
	
	/**
	 * 
	 * Interface all SimulatorListeners must implement.
	 *
	 */
	public interface SimulatorListener {
		
		/**
		 * Method that will be executed when an event is fired
		 * 
		 * @param ue		class containing all necessary information
		 * 					about the event and the simulator
		 * @param error		a string with the error if type is ERROR,
		 * 					"" otherwise
		 */
		void update(UpdateEvent ue, String error);
	}
	
	/**
	 * 
	 * Enum containing the types of event SimulatorListeners can be notified.
	 *
	 */
	public enum EventType {
		REGISTERED, RESET, NEW_EVENT, ADVANCED, ERROR;
	}

	/**
	 * 
	 * Class containing current simulator state, sent when an event is fired
	 * to the SimulatorListeners.
	 *
	 */
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
