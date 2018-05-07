package es.ucm.fdi.model.simobject;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

public class DirtTest {
	@Test
	public void dirtTest() {
		List<Junction> itinerary = new ArrayList<>();
		itinerary.add(new Junction("j1"));
		itinerary.add(new Junction("j2"));
		itinerary.add(new Junction("j3"));
		Vehicle v = new Vehicle(5, itinerary, "v1");
		Vehicle v1 = new Vehicle(15, itinerary, "v2");
		Vehicle v2 = new Vehicle(15, itinerary, "v3");
		Dirt r1 = new Dirt("r1", 30, 15, itinerary.get(0), itinerary.get(1),
				"Dirt");
		Dirt r2 = new Dirt("r2", 15, 20, itinerary.get(1), itinerary.get(2),
				"Dirt");
		itinerary.get(1).newIncoming(r1);
		itinerary.get(1).addOutgoing(r2);
		itinerary.get(2).newIncoming(r2);
		v.moveToNextRoad(r1);
		v1.moveToNextRoad(r1);
		v2.moveToNextRoad(r1);
		v.setFaultyTime(2);
		v1.setFaultyTime(2);
		r1.advance();
		assertTrue("Velocidad inadecuada", v2.getCurrentSpeed() == 5);
	}

}
