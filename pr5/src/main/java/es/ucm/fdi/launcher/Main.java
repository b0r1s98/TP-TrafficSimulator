package es.ucm.fdi.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import es.ucm.fdi.control.Controller;
import es.ucm.fdi.control.TrafficSimulator;
import es.ucm.fdi.control.TrafficSimulator.UpdateEvent;
import es.ucm.fdi.ini.Ini;
import es.ucm.fdi.ini.IniError;
import es.ucm.fdi.view.SimWindow;

/**
 * 
 * Parses initial arguments and starts correct mode.
 *
 */
public class Main {

	private final static Integer _timeLimitDefaultValue = 10;
	private static Integer _timeLimit = null;
	private static String _inFile = null;
	private static String _outFile = null;
	private static String _mode = null;
	
	private static final Logger logger =
			Logger.getLogger(Main.class.getName());
	
	/**
	 * Redirects logger to a file and sets its level to level
	 * @param level	the level to be set to log
	 */
	public static void setupLogging(Level level) {
		Logger log = Logger.getLogger("");
		for (Handler h : log.getHandlers()) {
			log.removeHandler( h );
		}
		FileHandler ch;
		try {
			ch = new FileHandler("log.txt");
			ch.setFormatter(new SimpleFormatter() {
				@Override
				public synchronized String format(LogRecord record) {
					return record.getMessage() + "\n";
				}
			});
			log.addHandler(ch);
			log.setLevel(level);
			ch.setLevel(level);
		} catch (SecurityException | IOException e) {
			log.setLevel(Level.OFF);
		}
	}

