package es.ucm.fdi.model;

import java.util.Map;


/**
 * 
 * Interface that an object must implement
 * to be shown in a SimTable.
 *
 */
public interface Describable {
	
	/**
	 * Map to be filled with the data to be shown in a table
	 * @param out	map to be filled
	 */
	void describe(Map<String, String> out);
}