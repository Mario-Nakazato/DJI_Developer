package com.dev.coverpathplan;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;

import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;

import java.util.ArrayList;
import java.util.List;

public class OrientedBoundingBox {
    private List<Coordinate> coordinate;
    private Polygon obb;
    private static final GeometryFactory geometryFactory = new GeometryFactory();
    OrientedBoundingBox() {}

    Polygon getPlg() {
        return obb;
    }

    void setPlg(Polygon polygon) {
        obb = polygon;
    }

    boolean isPolygon() {
        return coordinate.size() >= 3;
    }

    void createOrientedBoundingBox(List<LatLng> vertex) {
        coordinate = new ArrayList<>();
        for (LatLng apex : vertex) {
            coordinate.add(new Coordinate(apex.longitude, apex.latitude));
        }

        if (isPolygon()) {
            // Garanta que a lista de coordenadas seja fechada
            if (!coordinate.get(0).equals(coordinate.get(coordinate.size() - 1))) {
                coordinate.add(coordinate.get(0));
            }

            // Criar um anel linear com as coordenadas
            Coordinate[] coordArray = coordinate.toArray(new Coordinate[coordinate.size()]);
            LinearRing linearRing = geometryFactory.createLinearRing(coordArray);

            // Criar um polígono com o anel linear
            org.locationtech.jts.geom.Polygon plgjts = geometryFactory.createPolygon(linearRing, null);

            // Calcular o diâmetro mínimo
            MinimumDiameter minimumDiameter = new MinimumDiameter(plgjts);
            Coordinate[] diametroMinimo = minimumDiameter.getMinimumRectangle().getCoordinates();

            List<LatLng> vertexobb = new ArrayList<>();
            for (Coordinate coordinate : diametroMinimo) {
                LatLng point = new LatLng(coordinate.y, coordinate.x);
                vertexobb.add(point);
            }
            obb.setPoints(vertexobb);
        }
    }
}
