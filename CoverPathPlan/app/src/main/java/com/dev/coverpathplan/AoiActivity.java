package com.dev.coverpathplan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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
import com.google.android.gms.maps.model.PolygonOptions;

public class AoiActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityAoiBinding binding;
    private Button bExcluir;
    private Marker vant;
    private Marker markerSelected;
    private AreaOfInterest aoi;

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
                aoi.removeMarker(markerSelected);
                markerSelected = null;
            }
        });

        // Iniciarlizar
        aoi = new AreaOfInterest();
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

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latlng) {
                Marker marker = mMap.addMarker(new MarkerOptions().position(latlng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).draggable(true));
                marker.setTitle("V " + marker.getId());
                marker.setTag("vértice");

                if (aoi.getPlg() == null)
                    aoi.setPlg(mMap.addPolygon(new PolygonOptions().add(latlng).geodesic(true)), marker);
                else
                    aoi.addVertex(marker);
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
                aoi.setMarker(marker);
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
                else {
                    markerSelected = null;
                }
                return false;
            }
        });
    }
}