package com.dev.coverpathplan;

import com.google.android.gms.maps.model.LatLng;

import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

public class JTSGeometryUtils {
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    List<Coordinate> LatLngToCoordinate(List<LatLng> vertices) {
        List<Coordinate> coors = new ArrayList<>();
        for (LatLng vertex : vertices) {
            coors.add(new Coordinate(vertex.longitude, vertex.latitude));
        }
        return coors;
    }

    List<LatLng> CoordinateToLatLng(Coordinate[] coors) {
        List<LatLng> vertices = new ArrayList<>();
        for (Coordinate coor : coors) {
            vertices.add(new LatLng(coor.y, coor.x));
        }
        return vertices;
    }

    Polygon createPolygon(List<Coordinate> coors) {
        if (coors.size() < 3)
            return null;

        // Garanta que a lista de coordenadas seja fechada
        if (!coors.get(0).equals(coors.get(coors.size() - 1))) {
            coors.add(coors.get(0));
        }

        // Criar um anel linear com as coordenadas
        Coordinate[] coordArray = coors.toArray(new Coordinate[coors.size()]);
        LinearRing linearRing = geometryFactory.createLinearRing(coordArray);

        // Criar um polígono com o anel linear
        return geometryFactory.createPolygon(linearRing, null);
    }

    Coordinate[] getMinimumRectangle(Polygon polygon) {
        // Calcular o diâmetro mínimo
        MinimumDiameter minimumDiameter = new MinimumDiameter(polygon);
        return minimumDiameter.getMinimumRectangle().getCoordinates();
    }

    List<LatLng> calculateOrientedBoundingBox(List<LatLng> polygonVertices) {
        if (polygonVertices.size() < 3)
            return polygonVertices;

        List<Coordinate> coors = LatLngToCoordinate(polygonVertices);
        Polygon polygon = createPolygon(coors);
        Coordinate[] diametroMinimo = getMinimumRectangle(polygon);
        return new ArrayList<>(CoordinateToLatLng(diametroMinimo));
    }
}