package es.ucm.fdi.simobject;

import java.util.Map;

public class MostCrowed extends Junction {
	
	protected String type;

	public MostCrowed(String id, String type) {
		super(id);
		this.type = type;
	}
	
	public void newIncoming(Road r) {
		IncomingRoad ir = new IncomingRoad(r.getId());
		saberInc.put(r, ir);
		entrantes.add(ir);
		avanzaSemaforo();
	}

	public void avanza() {
		if (!entrantes.isEmpty()) {
			IncomingRoad roadGreen = (IncomingRoad) entrantes.get(semaforo);
			if (!roadGreen.cola.isEmpty()) {
				Vehicle lucky = roadGreen.cola.getFirst();
				lucky.getRoad().removeVehicle(lucky);
				roadGreen.cola.pop();
				moveVehicleToNextRoad(lucky);
			}
			
			roadGreen.timeUnitsUsed++;
			
			if (roadGreen.timeUnitsUsed == roadGreen.timeInterval) 
				avanzaSemaforo();
		}
	}
	
	protected void avanzaSemaforo(){
		IncomingRoad roadGreen = (IncomingRoad) entrantes.get(semaforo);
		roadGreen.semaforoVerde = false;
		
		IncomingRoad moreVehicles = roadGreen;
		int max = semaforo;
		for(int i = 0; i<entrantes.size(); i++){
			IncomingRoad r = (IncomingRoad) entrantes.get(i);
			if((r.cola.size() > moreVehicles.cola.size() && i!=semaforo) || max == semaforo){
				moreVehicles = r;
				max = i;
			}
		}
		
		semaforo = max;
		moreVehicles.semaforoVerde = true;
		moreVehicles.timeInterval = Math.max(moreVehicles.cola.size() / 2, 1);
		moreVehicles.timeUnitsUsed = 0;
	}
	
	protected void fillReportDetails(Map<String, String> out) {
		super.fillReportDetails(out);
		out.put("type", type);
	}

	protected class IncomingRoad extends Junction.IncomingRoad {

		protected int timeInterval;
		protected int timeUnitsUsed;

		public IncomingRoad(String r) {
			super(r);
			this.timeInterval = 0;
			this.timeUnitsUsed = 0;
		}

		protected String semaforoReport() {
			StringBuilder r = new StringBuilder();
			r.append(super.semaforoReport());
			if (semaforoVerde)
				r.append(":" + (timeInterval - timeUnitsUsed));
			return r.toString();
		}
	}
}
