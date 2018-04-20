package es.ucm.fdi.view;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import es.ucm.fdi.control.RoadMap;
import es.ucm.fdi.extra.graphlayout.Dot;
import es.ucm.fdi.extra.graphlayout.Edge;
import es.ucm.fdi.extra.graphlayout.Graph;
import es.ucm.fdi.extra.graphlayout.GraphComponent;
import es.ucm.fdi.extra.graphlayout.Node;
import es.ucm.fdi.model.simobject.Junction;
import es.ucm.fdi.model.simobject.Road;
import es.ucm.fdi.model.simobject.Vehicle;

@SuppressWarnings("serial")
public class GraphLayout extends JPanel  {
	
	private GraphComponent _graphComp;
	private RoadMap roadMap;
    
	public GraphLayout(RoadMap roadMap) {
		this.roadMap = roadMap;
		_graphComp = new GraphComponent();
		this.add(_graphComp, BorderLayout.CENTER);
	}

	protected void generateGraph() {	
		Graph g = new Graph();
		Map<Junction, Node> js = new HashMap<>();
		for (Junction j : roadMap.getJunctions()) {
			Node n = new Node(j.getId());
			js.put(j, n);
			g.addNode(n);
		}
		for (Road r : roadMap.getRoads()) {
			Edge e = new Edge(r.getId(),js.get(r.getInicio()),js.get(r.getFinal()), r.getLongitud());
			for(Vehicle v : r.getVehicles()) {
				e.addDot(new Dot(v.getId(), v.getLocation()));
			}
			g.addEdge(e);
		}
		
		_graphComp.setGraph(g);
	}
}