package es.ucm.fdi.simobject;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class Car extends Vehicle {

	protected String type;
	protected int resistanceKm;
	protected int kmSinceFault;
	protected double faultProbability;
	protected int maxFaultDuration;
	protected Random numRand;

	public Car(int velMax, List<Junction> cruc, String id, String type,
			int resistanceKm, double faultProbability, int maxFaultDuration,
			long seed) {
		super(velMax, cruc, id);
		this.type = type;
		this.resistanceKm = resistanceKm;
		this.kmSinceFault = 0;
		this.faultProbability = faultProbability;
		this.maxFaultDuration = maxFaultDuration;
		this.numRand = new Random(seed);
	}

	public void avanza() {
		if (tiempoAveria == 0 && kmSinceFault >= resistanceKm
				&& numRand.nextDouble() < faultProbability) {
			setTiempoAveria(numRand.nextInt(maxFaultDuration) + 1);
		}

		if (tiempoAveria > 0) {
			tiempoAveria--;
			kmSinceFault = 0;
		} else {
			if (localizacion + velActual >= actual.getLongitud()) {
				kilometrage += actual.getLongitud() - localizacion;
				kmSinceFault += actual.getLongitud() - localizacion;
				localizacion = actual.getLongitud();
				velActual = 0;

				actual.getFinal().newVehicle(this);
			} else {
				kilometrage += velActual;
				kmSinceFault += velActual;
				localizacion += velActual;
			}
		}
	}

	public void fillReportDetails(Map<String, String> out) {
		out.put("type", type);
		super.fillReportDetails(out);
	}

}
