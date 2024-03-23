package com.dev.coverpathplan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.dev.coverpathplan.databinding.ActivityAoiBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

import dji.common.error.DJIError;

public class AoiActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private ActivityAoiBinding binding;
    private Button bExcluir, bGsd, bLocate, bAdd, bIsSimulating, bConfig, bRun;
    private Marker markerSelected;
    private int adding = 0;
    private boolean isSimulating = false;
    private boolean isRunning = false;
    private float mSpeed = 8.0f;
    private int mFinishedAction = 1;
    private AreaOfInterest aoi;
    private JTSGeometryUtils jtsgu;
    private GeoCalcGeodeticUtils grid;
    private FlightControllerDJI dji;
    private MissionOperatorDJI mission;
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (onProductConnectionChange())
                showToast("Drone conectado");
            else
                showToast("Drone desconectado");
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
        mission = new MissionOperatorDJI();
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
                        aoi.addInitialPoint(latlng);
                        aoi.setInitialPath();
                        break;
                    case 2:
                        if (aoi.addVertex(latlng)) {
                            aoi.setObb(jtsgu.calculateOrientedBoundingBox(aoi.getAoiVertex()));
                            aoi.setGrid(new ArrayList<>());
                            aoi.setBoustrophedonPath();
                        }
                        break;
                    case 3:
                        aoi.addFinalPoint(latlng);
                        aoi.setInitialPath();
                        aoi.setFinalPath();
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
                switch (adding) {
                    case 1:
                        if (aoi.modifyInitialPoint(marker)) {
                            aoi.setInitialPath();
                        }
                        break;
                    case 2:
                        if (aoi.modifyVertex(marker)) {
                            aoi.setObb(jtsgu.calculateOrientedBoundingBox(aoi.getAoiVertex()));
                            aoi.setGrid(new ArrayList<>());
                            aoi.setBoustrophedonPath();
                        }
                        break;
                    case 3:
                        if (aoi.modifyFinalPoint(marker)) {
                            aoi.setInitialPath();
                            aoi.setFinalPath();
                        }
                        break;
                    default:
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
        loadPath();
        onProductConnectionChange();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        dji.onDestroyController();
        dji.onDestroySimulator();
        mission.removeListener();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.locate) {
            updateDroneLocation.execute();
            cameraUpdate(); // Locate the drone's place
        } else if (id == R.id.add) {
            addPath();
        } else if (id == R.id.config) {
            showSettingDialog();
        } else if (id == R.id.run) {
            isRunning = !isRunning;
            if (isRunning)
                bRun.setText("Parar");
            else
                bRun.setText("Iniciar");
            run();
        }
    }

    private void initUI() {
        bExcluir = findViewById(R.id.excluir);

        bExcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (markerSelected == null)
                    return;
                switch (adding) {
                    case 1:
                        if (aoi.deleteInitialPoint(markerSelected)) {
                            aoi.setInitialPath();
                        }
                        break;
                    case 2:
                        if (aoi.deleteVertex(markerSelected)) {
                            aoi.setObb(jtsgu.calculateOrientedBoundingBox(aoi.getAoiVertex()));
                            aoi.setGrid(new ArrayList<>());
                            aoi.setBoustrophedonPath();
                        }
                        break;
                    case 3:
                        if (aoi.deleteFinalPoint(markerSelected)) {
                            aoi.setInitialPath();
                            aoi.setFinalPath();
                        }
                        break;
                    default:
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
                if (isSimulating) {
                    bIsSimulating.setText("Controlar");
                    showToast("Simulador ligado");
                } else {
                    bIsSimulating.setText("Simular");
                    showToast("Simulador desligado");
                }
                onProductConnectionChange();
            }
        });

        bLocate = findViewById(R.id.locate);
        bAdd = findViewById(R.id.add);

        bConfig = (Button) findViewById(R.id.config);
        bRun = (Button) findViewById(R.id.run);

        bLocate.setOnClickListener(this);
        bAdd.setOnClickListener(this);
        bConfig.setOnClickListener(this);
        bRun.setOnClickListener(this);
    }

    private void addPath() {
        adding++;
        switch (adding) {
            case 1:
                showToast("Configure o caminho inicial");
                aoi.setDraggableInitial(true);
                bAdd.setText("Inicial");
                break;
            case 2:
                showToast("Configure caminho de cobertura");
                aoi.setDraggableInitial(false);
                aoi.setVisibleVertex(true);
                aoi.setVisibleObb(true);
                aoi.setInitialPath();
                bAdd.setText("Cobertura");
                break;
            case 3:
                showToast("Configure caminho final");
                aoi.setVisibleVertex(false);
                aoi.setVisibleObb(false);
                aoi.setDraggableFinal(true);
                createPath();
                bAdd.setText("Final");
                break;
            default:
                adding = 0;
                aoi.setDraggableFinal(false);
                loadPath();
                bAdd.setText("Caminho");
        }
    }

    private void loadPath() {
        if (aoi == null)
            return;
        if (mission.setPathWaypoint(aoi.getPathPoint())) {
            DJIError error = mission.loadMission(mFinishedAction, mSpeed);
            if (error == null) {
                showToast("Carregamento da missão com sucesso");
            } else
                showToast("Falha no carregamento da missão, erro: " + error.getDescription());
        }
    }

    private void run() {
        if (isRunning) {
            mission.uploadMission(uploadMission);
        } else {
            mission.stopMission(stopMission);
        }
    }

    ErrorCallback uploadMission = (DJIError error) -> {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (error == null) {
                    showToast("Upload da missão com sucesso!");
                    mission.startMission(startMission);
                } else {
                    isRunning = false;
                    bRun.setText("Iniciar");
                    showToast("Falha no upload da missão, erro: " + error.getDescription() + " tentando novamente...");
                }
            }
        });
    };

    ErrorCallback startMission = (DJIError error) -> {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast("Início da missão" + (error == null ? " com sucesso" : ", erro: " + error.getDescription()));
            }
        });
    };

    ErrorCallback stopMission = (DJIError error) -> {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast("Parada da missão" + (error == null ? " com sucesso" : ", erro: " + error.getDescription()));
            }
        });
    };

    ErrorCallback finishMission = (DJIError error) -> {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isRunning = false;
                bRun.setText("Iniciar");
                showToast("Missão concluída" + (error == null ? " com sucesso!" : ", erro: " + error.getDescription()));
            }
        });
    };

    private void cameraUpdate() {
        LatLng latlng = dji.getLocation();

        if (latlng == null)
            latlng = new LatLng(-23.1858535, -50.6574255);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 19.0f));
    }

    private void createPath() {
        if (aoi != null) {
            //aoi.setGrid(grid.createStcGrid(aoi.getObbPoints()));
            aoi.setGrid(grid.createBoustrophedonGrid(aoi.getObbPoints()));
            aoi.setGrid(jtsgu.pointsInsidePolygons(aoi.getAoiVertex(), aoi.getGridPoints()));
            aoi.setBoustrophedonPath();
            aoi.setInitialPath();
            aoi.setFinalPath();
        }
    }

    private void showSettingDialog() {
        LinearLayout wayPointSettings = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_waypointsetting, null);
        RadioGroup speed_RG = wayPointSettings.findViewById(R.id.speed);
        RadioGroup actionAfterFinished_RG = wayPointSettings.findViewById(R.id.actionAfterFinished);
        RadioGroup photo_RG = wayPointSettings.findViewById(R.id.takePhoto);

        speed_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.lowSpeed) {
                    mSpeed = 2.0f;
                } else if (checkedId == R.id.MidSpeed) {
                    mSpeed = 4.0f;
                } else if (checkedId == R.id.HighSpeed) {
                    mSpeed = 8.0f;
                }
            }
        });

        actionAfterFinished_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.finishNone) {
                    mFinishedAction = 0;
                } else if (checkedId == R.id.finishGoHome) {
                    mFinishedAction = 1;
                } else if (checkedId == R.id.finishAutoLanding) {
                    mFinishedAction = 2;
                } else if (checkedId == R.id.finishToFirst) {
                    mFinishedAction = 3;
                }
            }
        });

        photo_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.yes) {
                    mission.setTakePhoto(true);
                } else if (checkedId == R.id.no) {
                    mission.setTakePhoto(false);
                }
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("")
                .setView(wayPointSettings)
                .setPositiveButton("Finalizar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loadPath();
                    }

                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    private boolean onProductConnectionChange() {
        mission.setMissionOperator(MainActivity.getMissionOperatorInstance(), finishMission);
        return dji.setProduct(MainActivity.getProductInstance(), isSimulating, updateDroneLocation);
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