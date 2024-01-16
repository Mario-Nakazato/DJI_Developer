package com.dev.coverpathplan;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;

import java.util.ArrayList;
import java.util.List;

public class AreaOfInterest {
    private List<LatLng> vertex;
    private List<Marker> vertexMarker;
    private Polygon plg;
    /* Inicializar construtor com poligono?
     * Se sim, dica deve ser semelhante ao metodo setPlg
     * */
    AreaOfInterest() {
        vertex = new ArrayList<>();
        vertexMarker = new ArrayList<>();
    }

    Polygon getPlg() {
        return plg;
    }

    /* Razão para dois parametros se deve ao fato de como o poligono é instanciado pelo Google Maps
     * e o marcador para associar ao vértice.
     * */
    void setPlg(Polygon polygon, Marker marker) {
        plg = polygon;
        addVertex(marker);
    }

    void addVertex(Marker marker) {
        vertex.add(marker.getPosition());
        vertexMarker.add(marker);
        plg.setPoints(vertex);

        if (isPolygon())
            plg.setStrokeColor(Color.GREEN);
    }

    boolean isPolygon() {
        return vertex.size() >= 3;
    }

    Marker setMarker(Marker marker){
        if (!vertexMarker.contains(marker))
            return null;

        vertex.set(vertexMarker.indexOf(marker), marker.getPosition());
        Marker setMarker = vertexMarker.set(vertexMarker.indexOf(marker), marker);

        plg.setPoints(vertex);
        return setMarker;
    }
}