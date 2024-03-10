package com.dev.coverpathplan;

import com.google.android.gms.maps.model.LatLng;
import com.grum.geocalc.Coordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;

import java.util.ArrayList;
import java.util.List;

public class GeoCalcGeodeticUtils {
    List<Point> LatLngToPoint(List<LatLng> vertices) {
        List<Point> points = new ArrayList<>();
        for (LatLng vertex : vertices) {
            points.add(Point.at(Coordinate.fromDegrees(vertex.latitude), Coordinate.fromDegrees(vertex.longitude)));
        }
        return points;
    }

    List<LatLng> createGrid(List<LatLng> rectangleVertices) {
        if (rectangleVertices.size() < 4) // rectangleVertices é 5 por que é um anel linear fechado
            return new ArrayList<>();

        List<Point> coors = LatLngToPoint(rectangleVertices);
        Point coor1 = coors.get(0);
        Point coor2 = coors.get(1);
        //Point coor3 = coors.get(2);
        Point coor4 = coors.get(3);

        double offsetLargura = 0.5; // deslocamento da posição
        double offsetAltura = 0.5;
        double overlapLargura = 0.6; // overlap %, caso for 0 será um alinhado não pode ser 1
        double overlapAltura = 0.6;

        // Referencia a Largura
        double bearingLargura = EarthCalc.vincenty.bearing(coor1, coor2); // Direção
        double lengthLargura = EarthCalc.vincenty.distance(coor1, coor2); // Comprimento do lado do retangulo
        int nFootprintLargura = (int) Math.ceil(lengthLargura / CaptureArea.getFootprintLargura()); // Número de área de captura sem overlap, ajuda a definir a distancia inicial da borda
        double realFootprintLargura = lengthLargura / nFootprintLargura; // Comprimento real necessário de cada área de captura
        double offsetFootprintLargura = realFootprintLargura * offsetLargura; // offset da área de captura
        double distanceLargura = lengthLargura - (2 * offsetFootprintLargura); // distancia entre o primeiro e o último ponto
        double usefulFootprintLargura = CaptureArea.getFootprintLargura() * (1 - overlapLargura); // Área util de captura
        int nRealFootprintLargura = (int) Math.ceil(distanceLargura / usefulFootprintLargura); // Número de área de captura com overlap, depois da distancia inicial
        double distanceBetweenFootprintLargura = distanceLargura / nRealFootprintLargura; // Distancia entre área de captura

        // Referencia a altura
        double bearingAltura = EarthCalc.vincenty.bearing(coor1, coor4);
        double lengthAltura = EarthCalc.vincenty.distance(coor1, coor4);
        int nFootprintAltura = (int) Math.ceil(lengthAltura / CaptureArea.getFootprintAltura());
        double realFootprintAltura = lengthAltura / nFootprintAltura;
        double offsetFootprintAltura = realFootprintAltura * offsetAltura;
        double distanceAltura = lengthAltura - (2 * offsetFootprintAltura);
        double usefulFootprintAltura = CaptureArea.getFootprintAltura() * (1 - overlapAltura);
        int nRealFootprintAltura = (int) Math.ceil(distanceAltura / usefulFootprintAltura);
        double distanceBetweenFootprintAltura = distanceAltura / nRealFootprintAltura;

        List<LatLng> cells = new ArrayList<>();
        double currentDistanceLargura = offsetFootprintLargura; // Distancia inicial do primeiro ponto
        for (int i1 = 0; i1 <= nRealFootprintLargura; i1++) {
            Point currentCoorLargura = EarthCalc.gcd.pointAt(coor1, bearingLargura, currentDistanceLargura);
            double currentDistanceAltura = offsetFootprintAltura;
            for (int i2 = 0; i2 <= nRealFootprintAltura; i2++) {
                Point currentCoorAltura = EarthCalc.gcd.pointAt(currentCoorLargura, bearingAltura, currentDistanceAltura);
                cells.add(new LatLng(currentCoorAltura.latitude, currentCoorAltura.longitude));
                currentDistanceAltura += distanceBetweenFootprintAltura;
            }
            currentDistanceLargura += distanceBetweenFootprintLargura;
        }
        return cells;
    }

