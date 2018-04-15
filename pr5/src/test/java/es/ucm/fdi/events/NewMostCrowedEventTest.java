package es.ucm.fdi.events;

import static org.junit.Assert.fail;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import es.ucm.fdi.controller.RoadMap;
import es.ucm.fdi.simobject.Junction;

public class NewMostCrowedEventTest {
	@Test
	public void newMostCrowedEventTest() {
		try {
			Map<String, String> test = new LinkedHashMap<>();
			test.put("time", "-1");
			test.put("id", "j1");
			test.put("type", "mc");
			NewMostCrowedEvent.Builder r = new NewMostCrowedEvent.Builder();
			r.fill(test);
			fail("Se esperaba excepción por tiempo no válido\n");

		} catch (Exception e) {
		}
		try {

			Map<String, String> test = new LinkedHashMap<>();
			test.put("time", "0");
			test.put("id", "j-1");
			test.put("type", "mc");
			NewMostCrowedEvent.Builder r = new NewMostCrowedEvent.Builder();
			r.fill(test);
			fail("Se esperaba excepción por id no válida\n");

		} catch (Exception e) {
		}

		try {

			Map<String, String> test = new LinkedHashMap<>();
			test.put("time", "0");
			test.put("id", "j1");
			test.put("type", "mc");
			NewMostCrowedEvent.Builder r = new NewMostCrowedEvent.Builder();
			Event e = r.fill(test);
			RoadMap s = new RoadMap();
			e.execute(s);
		} catch (Exception e) {
			fail("no se esperaba excepción.\n");
		}
		try {

			Map<String, String> test = new LinkedHashMap<>();
			test.put("time", "0");
			test.put("id", "j1");
			test.put("type", "mc");
			NewMostCrowedEvent.Builder r = new NewMostCrowedEvent.Builder();
			Event e = r.fill(test);
			RoadMap s = new RoadMap();
			s.addJunction(new Junction("j1"));
			e.execute(s);
			fail("Se esperaba fallo por existir id\n");
		} catch (Exception e) {
		}
	}

}
