package es.ucm.fdi.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;

import es.ucm.fdi.control.Controller;
import es.ucm.fdi.control.RoadMap;
import es.ucm.fdi.control.SimulatorAction;
import es.ucm.fdi.control.TrafficSimulator.SimulatorListener;
import es.ucm.fdi.control.TrafficSimulator.UpdateEvent;
import es.ucm.fdi.ini.IniError;
import es.ucm.fdi.model.events.Event;

@SuppressWarnings("serial")
/**
 * 
 * Principal GUI window
 *
 */
public class SimWindow extends JPanel implements SimulatorListener{

	private JFileChooser fc;
	private JTextField time;
	private JSpinner cicles;
	private JCheckBoxMenuItem redirect;
	private OutputStream outText;
	private OutputStream outNull;
	private Templates templates;
	
	private JPopupMenu botonDer;
	private JTextArea eventsEditor;
	private SimTable eventsTable;
	private JTextArea reports;
	private SimTable vehiclesTable;
	private SimTable roadsTable;
	private SimTable junctionsTable;
	private GraphLayout graphRoadMap;
	private JLabel statusBar;
	
	private Controller controller;
	
	private static final Logger logger =
			Logger.getLogger(SimWindow.class.getName());
	
	/**
	 * Class constructor
	 * 
	 * @param controller	Controller that communicates with the TrafficSimulator
	 * @param file			initial file to take events from
	 * @param ticks			initial ticks to run simulator
	 */
	public SimWindow(Controller controller, File file, Integer ticks) {
		super();
		this.controller = controller;
		
		initGUI();
		controller.getSimulator().addSimulatorListener(this);
		
		if(file != null) {
			loadFromFile(file);
		}
		if(ticks != null) {
			setTime(ticks);
		}
	}

	/**
	 * Initiates GUI
	 */
	private void initGUI() {
		logger.info("Initializing GUI");
		JFrame jframe = new JFrame("Traffic Simulator");
		this.setLayout(new BorderLayout());
		jframe.setContentPane(this);

		fc = new JFileChooser();
		createActions();
		getAction(Command.Run).setEnabled(false);
		getAction(Command.Reset).setEnabled(false);
		jframe.setJMenuBar(createMenuBar());
		add(createJToolBar(), BorderLayout.PAGE_START);
		
		graphRoadMap = new GraphLayout();
		
		JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				createDownLeft(), graphRoadMap);
		bottomSplit.setResizeWeight(.5);

