package com.dev.coverpathplan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dev.coverpathplan.databinding.ActivityAoiBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class AoiActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private ActivityAoiBinding binding;
    private Button bExcluir, bGsd, bLocate, bAdd, bIsSimulating;
    private Marker markerSelected;
    private int adding = 0;
    private boolean isSimulating = false;
    private AreaOfInterest aoi;
    private JTSGeometryUtils jtsgu;
    private GeoCalcGeodeticUtils grid;
    private FlightControllerDJI dji;
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAoiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        initUI();

        // Inicializar
        jtsgu = new JTSGeometryUtils();
        grid = new GeoCalcGeodeticUtils();
        dji = new FlightControllerDJI();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap; // Prefixo 'm' significa membro da classe

        cameraUpdate();
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Inicializar
        aoi = new AreaOfInterest(mMap);

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latlng) {
                switch (adding) {
                    case 1:
                        // Anterior
                        break;
                    case 2:
                        if (aoi.addVertex(latlng)) {
                            aoi.setObb(jtsgu.calculateOrientedBoundingBox(aoi.getAoiVertex()));
                            aoi.setGrid(new ArrayList<>());
                            aoi.setBoustrophedonPath();
                        }
                        break;
                    case 3:
                        createPath();
                        // Posterior
                        break;
                    default:
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
                    aoi.setObb(jtsgu.calculateOrientedBoundingBox(aoi.getAoiVertex()));
                    aoi.setGrid(new ArrayList<>());
                    aoi.setBoustrophedonPath();
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

    @Override
    protected void onResume() {
        super.onResume();
        createPath();
        onProductConnectionChange();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        dji.onDestroyController();
        dji.onDestroySimulator();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.locate) {
            updateDroneLocation.execute();
            cameraUpdate(); // Locate the drone's place
        } else if (id == R.id.add) {
            add();
        }
    }

    private void initUI() {
        bExcluir = findViewById(R.id.excluir);

        bExcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (markerSelected == null)
                    return;
                if (aoi.deleteVertex(markerSelected)) {
                    aoi.setObb(jtsgu.calculateOrientedBoundingBox(aoi.getAoiVertex()));
                    aoi.setGrid(new ArrayList<>());
                    aoi.setBoustrophedonPath();
                }
                markerSelected = null;
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

        bIsSimulating = findViewById(R.id.simulate);

        bIsSimulating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSimulating = !isSimulating;
                if (isSimulating)
                    bIsSimulating.setText("Controlar");
                else
                    bIsSimulating.setText("Simular");
                onProductConnectionChange();
            }
        });

        bLocate = findViewById(R.id.locate);
        bAdd = findViewById(R.id.add);

        bLocate.setOnClickListener(this);
        bAdd.setOnClickListener(this);
    }

    private void add() {
        adding++;
        switch (adding) {
            case 1:
                bAdd.setText("Anterior");
                break;
            case 2:
                bAdd.setText("Região");
                break;
            case 3:
                createPath();
                bAdd.setText("Posterior");
                break;
            default:
                adding = 0;
                bAdd.setText("Caminho");
        }
    }

    private void cameraUpdate() {
        LatLng latlng = dji.getLocation();

        if (latlng == null)
            latlng = new LatLng(-23.1858535, -50.6574255);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 19.0f));
    }

    private void createPath(){
        if (aoi != null) {
            //aoi.setGrid(grid.createStcGrid(aoi.getObbPoints()));
            aoi.setGrid(grid.createBoustrophedonGrid(aoi.getObbPoints()));
            aoi.setGrid(jtsgu.pointsInsidePolygons(aoi.getAoiVertex(), aoi.getGridPoints()));
            aoi.setBoustrophedonPath();
        }
    }

    private void onProductConnectionChange() {
        dji.setProduct(MainActivity.getProductInstance(), isSimulating, updateDroneLocation);
    }

    StateCallback updateDroneLocation = () -> {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LatLng latlng = dji.getLocation();
                aoi.setVant(latlng, dji.getAttitudeYaw());
            }
        });
    };

    private void showToast(final String toastMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        });
    }
}