package es.ucm.fdi.model.events;

import static org.junit.Assert.fail;


import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import es.ucm.fdi.control.RoadMap;
import es.ucm.fdi.model.simobject.Junction;

public class NewJunctionEventTest {
	@Test
	public void newJunctionEventTest() {
		try {
			Map<String, String> test = new LinkedHashMap<>();
			test.put("time", "-1");
			test.put("id", "j1");
			NewJunctionEvent.Builder r = new NewJunctionEvent.Builder();
			r.parse(test);
			fail("Se esperaba excepción por tiempo no válido\n");

		} catch (Exception e) {
		}
		try {

			Map<String, String> test = new LinkedHashMap<>();
			test.put("time", "0");
			test.put("id", "j-1");
			NewJunctionEvent.Builder r = new NewJunctionEvent.Builder();
			r.parse(test);
			fail("Se esperaba excepción por id no válida\n");

		} catch (Exception e) {
		}

		try {

			Map<String, String> test = new LinkedHashMap<>();
			test.put("time", "0");
			test.put("id", "j1");
			NewJunctionEvent.Builder r = new NewJunctionEvent.Builder();
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
			NewJunctionEvent.Builder r = new NewJunctionEvent.Builder();
			Event e = r.parse(test);
			RoadMap s = new RoadMap();
			s.addJunction(new Junction("j1"));
			e.execute(s);
			fail("Se esperaba fallo por existir id\n");
		} catch (Exception e) {
		}
	}

}
