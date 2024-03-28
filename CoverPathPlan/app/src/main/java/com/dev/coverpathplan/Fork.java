package com.dev.coverpathplan;

import com.google.android.gms.maps.model.LatLng;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm;
import org.jgrapht.alg.spanning.PrimMinimumSpanningTree;
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
    GraphStructure SimpleWeightedGraph(List<List<Node>> cells, List<LatLng> cellsRemove) {
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

        for (LatLng x : cellsRemove) {
            Node nodeToRemove = null;
            for (Node node : g.vertexSet()) {
                if (node.node.equals(x)) {
                    nodeToRemove = node;
                    break;
                }
            }
            if (nodeToRemove != null) {
                g.removeVertex(nodeToRemove);
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

    GraphStructure minimumSpanningTree(GraphStructure graphStructure) {
        GraphStructure gs = new GraphStructure();
        Graph<Node, DefaultWeightedEdge> g = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        for (Node l : graphStructure.nodes) {
            g.addVertex(l);
        }

        for (int i = 0; i < graphStructure.arcs.size() - 1; i += 2) {
            Node sourceLatLng = graphStructure.arcs.get(i);
            Node targetLatLng = graphStructure.arcs.get(i + 1);
            g.addEdge(sourceLatLng, targetLatLng);
            g.setEdgeWeight(sourceLatLng, targetLatLng, graphStructure.weight.get(i / 2));
        }

        PrimMinimumSpanningTree<Node, DefaultWeightedEdge> prim = new PrimMinimumSpanningTree<>(g);
        //KruskalMinimumSpanningTree<Integer, DefaultEdge> prim = new KruskalMinimumSpanningTree<>(g);
        SpanningTreeAlgorithm.SpanningTree<DefaultWeightedEdge> minimumSpanningTree = prim.getSpanningTree();
        Graph<Node, DefaultWeightedEdge> g1 = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllEdges(g1, g, minimumSpanningTree.getEdges());

        gs.nodes.addAll(g1.vertexSet());

        for (DefaultWeightedEdge edge : g1.edgeSet()) {
            Node sourceLatLng = g1.getEdgeSource(edge);
            Node targetLatLng = g1.getEdgeTarget(edge);
            gs.arcs.add(sourceLatLng);
            gs.arcs.add(targetLatLng);
            gs.weight.add(g1.getEdgeWeight(edge));
        }

        return gs;
    }
}