package uSauron.graph;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonElement;

import uSauron.dataStructure.TestSession;

public class Graph {
	private HashMap<Vertex, ArrayList<Vertex>> adjVertices;

	public Graph() {
		this.adjVertices = new HashMap<Vertex, ArrayList<Vertex>>();
	}

	public Graph(HashMap<Vertex, ArrayList<Vertex>> _graph) {
		this.adjVertices = new HashMap<Vertex, ArrayList<Vertex>>();
		for (HashMap.Entry<Vertex, ArrayList<Vertex>> entry : _graph.entrySet()) {
			this.addVertex(entry.getKey().getLabel());
			for (int i = 0; i < entry.getValue().size(); i++) {
				this.adjVertices.get(entry.getKey()).add(new Vertex(entry.getValue().get(i).getLabel()));
			}
		}
	}
	
	public Graph(HashMap<String, ArrayList<String>> _graph, int ignore) {
		this.adjVertices = new HashMap<Vertex, ArrayList<Vertex>>();
		
		for (HashMap.Entry<String, ArrayList<String>> entry : _graph.entrySet()) {
			this.addVertex(entry.getKey());
			
			Vertex v1 = new Vertex(entry.getKey());
			for (int i = 0; i < entry.getValue().size(); i++) {
				this.adjVertices.get(v1).add(new Vertex(entry.getValue().get(i)));
			}
		}
	}

	public void addVertex(String label) {
		adjVertices.putIfAbsent(new Vertex(label), new ArrayList<>());
	}

	public void removeVertex(String label) {
		Vertex v = new Vertex(label);
		adjVertices.values().stream().forEach(e -> e.remove(v));
		adjVertices.remove(new Vertex(label));
	}

	public void addEdge(String label1, String label2) {
		Vertex v1 = new Vertex(label1);
		Vertex v2 = new Vertex(label2);

		if (!adjVertices.get(v1).contains(v2)) {
			adjVertices.get(v1).add(v2);
		}

	}

	public void removeEdge(String label1, String label2) {
		Vertex v1 = new Vertex(label1);
		Vertex v2 = new Vertex(label2);
		ArrayList<Vertex> eV1 = adjVertices.get(v1);
		ArrayList<Vertex> eV2 = adjVertices.get(v2);
		if (eV1 != null)
			eV1.remove(v2);
		if (eV2 != null)
			eV2.remove(v1);
	}

	public HashMap<String, ArrayList<String>> getOnlyNames() {

		HashMap<String, ArrayList<String>> adjVerticesNames = new HashMap<String, ArrayList<String>>();

		for (HashMap.Entry<Vertex, ArrayList<Vertex>> entry : adjVertices.entrySet()) {

			ArrayList<String> tempArray = new ArrayList<String>();

			for (int i = 0; i < entry.getValue().size(); i++) {
				tempArray.add(entry.getValue().get(i).getLabel());
			}

			adjVerticesNames.put(entry.getKey().getLabel(), tempArray);
		}

		return adjVerticesNames;
	}

	public void buildGraph(HashMap<Integer, TestSession> sessions) {

		for (HashMap.Entry<Integer, TestSession> session : sessions.entrySet()) {

			for (int i = 0; i < session.getValue().getServices().size(); i++) {
				this.addVertex(session.getValue().getServices().get(i).getName());
			}

			if (session.getValue().getServices().size() > 1) {
				for (int i = 0; i < session.getValue().getServices().size(); i++) {
					for (int j = 0; j < session.getValue().getServices().size(); j++) {

						for (int m = 0; m < session.getValue().getServices().get(i).getPackets().size(); m++) {
							for (int n = 0; n < session.getValue().getServices().get(j).getPackets().size(); n++) {
								if (session.getValue().getServices().get(i).getPackets().get(m).geturiReceiver().equals(
										session.getValue().getServices().get(j).getPackets().get(n).geturiSender())) {
									this.addEdge(session.getValue().getServices().get(i).getName(),
											session.getValue().getServices().get(j).getName());
								} else if (session.getValue().getServices().get(i).getPackets().get(m).geturiSender()
										.equals(session.getValue().getServices().get(j).getPackets().get(n)
												.geturiReceiver())) {
									this.addEdge(session.getValue().getServices().get(j).getName(),
											session.getValue().getServices().get(i).getName());
								}

							}
						}

					}
				}

			}
		}

	}

	public int getLevel(String service) {

		Graph tempGraph = new Graph(this.adjVertices);
		ArrayList<String> toExplore = new ArrayList<String>();
		toExplore.add(service);

		return getLevelInt(toExplore, tempGraph);
	}

	private int getLevelInt(ArrayList<String> toExplore, Graph tempGraph) {

		for (HashMap.Entry<Vertex, ArrayList<Vertex>> entry : tempGraph.getAdjVertices().entrySet()) {
			if (!toExplore.contains(entry.getKey().getLabel())) {
				if (entry.getValue().contains(new Vertex(toExplore.get(toExplore.size()-1)))) {
					toExplore.add(entry.getKey().getLabel());
					return getLevelInt(toExplore, tempGraph);
				}
			}
		}

		return toExplore.size()-1;
	}

	public HashMap<Vertex, ArrayList<Vertex>> getAdjVertices() {
		return adjVertices;
	}
	
	public int getDependencies() {
		int depNum = 0;
		
		for (HashMap.Entry<Vertex, ArrayList<Vertex>> entry : this.adjVertices.entrySet()) {
			depNum += entry.getValue().size();
		}
		
		return depNum;
	}

	public void setAdjVertices(HashMap<Vertex, ArrayList<Vertex>> adjVertices) {
		this.adjVertices = adjVertices;
	}
}
