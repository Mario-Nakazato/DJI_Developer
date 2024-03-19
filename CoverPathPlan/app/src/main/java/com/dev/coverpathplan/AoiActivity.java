package com.dev.coverpathplan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.dev.coverpathplan.databinding.ActivityAoiBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.simulator.InitializationData;
import dji.common.flightcontroller.simulator.SimulatorState;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.Simulator;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

public class AoiActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private ActivityAoiBinding binding;
    private Button bExcluir;
    private Button bGrade;
    private Button bGsd;
    private Marker vant;
    private Marker markerSelected;
    private AreaOfInterest aoi;
    private JTSGeometryUtils jtsgu;
    private GeoCalcGeodeticUtils grid;

    protected static final String TAG = "GSDemoActivity";
    private Button locate, add, clear;
    private Button config, upload, start, stop;
    private boolean isAdd = false;
    private double droneLocationLat = 181, droneLocationLng = 181;
    private List<Marker> mMarkers = new ArrayList<>();
    private float mSpeed = 8.0f;
    private List<Waypoint> waypointList = new ArrayList<>();
    public static WaypointMission.Builder waypointMissionBuilder;
    private FlightController mFlightController;
    private Simulator mFlightControllerSimulator;
    private boolean issimulating = true;
    private WaypointMissionOperator instance;
    private WaypointMissionFinishedAction mFinishedAction = WaypointMissionFinishedAction.NO_ACTION;
    private WaypointMissionHeadingMode mHeadingMode = WaypointMissionHeadingMode.AUTO;
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
        }
    };
    private WaypointMissionOperatorListener eventNotificationListener = new WaypointMissionOperatorListener() {
        @Override
        public void onDownloadUpdate(WaypointMissionDownloadEvent downloadEvent) {
        }

        @Override
        public void onUploadUpdate(WaypointMissionUploadEvent uploadEvent) {
        }

        @Override
        public void onExecutionUpdate(WaypointMissionExecutionEvent executionEvent) {
        }

        @Override
        public void onExecutionStart() {
        }

        @Override
        public void onExecutionFinish(@Nullable final DJIError error) {
            setResultToToast("Execução concluída: " + (error == null ? "Sucesso!" : error.getDescription()));
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
        filter.addAction(DJIDemoApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        initUI();
        addListener();

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

        bGrade = findViewById(R.id.grade);

        bGrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //aoi.setGrid(grid.createStcGrid(aoi.getObbPoints()));
                aoi.setGrid(grid.createBoustrophedonGrid(aoi.getObbPoints()));
                aoi.setGrid(jtsgu.pointsInsidePolygons(aoi.getAoiVertex(), aoi.getGridPoints()));
                aoi.setBoustrophedonPath();

                List<LatLng> wpList = aoi.getGridPoints();

                for (LatLng wp : wpList) {
                    Waypoint mWaypoint = new Waypoint(wp.latitude, wp.longitude, (float) CaptureArea.getAltitude());
                    //Add Waypoints to Waypoint arraylist;
                    if (waypointMissionBuilder != null) {
                        waypointList.add(mWaypoint);
                        waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
                    } else {
                        waypointMissionBuilder = new WaypointMission.Builder();
                        waypointList.add(mWaypoint);
                        waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
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
        jtsgu = new JTSGeometryUtils();
        grid = new GeoCalcGeodeticUtils();
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
                if (isAdd) {
                    markWaypoint(latlng);
                    Waypoint mWaypoint = new Waypoint(latlng.latitude, latlng.longitude, (float) CaptureArea.getAltitude());
                    //Add Waypoints to Waypoint arraylist;
                    if (waypointMissionBuilder != null) {
                        waypointList.add(mWaypoint);
                        waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
                    } else {
                        waypointMissionBuilder = new WaypointMission.Builder();
                        waypointList.add(mWaypoint);
                        waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
                    }
                } else {
                    if (aoi.addVertex(latlng)) {
                        aoi.setObb(jtsgu.calculateOrientedBoundingBox(aoi.getAoiVertex()));
                        aoi.setGrid(new ArrayList<>());
                        aoi.setBoustrophedonPath();
                    }
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
        initFlightController();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        removeListener();
        if (mFlightControllerSimulator != null) {
            mFlightControllerSimulator.stop(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {

                }
            });
            mFlightControllerSimulator.setStateCallback(null);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.locate) {
            updateDroneLocation();
            cameraUpdate(); // Locate the drone's place
        } else if (id == R.id.add) {
            enableDisableAdd();
        } else if (id == R.id.clear) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //mMap.clear();
                }
            });
            for (Marker m : mMarkers) {
                m.remove();
            }
            waypointList.clear();
            if (waypointMissionBuilder != null)
                waypointMissionBuilder.waypointList(waypointList);
            updateDroneLocation();
        } else if (id == R.id.config) {
            showSettingDialog();
        } else if (id == R.id.upload) {
            uploadWayPointMission();
        } else if (id == R.id.start) {
            startWaypointMission();
        } else if (id == R.id.stop) {
            stopWaypointMission();
        }
    }

    private void initUI() {
        locate = (Button) findViewById(R.id.locate);
        add = (Button) findViewById(R.id.add);
        clear = (Button) findViewById(R.id.clear);
        config = (Button) findViewById(R.id.config);
        upload = (Button) findViewById(R.id.upload);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);

        locate.setOnClickListener(this);
        add.setOnClickListener(this);
        clear.setOnClickListener(this);
        config.setOnClickListener(this);
        upload.setOnClickListener(this);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
    }

    private void onProductConnectionChange() {
        initFlightController();
    }

    private void initFlightController() {
        BaseProduct product = DJIDemoApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                if (!issimulating)
                    mFlightController = ((Aircraft) product).getFlightController();
                else
                    mFlightControllerSimulator = ((Aircraft) product).getFlightController().getSimulator();
                product.getGimbal().
                        rotate(new Rotation.Builder().pitch(-90)
                                .mode(RotationMode.ABSOLUTE_ANGLE)
                                .yaw(Rotation.NO_ROTATION)
                                .roll(Rotation.NO_ROTATION)
                                .build(), new CommonCallbacks.CompletionCallback() {

                            @Override
                            public void onResult(DJIError error) {
                            }
                        });
            }
        }

        if (mFlightController != null) {
            mFlightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(FlightControllerState djiFlightControllerCurrentState) {
                    droneLocationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                    droneLocationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                    updateDroneLocation();
                }
            });
        }

        if (mFlightControllerSimulator != null) {
            mFlightControllerSimulator.start(InitializationData.createInstance(new LocationCoordinate2D(-23.1858535, -50.6574255), 10, 10),
                    new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {

                        }
                    });
            mFlightControllerSimulator.setStateCallback(new SimulatorState.Callback() {
                @Override
                public void onUpdate(SimulatorState simulatorState) {
                    droneLocationLat = simulatorState.getLocation().getLatitude();
                    droneLocationLng = simulatorState.getLocation().getLongitude();
                    updateDroneLocation();
                }
            });
        }
    }

    //Add Listener for WaypointMissionOperator
    private void addListener() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().addListener(eventNotificationListener);
        }
    }

    private void removeListener() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().removeListener(eventNotificationListener);
        }
    }

    public WaypointMissionOperator getWaypointMissionOperator() {
        if (instance == null) {
            if (DJISDKManager.getInstance().getMissionControl() != null) {
                instance = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
            }
        }
        return instance;
    }

    public static boolean checkGpsCoordination(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    // Update the drone location based on states from MCU.
    private void updateDroneLocation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LatLng latlng;

                if (checkGpsCoordination(droneLocationLat, droneLocationLng)) {
                    latlng = new LatLng(droneLocationLat, droneLocationLng);
                } else {
                    latlng = new LatLng(-23.1858535, -50.6574255);
                }

                if (vant == null) {
                    vant = mMap.addMarker(new MarkerOptions().position(latlng).title("VANT")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft)).flat(true).anchor(0.5f, 0.5f));
                    vant.setTag("vant");
                } else
                    vant.setPosition(latlng);
            }
        });
    }

    private void markWaypoint(LatLng point) {
        //Create MarkerOptions object
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        Marker marker = mMap.addMarker(markerOptions);
        mMarkers.add(marker);
    }

    private void cameraUpdate() {
        LatLng latlng;

        if (checkGpsCoordination(droneLocationLat, droneLocationLng))
            latlng = new LatLng(droneLocationLat, droneLocationLng);
        else
            latlng = new LatLng(-23.1858535, -50.6574255);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 19.0f));
    }

    private void enableDisableAdd() {
        if (isAdd) {
            add.setText("Adicionar");
        } else {
            add.setText("Sair");
        }
        isAdd = !isAdd;
    }

    private void showSettingDialog() {
        LinearLayout wayPointSettings = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_waypointsetting, null);
        final TextView wpAltitude_TV = (TextView) wayPointSettings.findViewById(R.id.altitude);
        RadioGroup speed_RG = (RadioGroup) wayPointSettings.findViewById(R.id.speed);
        RadioGroup actionAfterFinished_RG = (RadioGroup) wayPointSettings.findViewById(R.id.actionAfterFinished);
        RadioGroup heading_RG = (RadioGroup) wayPointSettings.findViewById(R.id.heading);

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
                Log.d(TAG, "Select finish action");
                if (checkedId == R.id.finishNone) {
                    mFinishedAction = WaypointMissionFinishedAction.NO_ACTION;
                } else if (checkedId == R.id.finishGoHome) {
                    mFinishedAction = WaypointMissionFinishedAction.GO_HOME;
                } else if (checkedId == R.id.finishAutoLanding) {
                    mFinishedAction = WaypointMissionFinishedAction.AUTO_LAND;
                } else if (checkedId == R.id.finishToFirst) {
                    mFinishedAction = WaypointMissionFinishedAction.GO_FIRST_WAYPOINT;
                }
            }
        });

        heading_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "Select heading");
                if (checkedId == R.id.headingNext) {
                    mHeadingMode = WaypointMissionHeadingMode.AUTO;
                } else if (checkedId == R.id.headingInitDirec) {
                    mHeadingMode = WaypointMissionHeadingMode.USING_INITIAL_DIRECTION;
                } else if (checkedId == R.id.headingRC) {
                    mHeadingMode = WaypointMissionHeadingMode.CONTROL_BY_REMOTE_CONTROLLER;
                } else if (checkedId == R.id.headingWP) {
                    mHeadingMode = WaypointMissionHeadingMode.USING_WAYPOINT_HEADING;
                }
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("")
                .setView(wayPointSettings)
                .setPositiveButton("Finalizar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String altitudeString = wpAltitude_TV.getText().toString();
                        //altitude = Integer.parseInt(nulltoIntegerDefalt(altitudeString));
                        Log.e(TAG, "altitude " + CaptureArea.getAltitude());
                        Log.e(TAG, "speed " + mSpeed);
                        Log.e(TAG, "mFinishedAction " + mFinishedAction);
                        Log.e(TAG, "mHeadingMode " + mHeadingMode);
                        configWayPointMission();
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

    private void configWayPointMission() {
        if (waypointMissionBuilder == null) {
            waypointMissionBuilder = new WaypointMission.Builder().finishedAction(mFinishedAction)
                    .headingMode(mHeadingMode)
                    .autoFlightSpeed(mSpeed)
                    .maxFlightSpeed(mSpeed)
                    .flightPathMode(WaypointMissionFlightPathMode.NORMAL);

        } else {
            waypointMissionBuilder.finishedAction(mFinishedAction)
                    .headingMode(mHeadingMode)
                    .autoFlightSpeed(mSpeed)
                    .maxFlightSpeed(mSpeed)
                    .flightPathMode(WaypointMissionFlightPathMode.NORMAL);

        }

        WaypointAction action = new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1);
        if (!waypointMissionBuilder.getWaypointList().isEmpty()) {
            for (int i = 0; i < waypointMissionBuilder.getWaypointList().size(); i++) {
                waypointMissionBuilder.getWaypointList().get(i).altitude = (float) CaptureArea.getAltitude();
                waypointMissionBuilder.getWaypointList().get(i).addAction(action);
            }
            setResultToToast("Atitude definido do ponto de referência com sucesso");
        }

        DJIError error = getWaypointMissionOperator().loadMission(waypointMissionBuilder.build());
        if (error == null) {
            setResultToToast("Upload do ponto de referência com sucesso");
        } else {
            setResultToToast("Falha no upload do ponto de referência " + error.getDescription());
        }
    }

    private void uploadWayPointMission() {
        getWaypointMissionOperator().uploadMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                if (error == null) {
                    setResultToToast("Upload da missão com sucesso!");
                } else {
                    setResultToToast("Falha no upload da missão, erro: " + error.getDescription() + " tentando novamente...");
                    getWaypointMissionOperator().retryUploadMission(null);
                }
            }
        });
    }

    private void startWaypointMission() {
        getWaypointMissionOperator().startMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                setResultToToast("Início da missão: " + (error == null ? "Com sucesso" : error.getDescription()));
            }
        });
    }

    private void stopWaypointMission() {
        getWaypointMissionOperator().stopMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                setResultToToast("Parada da missão: " + (error == null ? "Com sucesso" : error.getDescription()));
            }
        });
    }

    private void setResultToToast(final String string) {
        AoiActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AoiActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Excluir
    String nulltoIntegerDefalt(String value) {
        if (!isIntValue(value)) value = "0";
        return value;
    }

    boolean isIntValue(String val) {
        try {
            val = val.replace(" ", "");
            Integer.parseInt(val);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}