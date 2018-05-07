package es.ucm.fdi.model.simobject;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 
 * A SimObject called Car.
 *
 */
public class Car extends Vehicle {

	protected String type;
	protected int resistanceKm;
	protected int kmSinceFault;
	protected double faultProbability;
	protected int maxFaultDuration;
	protected Random numRand;

	/**
	 * Class constructor
	 * 
	 * @param speedMax			maximum speed
	 * @param itinerary			array of the junctions the vehicle must pass through
	 * @param id				name of the vehicle
	 * @param type				string containing "car"
	 * @param resistanceKm		minimum km without being faulty
	 * @param faultProbability	probability of getting faulty
	 * @param maxFaultDuration	maximum ticks it can be faulty
	 * @param seed				seed used to calculate probability
	 */
	public Car(int speedMax, List<Junction> itinerary, String id, String type,
			int resistanceKm, double faultProbability, int maxFaultDuration,
			long seed) {
		super(speedMax, itinerary, id);
		this.type = type;
		this.resistanceKm = resistanceKm;
		this.kmSinceFault = 0;
		this.faultProbability = faultProbability;
		this.maxFaultDuration = maxFaultDuration;
		this.numRand = new Random(seed);
	}

	@Override
	public void advance() {
		if (faultyTime == 0 && kmSinceFault > resistanceKm
				&& numRand.nextDouble() < faultProbability) {
			setFaultyTime(numRand.nextInt(maxFaultDuration) + 1);
		}

		if (faultyTime > 0) {
			faultyTime--;
			kmSinceFault = 0;
		} else {
			if (location + speed >= current.getLength()) {
				kilometers += current.getLength() - location;
				kmSinceFault += current.getLength() - location;
				location = current.getLength();
				speed = 0;

				current.getEnd().newVehicle(this);
			} else {
				kilometers += speed;
				kmSinceFault += speed;
				location += speed;
			}
		}
	}

	@Override
	public void fillReportDetails(Map<String, String> out) {
		out.put("type", type);
		super.fillReportDetails(out);
	}

}