		JSplitPane topSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				createUp(), bottomSplit);
		
		this.add(topSplit, BorderLayout.CENTER);
		
		outText = new OutputStreamGUI(reports);
		outNull = new OutputStreamNull();
		
		statusBar = new JLabel("Ready!");
		statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
		jframe.add(statusBar, BorderLayout.PAGE_END);

		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setSize(1000, 1000);
		jframe.setVisible(true);
		topSplit.setDividerLocation(.5);
		bottomSplit.setDividerLocation(.5);
	}
	
	/**
	 * Inserts current roadMap and events to tables
	 * 
	 * @param events	current events in simulator
	 * @param roadmap	current roadMap in simulator
	 */
	private void connectTables(List<Event> events, RoadMap roadmap) {
		logger.info("Connecting tables");
		String[] eFields = { "#", "Time", "Type"};
		eventsTable.setModel(eFields, events);
		
		String[] vFields = {"ID", "Road", "Location", "Speed", "Km",
				"Faulty Units", "Itinerary" };
		vehiclesTable.setModel(vFields, roadmap.getVehicles());
		
		String[] rFields = { "ID", "Source", "Target", "Length", "Max Speed",
				"Vehicles" };
		roadsTable.setModel(rFields, roadmap.getRoads());
		
		String[] jFields = { "ID", "Green", "Red"};
		junctionsTable.setModel(jFields, roadmap.getJunctions());
	}

	/** 
	 * @return jpanel containing down left tables
	 */
	private JPanel createDownLeft() {
		logger.info("Creating Down Left Panel");
		JPanel panel = new JPanel(new GridLayout(3,1));
		
		vehiclesTable = new SimTable();
		panel.add(vehiclesTable.intoPanel("Vehicles"));
		
		roadsTable = new SimTable();
		panel.add(roadsTable.intoPanel("Roads"));
		
		junctionsTable = new SimTable();
		panel.add(junctionsTable.intoPanel("Junctions"));
		
		return panel;
	}

	/**
	 * @return jpanel containing up part of the GUI
	 */
	private JPanel createUp() {
		logger.info("Creating Up Panel");
		JPanel panel = new JPanel(new GridLayout(1,3));
		Border b = BorderFactory.createLineBorder(Color.black, 2);
		
		eventsEditor = new JTextArea();
		eventsEditor.setEnabled(true);
		eventsEditor.setLineWrap(true);
		eventsEditor.setWrapStyleWord(true);
		JPanel panelIzq = new JPanel(new BorderLayout());
		panelIzq.setBorder(BorderFactory.createTitledBorder(b, "Events: "));
		panelIzq.add(new JScrollPane(eventsEditor));
		panel.add(panelIzq);
		initiatePopUpMenu();
		eventsEditor.addMouseListener(new MouseListener() {
			@Override
			public void mousePressed(MouseEvent e) {
				showPopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				showPopup(e);
			}

			private void showPopup(MouseEvent e) {
				if (e.isPopupTrigger() && eventsEditor.isEnabled()) {
					botonDer.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}
		});
		
		eventsTable = new SimTable();
		panel.add(eventsTable.intoPanel("Events Queue"));

		reports = new JTextArea();
		reports.setEditable(false);
		reports.setLineWrap(true);
		reports.setWrapStyleWord(true);
		JPanel panelDer = new JPanel(new BorderLayout());
		panelDer.setBorder(BorderFactory.createTitledBorder(b, "Reports"));
		panelDer.add(new JScrollPane(reports));
		panel.add(panelDer);
		
		return panel;
	}
	
	/**
	 * 
	 * Enum with the names of all the actions
	 *
	 */
	enum Command {
		LoadEvents("Load Events"), SaveEvents("Save Events"), ClearEvents("Clear Events"),
		InsertEvents("Insert events"), Run("Run"), Reset("Reset"),
		GenerateReports("Generate"), ClearReports("Clear"),
		SaveReport("Save Report"), Exit("Exit");
		
		private String text;
		
		Command(String text) {
			this.text = text;
		}
		
		public String toString() {
			return text;
		}
	}
	
	/**
	 * Creates actions
	 */
	private void createActions(){
		logger.info("Creating actions");

		SimulatorAction loadEvents = new SimulatorAction(Command.LoadEvents,
				"open.png", "Cargar un fichero de eventos", KeyEvent.VK_L,
				"ctrl L", () -> loadFile());
		loadEvents.register(this);

		SimulatorAction saveEvents = new SimulatorAction(Command.SaveEvents,
				"save.png", "Guardar un fichero de eventos", KeyEvent.VK_A,
				"ctrl S", () -> saveFile(eventsEditor));
		saveEvents.register(this);

		SimulatorAction cleanEvents = new SimulatorAction(Command.ClearEvents,
				"clear.png", "Limpiar la zona de eventos", KeyEvent.VK_E,
				"alt E", () -> {
					eventsEditor.setText("");
					logger.info("Events cleaned");
				});
		cleanEvents.register(this);
		
		SimulatorAction insertEvents = new SimulatorAction(Command.InsertEvents,
				"events.png", "Insertar eventos en el simulador", KeyEvent.VK_I,
				"alt I", () -> addEvents());
		insertEvents.register(this);
		
		SimulatorAction play = new SimulatorAction(Command.Run,
				"play.png", "Ejecutar el simulador", KeyEvent.VK_U,
				"ctrl P", () -> runSimulator());
		play.register(this);
		
		SimulatorAction reset = new SimulatorAction(Command.Reset,
				"reset.png", "Reiniciar el simulador", KeyEvent.VK_T,
				"ctrl T", () -> controller.getSimulator().reset());
		reset.register(this);
		
		SimulatorAction generateReports = new SimulatorAction(Command.GenerateReports,
				"report.png", "Generar informes", KeyEvent.VK_G,
				"ctrl K", () -> generateReports());
		generateReports.register(this);
		
		SimulatorAction cleanReports = new SimulatorAction(Command.ClearReports,
				"delete_report.png", "Limpiar el área de informes", KeyEvent.VK_C,
				"ctrl U", () -> {
					reports.setText("");
					logger.info("Reports cleaned");
				});
		cleanReports.register(this);
		
		SimulatorAction saveReports = new SimulatorAction(Command.SaveReport,
				"save_report.png", "Guardar los informes", KeyEvent.VK_V,
				"ctrl R", () -> saveFile(reports));
		saveReports.register(this);

		SimulatorAction quit = new SimulatorAction(Command.Exit,
				"exit.png", "Salir de la aplicación", KeyEvent.VK_X,
				"control shift X", () -> {
					logger.info("Exiting...");
					System.exit(0);
				});
		quit.register(this);
	}

	/**
	 * Shortcut for getting an action from the ActionMap
	 * 
	 * @param c enum value of the action
	 * @return	corresponding action
	 */
	private Action getAction(Command c){
		return getActionMap().get(c.toString());
	}
	
	/**
	 * @return menu bar
	 */
	private JMenuBar createMenuBar() {
		logger.info("Creating Menu Bar");
		JMenuBar menuBar = new JMenuBar();

		JMenu file = new JMenu("File");
		menuBar.add(file);
		file.setMnemonic(KeyEvent.VK_F);

		file.add(getAction(Command.LoadEvents));
		file.add(getAction(Command.SaveEvents));
		file.addSeparator();
		file.add(getAction(Command.SaveReport));
		file.addSeparator();
		file.add(getAction(Command.Exit));

		JMenu simulator = new JMenu("Simulator");
		menuBar.add(simulator);
		simulator.setMnemonic(KeyEvent.VK_S);

		simulator.add(getAction(Command.Run));
		simulator.add(getAction(Command.Reset));
		redirect = new JCheckBoxMenuItem("Redirect Output");
		redirect.setSelected(true);
		redirect.setMnemonic(KeyEvent.VK_O);
		redirect.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
		simulator.add(redirect);
		
		JMenu reports = new JMenu("Reports");
		menuBar.add(reports);
		reports.setMnemonic(KeyEvent.VK_R);

		reports.add(getAction(Command.GenerateReports));
		reports.add(getAction(Command.ClearReports));
		
		return menuBar;
	}
	
	/**
	 * @return tool bar
	 */
	private JToolBar createJToolBar() {
		logger.info("Creating Tool Bar");
		JToolBar toolBar = new JToolBar();

		toolBar.add(getAction(Command.LoadEvents));
		toolBar.add(getAction(Command.SaveEvents));
		toolBar.add(getAction(Command.ClearEvents));
		toolBar.addSeparator();
		toolBar.add(getAction(Command.InsertEvents));
		toolBar.add(getAction(Command.Run));
		toolBar.add(getAction(Command.Reset));
		
		toolBar.add(new JLabel(" Steps: "));
		cicles = new JSpinner();
		((SpinnerNumberModel) cicles.getModel()).setMinimum(1);
		setTime(1);
		cicles.setToolTipText("Número de ciclos que avanzará el simulador");
		Dimension d = cicles.getPreferredSize();
		d.width = 100;
		cicles.setPreferredSize(d);
		toolBar.add(cicles);
		
		toolBar.add(new JLabel(" Time: "));
		time = new JTextField();
		time.setText("0");
		time.setEditable(false);
		time.setName("Time:");
		time.setToolTipText("Instante de la simuación");
		toolBar.add(time);
		
		toolBar.addSeparator();
		toolBar.add(getAction(Command.GenerateReports));
		toolBar.add(getAction(Command.ClearReports));
		toolBar.add(getAction(Command.SaveReport));
		toolBar.addSeparator();
		toolBar.add(getAction(Command.Exit));
		
		return toolBar;
	}

	/**
	 * Shows dialog and save text from JTextArea
	 * 
	 * @param area the JTextArea
	 */
	private void saveFile(JTextArea area) {
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			writeFile(file, area.getText());
		}
	}

	/**
	 * Shows dialog and loads file to JTextArea eventsEditor
	 */
	private void loadFile() {
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			eventsEditor.setText(readFile(file));
		}
	}

	/**
	 * Loads File
	 * 
	 * @param file	file to be loaded
	 * @return		string containing file's text
	 */
	private static String readFile(File file) {
		String s = "";
		try {
			s = new String(Files.readAllBytes(file.toPath()), "UTF-8");
			logger.info("File loaded");
		} catch (IOException e) {
			String msg = "File couldn't be loaded.";
			createAlert(msg);
			logger.log(Level.WARNING, msg, e);
		}
		return s;
	}

	/**
	 * Saves content to file.toPath()
	 * 
	 * @param file		gives file where to save content
	 * @param content	string to be saved
	 */
	private static void writeFile(File file, String content) {
		try {
			Files.write(file.toPath(), content.getBytes("UTF-8"));
			logger.info("File saved");
		} catch (IOException e) {
			String msg = "File couldn't be saved.";
			createAlert(msg);
			logger.log(Level.WARNING, msg, e);
		}
	}
	
	/**
	 * Loads text from file and writes it in eventsEditor
	 * 
	 * @param file	file loaded
	 */
	private void loadFromFile(File file) {
		eventsEditor.setText(readFile(file));
	}
	
	/**
	 * Sets time in JSpinner to ticks
	 * 
	 * @param ticks
	 */
	private void setTime(int ticks) {
		((SpinnerNumberModel) cicles.getModel()).setValue(ticks);
	}
	
	/**
	 * 
	 * OutputStream that writes into JTextArea
	 *
	 */
	private class OutputStreamGUI extends OutputStream{
		private JTextArea area;
		
		/**
		 * Class constructor
		 * 
		 * @param area	JTextArea where it'll write
		 */
		public OutputStreamGUI(JTextArea area) {
			super();
			this.area = area;
		}

		@Override
		public void write(int b) throws IOException {
	        area.append(""+(char)b);
	        area.setCaretPosition(area.getDocument().getLength());
		}
	}
	
	/**
	 * 
	 * OutputStream that writes nowhere
	 *
	 */
	private class OutputStreamNull extends OutputStream{

		@Override
		public void write(int arg0) throws IOException {
			
		}
		
	}
	
	/**
	 * Initiates right-click menu
	 */
	private void initiatePopUpMenu() {
		logger.info("Initializing right-click menu");
		botonDer = new JPopupMenu();
		JMenu addTemplate = new JMenu("Add Template");
		
		String[] templates = { "New RR Junction", "New MC Junction", "New Junction",
				"New Dirt Road", "New Lanes Road", "New Road", "New Bike", "New Car", 
				"New Vehicle", "Make Vehicle Faulty"};
		for (String s : templates) {
			JMenuItem menuItem = new JMenuItem(s);
			menuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					eventsEditor.insert(showTemplate(s), eventsEditor.getCaretPosition());
					eventsEditor.insert("\n", eventsEditor.getCaretPosition());
				}
			});
			addTemplate.add(menuItem);
		}
		
		botonDer.add(addTemplate);
		botonDer.addSeparator();
		botonDer.add(getAction(Command.LoadEvents));
		botonDer.add(getAction(Command.SaveEvents));
		botonDer.add(getAction(Command.ClearEvents));
	}
	
	/**
	 * @param s
	 * @return 	string containing template with name s
	 * 			(but with '_' instead of ' ')
	 */
	private String showTemplate(String s) {
		String name = String.join("_", s.split(" "));
		
		if(templates == null) {
			try {
				templates = new Templates();
			} catch (IOException e) {
				String msg = "Error loading templates";
				logger.log(Level.WARNING, msg, e);
				createAlert(msg);
			}
		}
		return templates.getTemplate(name);
	}
	
	/**
	 * Creates an alert message with message
	 * 
	 * @param message string to be shown on the alert
	 */
	private static void createAlert(String message) {
		JOptionPane.showMessageDialog(null, message, "Warning", JOptionPane.WARNING_MESSAGE);
	}
	
	/**
	 * Generates reports of selected ids on tables
	 */
	private void generateReports() {
		logger.info("Getting selected ids on tables");
		Set<String> ids = new HashSet<>();
		ids.addAll(junctionsTable.getSelectedIds());
		ids.addAll(roadsTable.getSelectedIds());
		ids.addAll(vehiclesTable.getSelectedIds());
		OutputStream out = redirect.isSelected() ? outText : outNull;
		try {
			reports.setText("");
			controller.getSimulator().generateReport(out, ids);
		} catch (IOException e) {
			String msg = "Couldn't create reports";
			logger.log(Level.WARNING, msg, e);
			createAlert(msg);
		}
	}
	
	/**
	 * Adds events to simulator from eventsEditor
	 */
	private void addEvents(){
		controller.getSimulator().removeEvents();
		controller.getSimulator().reset();
		try {
			controller.loadEvents(new ByteArrayInputStream(eventsEditor.getText().getBytes()));
		} catch (IOException | IniError | IllegalArgumentException e) {
			String msg = "Incorrect event";
			logger.log(Level.WARNING, msg, e);
			createAlert("Incorrect event\n"+e.getMessage());
			statusBar.setText(msg);
		}
		getAction(Command.Run).setEnabled(true);
		getAction(Command.Reset).setEnabled(true);
	}
	
	/**
	 * Runs simulator with the corresponding ticks and outputstream
	 */
	private void runSimulator(){
		int ticks = ((SpinnerNumberModel) cicles.getModel()).getNumber().intValue();
		OutputStream out = redirect.isSelected() ? outText : outNull;
		try {
			controller.getSimulator().run(ticks, out);
		} catch (IOException e) {
			String msg = "Error generating report";
			logger.log(Level.WARNING, msg + " (should not happen)", e);
			createAlert(msg);
		}
	}
	
	/**
	 * Repaints tables to show updated information
	 */
	private void updateTables() {
		logger.info("Updating tables");
		vehiclesTable.update();
		roadsTable.update();
		junctionsTable.update();
		eventsTable.update();
	}
	
	@Override
	public void update(UpdateEvent ue, String error) {
		switch(ue.getEvent()){
		case REGISTERED:
			connectTables(ue.getEvenQueue(),ue.getRoadMap());
			break;
		case RESET:
			time.setText("0");
			reports.setText("");
			connectTables(ue.getEvenQueue(),ue.getRoadMap());
			updateTables();
			graphRoadMap.generateGraph(ue.getRoadMap());
			statusBar.setText("Simulator has been reseted");
			getAction(Command.Run).setEnabled(true);
			break;
		case NEW_EVENT:
			updateTables();
			statusBar.setText("Events have been loaded to the simulator");
			break;
		case ADVANCED:
			time.setText(""+ (Integer.parseInt(time.getText()) + 1));
			connectTables(ue.getEvenQueue(),ue.getRoadMap());
			updateTables();
			graphRoadMap.generateGraph(ue.getRoadMap());
			statusBar.setText("Simulator has advanced in time!");
			break;
		case ERROR:
			createAlert(error);
			statusBar.setText("Fatal error.");
			getAction(Command.Run).setEnabled(false);
			break;
		default:
			break;
		}
	}
}