    List<LatLng> createBoustrophedonGrid(List<LatLng> rectangleVertices) {
        if (rectangleVertices.size() < 4) // rectangleVertices é 5 por que é um anel linear fechado
            return new ArrayList<>();

        List<Point> coors = LatLngToPoint(rectangleVertices);
        Point coor1 = coors.get(0);
        Point coor2 = coors.get(1);
        //Point coor3 = coors.get(2);
        Point coor4 = coors.get(3);

        double offsetLargura = 0.5;
        double offsetAltura = 0.5;
        double overlapLargura = 0.6;
        double overlapAltura = 0.6;

        double bearingLargura = EarthCalc.vincenty.bearing(coor1, coor2);
        double lengthLargura = EarthCalc.vincenty.distance(coor1, coor2);
        int nFootprintLargura = (int) Math.ceil(lengthLargura / CaptureArea.getFootprintLargura());
        double realFootprintLargura = lengthLargura / nFootprintLargura;
        double offsetFootprintLargura = realFootprintLargura * offsetLargura;
        double distanceLargura = lengthLargura - (2 * offsetFootprintLargura);
        double usefulFootprintLargura = CaptureArea.getFootprintLargura() * (1 - overlapLargura);
        int nRealFootprintLargura = (int) Math.ceil(distanceLargura / usefulFootprintLargura);
        double distanceBetweenFootprintLargura = distanceLargura / nRealFootprintLargura;

        double bearingAltura = EarthCalc.vincenty.bearing(coor1, coor4);
        double lengthAltura = EarthCalc.vincenty.distance(coor1, coor4);
        int nFootprintAltura = (int) Math.ceil(lengthAltura / CaptureArea.getFootprintAltura());
        double realFootprintAltura = lengthAltura / nFootprintAltura;
        double offsetFootprintAltura = realFootprintAltura * offsetAltura;
        double distanceAltura = lengthAltura - (2 * offsetFootprintAltura);
        double usefulFootprintAltura = CaptureArea.getFootprintAltura() * (1 - overlapAltura);
        int nRealFootprintAltura = (int) Math.ceil(distanceAltura / usefulFootprintAltura);
        double distanceBetweenFootprintAltura = distanceAltura / nRealFootprintAltura;

        List<LatLng> cells = new ArrayList<>();
        double currentDistanceLargura = offsetFootprintLargura;
        for (int i1 = 0; i1 <= nRealFootprintLargura; i1++) {
            Point currentCoorLargura = EarthCalc.gcd.pointAt(coor1, bearingLargura, currentDistanceLargura);
            double currentDistanceAltura = offsetFootprintAltura;
            if (i1 % 2 == 0) { // Se a linha atual for par, adicione os pontos da esquerda para a direita
                for (int i2 = 0; i2 <= nRealFootprintAltura; i2++) {
                    Point currentCoorAltura = EarthCalc.gcd.pointAt(currentCoorLargura, bearingAltura, currentDistanceAltura);
                    cells.add(new LatLng(currentCoorAltura.latitude, currentCoorAltura.longitude));
                    currentDistanceAltura += distanceBetweenFootprintAltura;
                }
            } else { // Se a linha atual for ímpar, adicione os pontos da direita para a esquerda
                currentDistanceAltura = distanceAltura + currentDistanceAltura;
                for (int i2 = nRealFootprintAltura; i2 >= 0; i2--) {
                    Point currentCoorAltura = EarthCalc.gcd.pointAt(currentCoorLargura, bearingAltura, currentDistanceAltura);
                    cells.add(new LatLng(currentCoorAltura.latitude, currentCoorAltura.longitude));
                    currentDistanceAltura -= distanceBetweenFootprintAltura;
                }
            }
            currentDistanceLargura += distanceBetweenFootprintLargura;
        }
        return cells;
    }
}