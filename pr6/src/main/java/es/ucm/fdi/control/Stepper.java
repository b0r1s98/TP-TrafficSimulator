package es.ucm.fdi.control;

/**
 * 
 * Runs three runnables in a new thread: one first,
 * another n times and when it ends, the last one
 *
 */
public class Stepper {
	
	private Runnable before;
	private Runnable during;
	private Runnable after;
	
	private boolean stopRequested = false;
	private int steps;
	
	/**
	 * Class constructor
	 * @param before	Runnable executed at the beginning on start
	 * @param during	Runnable executed n times on start
	 * @param after		Runnable executed at the end on start
	 */
	public Stepper(Runnable before, Runnable during, Runnable after) {
		this.before = before;
		this.during = during;
		this.after = after;
	}

	/**
	 * Creates a Thread and executes the Runnables
	 * @param steps	how many times during runnable will be executed
	 * @param delay	time waited between during runnable executions
	 * @return		the thread created
	 */
	public Thread start(int steps, int delay) {
		this.steps = steps;
		this.stopRequested = false;
		
		Thread t = new Thread(() -> {
			try {
				before.run();
				while (!stopRequested && Stepper.this.steps > 0) {
					during.run();
					try {
						Thread.sleep(delay);
					} catch (InterruptedException ie) {
						
					}
					Stepper.this.steps--;
				}
			} finally {
				after.run();
			}
		});
		t.start();
		return t;
	}
	
	/**
	 * Stops Thread
	 */
	public void stop() {
		stopRequested = true;
	}

}
