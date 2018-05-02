package es.ucm.fdi.model.simobject;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Test;

public class RoadTest {
	@Test
	public void roadTest() {
		List<Junction> itinerary = new ArrayList<>();
		itinerary.add(new Junction("j1"));
		itinerary.add(new Junction("j2"));
		itinerary.add(new Junction("j3"));
		Vehicle v1 = new Vehicle(15, itinerary, "v2");
		Road r1 = new Road("r1", 30, 10, itinerary.get(0), itinerary.get(1));
		Road r2 = new Road("r2", 15, 20, itinerary.get(1), itinerary.get(2));
		itinerary.get(1).newIncoming(r1);
		itinerary.get(1).newOutgoing(r2);
		v1.moveToNextRoad(r1);
		r1.advance();
		assertTrue("Los vehiculos no van a velocidad adecuada",
				 v1.getLocation() == 10);
		v1.setFaultyTime(2);
		r1.advance();
		assertTrue("El vehículo se mueve aún estando estropeado",
				v1.getLocation() ==10);

	}

}
