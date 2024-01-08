package com.dev.coverpathplan;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;

import java.util.ArrayList;
import java.util.List;

public class AreaOfInterest {
    private GoogleMap map;
    private List<LatLng> vertex;
    private MarkerOptions markerOp;
    private List<Marker> vertexMarker;
    private PolygonOptions plgOp;
    private Polygon plg;
    private AreaOfInterest obb;
    private List<Coordinate> vertexjts;
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    AreaOfInterest(GoogleMap googleMap) {
        map = googleMap;
        vertex = new ArrayList<>();
        vertexMarker = new ArrayList<>();
        plgOp = new PolygonOptions().geodesic(true);
        markerOp = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
    }

    MarkerOptions getMarkerOp() {
        return markerOp;
    }

    Polygon getPlg() {
        return plg;
    }

    boolean addVertex(LatLng latLng) {
        return vertex.add(latLng);
    }

    boolean addMarker(LatLng latLng) {
        addVertex(latLng);
        Marker marker = map.addMarker(markerOp.position(latLng));
        if (marker == null)
            return false;
        marker.setTitle(marker.getId());
        return vertexMarker.add(marker);
    }

    void addVertexPolygon(LatLng latLng) {
        addMarker(latLng);
        if (plg == null)
            plg = map.addPolygon(plgOp.add(latLng));
        else
            plg.setPoints(vertex);
        if (isPolygon())
            plg.setStrokeColor(Color.GREEN);
    }

    List<LatLng> getVertex() {
        return vertex;
    }

    void setVertex(List<LatLng> latLngList) {
        vertex = latLngList;
    }

    void setMarker(List<LatLng> latLngList) {
        for (Marker vertexMarker : vertexMarker) {
            vertexMarker.remove();
        }
        setVertex(latLngList);
        for (LatLng apex : vertex) {
            Marker marker = map.addMarker(markerOp.position(apex));
            marker.setTitle(marker.getId());
            vertexMarker.add(marker);
        }
    }

    void setVertexPolygon(List<LatLng> latLngList) {
        setMarker(latLngList);
        if (plg == null)
            plg = map.addPolygon(plgOp.addAll(vertex));
        else
            plg.setPoints(vertex);
        if (isPolygon())
            plg.setStrokeColor(Color.GREEN);
    }

    boolean isPolygon() {
        return getVertex().size() >= 3;
    }

    void orientedBoundingBox() {
        if (obb == null) {
            obb = new AreaOfInterest(map);
            obb.getMarkerOp().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        }
        if (isPolygon()) {
            vertexjts = new ArrayList<>();
            for (LatLng apex : vertex) {
                vertexjts.add(new Coordinate(apex.longitude, apex.latitude));
            }
            // Garanta que a lista de coordenadas seja fechada
            if (!vertexjts.get(0).equals(vertexjts.get(vertexjts.size() - 1))) {
                vertexjts.add(vertexjts.get(0));
            }

            // Criar um anel linear com as coordenadas
            Coordinate[] coordArray = vertexjts.toArray(new Coordinate[vertexjts.size()]);
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
            obb.setVertexPolygon(vertexobb);
        }
        if (obb.isPolygon()) {
            obb.getPlg().setStrokeColor(Color.BLUE);
            obb.getPlg().setStrokeWidth(14);
            obb.getPlg().setZIndex(-1);
        }
    }
}