	private static void parseArgs(String[] args) {

		// define the valid command line options
		//
		Options cmdLineOptions = buildOptions();

		// parse the command line as provided in args
		//
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(cmdLineOptions, args);
			parseHelpOption(line, cmdLineOptions);
			parseLaunchMode(line);
			parseInFileOption(line);
			parseOutFileOption(line);
			parseStepsOption(line);

			// if there are some remaining arguments, then something wrong is
			// provided in the command line!
			//
			String[] remaining = line.getArgs();
			if (remaining.length > 0) {
				String error = "Illegal arguments:";
				for (String o : remaining)
					error += (" " + o);
				throw new ParseException(error);
			}

		} catch (ParseException e) {
			// new Piece(...) might throw GameError exception
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}

	}

	private static Options buildOptions() {
		Options cmdLineOptions = new Options();

		cmdLineOptions.addOption(Option.builder("h").longOpt("help").desc("Print this message").build());
		cmdLineOptions.addOption(Option.builder("i").longOpt("input").hasArg().desc("Events input file").build());
		cmdLineOptions.addOption(Option.builder("m").longOpt("mode").hasArg()
				.desc("’batch’ for batch mode and ’gui’ for GUI mode (default value is ’batch’)").build());
		cmdLineOptions.addOption(
				Option.builder("o").longOpt("output").hasArg().desc("Output file, where reports are written").build());
		cmdLineOptions.addOption(Option.builder("t").longOpt("ticks").hasArg()
				.desc("Ticks to execute the simulator's main loop (default value is " + _timeLimitDefaultValue + ")")
				.build());

		return cmdLineOptions;
	}

	private static void parseHelpOption(CommandLine line, Options cmdLineOptions) {
		if (line.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(Main.class.getCanonicalName(), cmdLineOptions, true);
			System.exit(0);
		}
	}

	private static void parseInFileOption(CommandLine line) throws ParseException {
		_inFile = line.getOptionValue("i");
		if (_inFile == null && !"gui".equals(_mode)) {
			throw new ParseException("An events file is missing");
		}
	}

	private static void parseOutFileOption(CommandLine line) throws ParseException {
		_outFile = line.getOptionValue("o");
	}

	private static void parseStepsOption(CommandLine line) throws ParseException {
		String t = line.getOptionValue("t");
		if(!"gui".equals(_mode)){
			if(t == null) t = _timeLimitDefaultValue.toString();
			try {
				_timeLimit = Integer.parseInt(t);
				assert (_timeLimit < 0);
			} catch (Exception e) {
				throw new ParseException("Invalid value for time limit: " + t);
			}
		}
	}
	
	private static void parseLaunchMode(CommandLine line) throws ParseException {
		_mode = line.getOptionValue("m", "batch");
		
		if(!"batch".equals(_mode) && !"gui".equals(_mode))
			throw new ParseException("Invalid value for mode");
	}

	/**
	 * This method run the simulator on all files that ends with .ini if the given
	 * path, and compares that output to the expected output. It assumes that for
	 * example "example.ini" the expected output is stored in "example.ini.eout".
	 * The simulator's output will be stored in "example.ini.out"
	 * 
	 * @throws IOException
	 */
	public static void test(String path) throws IOException {
		setupLogging(Level.OFF);

		File dir = new File(path);

		if ( !dir.exists() ) {
			throw new FileNotFoundException(path);
		}
		
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".ini");
			}
		});
		
		for (File file : files) {
			test(file.getAbsolutePath(), file.getAbsolutePath() + ".out", file.getAbsolutePath() + ".eout",10);
		}

	}

	private static void test(String inFile, String outFile, String expectedOutFile, int timeLimit) throws IOException {
		_outFile = outFile;
		_inFile = inFile;
		_timeLimit = timeLimit;
		startBatchMode();
		boolean equalOutput = (new Ini(_outFile)).equals(new Ini(expectedOutFile));
		System.out.println("Result for: '" + _inFile + "' : "
				+ (equalOutput ? "OK!" : ("not equal to expected output +'" + expectedOutFile + "'")));
	}

	/**
	 * Run the simulator in batch mode
	 */
	private static void startBatchMode() {
		try {
			OutputStream out = System.out;
			if(_outFile != null) {
				try {
					out = new FileOutputStream(_outFile);
				} catch (FileNotFoundException e) {
					String s = "Output file doesn't exist, reports will be redirected to console.";
					System.err.println(s);
					logger.info(s);
				}
			}
			
			if(_timeLimit == null) {
				_timeLimit = _timeLimitDefaultValue;
			}
			Controller control = new Controller();
			control.getSimulator().addSimulatorListener( new TrafficSimulator.SimulatorListener() {
				
				@Override
				public void update(UpdateEvent ue, String error) {
					switch(ue.getEvent()){
					case ERROR:
						System.err.print("Simulation error: ");
						System.err.println(error);
						break;
					default:
						break;
					}
				}
			});
			
			InputStream in;
			try {
				in = new FileInputStream(_inFile);
			} catch (FileNotFoundException e) {
				throw new Exception("Input file doesn't exist", e);
			}
			
			try {
				control.loadEvents(in);
			} catch (IOException | IniError | IllegalArgumentException e) {
				throw new Exception("Error loading events from file "+_inFile, e);
			}
			
			try {
				control.getSimulator().run(_timeLimit, out);
			} catch (IOException e) {
				throw new Exception("Error reporting", e);
			}
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
			logger.log(Level.SEVERE, "Fatal error", e);
		}
		
		logger.info("Exiting...");
	}
	
	/**
	 * Runs the simulator in GUI mode
	 */
	private static void startGUIMode() {
		Controller control = new Controller();
		File input = (_inFile != null) ? new File(_inFile) : null;
		logger.info("Launching GUI");
		SwingUtilities.invokeLater(() -> new SimWindow(control, input, _timeLimit));
	}

	private static void start(String[] args) {
		parseArgs(args);
		setupLogging(Level.INFO);
		if("gui".equals(_mode)) {
			startGUIMode();
		}
		else {
			startBatchMode();
		}
	}

	public static void main(String[] args) throws InvocationTargetException, InterruptedException {

		// example command lines:
		//
		// -i resources/examples/events/basic/ex1.ini
		// -i resources/examples/events/basic/ex1.ini -o ex1.out
		// -i resources/examples/events/basic/ex1.ini -t 20
		// -i resources/examples/events/basic/ex1.ini -o ex1.out -t 20
		// --help
		//

		// Call test in order to test the simulator on all examples in a directory.
		//
	    	//test("src/test/resources/examples/basic");
	    	//test("src/test/resources/examples/err");
	    	//test("src/test/resources/examples/advanced");

		// Call start to start the simulator from command line, etc.
		start(args);

	}

}
