package es.ucm.fdi.events;

import static org.junit.Assert.fail;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import es.ucm.fdi.controller.RoadMap;
import es.ucm.fdi.simobject.Junction;

public class NewHighwayEventTest {
	@Test
	public void newHighwayEventTest() {
		try {
			Map<String, String> test = new LinkedHashMap<>();
			test.put("time", "-1");
			test.put("id", "r1");
			test.put("max_speed", "10");
			test.put("src", "j1");
			test.put("dest", "j2");
			test.put("length", "30");
			test.put("typè", "lanes");
			test.put("lanes", "2");
			NewHighwayEvent.Builder r = new NewHighwayEvent.Builder();
			r.fill(test);
			fail("Se esperaba excepción por tiempo no válido\n");

		} catch (Exception e) {
		}
		try {

			Map<String, String> test = new LinkedHashMap<>();
			test.put("time", "0");
			test.put("id", "r-1");
			test.put("max_speed", "10");
			test.put("src", "j1");
			test.put("dest", "j2");
			test.put("length", "30");
			test.put("typè", "lanes");
			test.put("lanes", "2");
			NewHighwayEvent.Builder r = new NewHighwayEvent.Builder();
			r.fill(test);
			fail("Se esperaba excepción por id no válida\n");

		} catch (Exception e) {
		}
		try {

			Map<String, String> test = new LinkedHashMap<>();
			test.put("time", "0");
			test.put("id", "r1");
			test.put("max_speed", "-5");
			test.put("src", "j1");
			test.put("dest", "j2");
			test.put("length", "30");
			test.put("typè", "lanes");
			test.put("lanes", "2");
			NewHighwayEvent.Builder r = new NewHighwayEvent.Builder();
			r.fill(test);
			fail("Se esperaba excepción por velocidad no válida.\n");

		} catch (Exception e) {
		}
		try {

			Map<String, String> test = new LinkedHashMap<>();
			test.put("time", "0");
			test.put("id", "r1");
			test.put("max_speed", "10");
			test.put("src", "j1");
			test.put("dest", "j2");
			test.put("length", "30");
			test.put("typè", "lanes");
			test.put("lanes", "2");
			NewHighwayEvent.Builder r = new NewHighwayEvent.Builder();
			Event e = r.fill(test);
			RoadMap s = new RoadMap();
			s.addJunction(new Junction("j1"));
			s.addJunction(new Junction("j2"));
			s.addJunction(new Junction("j3"));
			e.execute(s);
		} catch (Exception e) {
			fail("no se esperaba excepción.\n");
		}
	}

}
