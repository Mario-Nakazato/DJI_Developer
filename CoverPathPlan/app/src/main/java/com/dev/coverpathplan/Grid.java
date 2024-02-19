package com.dev.coverpathplan;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;

import java.util.ArrayList;
import java.util.List;

public class Grid {
    private List<Point> points;
    private List<LatLng> cells;
    private List<Marker> cellsMarker;
    Grid() {
        cells = new ArrayList<>();
        cellsMarker = new ArrayList<>();
    }
    List<LatLng> getCells() {
        return cells;
    }

    void setCells(List<LatLng> latLngList) {
        cells = latLngList;
    }

    void addCellsMarker(Marker marker) {
        cellsMarker.add(marker);
    }

    List<Marker> getCellsMarker() {
        return  cellsMarker;
    }

    void removeAllCells() {
        for (Marker marker : cellsMarker) {
            marker.remove();
        }
        cells.clear();
        cellsMarker.clear();
    }

    void createGrid(List<LatLng> vertex) {
        points = new ArrayList<>();
        for (LatLng apex : vertex) {
            points.add(Point.at(com.grum.geocalc.Coordinate.fromDegrees(apex.latitude), com.grum.geocalc.Coordinate.fromDegrees(apex.longitude)));
        }

        Point c1 = points.get(0);
        Point c2 = points.get(1);
        Point c3 = points.get(2);
        Point c4 = points.get(3);

        Point coor;
        int pos = 2; // inteiro por enquanto a ideia é usar ou a metade do gsd ou inteiro 2 sendo 50% e 1 sendo 100%
        double overlap = 2.5; // 2 para overlap é para ser o 50% caso for 1 será um alinhado ao outro 2.5 60% e 5 para 80% 0.5 para 200%

        /*// Centroide
        double bear = EarthCalc.vincenty.bearing(c1, c3);
        double distance = EarthCalc.vincenty.distance(c1, c3);
        coor = EarthCalc.gcd.pointAt(c1, bear, distance / 2);
        cells.add(new LatLng(coor.latitude, coor.longitude));

        // lateral
        bear = EarthCalc.vincenty.bearing(c1, c2);
        distance = EarthCalc.vincenty.distance(c1, c2);

        double dis = distance - (2 * distance / Math.ceil(distance / CaptureArea.getFootprintLargura()) / pos);
        double d = distance / Math.ceil(distance / CaptureArea.getFootprintLargura()) / pos; // Posição do primeiro ponto
        int j = (int) Math.ceil(dis / CaptureArea.getFootprintLargura() * overlap); // Quantidade de divisão "internas" depois do d

        for (int i = 0; i <= j; i++) {
            coor = EarthCalc.gcd.pointAt(c1, bear, d);
            cells.add(new LatLng(coor.latitude, coor.longitude));
            d += dis / Math.ceil(dis / CaptureArea.getFootprintLargura() * overlap);
        }

        // lateral
        bear = EarthCalc.vincenty.bearing(c1, c4);
        distance = EarthCalc.vincenty.distance(c1, c4);

        dis = distance - (2 * distance / Math.ceil(distance / CaptureArea.getFootprintAltura()) / pos);
        d = distance / Math.ceil(distance / CaptureArea.getFootprintAltura()) / pos; // Posição do primeiro ponto
        j = (int) Math.ceil(dis / CaptureArea.getFootprintAltura() * overlap); // Quantidade de divisão "internas" depois do d

        for (int i = 0; i <= j; i++) {
            coor = EarthCalc.gcd.pointAt(c1, bear, d);
            cells.add(new LatLng(coor.latitude, coor.longitude));
            d += dis / Math.ceil(dis / CaptureArea.getFootprintAltura() * overlap);
        }*/

        // codigo
        double bear1 = EarthCalc.vincenty.bearing(c1, c2);
        double distance1 = EarthCalc.vincenty.distance(c1, c2);

        double dis1 = distance1 - (2 * distance1 / Math.ceil(distance1 / CaptureArea.getFootprintLargura()) / pos);
        double d1 = distance1 / Math.ceil(distance1 / CaptureArea.getFootprintLargura()) / pos; // Posição do primeiro ponto
        int j1 = (int) Math.ceil(dis1 / CaptureArea.getFootprintLargura() * overlap); // Quantidade de divisão "internas" depois do d

        double bear2 = EarthCalc.vincenty.bearing(c1, c4);
        double distance2 = EarthCalc.vincenty.distance(c1, c4);

        double dis2 = distance2 - (2 * distance2 / Math.ceil(distance2 / CaptureArea.getFootprintAltura()) / pos);
        int j2 = (int) Math.ceil(dis2 / CaptureArea.getFootprintAltura() * overlap); // Quantidade de divisão "internas" depois do d
        for (int i1 = 0; i1 <= j1; i1++) {
            Point coor0 = EarthCalc.gcd.pointAt(c1, bear1, d1);
            double d2 = distance2 / Math.ceil(distance2 / CaptureArea.getFootprintAltura()) / pos; // Posição do primeiro ponto
            for (int i2 = 0; i2 <= j2; i2++) {
                coor = EarthCalc.gcd.pointAt(coor0, bear2, d2);
                cells.add(new LatLng(coor.latitude, coor.longitude));
                d2 += dis2 / Math.ceil(dis2 / CaptureArea.getFootprintAltura() * overlap);
            }
            d1 += dis1 / Math.ceil(dis1 / CaptureArea.getFootprintLargura() * overlap);
        }
    }
}