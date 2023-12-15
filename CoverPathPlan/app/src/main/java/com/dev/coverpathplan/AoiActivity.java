package com.dev.coverpathplan;

import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.dev.coverpathplan.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.algorithm.MinimumDiameter;

import java.util.ArrayList;
import java.util.List;

public class AoiActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private List<Point> calccoorcbo = new ArrayList<>(); // Caixa Delimitadora Orientada GeoCalc
    private List<Coordinate> coorplgcdo = new ArrayList<>(); // Caixa Delimitadora Orientada jts
    private List<LatLng> coorcdo = new ArrayList<>(); // Caixa Delimitadora Orientada google maps
    private List<Marker> vertexMarkers = new ArrayList<>();
    private Polygon cdo = null; // Caixa Delimitadora Orientada google maps
    private Polygon plg = null;
    private Marker debugMarker = null;
    private List<Marker> debugMarkerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap; // Prefixo 'm' significa membro da classe

        LatLng ctrl = new LatLng(-23.1858658, -50.6573493);
        mMap.addMarker(new MarkerOptions().position(ctrl).title("Controle remoto"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ctrl));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ctrl, 19.0f));
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.setMyLocationEnabled(true);

        mMap.getUiSettings().setMapToolbarEnabled(false);

        LatLng vant = new LatLng(-23.1858535, -50.6574255);
        mMap.addMarker(new MarkerOptions().position(vant).title("VANT")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft)).flat(true).anchor(0.5f, 0.5f));
        mMap.addMarker(new MarkerOptions().position(vant));
        pent();
        rect();
        rect1();

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Marker marker = mMap.addMarker(new MarkerOptions().position(latLng));
                marker.setTitle(marker.getId());
                vertexMarkers.add(marker);

                if (plg == null)
                    plg = mMap.addPolygon(new PolygonOptions().add(latLng).geodesic(true).zIndex(4));
                else
                    plg.setPoints(getPolygonPoints());

                if (vertexMarkers.size() >= 3) {
                    plg.setStrokeColor(Color.GREEN);

                    GeometryFactory geometryFactory = new GeometryFactory();

                    getRecPoints();
                    // Garanta que a lista de coordenadas seja fechada
                    if (!coorplgcdo.get(0).equals(coorplgcdo.get(coorplgcdo.size() - 1))) {
                        coorplgcdo.add(coorplgcdo.get(0));
                    }

                    // Criar um anel linear com as coordenadas
                    Coordinate[] coordArray = coorplgcdo.toArray(new Coordinate[coorplgcdo.size()]);
                    LinearRing linearRing = geometryFactory.createLinearRing(coordArray);

                    // Criar um polígono com o anel linear
                    org.locationtech.jts.geom.Polygon plgjts = geometryFactory.createPolygon(linearRing, null);

                    // Calcular o diâmetro mínimo
                    MinimumDiameter minimumDiameter = new MinimumDiameter(plgjts);
                    Coordinate[] diametroMinimo = minimumDiameter.getMinimumRectangle().getCoordinates();

                    coorcdo = new ArrayList<>();
                    calccoorcbo = new ArrayList<>();
                    for (Coordinate coordinate : diametroMinimo) {
                        LatLng point = new LatLng(coordinate.y, coordinate.x);
                        coorcdo.add(point);
                        calccoorcbo.add(Point.at(com.grum.geocalc.Coordinate.fromDegrees(point.latitude), com.grum.geocalc.Coordinate.fromDegrees(point.longitude)));
                    }

                    if (cdo != null)
                        cdo.remove();
                    cdo = mMap.addPolygon(new PolygonOptions().addAll(coorcdo)
                            .strokeColor(Color.BLUE)
                            .strokeWidth(14)
                            .geodesic(true));

                    Log.v("Distancia", "Distância caixa delimitadora orientado " + calccoorcbo.size());
                    // Calcular e exibir a distância entre cada par de pontos consecutivos
                    for (int i = 0; i < calccoorcbo.size() - 1; i++) {
                        Point coor1 = calccoorcbo.get(i);
                        Point coor2 = calccoorcbo.get(i + 1);
                        double bear = EarthCalc.vincenty.bearing(coor1, coor2);
                        double distance = EarthCalc.vincenty.distance(coor1, coor2);
                        Log.v("Distancia", "Distância entre ponto " + i + " e ponto " + (i + 1) + ": " + distance + " metros");
                        Log.v("Distancia", "latitude: " + coor1.latitude + " longitude: " + coor1.longitude);
                        Log.v("Distancia", "Direção: " + String.valueOf(bear));
                    }

                    Point coor1 = calccoorcbo.get(0);
                    Point coor2 = calccoorcbo.get(2);

                    // Centroide
                    double bear = EarthCalc.vincenty.bearing(coor1, coor2);
                    double distance = EarthCalc.vincenty.distance(coor1, coor2);
                    Point coordebug = EarthCalc.gcd.pointAt(coor1, bear, distance / 2);

                    if (debugMarker == null) {
                        debugMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(coordebug.latitude, coordebug.longitude))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                        debugMarker.setTitle(debugMarker.getId() + " Centroide");
                    } else
                        debugMarker.setPosition(new LatLng(coordebug.latitude, coordebug.longitude));

                    for (Marker marke : debugMarkerList) {
                        marke.remove();
                    }
                    debugMarkerList.clear();

                    // lateral
                    coor2 = calccoorcbo.get(1);
                    bear = EarthCalc.vincenty.bearing(coor1, coor2);
                    distance = EarthCalc.vincenty.distance(coor1, coor2);

                    int pos = 2; // inteiro por enquanto a ideia é usar ou a metade do gsd ou inteiro 2 sendo 50% e 1 sendo 100%
                    double dis = distance - (2 * distance / Math.ceil(distance / 21.41) / pos);
                    double overlap = 2.5; // 2 para overlap é para ser o 50% caso for 1 será um alinhado ao outro 2.5 60% e 5 para 80% 0.5 para 200%
                    double d = distance / Math.ceil(distance / 21.41) / pos; // Posição do primeiro ponto
                    int j = (int) Math.ceil(dis / 21.41 * overlap); // Quantidade de divisão "internas" depois do d

                    for (int i = 0; i <= j; i++) {
                        Point coor = EarthCalc.gcd.pointAt(coor1, bear, d);
                        marker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(coor.latitude, coor.longitude))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                        marker.setTitle(marker.getId() + " " + d);
                        debugMarkerList.add(marker);
                        d += dis / Math.ceil(dis / 21.41 * overlap);
                    }

                    // lateral
                    coor2 = calccoorcbo.get(3);
                    bear = EarthCalc.vincenty.bearing(coor1, coor2);
                    distance = EarthCalc.vincenty.distance(coor1, coor2);

                    dis = distance - (2 * distance / Math.ceil(distance / 15.79) / pos);
                    d = distance / Math.ceil(distance / 15.79) / pos; // Posição do primeiro ponto
                    j = (int) Math.ceil(dis / 15.79 * overlap); // Quantidade de divisão "internas" depois do d

                    for (int i = 0; i <= j; i++) {
                        Point coor = EarthCalc.gcd.pointAt(coor1, bear, d);
                        marker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(coor.latitude, coor.longitude))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        marker.setTitle(marker.getId() + " " + d);
                        debugMarkerList.add(marker);
                        d += dis / Math.ceil(dis / 15.79 * overlap);
                    }

                    // codigo
                    Point c1 = calccoorcbo.get(0);
                    Point c2 = calccoorcbo.get(1);
                    Point c3 = calccoorcbo.get(3);

                    double bear1 = EarthCalc.vincenty.bearing(c1, c2);
                    double distance1 = EarthCalc.vincenty.distance(c1, c2);

                    double dis1 = distance1 - (2 * distance1 / Math.ceil(distance1 / 21.41) / pos);
                    double d1 = distance1 / Math.ceil(distance1 / 21.41) / pos; // Posição do primeiro ponto
                    int j1 = (int) Math.ceil(dis1 / 21.41 * overlap); // Quantidade de divisão "internas" depois do d

                    double bear2 = EarthCalc.vincenty.bearing(c1, c3);
                    double distance2 = EarthCalc.vincenty.distance(c1, c3);

                    double dis2 = distance2 - (2 * distance2 / Math.ceil(distance2 / 15.79) / pos);
                    int j2 = (int) Math.ceil(dis2 / 15.79 * overlap); // Quantidade de divisão "internas" depois do d
                    for (int i1 = 0; i1 <= j1; i1++) {
                        Point coor0 = EarthCalc.gcd.pointAt(c1, bear1, d1);
                        double d2 = distance2 / Math.ceil(distance2 / 15.79) / pos; // Posição do primeiro ponto
                        for (int i2 = 0; i2 <= j2; i2++) {
                            Point coo = EarthCalc.gcd.pointAt(coor0, bear2, d2);

                            Coordinate pontoDentro = new Coordinate(coo.longitude, coo.latitude);
                            org.locationtech.jts.geom.Point ponto = geometryFactory.createPoint(pontoDentro);
                            // Verificando se o ponto está dentro do polígono
                            if (plgjts.contains(ponto)) {
                                marker = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(coo.latitude, coo.longitude))
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                                marker.setTitle(marker.getId() + " " + d1 + " " + d2);
                                debugMarkerList.add(marker);
                            }
                            d2 += dis2 / Math.ceil(dis2 / 15.79 * overlap);
                        }
                        d1 += dis1 / Math.ceil(dis1 / 21.41 * overlap);
                    }
                }
            }

            // Método para obter os pontos do polígono com base nos marcadores
            private List<LatLng> getPolygonPoints() {
                List<LatLng> points = new ArrayList<>();
                for (Marker marker : vertexMarkers) {
                    points.add(marker.getPosition());
                }
                return points;
            }

            private void getRecPoints() {
                coorplgcdo = new ArrayList<>();
                for (Marker marker : vertexMarkers) {
                    coorplgcdo.add(new Coordinate(marker.getPosition().longitude, marker.getPosition().latitude));
                }
            }
        });

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
            }
        });
    }

    private void pent() {
        LatLng center = new LatLng(-23.1858, -50.6575);

        double radius = 0.0001;

        PolygonOptions polygonOptions = new PolygonOptions();
        for (int i = 0; i < 5; i++) {
            double angle = (Math.PI / 180) * (i * 360 / 5);
            double x = center.latitude + radius * Math.cos(angle);
            double y = center.longitude + radius * Math.sin(angle);
            polygonOptions.add(new LatLng(x, y));
        }

        Polygon polygon = mMap.addPolygon(polygonOptions);

        polygon.setFillColor(Color.argb(128, 255, 0, 0));
        polygon.setStrokeColor(Color.RED);

        LatLng startPoint = polygon.getPoints().get(0);
        LatLng endPoint = polygon.getPoints().get(2);

        PolylineOptions lineOptions = new PolylineOptions()
                .add(startPoint, endPoint)
                .color(Color.BLACK)
                .width(5);

        mMap.addPolyline(lineOptions);
    }

    private void rect() {
        LatLng center = new LatLng(-45, 45);

        double radius = 30;

        PolygonOptions polygonOptions = new PolygonOptions();
        List<Point> polygonPoints = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            double angle = (Math.PI / 180) * (i * 360 / 4) + (Math.PI / 4);
            double x = center.latitude + radius * Math.cos(angle);
            double y = center.longitude + radius * Math.sin(angle);
            LatLng point = new LatLng(x, y);
            polygonOptions.add(point);
            polygonPoints.add(Point.at(com.grum.geocalc.Coordinate.fromDegrees(point.latitude), com.grum.geocalc.Coordinate.fromDegrees(point.longitude)));
        }

        Polygon polygon = mMap.addPolygon(polygonOptions.geodesic(true));

        polygon.setFillColor(Color.argb(128, 255, 0, 0));
        polygon.setStrokeColor(Color.RED);

        // Calcular e exibir a distância entre cada par de pontos consecutivos
        for (int i = 0; i < polygonPoints.size() - 1; i++) {
            Point coor1 = polygonPoints.get(i);
            Point coor2 = polygonPoints.get(i + 1);
            double distance = EarthCalc.vincenty.distance(coor1, coor2);
            Log.v("Distancia", "Distância entre ponto " + i + " e ponto " + (i + 1) + ": " + distance + " metros");
        }
        Point coor1 = polygonPoints.get(3);
        Point coor2 = polygonPoints.get(0);
        double distance = EarthCalc.vincenty.distance(coor1, coor2);
        Log.v("Distancia", "Distância entre ponto " + 3 + " e ponto " + 0 + ": " + distance + " metros");

        // Adicionar a linha entre o primeiro e o último ponto do polígono
        LatLng startPoint = polygon.getPoints().get(0);
        LatLng endPoint = polygon.getPoints().get(polygon.getPoints().size() - 3);

        PolylineOptions lineOptions = new PolylineOptions()
                .add(startPoint, endPoint)
                .color(Color.BLACK)
                .width(5).geodesic(true);

        mMap.addPolyline(lineOptions);
    }

    private void rect1() {
        double distancia = 3897840;

        com.grum.geocalc.Coordinate lat = com.grum.geocalc.Coordinate.fromDegrees(-45);
        com.grum.geocalc.Coordinate lng = com.grum.geocalc.Coordinate.fromDegrees(45);

        PolygonOptions polygonOptions = new PolygonOptions();
        List<Point> polygonPoints = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            double angulo = (Math.PI / 180) * (i * 360 / 4) + (Math.PI / 4);

            // Calcular as coordenadas usando EarthCalc.gcd.pointAt
            Point point = EarthCalc.gcd.pointAt(Point.at(lat, lng), Math.toDegrees(angulo), distancia);

            LatLng latLngPoint = new LatLng(point.latitude, point.longitude);
            polygonOptions.add(latLngPoint);
            polygonPoints.add(Point.at(com.grum.geocalc.Coordinate.fromDegrees(latLngPoint.latitude), com.grum.geocalc.Coordinate.fromDegrees(latLngPoint.longitude)));
        }

        Polygon polygon = mMap.addPolygon(polygonOptions.geodesic(true));

        polygon.setFillColor(Color.argb(128, 255, 0, 0));
        polygon.setStrokeColor(Color.RED);

        // Calcular e exibir a distância entre cada par de pontos consecutivos
        for (int i = 0; i < polygonPoints.size() - 1; i++) {
            Point coor1 = polygonPoints.get(i);
            Point coor2 = polygonPoints.get(i + 1);
            double distance = EarthCalc.vincenty.distance(coor1, coor2);
            Log.v("Distancia", "Distância entre ponto " + i + " e ponto " + (i + 1) + ": " + distance + " metros");
            Log.v("Distancia", "latitude: " + coor1.latitude + " longitude: " + coor1.longitude);
        }

        // Adicionar a linha entre o primeiro e o último ponto do polígono
        LatLng startPoint = polygon.getPoints().get(0);
        LatLng endPoint = polygon.getPoints().get(polygon.getPoints().size() - 3);

        PolylineOptions lineOptions = new PolylineOptions()
                .add(startPoint, endPoint)
                .color(Color.BLACK)
                .width(5).geodesic(true);

        mMap.addPolyline(lineOptions);
    }
}