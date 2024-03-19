package com.dev.coverpathplan;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.StrokeStyle;
import com.google.android.gms.maps.model.StyleSpan;

import java.util.ArrayList;
import java.util.List;

public class AreaOfInterest {
    private GoogleMap googleMap;
    private List<LatLng> aoiVertex;
    private List<Marker> aoiVertexMarker;
    private Polygon aoi;
    private Polygon obb;
    private List<LatLng> gridPoints;
    private List<Marker> gridPointsMarker;
    private Polyline boustrophedonPath;

    AreaOfInterest(GoogleMap googleMap) {
        this.googleMap = googleMap;
        aoiVertex = new ArrayList<>();
        aoiVertexMarker = new ArrayList<>();
        gridPoints = new ArrayList<>();
        gridPointsMarker = new ArrayList<>();
    }

    boolean isPolygon() {
        return aoiVertex.size() >= 3;
    }

    private boolean addVertexMarker(LatLng vertex) {
        Marker marker = googleMap.addMarker(new MarkerOptions().position(vertex).draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        marker.setTitle("V " + marker.getId());
        marker.setTag("vértice");
        return aoiVertexMarker.add(marker);
    }

    private boolean addPointMarker(LatLng point) {
        Marker marker = googleMap.addMarker(new MarkerOptions().position(point).alpha(0.64f)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        marker.setTitle("P " + marker.getId());
        marker.setTag("ponto");
        return gridPointsMarker.add(marker);
    }

    boolean addVertex(LatLng vertex) {
        try {
            aoiVertex.add(vertex);
            addVertexMarker(vertex);

            if (aoi == null)
                aoi = googleMap.addPolygon(new PolygonOptions().addAll(aoiVertex).geodesic(true));
            else
                aoi.setPoints(aoiVertex);

            if (isPolygon())
                aoi.setStrokeColor(Color.GREEN);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    boolean modifyVertex(Marker vertexMarker) {
        int i = aoiVertexMarker.indexOf(vertexMarker);

        if (i == -1)
            return false;

        try {
            aoiVertex.set(i, vertexMarker.getPosition());
            aoiVertexMarker.set(i, vertexMarker);
            aoi.setPoints(aoiVertex);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    boolean deleteVertex(Marker vertexMarker) {
        int i = aoiVertexMarker.indexOf(vertexMarker);

        if (i == -1)
            return false;

        try {
            aoiVertex.remove(i);
            aoiVertexMarker.remove(i).remove();
            aoi.setPoints(aoiVertex);

            if (!isPolygon())
                aoi.setStrokeColor(Color.BLACK);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    List<LatLng> getAoiVertex() {
        return aoiVertex;
    }

    boolean setObb(List<LatLng> vertex) {
        try {
            if (obb == null)
                obb = googleMap.addPolygon(new PolygonOptions().addAll(vertex).geodesic(true)
                        .strokeColor(Color.BLUE).strokeWidth(14).zIndex(-1));
            else
                obb.setPoints(vertex);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    List<LatLng> getObbPoints() {
        if (obb == null)
            return new ArrayList<>();
        return obb.getPoints();
    }

    boolean setGrid(List<LatLng> points) {
        for (Marker pointMarker : gridPointsMarker) {
            pointMarker.remove();
        }
        gridPoints.clear();
        gridPointsMarker.clear();
        try {
            gridPoints.addAll(points);
            for (LatLng point : gridPoints) {
                addPointMarker(point);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    List<LatLng> getGridPoints() {
        return gridPoints;
    }

    boolean setBoustrophedonPath() {
        if (gridPoints.isEmpty()) {
            if (boustrophedonPath != null){
                boustrophedonPath.remove();
                boustrophedonPath = null;
            }
            return false;
        }

        if (boustrophedonPath == null)
            boustrophedonPath = googleMap.addPolyline(new PolylineOptions()
                    .addAll(gridPoints).geodesic(true).addSpan(new StyleSpan(StrokeStyle
                            .gradientBuilder(Color.RED, Color.YELLOW).build())));
        else
            boustrophedonPath.setPoints(gridPoints);
        return true;
    }
}