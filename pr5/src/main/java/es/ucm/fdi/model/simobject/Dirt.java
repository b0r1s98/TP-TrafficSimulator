package es.ucm.fdi.model.simobject;

import java.util.Map;

import es.ucm.fdi.util.MultiTreeMap;

public class Dirt extends Road {
	
	protected String type;

	public Dirt(String ide, int lon, int maxv, Junction princ, Junction fin, String type) {
		super(ide, lon, maxv, princ, fin);
		this.type = type;
	}
	
	public int calcularVelBase() {
		return maxVel;
	}
	
	public void avanza() {
		MultiTreeMap<Integer, Vehicle> nuevos = new MultiTreeMap<>((a, b) -> b-a);
		int velocidadBase = calcularVelBase();
		int factorReduccion = 1;
		for (Vehicle v : vehicles.innerValues()) {
			if(v.getTiempoAveria() != 0){
				factorReduccion++;
			}
			if(v.getLocation() < longitud){
				if (v.getTiempoAveria() == 0)
					v.setVelocidadActual(velocidadBase / factorReduccion);
				v.avanza();
			}
			nuevos.putValue(v.getLocation(), v);
		}
		vehicles = nuevos;
	}
	
	public void fillReportDetails(Map<String, String> out) {
		out.put("type", type);
		super.fillReportDetails(out);
	}
}
