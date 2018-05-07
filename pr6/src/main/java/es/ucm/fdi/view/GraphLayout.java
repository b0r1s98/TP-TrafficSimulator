package es.ucm.fdi.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
/**
 * 
 * Paints a RoadMap into a Graph.
 *
 */
public class GraphLayout extends JPanel  {
	
	private GraphComponent _graphComp;
	
	private static final Logger logger =
			Logger.getLogger(GraphLayout.class.getName());
    
	/**
	 * Class Constructor
	 */
	public GraphLayout() {
		this._graphComp = new GraphComponent();
		this.add(_graphComp, BorderLayout.CENTER);
	}

	/**
	 * Generates a graph based on the roadMap:
	 * vehicles are dots, roads are edges and junctions are nodes
	 * 
	 * @param roadMap roadMap to be painted
	 */
	public void generateGraph(RoadMap roadMap) {
		logger.info("Painting graph");
		Graph g = new Graph();
		Map<Junction, Node> js = new HashMap<>();
		for (Junction j : roadMap.getJunctions()) {
			Node n = new Node(j.getId());
			js.put(j, n);
			g.addNode(n);
		}
		for (Road r : roadMap.getRoads()) {
			Edge e = new Edge(r.getId(),js.get(r.getStart()),js.get(r.getEnd()), r.getLength(),
					(r.getEnd().isRoadGreen(r) ? Color.GREEN : Color.RED));
			for(Vehicle v : r.getVehicles()) {
				e.addDot(new Dot(v.getId(), v.getLocation()));
			}
			g.addEdge(e);
		}
		
		_graphComp.setGraph(g);
	}
}