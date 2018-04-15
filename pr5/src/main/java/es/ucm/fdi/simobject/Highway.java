package es.ucm.fdi.simobject;

import java.util.Map;

import es.ucm.fdi.util.MultiTreeMap;

public class Highway extends Road {
	
	protected String type;
	protected int numLanes;

	public Highway(String ide, int lon, int maxv, Junction princ, Junction fin, String type, int numLanes) {
		super(ide, lon, maxv, princ, fin);
		this.type = type;
		this.numLanes = numLanes;
	}
	
	public int calcularVelBase() {
		long n = vehicles.sizeOfValues();
		if (n < 1)
			n = 1;
		int value = (maxVel*numLanes / (int) n) + 1;
		if (maxVel < value)
			return maxVel;
		return value;
	}
	
	public void avanza() {
		MultiTreeMap<Integer, Vehicle> nuevos = new MultiTreeMap<>((a, b) -> b-a);
		int velocidadBase = calcularVelBase();
		int factorReduccion = 1;
		int numFaulties = 0;
		for (Vehicle v : vehicles.innerValues()) {
			if(factorReduccion == 1)
				if(v.getTiempoAveria() != 0){
					numFaulties++;
					if(numFaulties >= numLanes) 
						factorReduccion=2;
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
