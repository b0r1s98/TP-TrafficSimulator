package es.ucm.fdi.model.simobject;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

public class MostCrowedTest {
	@Test
	public void mostCrowedTest() {
		List<Junction> itinerary = new ArrayList<>();
		List<Junction> itinerary2 = new ArrayList<>();
		Junction j0 = new Junction("j0");
		MostCrowed j1 = new MostCrowed("j1", "mc");
		itinerary.add(j1);
		itinerary.add(new Junction("j2"));
		itinerary.add(new Junction("j3"));
		itinerary2 = itinerary;
		itinerary2.remove(itinerary.get(0));
		itinerary2.add(0, j0);
		Vehicle c = new Vehicle(10, itinerary2, "v4");
		Vehicle c1 = new Vehicle(5, itinerary2, "v5");
		Vehicle v = new Vehicle(10, itinerary, "v1");
		Vehicle v1 = new Vehicle(15, itinerary, "v2");
		Vehicle v2 = new Vehicle(15, itinerary, "v3");
		Road r1 = new Road("r1", 30, 10, itinerary.get(0), itinerary.get(1));
		Road r2 = new Road("r2", 15, 20, itinerary.get(1), itinerary.get(2));
		Road r3 = new Road("r3", 20, 20, j0, itinerary.get(1));
		itinerary.get(1).newIncoming(r3);
		itinerary.get(1).newIncoming(r1);
		itinerary.get(1).newOutgoing(r2);
		itinerary.get(2).newIncoming(r2);
		c.moveToNextRoad(r3);
		c1.moveToNextRoad(r3);
		v.moveToNextRoad(r1);
		v1.moveToNextRoad(r1);
		v2.moveToNextRoad(r1);
		r3.advance();
		r3.advance();
		r1.advance();
		r1.advance();
		j1.advance();
		assertTrue("No se ha puesto el primer semáforo en verde bien",
				v1.getRoad() == r1);
		j1.advance();
		assertTrue("No ha cambiado el semáforo", c.getRoad() == r3);
		j1.advance();
		assertTrue("No ha cambiado el semáforo", v2.getRoad() == r1);

	}

}
