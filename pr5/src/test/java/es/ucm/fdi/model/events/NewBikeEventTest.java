package es.ucm.fdi.model.events;

import static org.junit.Assert.*;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import es.ucm.fdi.control.RoadMap;
import es.ucm.fdi.model.simobject.Bike;
import es.ucm.fdi.model.simobject.Junction;
import es.ucm.fdi.model.simobject.Road;

public class NewBikeEventTest {
	@Test
	public void newBikeEventTest() {
		try {
			Map<String, String> test = new LinkedHashMap<>();
			test.put("time", "-1");
			test.put("id", "v1");
			test.put("max_speed", "10");
			test.put("itinerary", "j1,j2,j3");
			test.put("type", "bike");
			NewBikeEvent.Builder r = new NewBikeEvent.Builder();
			r.fill(test);
			fail("Se esperaba excepción por tiempo no válido\n");

		} catch (Exception e) {
		}
		try {

			Map<String, String> test = new LinkedHashMap<>();
			test.put("time", "0");
			test.put("id", "v-1");
			test.put("max_speed", "10");
			test.put("itinerary", "j1,j2,j3");
			test.put("type", "bike");
			NewBikeEvent.Builder r = new NewBikeEvent.Builder();
			r.fill(test);
			fail("Se esperaba excepción por id no válida\n");

		} catch (Exception e) {
		}
		try {

			Map<String, String> test = new LinkedHashMap<>();
			test.put("time", "0");
			test.put("id", "v1");
			test.put("max_speed", "-5");
			test.put("itinerary", "j1,j2,j3");
			test.put("type", "bike");
			NewBikeEvent.Builder r = new NewBikeEvent.Builder();
			r.fill(test);
			fail("Se esperaba excepción por velocidad no válida.\n");

		} catch (Exception e) {
		}
		try {

			Map<String, String> test = new LinkedHashMap<>();
			test.put("time", "0");
			test.put("id", "v1");
			test.put("max_speed", "10");
			test.put("itinerary", "j1,j2,j3");
			test.put("type", "bike");
			NewBikeEvent.Builder r = new NewBikeEvent.Builder();
			Event e = r.fill(test);
			RoadMap s = new RoadMap();
			Junction J1=new Junction("j1");
			J1.newOutgoing(new Road("r1",10,5,J1,new Junction("j2")));
			s.addJunction(J1);
			s.addJunction(new Junction("j2"));
			s.addJunction(new Junction("j3"));
			e.execute(s);
		} catch (Exception e) {
			fail("no se esperaba excepción.\n");
		}
		try {

			Map<String, String> test = new LinkedHashMap<>();
			test.put("time", "0");
			test.put("id", "v1");
			test.put("max_speed", "10");
			test.put("itinerary", "j1,j2,j3");
			test.put("type", "bike");
			NewBikeEvent.Builder r = new NewBikeEvent.Builder();
			Event e = r.fill(test);
			RoadMap s = new RoadMap();
			s.addJunction(new Junction("j1"));
			s.addJunction(new Junction("j2"));
			s.addJunction(new Junction("j3"));
			List<Junction> itinerary = new ArrayList<>();
			itinerary.add(new Junction("j5"));
			itinerary.add(new Junction("j6"));
			itinerary.add(new Junction("j7"));
			Bike v = new Bike(5, itinerary, "v1", "bike");
			s.addVehicle(v);
			e.execute(s);
			fail("Se esperaba fallo por existir id\n");
		} catch (Exception e) {
		}
	}

}
