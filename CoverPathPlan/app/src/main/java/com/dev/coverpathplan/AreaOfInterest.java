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
    private Marker vant;
    private List<LatLng> initialPoints;
    private List<Marker> initialPointsMarker;
    private Polyline initialPath;
    private List<LatLng> finalPoints;
    private List<Marker> finalPointsMarker;
    private Polyline finalPath;

    AreaOfInterest(GoogleMap googleMap) {
        this.googleMap = googleMap;
        aoiVertex = new ArrayList<>();
        aoiVertexMarker = new ArrayList<>();
        gridPoints = new ArrayList<>();
        gridPointsMarker = new ArrayList<>();
        initialPoints = new ArrayList<>();
        initialPointsMarker = new ArrayList<>();
        finalPoints = new ArrayList<>();
        finalPointsMarker = new ArrayList<>();
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
        Marker marker = googleMap.addMarker(new MarkerOptions().position(point).alpha(0.32f)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        marker.setTitle("P " + marker.getId());
        marker.setTag("ponto");
        return gridPointsMarker.add(marker);
    }

    private boolean addInitialPointMarker(LatLng point) {
        Marker marker = googleMap.addMarker(new MarkerOptions().position(point).alpha(0.64f).draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        marker.setTitle("I " + marker.getId());
        marker.setTag("inicial");
        return initialPointsMarker.add(marker);
    }

    private boolean addFinalPointMarker(LatLng point) {
        Marker marker = googleMap.addMarker(new MarkerOptions().position(point).alpha(0.64f).draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        marker.setTitle("F " + marker.getId());
        marker.setTag("final");
        return finalPointsMarker.add(marker);
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
            if (boustrophedonPath != null) {
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

    boolean setVant(LatLng position, double droneRotationYaw) {
        try {
            if (vant == null) {
                vant = googleMap.addMarker(new MarkerOptions().position(position).title("VANT").zIndex(1)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft)).flat(true)
                        .rotation((float) droneRotationYaw).anchor(0.5f, 0.5f));
                vant.setTag("vant");
            } else {
                if (position == null)
                    vant.setVisible(false);
                else
                    vant.setVisible(true);
                vant.setPosition(position);
                vant.setRotation((float) droneRotationYaw);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    void setVisibleVertex(boolean visible) {
        for (Marker vertexMarker : aoiVertexMarker) {
            vertexMarker.setVisible(visible);
        }
    }

    void setVisibleObb(boolean visible) {
        if (obb != null)
            obb.setVisible(visible);
    }

    void setDraggableInitial(boolean visible) {
        for (Marker pointMarker : initialPointsMarker) {
            pointMarker.setDraggable(visible);
        }
    }

    void setDraggableFinal(boolean visible) {
        for (Marker pointMarker : finalPointsMarker) {
            pointMarker.setDraggable(visible);
        }
    }

    boolean addInitialPoint(LatLng point) {
        try {
            initialPoints.add(point);
            addInitialPointMarker(point);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    boolean modifyInitialPoint(Marker pointMarker) {
        int i = initialPointsMarker.indexOf(pointMarker);

        if (i == -1)
            return false;

        try {
            initialPoints.set(i, pointMarker.getPosition());
            initialPointsMarker.set(i, pointMarker);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    boolean deleteInitialPoint(Marker pointMarker) {
        int i = initialPointsMarker.indexOf(pointMarker);

        if (i == -1)
            return false;

        try {
            initialPoints.remove(i);
            initialPointsMarker.remove(i).remove();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    boolean addFinalPoint(LatLng point) {
        try {
            finalPoints.add(point);
            addFinalPointMarker(point);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    boolean modifyFinalPoint(Marker pointMarker) {
        int i = finalPointsMarker.indexOf(pointMarker);

        if (i == -1)
            return false;

        try {
            finalPoints.set(i, pointMarker.getPosition());
            finalPointsMarker.set(i, pointMarker);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    boolean deleteFinalPoint(Marker pointMarker) {
        int i = finalPointsMarker.indexOf(pointMarker);

        if (i == -1)
            return false;

        try {
            finalPoints.remove(i);
            finalPointsMarker.remove(i).remove();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    boolean setInitialPath() {
        if (initialPoints.isEmpty()) {
            if (initialPath != null) {
                initialPath.remove();
                initialPath = null;
            }
            return false;
        }

        List<LatLng> join = new ArrayList<>(initialPoints);
        if (!gridPoints.isEmpty())
            join.add(gridPoints.get(0));
        else
            if (!finalPoints.isEmpty())
                join.add(finalPoints.get(0));

        if (initialPath == null)
            initialPath = googleMap.addPolyline(new PolylineOptions()
                    .addAll(join).geodesic(true).addSpan(new StyleSpan(Color.RED)));
        else
            initialPath.setPoints(join);
        return true;
    }

    boolean setFinalPath() {
        if (finalPoints.isEmpty()) {
            if (finalPath != null) {
                finalPath.remove();
                finalPath = null;
            }
            return false;
        }

        List<LatLng> join = new ArrayList<>();
        if (!gridPoints.isEmpty())
            join.add(gridPoints.get(gridPoints.size() - 1));
        join.addAll(finalPoints);

        if (finalPath == null)
            finalPath = googleMap.addPolyline(new PolylineOptions()
                    .addAll(join).geodesic(true).addSpan(new StyleSpan(Color.YELLOW)));
        else
            finalPath.setPoints(join);
        return true;
    }

    List<LatLng> getInitialPoints() {
        return initialPoints;
    }

    List<LatLng> getFinalPoints() {
        return finalPoints;
    }

    List<LatLng> getPathPoint() {
        List<LatLng> join = new ArrayList<>();
        join.addAll(getInitialPoints());
        join.addAll(getGridPoints());
        join.addAll(getFinalPoints());
        return join;
    }
}