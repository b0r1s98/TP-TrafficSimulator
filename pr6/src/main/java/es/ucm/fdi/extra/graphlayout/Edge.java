package es.ucm.fdi.extra.graphlayout;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Edge {
	private String _id;
	private Node _source;
	private Node _target;
	private int _length;
	private List<Dot> _dots;
	private Color _color;
	
	public Edge(String id, Node source, Node target, int length, Color color) {
		_source = source;
		_target = target;
		_id = id;
		_length = length;
		_dots = new ArrayList<>();
		_color = color;
	}
	
	public Edge(String id, Node source, Node target, int length) {
		_source = source;
		_target = target;
		_id = id;
		_length = length;
		_dots = new ArrayList<>();
		_color = Math.random() > 0.5 ? Color.RED : Color.GREEN;
	}

	public void addDot(Dot e) {
		_dots.add(e);
	}
	public String getId() {
		return _id;
	}
	
	public Node getSource() {
		return _source;
	}
	
	public Node getTarget() {
		return _target;
	}

	public int getLength() {
		return _length;
	}
	
	public Color getColor() {
		return _color;
	}
	
	public List<Dot> getDots() {
		return _dots;
	}
}
