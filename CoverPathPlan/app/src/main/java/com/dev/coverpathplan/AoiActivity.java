package com.dev.coverpathplan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.dev.coverpathplan.databinding.ActivityAoiBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class AoiActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityAoiBinding binding;
    private Button bExcluir;
    private Button bGrade;
    private Button bGsd;
    private Marker vant;
    private Marker markerSelected;
    private AreaOfInterest aoi;
    private JTSGeometryUtils obb;
    private Grid grid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAoiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        bExcluir = findViewById(R.id.excluir);

        bExcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (markerSelected == null)
                    return;
                if (aoi.deleteVertex(markerSelected)) {
                    aoi.setObb(obb.calculateOrientedBoundingBox(aoi.getAoiVertex()));
                    //grid.removeAllCells();
                }
                markerSelected = null;
            }
        });

        bGrade = findViewById(R.id.grade);

        bGrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                grid.removeAllCells();
                if (aoi.isPolygon()) {
                    //grid.createGrid(obb.getPlg().getPoints());
                    //grid.setCells(obb.pointsInsidePolygons(grid.getCells()));
                    for (LatLng latLng : grid.getCells()) {
                        Marker cell = mMap.addMarker(new MarkerOptions().position(latLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                        cell.setTitle(cell.getId());
                        grid.addCellsMarker(cell);
                    }
                }
            }
        });

        bGsd = findViewById(R.id.gsd);

        bGsd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AoiActivity.this, GsdActivity.class);
                startActivity(intent);
            }
        });

        // Inicializar
        obb = new JTSGeometryUtils();
        grid = new Grid();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap; // Prefixo 'm' significa membro da classe

        LatLng latlng = new LatLng(-23.1858535, -50.6574255);
        vant = mMap.addMarker(new MarkerOptions().position(latlng).title("VANT")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft)).flat(true).anchor(0.5f, 0.5f));
        vant.setTag("vant");

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 19.0f));

        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Inicializar
        aoi = new AreaOfInterest(mMap);

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latlng) {
                if (aoi.addVertex(latlng)) {
                    aoi.setObb(obb.calculateOrientedBoundingBox(aoi.getAoiVertex()));
                    //grid.removeAllCells();
                }
            }
        });

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(@NonNull Marker marker) {
                Log.v("Debug", "Drag " + String.valueOf(marker.getPosition()));
            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                Log.v("Debug", "Drag end " + String.valueOf(marker.getPosition()));
                if (aoi.modifyVertex(marker)) {
                    aoi.setObb(obb.calculateOrientedBoundingBox(aoi.getAoiVertex()));
                    //grid.removeAllCells();
                }
            }

            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {
                Log.v("Debug", "Drag start " + String.valueOf(marker.getPosition()));
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                Log.v("Debug", "Click " + String.valueOf(marker.getPosition()));
                if (!marker.equals(markerSelected))
                    markerSelected = marker;
                else
                    markerSelected = null;
                return false;
            }
        });
    }
}