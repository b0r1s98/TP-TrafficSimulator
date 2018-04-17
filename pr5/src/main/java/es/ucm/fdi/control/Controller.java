package es.ucm.fdi.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import es.ucm.fdi.model.events.*;
import es.ucm.fdi.ini.Ini;
import es.ucm.fdi.ini.IniError;
import es.ucm.fdi.ini.IniSection;

public class Controller {
	private static Event.Builder[] avaliableEvents = {
			new NewVehicleEvent.Builder(), new NewCarEvent.Builder(),
			new NewBikeEvent.Builder(), new NewRoadEvent.Builder(),
			new NewHighwayEvent.Builder(), new NewDirtEvent.Builder(),
			new NewJunctionEvent.Builder(), new NewRoundRobinEvent.Builder(),
			new NewMostCrowedEvent.Builder(),
			new MakeVehicleFaultyEvent.Builder() };

	private TrafficSimulator simulation;
	private InputStream in;
	private OutputStream out;
	private int ticks;

	public Controller(InputStream in, OutputStream out, int ticks) {
		this();
		this.in = in;
		this.out = out;
		this.ticks = ticks;
	}
	
	public Controller() {
		this.simulation = new TrafficSimulator();
	}

	public void run() {
		try {
			loadEvents();
			simulation.run(ticks, out);
		} catch (IOException e) {
			System.out.println("Problems loading/saving");
		} catch (IniError e) {
			System.out.println("Problems loading/saving: " + e.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void loadEvents() throws IOException {
		Ini init = new Ini(in);
		List<IniSection> list = init.getSections();

		for (IniSection i : list) {
			boolean found = false;
			Map<String, String> map = i.getKeysMap();
			for (Event.Builder b : avaliableEvents) {
				if (b.canParse(i.getTag(), map.getOrDefault("type", ""))) {
					simulation.addEvent(b.fill(map));
					found = true;
					break;
				}
			}

			if (!found)
				throw new IllegalArgumentException(
						"Not sure about what is this: " + i.getTag());
		}
	}

	public TrafficSimulator getSimulator() {
		return simulation;
	}
	
	public void setOutputStream(OutputStream out) {
		this.out = out;
	}
	
	public void setInputStream(InputStream in) {
		this.in = in;
	}
}
