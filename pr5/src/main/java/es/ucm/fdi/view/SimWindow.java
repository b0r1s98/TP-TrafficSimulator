package es.ucm.fdi.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;

import es.ucm.fdi.control.Controller;
import es.ucm.fdi.control.RoadMap;
import es.ucm.fdi.control.SimulatorAction;
import es.ucm.fdi.control.TrafficSimulator;
import es.ucm.fdi.control.TrafficSimulator.Listener;
import es.ucm.fdi.control.TrafficSimulator.UpdateEvent;
import es.ucm.fdi.model.Describable;
import es.ucm.fdi.model.events.Event;

@SuppressWarnings("serial")
public class SimWindow extends JPanel implements Listener{

	private JFileChooser fc;
	private JTextField time;
	private JSpinner cicles;
	private OutputStream outText;
	
	private JTextArea eventsEditor;
	private JTable eventsQueue;
	private JTextArea reports;
	private JTable vehiclesTable;
	private JTable roadsTable;
	private JTable junctionsTable;
	private GraphComponent graphComp;
	//Grafo
	private JLabel statusBar;
	
	private Controller controller;
	

	public SimWindow(Controller controller, String file, Integer ticks) {
		super();
		this.controller = controller;
		controller.getSimulator().addSimulatorListener(this);
		
		initGUI();
		
		if(file != null) loadFromString(file);
		if(ticks != null) setTime(ticks);
	}

	private void initGUI() {
		JFrame jframe = new JFrame("Traffic Simulator");
		this.setLayout(new BorderLayout());
		jframe.setContentPane(this);

		fc = new JFileChooser();
		createActions();
		jframe.setJMenuBar(createMenuBar());
		add(createJToolBar(), BorderLayout.PAGE_START);
		
		graphComp = new GraphComponent();
		
		JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				createDownLeft(), graphComp);
		bottomSplit.setResizeWeight(.5);

