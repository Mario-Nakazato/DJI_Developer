package com.dev.coverpathplan;

import static com.dev.coverpathplan.FlightState.calculateElapsedTime;
import static com.dev.coverpathplan.FlightState.convertingDoubleToHoursMinutesSecondsMilliseconds;
import static com.dev.coverpathplan.GeoCalcGeodeticUtils.calculateDistance;
import static com.dev.coverpathplan.GeoCalcGeodeticUtils.calculateTotalDistance;
import static com.dev.coverpathplan.GeoCalcGeodeticUtils.mBearingLargura;
import static com.dev.coverpathplan.MainActivity.getDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.mission.waypoint.WaypointMissionExecuteState;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;

@SuppressLint("SetTextI18n")
@SuppressWarnings({"FieldCanBeLocal", "Convert2Lambda"})
public class AoiActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private GoogleMap mMap;
    private ActivityAoiBinding binding;
    private Button bDelete, bGsd, bLocate, bAdd, bIsSimulating, bConfig, bRun, bStatus, bClone;
    private TextView tPathDistance, tPathDistanceDJI, tEstimatedTime, tEstimatedTimeDJI, tQuantityPhoto,
            tDistanceTraveled, tBearing, tInitialDateTime, tCurrentDateTime, tFinalDateTime, tElapsedTime,
            tvelocityAverageX, tvelocityAverageY, tvelocityAverageZ, tvelocityAverage, tChargeRemaining,
            tChargeRemainingInPercent, tCurrent, tVoltage, tChargeConsumption, tChargeConsumptionInPercent,
            tPathDistanceMetrics, tPathDistanceDJIMetrics, tEstimatedTimeMetrics, tEstimatedTimeDJIMetrics,
            tQuantityPhotoMetrics, tBearingMetrics;
    private RadioGroup rgSpeed, rgActionAfterFinished, rgAlgorithm, rgPhoto, rgRec, rgAspectRadio, rgOrientation;
    private LinearLayout lSettings, lMetrics, lStatus;
    private AlertDialog adSetting, adMetrics, adStatus;
    private Marker markerSelected;
    private int adding = 0, mFinishedAction = 1, algorithm = 0, quantityPhoto = 0,
            batteryChargeRemaining = 0, batteryChargeRemainingInPercent = 0, batteryVoltage = 0, batteryCurrent = 0,
            batteryChargeConsumption = 0, batteryChargeConsumptionInPercent = 0;
    private boolean isSimulating = false, isCovering = false, isRecording = true;
    private float mSpeed = 2.0f, velocityN = 0, velocityX = 0, velocityY = 0, velocityZ = 0,
            velocityAverageX = 0, velocityAverageY = 0, velocityAverageZ = 0, velocityAverage = 0;
    private double bearing = 0, distanceTraveled = 0, pathDistance = 0, pathDistanceDJI = 0;
    private String estimatedTime = "HH:mm:ss.SSS", estimatedTimeDJI = "HH:mm:ss.SSS", initialDateTime = "dd/MM/yyyy HH:mm:ss.SSS",
            currentDateTime = "dd/MM/yyyy HH:mm:ss.SSS", finalDateTime = "dd/MM/yyyy HH:mm:ss.SSS",
            elapsedTime = "HH:mm:ss.SSS";
    private DecimalFormat decimalFormatter = new DecimalFormat("0.00");
    private AreaOfInterest aoi;
    private JTSGeometryUtils jtsgu;
    private GeoCalcGeodeticUtils gcgu;
    private FlightControllerDJI dji;
    private MissionOperatorDJI mission;
    private StateCallback updateDroneLocation;
    private MissionOperatorDJICallback missionCallback;
    private BatteryStateCallback batteryCallback;
    private Battery battery;
    private Camera camera;
    private Fork graph;
    private FlightState vant;
    private Database realtime;
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (onProductConnectionChange())
                showToast("Drone conectado");
            else
                showToast("Drone desconectado");
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAoiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        initUI();

        // Inicializar
        jtsgu = new JTSGeometryUtils();
        gcgu = new GeoCalcGeodeticUtils();
        dji = new FlightControllerDJI();
        mission = new MissionOperatorDJI();
        graph = new Fork();
        vant = new FlightState();
        realtime = new Database(getDatabase().getReference());
        realtime.updateCoveragePaths();
        battery = new Battery();
        camera = new Camera();
        updateDroneLocation = (FlightState flightState) -> runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isCovering) {
                    currentDateTime = vant.currentDateTime;
                    if (initialDateTime.equals("dd/MM/yyyy HH:mm:ss.SSS"))
                        initialDateTime = currentDateTime;
                    finalDateTime = currentDateTime;
                    elapsedTime = calculateElapsedTime(initialDateTime, finalDateTime);
                    distanceTraveled += calculateDistance(new LatLng(vant.latitude, vant.longitude),
                            new LatLng(flightState.latitude, flightState.longitude));
                    velocityN++;
                    velocityX += Math.abs(vant.velocityX);
                    velocityY += Math.abs(vant.velocityY);
                    velocityZ += Math.abs(vant.velocityZ);
                    velocityAverageX = velocityX / velocityN;
                    velocityAverageY = velocityY / velocityN;
                    velocityAverageZ = velocityZ / velocityN;
                    velocityAverage = (float) Math.sqrt(velocityAverageX * velocityAverageX
                            + velocityAverageY * velocityAverageY + velocityAverageZ * velocityAverageZ);

                    if (isRecording) {
                        realtime.pathRecord(flightState.clone(), batteryChargeRemaining,
                                batteryChargeRemainingInPercent, batteryVoltage, batteryCurrent);
                        realtime.metricsRecord(pathDistance, pathDistanceDJI, estimatedTime, estimatedTimeDJI,
                                quantityPhoto, initialDateTime, finalDateTime, elapsedTime, distanceTraveled,
                                velocityAverageX, velocityAverageY, velocityAverageZ, velocityAverage,
                                batteryChargeConsumption, batteryChargeConsumptionInPercent);
                    }

                    if (adStatus.isShowing())
                        updateStatusDialog();
                }

                aoi.setVant(new LatLng(flightState.latitude, flightState.longitude), flightState.yaw);
                vant = flightState.clone();
            }
        });
        missionCallback = new MissionOperatorDJICallback() {
            @Override
            public void uploadMission(DJIError error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (error == null) {
                            showMetricsDialog();
                            showToast("Upload da missão com sucesso!");
                        } else
                            showToast("Falha no upload da missão, tente novamente... Erro: " + error.getDescription());
                    }
                });
            }

            @Override
            public void startMission(DJIError error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bRun.setText(error == null ? "Parar" : "Upload");
                        showToast("Missão iniciada" + (error == null ? " com sucesso" : ", erro: " + error.getDescription()));
                    }
                });
            }

            @Override
            public void stopMission(DJIError error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bRun.setText("Upload");
                        isCovering = false;
                        showToast("Missão interrompida" + (error == null ? " com sucesso" : ", erro: " + error.getDescription()));
                    }
                });
            }

            @Override
            public void onExecutionUpdate(WaypointMissionExecutionEvent executionEvent) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        assert executionEvent.getProgress() != null;
                        int i = executionEvent.getProgress().targetWaypointIndex;

                        if (!isCovering && i == 1
                                && bRun.getText().equals("Parar")
                                && executionEvent.getProgress().executeState == WaypointMissionExecuteState.BEGIN_ACTION) {
                            isCovering = true;

                            initialDateTime = "dd/MM/yyyy HH:mm:ss.SSS";
                            distanceTraveled = 0;
                            velocityN = 0;
                            velocityX = 0;
                            velocityY = 0;
                            velocityZ = 0;
                            batteryChargeRemaining = 0;
                            batteryChargeConsumption = 0;
                            batteryChargeRemainingInPercent = 0;
                            batteryChargeConsumptionInPercent = 0;

                            if (isRecording)
                                realtime.planningRecord(aoi.getAoiVertex(), bearing, mSpeed, mFinishedAction,
                                        algorithm == 0 ? "Boustrophedon Cellular Decomposition" : "Spanning Tree Coverage",
                                        mission.isTakePhoto(), camera.getPhotoAspectRatio() == 0 ? "4:3" : "16:9",
                                        CaptureArea.getAltitude(),
                                        CaptureArea.getGsdLargura(), CaptureArea.getGsdAltura(),
                                        CaptureArea.getOverlapLargura(), CaptureArea.getOverlapAltura(),
                                        CaptureArea.getFootprintLargura(), CaptureArea.getFootprintAltura()
                                );
                            realtime.updateCoveragePaths();
                            showToast("Caminho de cobertura iniciado ");
                        } else if (isCovering && i == executionEvent.getProgress().totalWaypointCount - 2
                                && bRun.getText().equals("Parar")
                                && executionEvent.getProgress().executeState == WaypointMissionExecuteState.FINISHED_ACTION) {
                            isCovering = false;
                            showToast("Caminho de cobertura finalizado ");
                        }
                    }
                });
            }

            @Override
            public void onExecutionFinish(DJIError error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (bRun.getText().equals("Parar"))
                            showToast("Missão concluída" + (error == null ? " com sucesso!" : ", erro: " + error.getDescription()));
                        bRun.setText("Upload");
                    }
                });
            }
        };
        batteryCallback = (int chargeRemaining, int chargeRemainingInPercent, int voltage, int current) -> runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isCovering) {
                    if (batteryChargeRemaining != 0)
                        batteryChargeConsumption += chargeRemaining - batteryChargeRemaining;
                    if (batteryChargeRemainingInPercent != 0)
                        batteryChargeConsumptionInPercent += chargeRemainingInPercent - batteryChargeRemainingInPercent;

                    batteryChargeRemaining = chargeRemaining;
                    batteryChargeRemainingInPercent = chargeRemainingInPercent;
                    batteryCurrent = current;
                    batteryVoltage = voltage;
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap; // Prefixo 'm' significa membro da classe

        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        cameraUpdate();

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latlng) {
                switch (adding) {
                    case 1:
                        aoi.addInitialPoint(latlng);
                        aoi.setInitialPath();
                        break;
                    case 2:
                        if (aoi.addVertex(latlng)) {
                            aoi.setObb(jtsgu.calculateOrientedBoundingBox(aoi.getAoiVertex()));
                            aoi.setGrid(new ArrayList<>());
                            aoi.setPathPlanning();
                            if (algorithm == 1)
                                aoi.guideMinimumSpanningTree(new ArrayList<>());
                        }
                        break;
                    case 3:
                        aoi.addFinalPoint(latlng);
                        aoi.setInitialPath();
                        aoi.setFinalPath();
                        break;
                    default:
                        if (bRun.getText().equals("Upload"))
                            showToast("Defina caminhos no botão Caminho");
                }
            }
        });

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(@NonNull Marker marker) {
                Log.v("Debug", "Drag " + marker.getPosition());
            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                Log.v("Debug", "Drag end " + marker.getPosition());
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
                            aoi.setPathPlanning();
                            if (algorithm == 1)
                                aoi.guideMinimumSpanningTree(new ArrayList<>());
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
                Log.v("Debug", "Drag start " + marker.getPosition());
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                Log.v("Debug", "Click " + marker.getPosition());
                if (!marker.equals(markerSelected))
                    markerSelected = marker;
                else
                    markerSelected = null;
                return false;
            }
        });

        // Inicializar
        aoi = new AreaOfInterest(mMap);
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
        mission.removeListener();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.delete) {
            if (markerSelected == null)
                return;

            switch (adding) {
                case 1:
                    if (aoi.deleteInitialPoint(markerSelected))
                        aoi.setInitialPath();
                    break;
                case 2:
                    if (aoi.deleteVertex(markerSelected)) {
                        aoi.setObb(jtsgu.calculateOrientedBoundingBox(aoi.getAoiVertex()));
                        aoi.setGrid(new ArrayList<>());
                        aoi.setPathPlanning();
                        if (algorithm == 1)
                            aoi.guideMinimumSpanningTree(new ArrayList<>());
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
        } else if (id == R.id.simulate) {
            if (vant.areMotorsOn)
                return;
            isSimulating = !isSimulating;
            if (isSimulating) {
                bIsSimulating.setText("Controlar");
                showToast("Simulador ligado");
            } else {
                bIsSimulating.setText("Simular");
                showToast("Simulador desligado");
            }
            onProductConnectionChange();
        } else if (id == R.id.gsd) {
            if (bRun.getText().equals("Upload")) {
                Intent intent = new Intent(AoiActivity.this, GsdActivity.class);
                startActivity(intent);
            } else
                showToast("Missão em execução, não pode modificar GSD");
        } else if (id == R.id.locate)
            cameraUpdate();
        else if (id == R.id.add)
            addPath();
        else if (id == R.id.config)
            if (bRun.getText().equals("Upload"))
                showSettingDialog();
            else
                showToast("Missão em execução, não pode configurar opções");
        else if (id == R.id.run)
            runMission();
        else if (id == R.id.status)
            showStatusDialog();
        else if (id == R.id.clone)
            duplicate();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int id = group.getId();
        if (id == R.id.speed) {
            if (checkedId == R.id.lowSpeed)
                mSpeed = 2.0f;
            else if (checkedId == R.id.midSpeed)
                mSpeed = 4.0f;
            else if (checkedId == R.id.highSpeed)
                mSpeed = 8.0f;
            else if (checkedId == R.id.veryHighSpeed)
                mSpeed = 15.0f;
        } else if (id == R.id.actionAfterFinished) {
            if (checkedId == R.id.finishNone)
                mFinishedAction = 0;
            else if (checkedId == R.id.finishGoHome)
                mFinishedAction = 1;
            else if (checkedId == R.id.finishAutoLanding)
                mFinishedAction = 2;
            else if (checkedId == R.id.finishToFirst)
                mFinishedAction = 3;
        } else if (id == R.id.algorithm) {
            if (checkedId == R.id.bcd)
                algorithm = 0;
            else if (checkedId == R.id.stc)
                algorithm = 1;
        } else if (id == R.id.takePhoto) {
            if (checkedId == R.id.yes)
                mission.setTakePhoto(true);
            else if (checkedId == R.id.no)
                mission.setTakePhoto(false);
        } else if (id == R.id.record) {
            if (checkedId == R.id.yesRecord)
                isRecording = true;
            else if (checkedId == R.id.noRecord)
                isRecording = false;
        } else if (id == R.id.aspectRatio) {
            if (checkedId == R.id.radio4_3)
                camera.setPhotoAspectRatio(0);
            else if (checkedId == R.id.radio16_9)
                camera.setPhotoAspectRatio(1);
        } else if (id == R.id.orientation) {
            if (checkedId == R.id.landscape)
                camera.setOrientation(0);
            else if (checkedId == R.id.portrait)
                camera.setOrientation(1);
        }
    }

    private void initUI() {
        bDelete = findViewById(R.id.delete);
        bGsd = findViewById(R.id.gsd);
        bIsSimulating = findViewById(R.id.simulate);
        bLocate = findViewById(R.id.locate);
        bAdd = findViewById(R.id.add);
        bConfig = findViewById(R.id.config);
        bRun = findViewById(R.id.run);
        bStatus = findViewById(R.id.status);
        bClone = findViewById(R.id.clone);
        bDelete.setOnClickListener(this);
        bGsd.setOnClickListener(this);
        bIsSimulating.setOnClickListener(this);
        bLocate.setOnClickListener(this);
        bAdd.setOnClickListener(this);
        bConfig.setOnClickListener(this);
        bRun.setOnClickListener(this);
        bStatus.setOnClickListener(this);
        bClone.setOnClickListener(this);

        lSettings = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_setting, null);
        rgSpeed = lSettings.findViewById(R.id.speed);
        rgActionAfterFinished = lSettings.findViewById(R.id.actionAfterFinished);
        rgAlgorithm = lSettings.findViewById(R.id.algorithm);
        rgPhoto = lSettings.findViewById(R.id.takePhoto);
        rgRec = lSettings.findViewById(R.id.record);
        rgAspectRadio = lSettings.findViewById(R.id.aspectRatio);
        rgOrientation = lSettings.findViewById(R.id.orientation);
        rgSpeed.setOnCheckedChangeListener(this);
        rgActionAfterFinished.setOnCheckedChangeListener(this);
        rgAlgorithm.setOnCheckedChangeListener(this);
        rgPhoto.setOnCheckedChangeListener(this);
        rgRec.setOnCheckedChangeListener(this);
        rgAspectRadio.setOnCheckedChangeListener(this);
        rgOrientation.setOnCheckedChangeListener(this);

        adSetting = new AlertDialog.Builder(this)
                .setTitle("")
                .setView(lSettings)
                .setPositiveButton("Finalizar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        createPath();
                    }

                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .create();

        lMetrics = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_metrics, null);
        tPathDistance = lMetrics.findViewById(R.id.pathDistance);
        tPathDistanceDJI = lMetrics.findViewById(R.id.pathDistanceDJI);
        tEstimatedTime = lMetrics.findViewById(R.id.estimatedTime);
        tEstimatedTimeDJI = lMetrics.findViewById(R.id.estimatedTimeDJI);
        tQuantityPhoto = lMetrics.findViewById(R.id.quantityPhoto);
        tBearing = lMetrics.findViewById(R.id.bearing);

        adMetrics = new AlertDialog.Builder(this)
                .setTitle("")
                .setView(lMetrics)
                .setPositiveButton("Iniciar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (isRecording)
                            realtime.recordIn(isSimulating);
                        mission.startMission();
                    }

                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .create();

        lStatus = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_status, null);
        tInitialDateTime = lStatus.findViewById(R.id.initialDateTime);
        tCurrentDateTime = lStatus.findViewById(R.id.currentDateTime);
        tFinalDateTime = lStatus.findViewById(R.id.finalDateTime);
        tElapsedTime = lStatus.findViewById(R.id.elapsedTime);
        tDistanceTraveled = lStatus.findViewById(R.id.distanceTraveled);
        tvelocityAverageX = lStatus.findViewById(R.id.velocityAverageX);
        tvelocityAverageY = lStatus.findViewById(R.id.velocityAverageY);
        tvelocityAverageZ = lStatus.findViewById(R.id.velocityAverageZ);
        tvelocityAverage = lStatus.findViewById(R.id.velocityAverage);
        tChargeRemaining = lStatus.findViewById(R.id.chargeRemaining);
        tChargeRemainingInPercent = lStatus.findViewById(R.id.chargeRemainingInPercent);
        tCurrent = lStatus.findViewById(R.id.current);
        tVoltage = lStatus.findViewById(R.id.voltage);
        tChargeConsumption = lStatus.findViewById(R.id.chargeConsumption);
        tChargeConsumptionInPercent = lStatus.findViewById(R.id.chargeConsumptionInPercent);
        tPathDistanceMetrics = lStatus.findViewById(R.id.pathDistance);
        tPathDistanceDJIMetrics = lStatus.findViewById(R.id.pathDistanceDJI);
        tEstimatedTimeMetrics = lStatus.findViewById(R.id.estimatedTime);
        tEstimatedTimeDJIMetrics = lStatus.findViewById(R.id.estimatedTimeDJI);
        tQuantityPhotoMetrics = lStatus.findViewById(R.id.quantityPhoto);
        tBearingMetrics = lStatus.findViewById(R.id.bearing);

        adStatus = new AlertDialog.Builder(this)
                .setTitle("")
                .setView(lStatus)
                .setPositiveButton("Fechar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .create();
    }

    private void addPath() {
        if (bRun.getText().equals("Upload")) {
            adding++;
            switch (adding) {
                case 1:
                    showToast("Caminho inicial");
                    aoi.setDraggableInitial(true);
                    bAdd.setText("Inicial");
                    break;
                case 2:
                    showToast("Caminho de cobertura");
                    aoi.setDraggableInitial(false);
                    aoi.setVisibleVertex(true);
                    aoi.setVisibleObb(true);
                    aoi.setInitialPath();
                    bAdd.setText("Cobertura");
                    break;
                case 3:
                    showToast("Caminho final");
                    aoi.setVisibleVertex(false);
                    aoi.setVisibleObb(false);
                    aoi.setDraggableFinal(true);
                    createPath();
                    bAdd.setText("Final");
                    break;
                default:
                    adding = 0;
                    aoi.setDraggableFinal(false);
                    bAdd.setText("Caminho");
            }
        } else
            showToast("Missão em execução, não pode definir caminhos");
    }

    private void runMission() {
        if (bRun.getText().equals("Parar")) {
            mission.stopMission();
            return;
        }

        if (!bAdd.getText().equals("Caminho")) {
            showToast("Termine de definir o caminho antes do upload da missão");
            return;
        }

        if (!mission.setPathWaypoint(aoi.getPathPoint(), (int) bearing))
            return;

        pathDistanceDJI = mission.calculateTotalDistance();

        DJIError error = mission.loadMission(mFinishedAction, mSpeed);
        estimatedTimeDJI = convertingDoubleToHoursMinutesSecondsMilliseconds(mission.calculateTotalTime().longValue());

        if (error == null) {
            mission.uploadMission();
            showToast("Missão carregada com sucesso");
        } else
            showToast("Falha em carregar a missão, tente novamente... Erro: " + error.getDescription());
    }

    private void cameraUpdate() {
        LatLng latlng;
        if (dji.checkGpsCoordination(vant.latitude, vant.longitude))
            latlng = new LatLng(vant.latitude, vant.longitude);
        else
            latlng = new LatLng(-23.1858535, -50.6574255);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 19.0f));
    }

    private void createPath() {
        if (aoi != null) {
            if (algorithm == 0) {
                aoi.setGrid(gcgu.createBoustrophedonGrid(aoi.getObbPoints()));
                aoi.setGrid(jtsgu.pointsInsidePolygons(aoi.getAoiVertex(), aoi.getGridPoints()));
                aoi.guideMinimumSpanningTree(new ArrayList<>());
            }

            if (algorithm == 1) {
                List<List<Node>> nodes = gcgu.createStcGrid(aoi.getObbPoints());
                List<LatLng> node = gcgu.listNodeToLatLng(nodes);
                GraphStructure gs = graph.SimpleWeightedGraph(nodes, jtsgu.pointsOutsidePolygons(aoi.getAoiVertex(), node));
                gs = graph.minimumSpanningTree(gs);
                aoi.guideMinimumSpanningTree(gs.arcs);
                gs = graph.pathGraph(gs);
                aoi.setGrid(gcgu.nodeToLatLng(gs.nodes));
            }

            bearing = mBearingLargura;
            if (camera.getOrientation() == 1)
                bearing -= 90;

            pathDistance = calculateTotalDistance(aoi.getPathPoint());
            estimatedTime = convertingDoubleToHoursMinutesSecondsMilliseconds((long) (1.9 * calculateTotalDistance(aoi.getPathPoint()) / mSpeed));
            quantityPhoto = aoi.getGridPoints().size();

            aoi.setPathPlanning();
            aoi.setInitialPath();
            aoi.setFinalPath();
        }
    }

    private void showSettingDialog() {
        if (mSpeed == 2.0f)
            rgSpeed.check(R.id.lowSpeed);
        else if (mSpeed == 4.0f)
            rgSpeed.check(R.id.midSpeed);
        else if (mSpeed == 8.0f)
            rgSpeed.check(R.id.highSpeed);
        else if (mSpeed == 15.0f)
            rgSpeed.check(R.id.veryHighSpeed);

        switch (mFinishedAction) {
            case 0:
                rgActionAfterFinished.check(R.id.finishNone);
                break;
            case 1:
                rgActionAfterFinished.check(R.id.finishGoHome);
                break;
            case 2:
                rgActionAfterFinished.check(R.id.finishAutoLanding);
                break;
            case 3:
                rgActionAfterFinished.check(R.id.finishToFirst);
                break;
        }

        switch (algorithm) {
            case 0:
                rgAlgorithm.check(R.id.bcd);
                break;
            case 1:
                rgAlgorithm.check(R.id.stc);
                break;
        }

        rgPhoto.check(mission.isTakePhoto() ? R.id.yes : R.id.no);
        rgRec.check(isRecording ? R.id.yesRecord : R.id.noRecord);
        rgAspectRadio.check(camera.getPhotoAspectRatio() == 0 ? R.id.radio4_3 : R.id.radio16_9);
        rgOrientation.check(camera.getOrientation() == 0 ? R.id.landscape : R.id.portrait);

        adSetting.show();
    }

    private void showMetricsDialog() {
        tPathDistance.setText("Distância total do caminho: " + decimalFormatter.format(pathDistance) + " m");
        tPathDistanceDJI.setText("Distância total do caminho (DJI): " + decimalFormatter.format(pathDistanceDJI) + " m");
        tEstimatedTime.setText("Tempo total: " + estimatedTime);
        tEstimatedTimeDJI.setText("Tempo total (DJI): " + estimatedTimeDJI);
        tQuantityPhoto.setText("Quantidade de fotos: " + quantityPhoto);
        tBearing.setText("Rumo: " + bearing + " º");

        adMetrics.show();
    }

    private void updateStatusDialog() {
        tInitialDateTime.setText("Data e hora inicial: " + initialDateTime);
        tCurrentDateTime.setText("Data e hora atual: " + currentDateTime);
        tFinalDateTime.setText("Data e hora final: " + finalDateTime);
        tElapsedTime.setText("Tempo decorrido: " + calculateElapsedTime(initialDateTime, finalDateTime));
        tDistanceTraveled.setText("Distância percorrida: " + decimalFormatter.format(distanceTraveled) + " m");
        tvelocityAverageX.setText("Velocidade média X: " + decimalFormatter.format(velocityAverageX) + " m/s");
        tvelocityAverageY.setText("Velocidade média Y: " + decimalFormatter.format(velocityAverageY) + " m/s");
        tvelocityAverageZ.setText("Velocidade média Z: " + decimalFormatter.format(velocityAverageZ) + " m/s");
        tvelocityAverage.setText("Velocidade média: " + decimalFormatter.format(velocityAverage) + " m/s");
        tChargeRemaining.setText("Energia restante da bateria: " + batteryChargeRemaining + " mAh");
        tChargeRemainingInPercent.setText("Energia restante da bateria: " + batteryChargeRemainingInPercent + " %");
        tCurrent.setText("Corrente: " + batteryCurrent + " mA");
        tVoltage.setText("Tensão: " + batteryVoltage + " mV");
        tChargeConsumption.setText("Consumo de energia: " + batteryChargeConsumption + " mAh");
        tChargeConsumptionInPercent.setText("Consumo de energia: " + batteryChargeConsumptionInPercent + " %");

        tPathDistanceMetrics.setText("Distância total do caminho: " + decimalFormatter.format(pathDistance) + " m");
        tPathDistanceDJIMetrics.setText("Distância total do caminho (DJI): " + decimalFormatter.format(pathDistanceDJI) + " m");
        tEstimatedTimeMetrics.setText("Tempo total: " + estimatedTime);
        tEstimatedTimeDJIMetrics.setText("Tempo total (DJI): " + estimatedTimeDJI);
        tQuantityPhotoMetrics.setText("Quantidade de fotos: " + quantityPhoto);
        tBearingMetrics.setText("Rumo: " + bearing + " º");
    }

    private void showStatusDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateStatusDialog();
                adStatus.show();
            }
        });
    }

    OnCompleteListenerCallback copy = (Task<DataSnapshot> task) -> runOnUiThread(new Runnable() {
        @Override
        public void run() {
            List<LatLng> delete = new ArrayList<>(aoi.getAoiVertex());
            for (LatLng vertex : delete)
                aoi.deleteVertex(vertex);
            for (DataSnapshot snapshot : task.getResult().getChildren()) {
                double latitude = snapshot.child("latitude").getValue(Double.class);
                double longitude = snapshot.child("longitude").getValue(Double.class);
                LatLng vertex = new LatLng(latitude, longitude);
                aoi.addVertex(vertex);
                aoi.setObb(jtsgu.calculateOrientedBoundingBox(aoi.getAoiVertex()));
                createPath();
                if (!bAdd.getText().equals("Cobertura")) {
                    aoi.setVisibleVertex(false);
                    aoi.setVisibleObb(false);
                }
            }
            showToast("Cobertura clonada com sucesso");
        }
    });

    private void duplicate() {
        if (bRun.getText().equals("Upload"))
            realtime.iterateBetweenCoveragePaths(copy);
    }

    private boolean onProductConnectionChange() {
        boolean isConnected = dji.setProduct(MainActivity.getProductInstance(), isSimulating, updateDroneLocation);
        if (isConnected) {
            mission.setMissionOperator(MainActivity.getMissionOperatorInstance(), missionCallback);
            battery.setProduct(MainActivity.getProductInstance(), batteryCallback);
            camera.setProduct(MainActivity.getProductInstance());
        }
        return isConnected;
    }

    private void showToast(final String toastMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        });
    }
}