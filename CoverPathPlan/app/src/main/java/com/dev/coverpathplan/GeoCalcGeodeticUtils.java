package com.dev.coverpathplan;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.grum.geocalc.Coordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;

import java.util.ArrayList;
import java.util.List;

class Node {
    LatLng node;
    List<LatLng> cells;

    Node() {
        cells = new ArrayList<>();
    }
}

public class GeoCalcGeodeticUtils {
    static double mBearingLargura = 0;

    static double calculateDistance(LatLng value1, LatLng value2) {
        Point coor1 = Point.at(Coordinate.fromDegrees(value1.latitude), Coordinate.fromDegrees(value1.longitude));
        Point coor2 = Point.at(Coordinate.fromDegrees(value2.latitude), Coordinate.fromDegrees(value2.longitude));
        return EarthCalc.vincenty.distance(coor1, coor2);
    }

    static double calculateTotalDistance(List<LatLng> values) {
        double distance = 0;
        LatLng value1;
        LatLng value2 = null;

        for (LatLng cell : values) {
            value1 = cell;
            if (value2 != null)
                distance += calculateDistance(value1, value2);
            value2 = value1;
        }
        return distance;
    }

    List<LatLng> nodeToLatLng(List<Node> nodes) {
        List<LatLng> nodeToLatLng = new ArrayList<>();
        for (Node node : nodes)
            nodeToLatLng.add(node.node);
        return nodeToLatLng;
    }

    List<LatLng> listNodeToLatLng(List<List<Node>> nodes) {
        List<LatLng> listNodeToLatLng = new ArrayList<>();
        for (List<Node> node : nodes)
            for (Node n : node)
                listNodeToLatLng.add(n.node);
        return listNodeToLatLng;
    }

    List<Point> LatLngToPoint(List<LatLng> vertices) {
        List<Point> points = new ArrayList<>();
        for (LatLng vertex : vertices) {
            points.add(Point.at(Coordinate.fromDegrees(vertex.latitude), Coordinate.fromDegrees(vertex.longitude)));
        }
        return points;
    }

