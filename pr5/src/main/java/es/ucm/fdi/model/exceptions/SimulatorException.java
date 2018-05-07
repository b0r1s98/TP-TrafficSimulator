package es.ucm.fdi.model.exceptions;

@SuppressWarnings("serial")

/**
 * 
 * Exception thrown when a simulation error occurs
 *
 */
public class SimulatorException extends RuntimeException {

	public SimulatorException() {
		// TODO Auto-generated constructor stub
	}

	public SimulatorException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public SimulatorException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public SimulatorException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public SimulatorException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
