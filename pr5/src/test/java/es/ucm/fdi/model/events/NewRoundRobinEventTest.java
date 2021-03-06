package es.ucm.fdi.model.events;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import es.ucm.fdi.control.RoadMap;
import es.ucm.fdi.model.simobject.Junction;

public class NewRoundRobinEventTest {
	@Test
	public void newRoundRobinEventTest() {

		try {
			Map<String, String> test = new LinkedHashMap<>();
			test.put("time", "-1");
			test.put("id", "j1");
			test.put("type", "rr");
			test.put("max_time_slice", "10");
			test.put("min_time_slice", "5");

			NewRoundRobinEvent.Builder r = new NewRoundRobinEvent.Builder();
			r.parse(test);
			fail("Se esperaba excepción por tiempo no válido\n");

		} catch (Exception e) {
		}
		try {

			Map<String, String> test = new LinkedHashMap<>();
			test.put("time", "0");
			test.put("id", "j-1");
			test.put("type", "rr");
			test.put("max_time_slice", "10");
			test.put("min_time_slice", "5");

			NewRoundRobinEvent.Builder r = new NewRoundRobinEvent.Builder();
			r.parse(test);
			fail("Se esperaba excepción por id no válida\n");

		} catch (Exception e) {
		}

		try {

			Map<String, String> test = new LinkedHashMap<>();
			test.put("time", "0");
			test.put("id", "j1");
			test.put("type", "rr");
			test.put("max_time_slice", "10");
			test.put("min_time_slice", "5");

			NewRoundRobinEvent.Builder r = new NewRoundRobinEvent.Builder();
			Event e = r.parse(test);
			RoadMap s = new RoadMap();
			e.execute(s);
		} catch (Exception e) {
			fail("no se esperaba excepción.\n");
		}
		try {

			Map<String, String> test = new LinkedHashMap<>();
			test.put("time", "0");
			test.put("id", "j1");
			test.put("type", "rr");
			test.put("max_time_slice", "10");
			test.put("min_time_slice", "5");

			NewRoundRobinEvent.Builder r = new NewRoundRobinEvent.Builder();
			Event e = r.parse(test);
			RoadMap s = new RoadMap();
			s.addJunction(new Junction("j1"));
			e.execute(s);
			fail("Se esperaba fallo por existir id\n");
		} catch (Exception e) {
		}
	}
}
