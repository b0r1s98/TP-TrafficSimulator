package es.ucm.fdi.events;

import java.util.Map;

import es.ucm.fdi.controller.RoadMap;

public abstract class Event {
	private int time;

	public Event(int time) {
		this.time = time;
	}

	public int getTime() {
		return time;
	}

	public abstract void execute(RoadMap things);

	public interface Builder {

		public abstract boolean canParse(String title, String type);

		public abstract Event fill(Map<String, String> map);

		default boolean isValidId(String id) {
			return id.matches("[a-zA-Z0-9_]+");
		}

		default String checkId(Map<String, String> map) {
			String id = map.get("id");
			if (id == null)
				throw new IllegalArgumentException("Missing id");
			if (!isValidId(id))
				throw new IllegalArgumentException("Invalid id");
			return id;
		}

		default int checkNoNegativeIntOptional(String s, Map<String, String> map) {
			int check = (map.containsKey(s) ? Integer.parseInt(map.get(s)) : 0);
			if (check < 0)
				throw new IllegalArgumentException("Negative " + s);
			return check;
		}

		default int checkPositiveInt(String s, Map<String, String> map) {
			String num = map.get(s);
			if (num == null)
				throw new IllegalArgumentException("Missing " + s);
			int check = Integer.parseInt(num);
			if (check <= 0)
				throw new IllegalArgumentException("No positive " + s);
			return check;
		}

		default String checkContains(String s, Map<String, String> map) {
			if (!map.containsKey(s))
				throw new IllegalArgumentException("Missing " + s);
			return map.get(s);
		}

	}
}