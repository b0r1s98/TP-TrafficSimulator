package es.ucm.fdi.view;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.ucm.fdi.ini.Ini;
import es.ucm.fdi.ini.IniSection;

/**
 * 
 * Templates loader
 *
 */
public class Templates {
	
	private List<IniSection> list;

	private static final Logger logger =
			Logger.getLogger(Templates.class.getName());

	/**
	 * Class constructor
	 * @throws IOException if input exception occurs
	 */
	public Templates() throws IOException {
		loadTemplates();
	}
	
	/**
	 * Loads templates file
	 * 
	 * @throws IOException if input exception occurs
	 */
	private void loadTemplates() throws IOException {
		logger.info("Loading templates file");
		Ini ini = new Ini(this.getClass().getClassLoader().getResourceAsStream("templates.ini"));
		list = ini.getSections();
	}
	
	/**
	 * @param name
	 * @return 		string containing template whose name is name
	 */
	public String getTemplate(String name) {
		logger.info("Getting template");
		String s = "";
		for(IniSection i : list) {
			Map<String, String> map = i.getKeysMap();
			if(map.get("template").equals(name)){
				List<String> keys = i.getKeys();
				Map<String,String> newMap = new LinkedHashMap<>();
				keys.forEach(k -> newMap.put(k, map.get(k)));
				newMap.remove("template");
				s = generateTemplate(newMap, i.getTag());
			}
		}
		if("".equals(s)) logger.warning("Template wasn't found");
		return s;
	}
	
	/**
	 * Converts a template map to string
	 * @param map 	map containing each row
	 * @param title	title of the template
	 * @return		string containing template
	 */
	private String generateTemplate(Map<String,String> map, String title) {
		logger.info("Loading each template");
		OutputStream out = new ByteArrayOutputStream();
		IniSection section = new IniSection(title);
		map.forEach((k, v) -> section.setValue(k, v));
		try {
			section.store(out);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Error loading template (should not happen)", e);
		}
		return out.toString();
	}

}
