package es.ucm.fdi.control;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import es.ucm.fdi.ini.Ini;
import es.ucm.fdi.ini.IniSection;
import es.ucm.fdi.model.events.Event;
import es.ucm.fdi.model.events.MakeVehicleFaultyEvent;
import es.ucm.fdi.model.events.NewBikeEvent;
import es.ucm.fdi.model.events.NewCarEvent;
import es.ucm.fdi.model.events.NewDirtEvent;
import es.ucm.fdi.model.events.NewHighwayEvent;
import es.ucm.fdi.model.events.NewJunctionEvent;
import es.ucm.fdi.model.events.NewMostCrowedEvent;
import es.ucm.fdi.model.events.NewRoadEvent;
import es.ucm.fdi.model.events.NewRoundRobinEvent;
import es.ucm.fdi.model.events.NewVehicleEvent;

public class Controller {
	private static Event.Builder[] avaliableEvents = {
			new NewVehicleEvent.Builder(), new NewCarEvent.Builder(),
			new NewBikeEvent.Builder(), new NewRoadEvent.Builder(),
			new NewHighwayEvent.Builder(), new NewDirtEvent.Builder(),
			new NewJunctionEvent.Builder(), new NewRoundRobinEvent.Builder(),
			new NewMostCrowedEvent.Builder(),
			new MakeVehicleFaultyEvent.Builder() };

	private TrafficSimulator simulation;
	/*
	private InputStream in;
	private OutputStream out;
	private int ticks;

	public Controller(InputStream in, OutputStream out, int ticks) {
		this();
		this.in = in;
		this.out = out;
		this.ticks = ticks;
	}
	*/
	public Controller() {
		this.simulation = new TrafficSimulator();
	}
	/*
	public void run() throws IOException {
		loadEvents(in);
		simulation.run(ticks, out);
	}
	*/
	public void loadEvents(InputStream input) throws IOException {
		Ini init = new Ini(input);
		List<IniSection> list = init.getSections();

		for (IniSection i : list) {
			boolean found = false;
			Map<String, String> map = i.getKeysMap();
			for (Event.Builder b : avaliableEvents) {
				if (b.canParse(i.getTag(), map.getOrDefault("type", ""))) {
					simulation.addEvent(b.parse(map));
					found = true;
					break;
				}
			}

			if (!found) {
				throw new IllegalArgumentException(
						"Not sure about what is this: " + i.getTag());
			}
		}
	}

	public TrafficSimulator getSimulator() {
		return simulation;
	}
}
