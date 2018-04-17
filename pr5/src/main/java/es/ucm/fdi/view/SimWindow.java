package es.ucm.fdi.view;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import es.ucm.fdi.control.SimulatorAction;
import es.ucm.fdi.model.Describable;

@SuppressWarnings("serial")
public class SimWindow extends JPanel {

	private JFileChooser fc;
	private JTextArea eventsEditor;
	private JTable eventsQueue;
	private JTextArea reports;
	private JTable vehicles;
	private JTable roads;
	private JTable junctions;
	private GraphComponent graphComp;
	

	public SimWindow() {
		super();
		initGUI();
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

		JSplitPane topSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				createUp(), bottomSplit);
		
		this.add(topSplit, BorderLayout.CENTER);
		
		//Añadir barra de abajo

		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setSize(1000, 1000);
		jframe.setVisible(true);
		topSplit.setDividerLocation(.5);
		bottomSplit.setDividerLocation(.5);
	}

	private class ListOfMapsTableModel extends AbstractTableModel {
		private String[] fieldNames;
		private List<? extends Describable> elements;
		
		
		public ListOfMapsTableModel(String[] fieldNames,
				List<? extends Describable> elements) {
			this.fieldNames = fieldNames;
			this.elements = elements;
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
		// MEJORAAAAAAAAR--------------------------------------------
		public Object getValueAt(int rowIndex, int columnIndex) {
			return elements.get(rowIndex)
					.describe(new HashMap<String, String>())
					.get(fieldNames[columnIndex]);
		}
	}

	private JPanel createDownLeft() {
		JPanel panel = new JPanel();
		
		String[] vFields = {"ID", "Road", "Location", "Speed", "Km",
				"Faulty Units", "Itinerary" };
		//Que les paso de objeto con elementos? (a las 4)---------------------
		ListOfMapsTableModel vModel = new ListOfMapsTableModel(vFields, new ArrayList<Describable>());
		vehicles = new JTable(vModel);
		
		String[] rFields = { "ID", "Source", "Target", "Length", "Max Speed",
				"Vehicles" };
		ListOfMapsTableModel rModel = new ListOfMapsTableModel(rFields, new ArrayList<Describable>());
		roads = new JTable(rModel);
		
		String[] jFields = { "ID", "Green", "Red"};
		ListOfMapsTableModel jModel = new ListOfMapsTableModel(jFields, new ArrayList<Describable>());
		junctions = new JTable(jModel);
		
		return panel;
	}

	private JPanel createUp() {
		JPanel panel = new JPanel();

		eventsEditor = new JTextArea();
		eventsEditor.setEnabled(true);
		eventsEditor.setLineWrap(true);
		eventsEditor.setWrapStyleWord(true);
		panel.add(new JScrollPane(eventsEditor), BorderLayout.LINE_START);

		String[] eFields = { "#", "Time", "Type"};
		ListOfMapsTableModel eModel = new ListOfMapsTableModel(eFields, new ArrayList<Describable>());
		eventsQueue = new JTable(eModel);
		panel.add(eventsQueue, BorderLayout.CENTER);

		reports = new JTextArea();
		reports.setEnabled(false);
		reports.setLineWrap(true);
		reports.setWrapStyleWord(true);
		panel.add(new JScrollPane(reports), BorderLayout.LINE_END);
		
		//Falta soportar menús contextuales
		
		return panel;
	}
	
	enum Command {
		LoadEvents("Load Events"), SaveEvents("Save Events"), CleanEvents("Clean Events"),
		InsertEvents("Insert events"), Run("Run"), Reset("Reset"),
		GenerateReports("Generate"), CleanReports("Clear"),
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
				() -> loadFile(eventsEditor));
		loadEvents.register(this);

		SimulatorAction saveEvents = new SimulatorAction(Command.SaveEvents, "save.png",
				"Guardar un fichero de eventos", KeyEvent.VK_S, "control S",
				() -> saveFile(eventsEditor));
		saveEvents.register(this);

		SimulatorAction cleanEvents = new SimulatorAction(Command.CleanEvents,
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
		
		SimulatorAction cleanReports = new SimulatorAction(Command.CleanReports,
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
		reports.add(getAction(Command.CleanReports));
		
		return menuBar;
	}
	
	private JToolBar createJToolBar() {
		JToolBar toolBar = new JToolBar();

		toolBar.add(getAction(Command.LoadEvents));
		toolBar.add(getAction(Command.SaveEvents));
		toolBar.add(getAction(Command.CleanEvents));
		toolBar.addSeparator();
		toolBar.add(getAction(Command.InsertEvents));
		toolBar.add(getAction(Command.Run));
		toolBar.add(getAction(Command.Reset));
		//toolBar.add(setCiclos);
		//Añadir aqui etiqueta para el tiempo---------------
		toolBar.addSeparator();
		toolBar.add(getAction(Command.GenerateReports));
		toolBar.add(getAction(Command.CleanReports));
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

	private void loadFile(JTextArea area) {
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String s = readFile(file);
			area.setText(s);
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
	
	private void addEvents(){
		//hay que hacerlos
	}
	
	private void runSimulator(){
		
	}
	
	private void resetSimulator(){
		
	}
	
	private void generateReports(){
		
	}

	public static void main(String... args) {
		SwingUtilities.invokeLater(() -> new SimWindow());
	}
}
