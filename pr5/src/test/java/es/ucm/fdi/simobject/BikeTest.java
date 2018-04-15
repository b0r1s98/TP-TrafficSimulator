package es.ucm.fdi.simobject;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class BikeTest {
	@Test
	public void bikeTest() {
		List<Junction> itinerary = new ArrayList<>();
		itinerary.add(new Junction("j1"));
		itinerary.add(new Junction("j2"));
		itinerary.add(new Junction("j3"));
		Bike v = new Bike(10, itinerary, "v1", "bike");
		Bike v1 = new Bike(30, itinerary, "v2", "bike");
		v.setVelocidadActual(10);
		v1.setVelocidadActual(2);
		v.setTiempoAveria(1);
		v1.setTiempoAveria(1);

		assertTrue("No pone bien el tiempo de averia",v1.getTiempoAveria()==0);
		assertTrue("No pone bien el tiempo de averia", v.getTiempoAveria()!=0);
	}

}
