package es.ucm.fdi.model.simobject;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

public class RoundRobinTest {
	@Test
	public void roundRobinTest() {
		List<Junction> itinerary = new ArrayList<>();
		List<Junction> itinerary2 = new ArrayList<>();
		Junction j0 = new Junction("j0");
		RoundRobin j1 = new RoundRobin("j1", 2, 4, "RoundRobin");
		Junction j2=new Junction("j2");
		Junction j3=new Junction("j3");
		itinerary.add(j0);
		itinerary.add(j1);
		itinerary.add(j2);
		itinerary2.add(j3);
		itinerary2.add(j1);
		itinerary2.add(j2);
		Vehicle c = new Vehicle(10, itinerary2, "v4");
		Vehicle c1 = new Vehicle(10, itinerary2, "v5");
		Vehicle v = new Vehicle(10, itinerary, "v1");
		Vehicle v1 = new Vehicle(15, itinerary, "v2");
		Vehicle v2 = new Vehicle(15, itinerary, "v3");
		Road r1 = new Road("r1", 30, 80, j0,j1);
		Road r2 = new Road("r2", 15, 80,j1, j2);
		Road r3 = new Road("r3", 20, 80, j3, j1);
		j1.newIncoming(r3);
		j1.newIncoming(r1);
		j1.addOutgoing(r2);
		j2.newIncoming(r2);
		c.moveToNextRoad(r3);
		c1.moveToNextRoad(r3);
		v.moveToNextRoad(r1);
		v1.moveToNextRoad(r1);
		v2.moveToNextRoad(r1);
		r1.advance();
		r1.advance();
		r1.advance();
		j1.advance();
		j1.advance();
		assertTrue("No espera 2 ciclos para cambiar el semáforo", v1.getRoad()==r2 && v2.getRoad()==r2);
		j1.newVehicle(c);
		j1.advance();
		assertTrue("Debería pasar el coche",c.getRoad()!=r2);
	}

}