    @SuppressWarnings("unused")
    List<LatLng> createGrid(List<LatLng> rectangleVertices) {
        if (rectangleVertices.size() < 4) // rectangleVertices é 5 por que é um anel linear fechado
            return new ArrayList<>();

        List<Point> coors = LatLngToPoint(rectangleVertices);
        Point coor1 = coors.get(0);
        Point coor2 = coors.get(1);
        //Point coor3 = coors.get(2);
        Point coor4 = coors.get(3);

        Log.v("Grid", "Grid:");

        double offsetLargura = 0.5; // Deslocamento da posição
        double offsetAltura = 0.5;
        double overlapLargura = CaptureArea.getOverlapLargura(); // Overlap sobreposição
        double overlapAltura = CaptureArea.getOverlapAltura();

        // Referencia a Largura do retangulo com Altura do footprint
        double bearingLargura = EarthCalc.vincenty.bearing(coor1, coor4);
        double lengthLargura = EarthCalc.vincenty.distance(coor1, coor4);
        double offsetFootprintAltura = CaptureArea.getFootprintAltura() * offsetAltura;
        if (lengthLargura < CaptureArea.getFootprintAltura())
            offsetFootprintAltura = lengthLargura * offsetAltura;
        double distanceLargura = lengthLargura - (2 * offsetFootprintAltura);
        double usefulFootprintAltura = CaptureArea.getFootprintAltura() * (1 - overlapAltura);
        int nRealFootprintAltura = (int) Math.ceil(distanceLargura / usefulFootprintAltura);
        double distanceBetweenFootprintAltura = distanceLargura / nRealFootprintAltura;

        // Referencia da Altura do retangulo com Largura do footprint
        double bearingAltura = EarthCalc.vincenty.bearing(coor1, coor2); // Direção
        double lengthAltura = EarthCalc.vincenty.distance(coor1, coor2); // Comprimento do lado do retangulo
        double offsetFootprintLargura = CaptureArea.getFootprintLargura() * offsetLargura; // offset da área de captura
        if (lengthAltura < CaptureArea.getFootprintLargura()) // Caso footprint seja maior que os lados centralizar, usando offset?
            offsetFootprintLargura = lengthAltura * offsetLargura; // Não tenho certeza se é offset ou 0.5 investigar
        double distanceAltura = lengthAltura - (2 * offsetFootprintLargura); // distancia entre o primeiro e o último ponto
        double usefulFootprintLargura = CaptureArea.getFootprintLargura() * (1 - overlapLargura); // Área util de captura
        int nRealFootprintLargura = (int) Math.ceil(distanceAltura / usefulFootprintLargura); // Número de área de captura com overlap, depois da distancia inicial
        double distanceBetweenFootprintLargura = distanceAltura / nRealFootprintLargura; // Distancia entre área de captura

        List<LatLng> cells = new ArrayList<>();
        double currentDistanceAltura = offsetFootprintLargura; // Distancia inicial do primeiro ponto
        for (int i1 = 0; i1 <= nRealFootprintLargura; i1++) {
            Point currentCoorLargura = EarthCalc.gcd.pointAt(coor1, bearingAltura, currentDistanceAltura);
            double currentDistanceLargura = offsetFootprintAltura;
            for (int i2 = 0; i2 <= nRealFootprintAltura; i2++) {
                Point currentCoorAltura = EarthCalc.gcd.pointAt(currentCoorLargura, bearingLargura, currentDistanceLargura);
                cells.add(new LatLng(currentCoorAltura.latitude, currentCoorAltura.longitude));
                Log.v("Grid", "currentDistanceLargura: " + currentDistanceLargura);
                currentDistanceLargura += distanceBetweenFootprintAltura;
            }
            Log.v("Grid", "currentDistanceAltura: " + currentDistanceAltura);
            currentDistanceAltura += distanceBetweenFootprintLargura;
        }

        // Bloco de log para todas as variáveis
        Log.v("Grid", "offsetAltura: " + offsetAltura + " %");
        Log.v("Grid", "overlapAltura: " + overlapAltura + " %?");
        Log.v("Grid", "bearingAltura: " + bearingAltura + " °");
        Log.v("Grid", "lengthAltura: " + lengthAltura + " m");
        Log.v("Grid", "offsetFootprintLargura: " + offsetFootprintLargura + " m");
        Log.v("Grid", "distanceAltura: " + distanceAltura + " m");
        Log.v("Grid", "usefulFootprintLargura: " + usefulFootprintLargura + " m");
        Log.v("Grid", "nRealFootprintLargura: " + nRealFootprintLargura);
        Log.v("Grid", "distanceBetweenFootprintLargura: " + distanceBetweenFootprintLargura + " m");
        Log.v("Grid", "offsetLargura: " + offsetLargura + " %");
        Log.v("Grid", "overlapLargura: " + overlapLargura + " %?");
        Log.v("Grid", "bearingLargura: " + bearingLargura + " °");
        Log.v("Grid", "lengthLargura: " + lengthLargura + " m");
        Log.v("Grid", "offsetFootprintAltura: " + offsetFootprintAltura + " m");
        Log.v("Grid", "distanceLargura: " + distanceLargura + " m");
        Log.v("Grid", "usefulFootprintAltura: " + usefulFootprintAltura + " m");
        Log.v("Grid", "nRealFootprintAltura: " + nRealFootprintAltura);
        Log.v("Grid", "distanceBetweenFootprintAltura: " + distanceBetweenFootprintAltura + " m");

        Log.v("Grid Overlap", "realOverlapLargura: "
                + distanceBetweenFootprintLargura / CaptureArea.getFootprintLargura() * 100 + " % "
                + (1 - distanceBetweenFootprintLargura / CaptureArea.getFootprintLargura()) * 100 + " %");
        Log.v("Grid Overlap", "realOverlapAltura: "
                + distanceBetweenFootprintAltura / CaptureArea.getFootprintAltura() * 100 + " % "
                + (1 - distanceBetweenFootprintAltura / CaptureArea.getFootprintAltura()) * 100 + " %");
        Log.v("Grid Overlap", "OverlapLargura: "
                + usefulFootprintLargura / CaptureArea.getFootprintLargura() * 100 + " % "
                + (1 - usefulFootprintLargura / CaptureArea.getFootprintLargura()) * 100 + " %");
        Log.v("Grid Overlap", "OverlapAltura: "
                + usefulFootprintAltura / CaptureArea.getFootprintAltura() * 100 + " % "
                + (1 - usefulFootprintAltura / CaptureArea.getFootprintAltura()) * 100 + " %");
        return cells;
    }

