package es.ucm.fdi.model.simobject;

import java.util.ArrayList;

import org.junit.Test;

import java.util.List;
import static org.junit.Assert.*;

public class VehicleTest {
	List<Junction> itinerary = new ArrayList<>();

	@Test
	public void vehicleTest() {
		itinerary.add(new Junction("j1"));
		itinerary.add(new Junction("j2"));
		itinerary.add(new Junction("j3"));
		Vehicle v = new Vehicle(5, itinerary, "v1");
		Vehicle v1 = new Vehicle(15, itinerary, "v2");
		v.setCurrentSpeed(10);
		v.setCurrentSpeed(20);
		Road r1 = new Road("r1", 30, 20, itinerary.get(0), itinerary.get(1));
		Road r2 = new Road("r2", 15, 20, itinerary.get(1), itinerary.get(2));
		itinerary.get(1).newIncoming(r1);
		itinerary.get(1).addOutgoing(r2);
		v.moveToNextRoad(r1);
		v1.moveToNextRoad(r1);

		assertTrue("Fallo en moveToNextRoad", v.getRoad() == r1);

		v.setCurrentSpeed(10);
		v1.setCurrentSpeed(15);
		v.advance();
		v1.advance();
		assertTrue("No van a buena velocidad",
				v.getLocation() == 5 && v1.getLocation() == 15);

		v.setFaultyTime(2);
		v.advance();
		assertTrue("No hace lo que debería hacer con los estropeados",
				v.getFaultyTime() == 1);
	}
}
