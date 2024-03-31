package com.dev.coverpathplan;

import static com.dev.coverpathplan.GeoCalcGeodeticUtils.calculateDistance;

import com.google.android.gms.maps.model.LatLng;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
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

        for (List<Node> cell : cells)
            for (Node c : cell) {
                gs.nodes.add(c);
                g.addVertex(c);
            }

        int n = cells.size();
        for (int i1 = 0; i1 < n; i1++) {
            for (int i2 = 0; i2 < cells.get(i1).size(); i2++) {
                if (i1 < n - 1) {
                    g.addEdge(cells.get(i1).get(i2), cells.get(i1 + 1).get(i2));
                    g.setEdgeWeight(g.getEdge(cells.get(i1).get(i2), cells.get(i1 + 1).get(i2)), 2.0f);
                }
                if (i2 < cells.get(i1).size() - 1) {
                    g.addEdge(cells.get(i1).get(i2), cells.get(i1).get(i2 + 1));
                    g.setEdgeWeight(g.getEdge(cells.get(i1).get(i2), cells.get(i1).get(i2 + 1)), 1.0f);
                }
            }
        }

        for (LatLng cell : cellsRemove) {
            Node nodeToRemove = null;
            for (Node node : g.vertexSet())
                if (node.node.equals(cell)) {
                    nodeToRemove = node;
                    break;
                }

            if (nodeToRemove != null) {
                gs.nodes.remove(nodeToRemove);
                g.removeVertex(nodeToRemove);
            }
        }

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
        if (graphStructure.nodes.size() < 2)
            return graphStructure;

        GraphStructure gs = new GraphStructure();
        Graph<Node, DefaultWeightedEdge> g = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        for (Node node : graphStructure.nodes)
            g.addVertex(node);

        for (int i = 0; i < graphStructure.arcs.size() - 1; i += 2) {
            Node sourceLatLng = graphStructure.arcs.get(i);
            Node targetLatLng = graphStructure.arcs.get(i + 1);
            g.addEdge(sourceLatLng, targetLatLng);
            g.setEdgeWeight(sourceLatLng, targetLatLng, graphStructure.weight.get(i / 2));
        }

        PrimMinimumSpanningTree<Node, DefaultWeightedEdge> prim = new PrimMinimumSpanningTree<>(g);
        //KruskalMinimumSpanningTree<Integer, DefaultEdge> prim = new KruskalMinimumSpanningTree<>(g);
        SpanningTreeAlgorithm.SpanningTree<DefaultWeightedEdge> minimumSpanningTree = prim.getSpanningTree();
        Graph<Node, DefaultWeightedEdge> gPrim = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllEdges(gPrim, g, minimumSpanningTree.getEdges());

        gs.nodes.addAll(graphStructure.nodes);

        for (DefaultWeightedEdge edge : gPrim.edgeSet()) {
            Node sourceLatLng = gPrim.getEdgeSource(edge);
            Node targetLatLng = gPrim.getEdgeTarget(edge);
            gs.arcs.add(sourceLatLng);
            gs.arcs.add(targetLatLng);
            gs.weight.add(gPrim.getEdgeWeight(edge));
        }
        return gs;
    }

    GraphStructure pathGraph(GraphStructure graphStructure) {
        if (graphStructure.nodes.isEmpty())
            return graphStructure;

        GraphStructure gs = new GraphStructure();
        Graph<LatLng, DefaultWeightedEdge> g = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        for (Node node : graphStructure.nodes) {
            for (LatLng cell : node.cells)
                g.addVertex(cell);

            int rows = (int) Math.sqrt(node.cells.size()); // Assumindo que a grade seja quadrada
            for (int i = 0; i < node.cells.size(); i++) {
                LatLng currentNode = node.cells.get(i);
                int currentRow = i / rows;
                int currentCol = i % rows;

                if (currentCol < rows - 1) {
                    LatLng rightNode = node.cells.get(i + 1);
                    g.addEdge(currentNode, rightNode);
                    g.setEdgeWeight(g.getEdge(currentNode, rightNode), 2.0f);
                }

                if (currentRow < rows - 1) {
                    LatLng bottomNode = node.cells.get(i + rows);
                    g.addEdge(currentNode, bottomNode);
                    g.setEdgeWeight(g.getEdge(currentNode, bottomNode), 1.0f);
                }
            }
        }

        LatLng startLatLng = graphStructure.nodes.get(0).cells.get(2);
        LatLng finishLatLng = graphStructure.nodes.get(0).cells.get(0);
        g.removeEdge(startLatLng, finishLatLng);

        for (int i = 0; i < graphStructure.arcs.size() - 1; i += 2) {
            Node sourceLatLng = graphStructure.arcs.get(i);
            Node targetLatLng = graphStructure.arcs.get(i + 1);

            double smallestDistance1 = Double.MAX_VALUE;
            double smallestDistance2 = Double.MAX_VALUE;

            LatLng closestCellSource1 = null;
            LatLng closestCellTarget1 = null;
            LatLng closestCellSource2 = null;
            LatLng closestCellTarget2 = null;

            for (LatLng cellSource : sourceLatLng.cells) {
                for (LatLng cellTarget : targetLatLng.cells) {
                    // Calcular a distância entre as células source e target
                    double distance = calculateDistance(cellSource, cellTarget);

                    // Atualizar as menores distâncias
                    if (distance < smallestDistance1) {
                        smallestDistance2 = smallestDistance1;
                        closestCellSource2 = closestCellSource1;
                        closestCellTarget2 = closestCellTarget1;
                        smallestDistance1 = distance;
                        closestCellSource1 = cellSource;
                        closestCellTarget1 = cellTarget;
                    } else if (distance < smallestDistance2) {
                        smallestDistance2 = distance;
                        closestCellSource2 = cellSource;
                        closestCellTarget2 = cellTarget;
                    }
                }
            }

            g.removeEdge(closestCellSource1, closestCellSource2);
            g.addEdge(closestCellSource1, closestCellTarget1);
            g.setEdgeWeight(g.getEdge(closestCellSource1, closestCellTarget1), graphStructure.weight.get(i / 2));
            g.removeEdge(closestCellTarget1, closestCellTarget2);
            g.addEdge(closestCellSource2, closestCellTarget2);
            g.setEdgeWeight(g.getEdge(closestCellSource2, closestCellTarget2), graphStructure.weight.get(i / 2));
        }

        DijkstraShortestPath<LatLng, DefaultWeightedEdge> dijkstraAlg = new DijkstraShortestPath<>(g);
        GraphPath<LatLng, DefaultWeightedEdge> shortestPath = dijkstraAlg.getPath(startLatLng, finishLatLng);

        for (LatLng latLng : shortestPath.getVertexList()) {
            Node node = new Node();
            node.node = latLng;
            gs.nodes.add(node);
        }

        for (DefaultWeightedEdge edge : g.edgeSet()) {
            LatLng sourceLatLng = g.getEdgeSource(edge);
            LatLng targetLatLng = g.getEdgeTarget(edge);
            double weight = g.getEdgeWeight(edge);

            Node sourceNode = new Node();
            sourceNode.node = sourceLatLng;
            Node targetNode = new Node();
            targetNode.node = targetLatLng;

            gs.arcs.add(sourceNode);
            gs.arcs.add(targetNode);
            gs.weight.add(weight);
        }
        return gs;
    }
}