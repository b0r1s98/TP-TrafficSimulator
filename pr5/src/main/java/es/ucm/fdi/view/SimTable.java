package es.ucm.fdi.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;

import es.ucm.fdi.model.Describable;

@SuppressWarnings("serial")
/**
 * 
 * Table used in GUI
 *
 */
public class SimTable extends JTable {
	
	private AbstractTableModel model;
	
	/**
	 * Repaints table with updated data
	 */
	public void update() {
		model.fireTableDataChanged();
	}
	
	/**
	 * Set column names and elements contained in the table
	 * 
	 * @param fieldNames	column names
	 * @param elements		one element is a row
	 */
	public void setModel(String[] fieldNames,
				List<? extends Describable> elements){
		model = new ListOfMapsTableModel(fieldNames, elements);
		setModel(model);
	}
	
	/**
	 * Returns a panel with the table inside
	 * 
	 * @param name name of the panel
	 * @return jpanel containing table
	 */
	public JPanel intoPanel (String name) {
		Border b = BorderFactory.createLineBorder(Color.black, 2);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(b, name));
		panel.add(new JScrollPane(this));
		
		return panel;
	}

	/**
	 * @return a set containing selected ids in the table
	 */
	public Set<String> getSelectedIds() {
		int[] rows = this.getSelectedRows();
		Set<String> ids = new HashSet<>();
		for(int r : rows) {
			ids.add(model.getValueAt(r, 0).toString());
		}
		return ids;
	}

	/**
	 * 
	 * Own implementation of a TableModel
	 *
	 */
	private class ListOfMapsTableModel extends AbstractTableModel {
		private String[] fieldNames;
		private List<? extends Describable> elements;
		
		private HashMap<String, String> map;
		private int previousRow;
		
		/**
		 * Class constructor
		 * 
		 * @param fieldNames	column names
		 * @param elements		one element is a row
		 */
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
}
