package es.ucm.fdi.model.simobject;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

public class JunctionTest {
	@Test
	public void junctionTest() {
		List<Junction> itinerary = new ArrayList<>();
		itinerary.add(new Junction("j1"));
		itinerary.add(new Junction("j2"));
		itinerary.add(new Junction("j3"));
		Vehicle v = new Vehicle(5, itinerary, "v1");
		Vehicle v1 = new Vehicle(15, itinerary, "v2");
		Vehicle v2 = new Vehicle(15, itinerary, "v3");
		Road r1 = new Road("r1", 30, 10, itinerary.get(0), itinerary.get(1));
		Road r2 = new Road("r2", 15, 20, itinerary.get(1), itinerary.get(2));
		itinerary.get(1).newIncoming(r1);
		itinerary.get(1).newOutgoing(r2);
		itinerary.get(2).newIncoming(r2);
		v.moveToNextRoad(r1);
		v1.moveToNextRoad(r1);
		v2.moveToNextRoad(r1);
		Junction test = itinerary.get(1);
		test.newVehicle(v);
		test.newVehicle(v1);
		test.newVehicle(v2);
		test.avanza();
		assertTrue("El método avanza no funciona bien",
				v.getRoad() == r2 && v1.getRoad() == r1 && v2.getRoad() == r1);
		test.avanza();
		assertTrue("El método avanza no funciona bien", v1.getRoad() == r2
				&& v2.getRoad() == r1);
		test = itinerary.get(2);
		test.newVehicle(v);
		test.newVehicle(v1);
		test.avanza();
		assertTrue("No hace que un vehículo llegue",
				v.getArrived() && !v1.getArrived());
	}

}