    List<LatLng> createBoustrophedonGrid(List<LatLng> rectangleVertices) {
        if (rectangleVertices.size() < 4)
            return new ArrayList<>();

        List<Point> coors = LatLngToPoint(rectangleVertices);
        Point coor1 = coors.get(0);
        Point coor2 = coors.get(1);
        //Point coor3 = coors.get(2);
        Point coor4 = coors.get(3);

        Log.v("Grid", "BoustrophedonGrid:");

        double offsetLargura = 0.5;
        double offsetAltura = 0.5;
        double overlapLargura = CaptureArea.getOverlapLargura();
        double overlapAltura = CaptureArea.getOverlapAltura();

        double bearingLargura = EarthCalc.vincenty.bearing(coor1, coor4);
        double lengthLargura = EarthCalc.vincenty.distance(coor1, coor4);
        double offsetFootprintAltura = CaptureArea.getFootprintAltura() * offsetAltura;
        if (lengthLargura < CaptureArea.getFootprintAltura())
            offsetFootprintAltura = lengthLargura * offsetAltura;
        double distanceLargura = lengthLargura - (2 * offsetFootprintAltura);
        double usefulFootprintAltura = CaptureArea.getFootprintAltura() * (1 - overlapAltura);
        int nRealFootprintAltura = (int) Math.ceil(distanceLargura / usefulFootprintAltura);
        double distanceBetweenFootprintAltura = distanceLargura / nRealFootprintAltura;

        double bearingAltura = EarthCalc.vincenty.bearing(coor1, coor2);
        double lengthAltura = EarthCalc.vincenty.distance(coor1, coor2);
        double offsetFootprintLargura = CaptureArea.getFootprintLargura() * offsetLargura;
        if (lengthAltura < CaptureArea.getFootprintLargura())
            offsetFootprintLargura = lengthAltura * offsetLargura;
        double distanceAltura = lengthAltura - (2 * offsetFootprintLargura);
        double usefulFootprintLargura = CaptureArea.getFootprintLargura() * (1 - overlapLargura);
        int nRealFootprintLargura = (int) Math.ceil(distanceAltura / usefulFootprintLargura);
        double distanceBetweenFootprintLargura = distanceAltura / nRealFootprintLargura;

        List<LatLng> cells = new ArrayList<>();
        double currentDistanceAltura = offsetFootprintLargura;
        for (int i1 = 0; i1 <= nRealFootprintLargura; i1++) {
            Point currentCoorLargura = EarthCalc.gcd.pointAt(coor1, bearingAltura, currentDistanceAltura);
            double currentDistanceLargura = offsetFootprintAltura;
            if (i1 % 2 == 0) { // Se a linha atual for par, adicione os pontos da esquerda para a direita
                for (int i2 = 0; i2 <= nRealFootprintAltura; i2++) {
                    Point currentCoorAltura = EarthCalc.gcd.pointAt(currentCoorLargura, bearingLargura, currentDistanceLargura);
                    cells.add(new LatLng(currentCoorAltura.latitude, currentCoorAltura.longitude));
                    Log.v("Grid", "currentDistanceLargura: " + currentDistanceLargura);
                    currentDistanceLargura += distanceBetweenFootprintAltura;
                }
            } else { // Se a linha atual for ímpar, adicione os pontos da direita para a esquerda
                currentDistanceLargura = distanceLargura + currentDistanceLargura;
                for (int i2 = nRealFootprintAltura; i2 >= 0; i2--) {
                    Point currentCoorAltura = EarthCalc.gcd.pointAt(currentCoorLargura, bearingLargura, currentDistanceLargura);
                    cells.add(new LatLng(currentCoorAltura.latitude, currentCoorAltura.longitude));
                    Log.v("Grid", "currentDistanceLargura: " + currentDistanceLargura);
                    currentDistanceLargura -= distanceBetweenFootprintAltura;
                }
            }
            Log.v("Grid", "currentDistanceAltura: " + currentDistanceAltura);
            currentDistanceAltura += distanceBetweenFootprintLargura;
        }
        mBearingLargura = bearingLargura;

        // Bloco de log para todas as variáveis
        Log.v("Grid", "offsetAltura: " + offsetAltura + " %");
        Log.v("Grid", "overlapAltura: " + overlapAltura + " %?");
        Log.v("Grid", "bearingAltura: " + bearingAltura + " °");
        Log.v("Grid", "lengthAltura: " + lengthAltura + " m");
        Log.v("Grid", "offsetFootprintLargura: " + offsetFootprintLargura + " m");
        Log.v("Grid", "distanceAltura: " + distanceAltura + " m");
        Log.v("Grid", "usefulFootprintLargura: " + usefulFootprintLargura + " m");
        Log.v("Grid", "nRealFootprintLargura: " + nRealFootprintLargura);
        Log.v("Grid", "distanceBetweenFootprintLargura: " + distanceBetweenFootprintLargura + " m");
        Log.v("Grid", "offsetLargura: " + offsetLargura + " %");
        Log.v("Grid", "overlapLargura: " + overlapLargura + " %?");
        Log.v("Grid", "bearingLargura: " + bearingLargura + " °");
        Log.v("Grid", "lengthLargura: " + lengthLargura + " m");
        Log.v("Grid", "offsetFootprintAltura: " + offsetFootprintAltura + " m");
        Log.v("Grid", "distanceLargura: " + distanceLargura + " m");
        Log.v("Grid", "usefulFootprintAltura: " + usefulFootprintAltura + " m");
        Log.v("Grid", "nRealFootprintAltura: " + nRealFootprintAltura);
        Log.v("Grid", "distanceBetweenFootprintAltura: " + distanceBetweenFootprintAltura + " m");

        Log.v("Grid Overlap", "realOverlapLargura: "
                + distanceBetweenFootprintLargura / CaptureArea.getFootprintLargura() * 100 + " % "
                + (1 - distanceBetweenFootprintLargura / CaptureArea.getFootprintLargura()) * 100 + " %");
        Log.v("Grid Overlap", "realOverlapAltura: "
                + distanceBetweenFootprintAltura / CaptureArea.getFootprintAltura() * 100 + " % "
                + (1 - distanceBetweenFootprintAltura / CaptureArea.getFootprintAltura()) * 100 + " %");
        Log.v("Grid Overlap", "OverlapLargura: "
                + usefulFootprintLargura / CaptureArea.getFootprintLargura() * 100 + " % "
                + (1 - usefulFootprintLargura / CaptureArea.getFootprintLargura()) * 100 + " %");
        Log.v("Grid Overlap", "OverlapAltura: "
                + usefulFootprintAltura / CaptureArea.getFootprintAltura() * 100 + " % "
                + (1 - usefulFootprintAltura / CaptureArea.getFootprintAltura()) * 100 + " %");
        return cells;
    }

