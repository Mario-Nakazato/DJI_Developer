package com.dev.coverpathplan;

import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.dev.coverpathplan.databinding.ActivityMapsBinding;

public class AoiActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private AreaOfInterest aoi;

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

        LatLng vant = new LatLng(-23.1858535, -50.6574255);
        mMap.addMarker(new MarkerOptions().position(vant).title("VANT")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft)).flat(true).anchor(0.5f, 0.5f));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(vant));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vant, 19.0f));
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.setMyLocationEnabled(true);

        mMap.getUiSettings().setMapToolbarEnabled(false);

        aoi = new AreaOfInterest(mMap);

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                aoi.addVertexPolygon(latLng);
            }
        });
    }
}