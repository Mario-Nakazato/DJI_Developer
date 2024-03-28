package com.dev.coverpathplan;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.List;

class GraphStructure {
    List<Node> nodes;
    List<Node> arcs;
    List<Double> weight;

    GraphStructure() {
        nodes = new ArrayList<>();
        arcs = new ArrayList<>();
        weight = new ArrayList<>();
    }
}

public class Fork {
    GraphStructure SimpleWeightedGraph(List<List<Node>> cells) {
        GraphStructure gs = new GraphStructure();
        Graph<Node, DefaultWeightedEdge> g = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        for (List<Node> l : cells) {
            for (Node c : l) {
                g.addVertex(c);
            }
        }

        int n = cells.size();
        for (int i1 = 0; i1 < n; i1++) {
            for (int i2 = 0; i2 < cells.get(i1).size(); i2++) {
                if (i1 < n - 1) {
                    g.addEdge(cells.get(i1).get(i2), cells.get(i1 + 1).get(i2));
                    g.setEdgeWeight(g.getEdge(cells.get(i1).get(i2), cells.get(i1 + 1).get(i2)), 2);
                }
                if (i2 < cells.get(i1).size() - 1) {
                    g.addEdge(cells.get(i1).get(i2), cells.get(i1).get(i2 + 1));
                    g.setEdgeWeight(g.getEdge(cells.get(i1).get(i2), cells.get(i1).get(i2 + 1)), 1);
                }
            }
        }

        gs.nodes.addAll(g.vertexSet());

        for (DefaultWeightedEdge edge : g.edgeSet()) {
            Node sourceLatLng = g.getEdgeSource(edge);
            Node targetLatLng = g.getEdgeTarget(edge);
            double weight = g.getEdgeWeight(edge);
            gs.arcs.add(sourceLatLng);
            gs.arcs.add(targetLatLng);
            gs.weight.add(weight);
        }

        return gs;
    }
}