    List<List<Node>> createStcGrid(List<LatLng> rectangleVertices) {
        if (rectangleVertices.size() < 4)
            return new ArrayList<>();

        List<Point> coors = LatLngToPoint(rectangleVertices);
        Point coor1 = coors.get(0);
        Point coor2 = coors.get(1);
        //Point coor3 = coors.get(2);
        Point coor4 = coors.get(3);

        Log.v("Grid", "StcGrid:");

        double offsetLargura = 0.5;
        double offsetAltura = 0.5;
        double overlapLargura = CaptureArea.getOverlapLargura();
        double overlapAltura = CaptureArea.getOverlapAltura();

        double bearingLargura = EarthCalc.vincenty.bearing(coor1, coor4);
        double lengthLargura = EarthCalc.vincenty.distance(coor1, coor4);
        double offsetFootprintAltura = CaptureArea.getFootprintAltura() * offsetAltura;
        if (lengthLargura < CaptureArea.getFootprintAltura())
            offsetFootprintAltura = lengthLargura * offsetAltura;
        double distanceLargura = lengthLargura - (2 * offsetFootprintAltura);
        double usefulFootprintAltura = CaptureArea.getFootprintAltura() * (1 - overlapAltura);
        int nRealFootprintAltura = (int) Math.ceil(distanceLargura / usefulFootprintAltura);
        if (lengthLargura < CaptureArea.getFootprintAltura())
            nRealFootprintAltura = 0;
        else if (nRealFootprintAltura % 2 == 0)
            nRealFootprintAltura++;
        double distanceBetweenFootprintAltura = distanceLargura / nRealFootprintAltura;

        double bearingAltura = EarthCalc.vincenty.bearing(coor1, coor2);
        double lengthAltura = EarthCalc.vincenty.distance(coor1, coor2);
        double offsetFootprintLargura = CaptureArea.getFootprintLargura() * offsetLargura;
        if (lengthAltura < CaptureArea.getFootprintLargura())
            offsetFootprintLargura = lengthAltura * offsetLargura;
        double distanceAltura = lengthAltura - (2 * offsetFootprintLargura);
        double usefulFootprintLargura = CaptureArea.getFootprintLargura() * (1 - overlapLargura);
        int nRealFootprintLargura = (int) Math.ceil(distanceAltura / usefulFootprintLargura);
        if (lengthAltura < CaptureArea.getFootprintLargura())
            nRealFootprintLargura = 0;
        else if (nRealFootprintLargura % 2 == 0)
            nRealFootprintLargura++;
        double distanceBetweenFootprintLargura = distanceAltura / nRealFootprintLargura;

        List<List<LatLng>> cells = new ArrayList<>();
        double currentDistanceAltura = offsetFootprintLargura;
        for (int i1 = 0; i1 <= nRealFootprintLargura; i1++) {
            Point currentCoorLargura = EarthCalc.gcd.pointAt(coor1, bearingAltura, currentDistanceAltura);
            double currentDistanceLargura = offsetFootprintAltura;
            List<LatLng> row = new ArrayList<>();
            for (int i2 = 0; i2 <= nRealFootprintAltura; i2++) {
                Point currentCoorAltura = EarthCalc.gcd.pointAt(currentCoorLargura, bearingLargura, currentDistanceLargura);
                row.add(new LatLng(currentCoorAltura.latitude, currentCoorAltura.longitude));
                Log.v("Grid", "currentDistanceLargura: " + currentDistanceLargura);
                currentDistanceLargura += distanceBetweenFootprintAltura;
            }
            cells.add(row);
            Log.v("Grid", "currentDistanceAltura: " + currentDistanceAltura);
            currentDistanceAltura += distanceBetweenFootprintLargura;
        }

        // Bloco de log para todas as variáveis
        Log.v("Grid", "offsetAltura: " + offsetAltura + " %");
        Log.v("Grid", "overlapAltura: " + overlapAltura + " %?");
        Log.v("Grid", "bearingAltura: " + bearingAltura + " °");
        Log.v("Grid", "lengthAltura: " + lengthAltura + " m");
        Log.v("Grid", "offsetFootprintLargura: " + offsetFootprintLargura + " m");
        Log.v("Grid", "distanceAltura: " + distanceAltura + " m");
        Log.v("Grid", "usefulFootprintLargura: " + usefulFootprintLargura + " m");
        Log.v("Grid", "nRealFootprintLargura: " + nRealFootprintLargura);
        Log.v("Grid", "distanceBetweenFootprintLargura: " + distanceBetweenFootprintLargura + " m");
        Log.v("Grid", "offsetLargura: " + offsetLargura + " %");
        Log.v("Grid", "overlapLargura: " + overlapLargura + " %?");
        Log.v("Grid", "bearingLargura: " + bearingLargura + " °");
        Log.v("Grid", "lengthLargura: " + lengthLargura + " m");
        Log.v("Grid", "offsetFootprintAltura: " + offsetFootprintAltura + " m");
        Log.v("Grid", "distanceLargura: " + distanceLargura + " m");
        Log.v("Grid", "usefulFootprintAltura: " + usefulFootprintAltura + " m");
        Log.v("Grid", "nRealFootprintAltura: " + nRealFootprintAltura);
        Log.v("Grid", "distanceBetweenFootprintAltura: " + distanceBetweenFootprintAltura + " m");

        Log.v("Grid Overlap", "realOverlapLargura: "
                + distanceBetweenFootprintLargura / CaptureArea.getFootprintLargura() * 100 + " % "
                + (1 - distanceBetweenFootprintLargura / CaptureArea.getFootprintLargura()) * 100 + " %");
        Log.v("Grid Overlap", "realOverlapAltura: "
                + distanceBetweenFootprintAltura / CaptureArea.getFootprintAltura() * 100 + " % "
                + (1 - distanceBetweenFootprintAltura / CaptureArea.getFootprintAltura()) * 100 + " %");
        Log.v("Grid Overlap", "OverlapLargura: "
                + usefulFootprintLargura / CaptureArea.getFootprintLargura() * 100 + " % "
                + (1 - usefulFootprintLargura / CaptureArea.getFootprintLargura()) * 100 + " %");
        Log.v("Grid Overlap", "OverlapAltura: "
                + usefulFootprintAltura / CaptureArea.getFootprintAltura() * 100 + " % "
                + (1 - usefulFootprintAltura / CaptureArea.getFootprintAltura()) * 100 + " %");

        offsetFootprintAltura *= 2 * (distanceBetweenFootprintAltura / CaptureArea.getFootprintAltura()
                + (1 - distanceBetweenFootprintAltura / CaptureArea.getFootprintAltura()) / 2);
        nRealFootprintAltura = (nRealFootprintAltura + 1) / 2 - 1;
        distanceBetweenFootprintAltura *= 2;

        offsetFootprintLargura *= 2 * (distanceBetweenFootprintLargura / CaptureArea.getFootprintLargura()
                + (1 - distanceBetweenFootprintLargura / CaptureArea.getFootprintLargura()) / 2);
        nRealFootprintLargura = (nRealFootprintLargura + 1) / 2 - 1;
        distanceBetweenFootprintLargura *= 2;

        List<List<Node>> nodes = new ArrayList<>();
        currentDistanceAltura = offsetFootprintLargura;
        for (int i1 = 0; i1 <= nRealFootprintLargura; i1++) {
            Point currentCoorLargura = EarthCalc.gcd.pointAt(coor1, bearingAltura, currentDistanceAltura);
            double currentDistanceLargura = offsetFootprintAltura;
            List<Node> row = new ArrayList<>();
            for (int i2 = 0; i2 <= nRealFootprintAltura; i2++) {
                Point currentCoorAltura = EarthCalc.gcd.pointAt(currentCoorLargura, bearingLargura, currentDistanceLargura);
                Node node = new Node();
                node.node = new LatLng(currentCoorAltura.latitude, currentCoorAltura.longitude);
                node.cells.add(cells.get(i1 * 2).get(i2 * 2));
                node.cells.add(cells.get(i1 * 2).get(i2 * 2 + 1));
                node.cells.add(cells.get(i1 * 2 + 1).get(i2 * 2));
                node.cells.add(cells.get(i1 * 2 + 1).get(i2 * 2 + 1));
                row.add(node);
                Log.v("Grid", "currentDistanceLargura: " + currentDistanceLargura);
                currentDistanceLargura += distanceBetweenFootprintAltura;
            }
            nodes.add(row);
            Log.v("Grid", "currentDistanceAltura: " + currentDistanceAltura);
            currentDistanceAltura += distanceBetweenFootprintLargura;
        }
        mBearingLargura = bearingLargura;

        // Bloco de log para todas as variáveis
        Log.v("Grid", "offsetAltura: " + offsetAltura + " %");
        Log.v("Grid", "overlapAltura: " + overlapAltura + " %?");
        Log.v("Grid", "bearingAltura: " + bearingAltura + " °");
        Log.v("Grid", "lengthAltura: " + lengthAltura + " m");
        Log.v("Grid", "offsetFootprintLargura: " + offsetFootprintLargura + " m");
        Log.v("Grid", "distanceAltura: " + distanceAltura + " m");
        Log.v("Grid", "usefulFootprintLargura: " + usefulFootprintLargura + " m");
        Log.v("Grid", "nRealFootprintLargura: " + nRealFootprintLargura);
        Log.v("Grid", "distanceBetweenFootprintLargura: " + distanceBetweenFootprintLargura + " m");
        Log.v("Grid", "offsetLargura: " + offsetLargura + " %");
        Log.v("Grid", "overlapLargura: " + overlapLargura + " %?");
        Log.v("Grid", "bearingLargura: " + bearingLargura + " °");
        Log.v("Grid", "lengthLargura: " + lengthLargura + " m");
        Log.v("Grid", "offsetFootprintAltura: " + offsetFootprintAltura + " m");
        Log.v("Grid", "distanceLargura: " + distanceLargura + " m");
        Log.v("Grid", "usefulFootprintAltura: " + usefulFootprintAltura + " m");
        Log.v("Grid", "nRealFootprintAltura: " + nRealFootprintAltura);
        Log.v("Grid", "distanceBetweenFootprintAltura: " + distanceBetweenFootprintAltura + " m");

        Log.v("Grid Overlap", "realOverlapLargura: "
                + distanceBetweenFootprintLargura / CaptureArea.getFootprintLargura() * 100 + " % "
                + (1 - distanceBetweenFootprintLargura / CaptureArea.getFootprintLargura()) * 100 + " %");
        Log.v("Grid Overlap", "realOverlapAltura: "
                + distanceBetweenFootprintAltura / CaptureArea.getFootprintAltura() * 100 + " % "
                + (1 - distanceBetweenFootprintAltura / CaptureArea.getFootprintAltura()) * 100 + " %");
        Log.v("Grid Overlap", "OverlapLargura: "
                + usefulFootprintLargura / CaptureArea.getFootprintLargura() * 100 + " % "
                + (1 - usefulFootprintLargura / CaptureArea.getFootprintLargura()) * 100 + " %");
        Log.v("Grid Overlap", "OverlapAltura: "
                + usefulFootprintAltura / CaptureArea.getFootprintAltura() * 100 + " % "
                + (1 - usefulFootprintAltura / CaptureArea.getFootprintAltura()) * 100 + " %");
        return nodes;
    }
}