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
import java.util.HashMap;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;

import es.ucm.fdi.control.Controller;
import es.ucm.fdi.control.RoadMap;
import es.ucm.fdi.control.SimulatorAction;
import es.ucm.fdi.control.TrafficSimulator.Listener;
import es.ucm.fdi.control.TrafficSimulator.UpdateEvent;
import es.ucm.fdi.model.Describable;
import es.ucm.fdi.model.events.Event;
import es.ucm.fdi.model.simobject.SimObject;

@SuppressWarnings("serial")
public class SimWindow extends JPanel implements Listener{

	private JFileChooser fc;
	private JTextField time;
	private JSpinner cicles;
	private JCheckBox redirect;
	private OutputStream outText;
	
	private JPopupMenu botonDer;
	private JTextArea eventsEditor;
	private JTable eventsQueue;
	private JTextArea reports;
	private JTable vehiclesTable;
	private JTable roadsTable;
	private JTable junctionsTable;
	private GraphLayout graphRoadMap;
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
		getAction(Command.Run).setEnabled(false);
		getAction(Command.Reset).setEnabled(false);
		jframe.setJMenuBar(createMenuBar());
		add(createJToolBar(), BorderLayout.PAGE_START);
		
		RoadMap roadMap = controller.getSimulator().getRoadMap();
		graphRoadMap = new GraphLayout(roadMap);
		initTables(controller.getSimulator().getEvents(), roadMap);
		
		JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				createDownLeft(), graphRoadMap);
		bottomSplit.setResizeWeight(.5);

		JSplitPane topSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				createUp(), bottomSplit);
		
		this.add(topSplit, BorderLayout.CENTER);
		
		outText = new OutputStreamGUI(reports);
		
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

		SimulatorAction loadEvents = new SimulatorAction(Command.LoadEvents, "open.png",
				"Cargar un fichero de eventos", KeyEvent.VK_L, "ctrl L",
				() -> loadFile());
		loadEvents.register(this);

		SimulatorAction saveEvents = new SimulatorAction(Command.SaveEvents, "save.png",
				"Guardar un fichero de eventos", KeyEvent.VK_A, "ctrl S",
				() -> saveFile(eventsEditor));
		saveEvents.register(this);

		SimulatorAction cleanEvents = new SimulatorAction(Command.ClearEvents,
				"clear.png", "Limpiar la zona de eventos", KeyEvent.VK_N, "ctrl N",
				() -> eventsEditor.setText(""));
		cleanEvents.register(this);
		
		SimulatorAction insertEvents = new SimulatorAction(Command.InsertEvents,
				"events.png", "Insertar eventos en el simulador", KeyEvent.VK_E,
				"ctrl E", () -> addEvents());
		insertEvents.register(this);
		
		SimulatorAction play = new SimulatorAction(Command.Run,
				"play.png", "Ejecutar el simulador", KeyEvent.VK_U,
				"control p", () -> runSimulator());
		play.register(this);
		
		SimulatorAction reset = new SimulatorAction(Command.Reset,
				"reset.png", "Reiniciar el simulador", KeyEvent.VK_T,
				"control t", () -> resetSimulator());
		reset.register(this);
		
		SimulatorAction generateReports = new SimulatorAction(Command.GenerateReports,
				"report.png", "Generar informes", KeyEvent.VK_G,
				"control k", () -> generateReports());
		generateReports.register(this);
		
		SimulatorAction cleanReports = new SimulatorAction(Command.ClearReports,
				"delete_report.png", "Limpiar el área de informes", KeyEvent.VK_C,
				"control u", () -> reports.setText(""));
		cleanReports.register(this);
		
		SimulatorAction saveReports = new SimulatorAction(Command.SaveReport,
				"save_report.png", "Guardar los informes", KeyEvent.VK_V,
				"control r", () -> saveFile(reports));
		saveReports.register(this);

		SimulatorAction quit = new SimulatorAction(Command.Exit, "exit.png",
				"Salir de la aplicación", KeyEvent.VK_E, "control shift X",
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
		simulator.setMnemonic(KeyEvent.VK_S);

		simulator.add(getAction(Command.Run));
		simulator.add(getAction(Command.Reset));
		redirect = new JCheckBox("Redirect Output");
		redirect.setSelected(true);
		simulator.add(redirect);
		
		JMenu reports = new JMenu("Reports");
		menuBar.add(reports);
		reports.setMnemonic(KeyEvent.VK_R);

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
			s = new String(Files.readAllBytes(file.toPath()), "UTF-8");
		} catch (IOException e) {
			createAlert("File couldn't be loaded.");
		}
		return s;
	}

	private static void writeFile(File file, String content) {
		try {
			Files.write(file.toPath(), content.getBytes("UTF-8"));
		} catch (IOException e) {
			createAlert("File couldn't be saved.");
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
			controller.getSimulator().removeEvents();
			//resetSimulator();
			controller.loadEvents(new ByteArrayInputStream(eventsEditor.getText().getBytes()));
			getAction(Command.Run).setEnabled(true);
			getAction(Command.Reset).setEnabled(true);
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
	
	private static void createAlert(String message) {
		JFrame frame = new JFrame();
		JOptionPane.showMessageDialog(frame, message, "Warning", JOptionPane.WARNING_MESSAGE);
	}
	
	private void initiatePopUpMenu() {
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
	
	private String showTemplate(String s) { //ver cursor
		StringBuilder t = new StringBuilder();
		switch(s) {
		case "New RR Junction":
			t.append(showTemplate("New Junction"));
			t.append("max_time_slice = \n");
			t.append("min_time_slice = \n");
			t.append("type = rr\n");
			break;
		case "New MC Junction":
			t.append(showTemplate("New Junction"));
			t.append("type = mc\n");
			break;
		case "New Junction":
			t.append("[new_junction]\n");
			t.append("time = \n");
			t.append("id = \n");
			break;
		case "New Dirt Road":
			t.append(showTemplate("New Road"));
			t.append("type = dirt\n");
			break;
		case "New Lanes Road":
			t.append(showTemplate("New Road"));
			t.append("lanes = \n");
			t.append("type = lanes\n");
			break;
		case "New Road":
			t.append("[new_road]\n");
			t.append("time = \n");
			t.append("id = \n");
			t.append("src = \n");
			t.append("dest = \n");
			t.append("max_speed = \n");
			t.append("length = \n");
			break;
		case "New Bike":
			t.append(showTemplate("New Vehicle"));
			t.append("type = bike\n");
			break;
		case "New Car":
			t.append(showTemplate("New Vehicle"));
			t.append("resistance = \n");
			t.append("fault_probability = \n");
			t.append("max_fault_duration = \n");
			t.append("seed = \n");
			t.append("type = car\n");
			break;
		case "New Vehicle":
			t.append("[new_vehicle]\n");
			t.append("time = \n");
			t.append("id = \n");
			t.append("itinerary = \n");
			t.append("max_speed = \n");
			break;
		case "Make Vehicle Faulty":
			t.append("[make_vehicle_faulty]\n");
			t.append("time = \n");
			t.append("vehicles = \n");
			t.append("duration = \n");
			break;
		}
		return t.toString();
	}
	
	private void generateReports(){
		generateReportsTable(controller.getSimulator().getRoadMap(),vehiclesTable);
	}
	
	private void generateReportsTable(RoadMap r, JTable t) {
		int[] indices = t.getSelectedRows();
		String[] idsThings = new String[indices.length];
		for(int i=0; i<idsThings.length; i++) {
			idsThings[i] = (String) t.getValueAt(indices[i],0);
		}
		SimObject[] simobjects = new SimObject[idsThings.length];
		for(int i=0; i<idsThings.length; i++) {
			simobjects[i] = r.getObject(idsThings[i]);
		}
		for(SimObject o : simobjects){
			o.report(Integer.parseInt(time.getText()));
		}
	}
	
	private void runSimulator(){
		int ticks = ((SpinnerNumberModel) cicles.getModel()).getNumber().intValue();
		OutputStream out;
		if(redirect.isSelected()) out = outText;
		else out = new OutputStreamGUI(new JTextArea());
		controller.getSimulator().run(ticks, out);
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
			break;
		case RESET:
			time.setText("0");
			reports.setText("");
			updateTable(vehiclesTable);
			updateTable(roadsTable);
			updateTable(junctionsTable);
			graphRoadMap.generateGraph();
			statusBar.setText("Simulator has been reseted");
			getAction(Command.Run).setEnabled(true);
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
			graphRoadMap.generateGraph();
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
