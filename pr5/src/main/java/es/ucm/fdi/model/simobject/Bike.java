package es.ucm.fdi.model.simobject;

import java.util.List;
import java.util.Map;

public class Bike extends Vehicle {

	protected String type;

	public Bike(int velMax, List<Junction> cruc, String id, String type) {
		super(velMax, cruc, id);
		this.type = type;
	}

	public void setTiempoAveria(int n) {
		if(velActual > velMaxima/2){
			tiempoAveria += n;
			velActual = 0;
		}
	}
	
	public void fillReportDetails(Map<String, String> out) {
		out.put("type", type);
		super.fillReportDetails(out);
	}

}
