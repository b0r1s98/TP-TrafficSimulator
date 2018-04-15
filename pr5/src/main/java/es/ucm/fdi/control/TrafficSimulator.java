package es.ucm.fdi.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import es.ucm.fdi.events.Event;
import es.ucm.fdi.ini.Ini;
import es.ucm.fdi.ini.IniSection;
import es.ucm.fdi.simobject.Junction;
import es.ucm.fdi.simobject.Road;
import es.ucm.fdi.simobject.SimObject;
import es.ucm.fdi.util.MultiTreeMap;

public class TrafficSimulator {
	private RoadMap objects;
	private MultiTreeMap<Integer, Event> events;
	private int timeCounter;

	public TrafficSimulator() {
		this.objects = new RoadMap();
		this.events = new MultiTreeMap<>();
		this.timeCounter = 0;
	}

	public void addEvent(Event e) {
		if (e.getTime() < timeCounter)
			throw new IllegalArgumentException("We don't travel back in time!");
		events.putValue(e.getTime(), e);
	}

	public void run(int numSteps, OutputStream out) throws IOException {
		int timeLimit = timeCounter + numSteps - 1;
		while (timeCounter <= timeLimit) {
			List<Event> nowEvents = events.get(timeCounter);
			if (nowEvents != null)
				for (Event e : nowEvents)
					e.execute(objects);
			List<Road> roads = objects.getRoads();
			for (Road r : roads)
				r.avanza();
			List<Junction> junctions = objects.getJunctions();
			
			for (Junction j : junctions)
				j.avanza();
			timeCounter++;
			generateReport(out);
		}
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

	public void generateReport(OutputStream out) throws IOException {
		Ini report = new Ini();
		addSectionsFor(objects.getJunctions(), report);
		addSectionsFor(objects.getRoads(), report);
		addSectionsFor(objects.getVehicles(), report);
		report.store(out);
	}
}