		JSplitPane topSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				createUp(), bottomSplit);
		
		this.add(topSplit, BorderLayout.CENTER);
		
		outText = new OutputStreamGUI(reports);
		
		//revisar altura y grosor letra
		statusBar = new JLabel("Ready!");
		statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
		jframe.add(statusBar, BorderLayout.PAGE_END);

		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setSize(1000, 1000);
		jframe.setVisible(true);
		topSplit.setDividerLocation(.5);
		bottomSplit.setDividerLocation(.5);
	}
	
	private void initTables(List<Event> events, RoadMap roadmap) {
		String[] eFields = { "#", "Time", "Type"};
		ListOfMapsTableModel eModel = new ListOfMapsTableModel(eFields, events);
		eventsQueue = new JTable(eModel);
		
		String[] vFields = {"ID", "Road", "Location", "Speed", "Km",
				"Faulty Units", "Itinerary" };
		ListOfMapsTableModel vModel = new ListOfMapsTableModel(vFields, roadmap.getVehicles());
		vehiclesTable = new JTable(vModel);
		
		String[] rFields = { "ID", "Source", "Target", "Length", "Max Speed",
				"Vehicles" };
		ListOfMapsTableModel rModel = new ListOfMapsTableModel(rFields, roadmap.getRoads());
		roadsTable = new JTable(rModel);
		
		String[] jFields = { "ID", "Green", "Red"};
		ListOfMapsTableModel jModel = new ListOfMapsTableModel(jFields, roadmap.getJunctions());
		junctionsTable = new JTable(jModel);
	}
	
	private class ListOfMapsTableModel extends AbstractTableModel {
		private String[] fieldNames;
		private List<? extends Describable> elements;
		
		private HashMap<String, String> map;
		private int previousRow;
		
		public ListOfMapsTableModel(String[] fieldNames,
				List<? extends Describable> elements) {
			this.fieldNames = fieldNames;
			this.elements = elements;
			this.map = new HashMap<String, String>();
			this.previousRow = -1;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return fieldNames[columnIndex];
		}

		@Override
		public int getRowCount() {
			return elements.size();
		}

		@Override
		public int getColumnCount() {
			return fieldNames.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if(rowIndex != previousRow) {
				elements.get(rowIndex).describe(map);
				map.put("#", ""+rowIndex);
				previousRow = rowIndex;
			}
			return map.get(fieldNames[columnIndex]);
		}
	}

	private JPanel createDownLeft() {
		JPanel panel = new JPanel(new GridLayout(3,1));
		Border b = BorderFactory.createLineBorder(Color.black, 2);
		
		JPanel vPanel = new JPanel(new BorderLayout());
		vPanel.setBorder(BorderFactory.createTitledBorder(b, "Vehicles"));
		vPanel.add(new JScrollPane(vehiclesTable));
		panel.add(vPanel);
		
		JPanel rPanel = new JPanel(new BorderLayout());
		rPanel.setBorder(BorderFactory.createTitledBorder(b, "Roads"));
		rPanel.add(new JScrollPane(roadsTable));
		panel.add(rPanel);
		
		JPanel jPanel = new JPanel(new BorderLayout());
		jPanel.setBorder(BorderFactory.createTitledBorder(b, "Junctions"));
		jPanel.add(new JScrollPane(junctionsTable));
		panel.add(jPanel);
		
		return panel;
	}

	private JPanel createUp() {
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
		//crear menu contextual

		JPanel panelMed = new JPanel(new BorderLayout());
		panelMed.setBorder(BorderFactory.createTitledBorder(b, "Events Queue"));
		panelMed.add(new JScrollPane(eventsQueue));
		panel.add(panelMed);

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
	
	private void createActions(){
		// cambiar atajos teclado (aqui y abajo) -----------------------------

		SimulatorAction loadEvents = new SimulatorAction(Command.LoadEvents, "open.png",
				"Cargar un fichero de eventos", KeyEvent.VK_L, "control O",
				() -> loadFile());
		loadEvents.register(this);

		SimulatorAction saveEvents = new SimulatorAction(Command.SaveEvents, "save.png",
				"Guardar un fichero de eventos", KeyEvent.VK_S, "control S",
				() -> saveFile(eventsEditor));
		saveEvents.register(this);

		SimulatorAction cleanEvents = new SimulatorAction(Command.ClearEvents,
				"clear.png", "Limpiar la zona de eventos", KeyEvent.VK_S,
				"control S", () -> eventsEditor.setText(""));
		cleanEvents.register(this);
		
		SimulatorAction insertEvents = new SimulatorAction(Command.InsertEvents,
				"events.png", "Insertar eventos en el simulador", KeyEvent.VK_S,
				"control S", () -> addEvents());
		insertEvents.register(this);
		
		SimulatorAction play = new SimulatorAction(Command.Run,
				"play.png", "Ejecutar el simulador", KeyEvent.VK_S,
				"control S", () -> runSimulator());
		play.register(this);
		
		SimulatorAction reset = new SimulatorAction(Command.Reset,
				"reset.png", "Reiniciar el simulador", KeyEvent.VK_S,
				"control S", () -> resetSimulator());
		reset.register(this);
		
		SimulatorAction generateReports = new SimulatorAction(Command.GenerateReports,
				"report.png", "Generar informes", KeyEvent.VK_S,
				"control S", () -> generateReports());
		generateReports.register(this);
		
		SimulatorAction cleanReports = new SimulatorAction(Command.ClearReports,
				"delete_report.png", "Limpiar el área de informes", KeyEvent.VK_S,
				"control S", () -> reports.setText(""));
		cleanReports.register(this);
		
		SimulatorAction saveReports = new SimulatorAction(Command.SaveReport,
				"save_report.png", "Guardar los informes", KeyEvent.VK_S,
				"control S", () -> saveFile(reports));
		saveReports.register(this);

		SimulatorAction quit = new SimulatorAction(Command.Exit, "exit.png",
				"Salir de la aplicación", KeyEvent.VK_A, "control shift X",
				() -> System.exit(0));
		quit.register(this);
	}
	
	private Action getAction(Command c){
		return getActionMap().get(c.toString());
	}
	
	private JMenuBar createMenuBar() {
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
		file.setMnemonic(KeyEvent.VK_S);

		simulator.add(getAction(Command.Run));
		simulator.add(getAction(Command.Reset));
		//aqui hay boton especial--------------------------------
		
		JMenu reports = new JMenu("Reports");
		menuBar.add(reports);
		file.setMnemonic(KeyEvent.VK_S);

		reports.add(getAction(Command.GenerateReports));
		reports.add(getAction(Command.ClearReports));
		
		return menuBar;
	}
	
	private JToolBar createJToolBar() {
		JToolBar toolBar = new JToolBar();

		toolBar.add(getAction(Command.LoadEvents));
		toolBar.add(getAction(Command.SaveEvents));
		toolBar.add(getAction(Command.ClearEvents));
		toolBar.addSeparator();
		toolBar.add(getAction(Command.InsertEvents));
		toolBar.add(getAction(Command.Run));
		toolBar.add(getAction(Command.Reset));
		
		cicles = new JSpinner();
		((SpinnerNumberModel) cicles.getModel()).setMinimum(1);
		setTime(1);
		toolBar.add(cicles);
		time = new JTextField();
		time.setText("0");
		time.setEditable(false);
		toolBar.add(time);
		
		toolBar.addSeparator();
		toolBar.add(getAction(Command.GenerateReports));
		toolBar.add(getAction(Command.ClearReports));
		toolBar.add(getAction(Command.SaveReport));
		toolBar.addSeparator();
		toolBar.add(getAction(Command.Exit));
		
		return toolBar;
	}

	private void saveFile(JTextArea area) {
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			writeFile(file, area.getText());
		}
	}

	private void loadFile() {
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			eventsEditor.setText(readFile(file));
		}
	}

	private static String readFile(File file) {
		String s = "";
		try {
			s = new String(Files.readAllBytes(file.toPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s;
	}

	private static void writeFile(File file, String content) {
		try {
			Files.write(file.toPath(), content.getBytes("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadFromString(String fileName) {
		File file = new File(fileName);
		eventsEditor.setText(readFile(file));
	}
	
	private void setTime(int ticks) {
		((SpinnerNumberModel) cicles.getModel()).setValue(ticks);
	}
	
	private class OutputStreamGUI extends OutputStream{
		private JTextArea area;
		
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
	
	private void addEvents(){
		try {
			controller.loadEvents(new ByteArrayInputStream(eventsEditor.getText().getBytes()));
		} catch (Exception e) {
			String s = eventsEditor.getText();
			int nextEventIndex = s.indexOf("[", s.indexOf("[")+1);
			if(nextEventIndex == -1) nextEventIndex = s.length();
			eventsEditor.requestFocus();
			eventsEditor.setSelectionStart(0);
			eventsEditor.setSelectionEnd(nextEventIndex);
			createAlert("El evento seleccionado parece incorrecto");
			statusBar.setText("Incorrect event");
		}
	}
	
	private void createAlert(String message) {
		JFrame frame = new JFrame();
		JOptionPane.showMessageDialog(frame, message, "Warning", JOptionPane.WARNING_MESSAGE);
		
	}
	
	private void generateReports(){
		List<String> v = new ArrayList<>();
		List<String> r = new ArrayList<>();
		List<String> j = new ArrayList<>();
		
		//DialogWindow dialog = new DialogWindow(new JFrame("Generate Reports"));
		//dialog.set
		
	}
	
	private void runSimulator(){
		int ticks = ((SpinnerNumberModel) cicles.getModel()).getNumber().intValue();
		//conseguir out correcto del boton en este momento
		try {
			controller.getSimulator().run(ticks, outText);
		} catch (IOException e) {
			//que meto aqui
		}
	}
	
	private void resetSimulator(){
		controller.getSimulator().reset();
	}
	
	private void updateTable(JTable table) {
		((ListOfMapsTableModel) table.getModel()).fireTableDataChanged();
	}
	
	@Override
	public void update(UpdateEvent ue, String error) {
		switch(ue.getEvent()){
		case REGISTERED:
			initTables(ue.getEvenQueue(), ue.getRoadMap());
			break;
		case RESET:
			time.setText("0");
			reports.setText("");
			updateTable(vehiclesTable);
			updateTable(roadsTable);
			updateTable(junctionsTable);
			//repintar grafo
			statusBar.setText("Simulator has been reseted");
			break;
		case NEW_EVENT:
			updateTable(eventsQueue);
			statusBar.setText("Events have been loaded to the simulator");
			break;
		case ADVANCED:
			time.setText(""+ (Integer.parseInt(time.getText()) + 1));
			updateTable(vehiclesTable);
			updateTable(roadsTable);
			updateTable(junctionsTable);
			//repintar grafo
			statusBar.setText("Simulator has advanced in time!");
			break;
		case ERROR:
			createAlert(error);
			statusBar.setText("Fatal error.");
			break;
		default:
			break;
		}
	}
}
