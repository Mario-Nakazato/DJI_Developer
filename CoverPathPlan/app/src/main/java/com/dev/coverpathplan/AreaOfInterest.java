package com.dev.coverpathplan;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;

public class AreaOfInterest {
    private GoogleMap map;
    private List<LatLng> vertex;
    private MarkerOptions markerOp;
    private List<Marker> vertexMarker;
    private PolygonOptions plgOp;
    private Polygon plg;

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
}