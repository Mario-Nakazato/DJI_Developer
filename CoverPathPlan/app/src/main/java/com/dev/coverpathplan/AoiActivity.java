package com.dev.coverpathplan;

import static com.dev.coverpathplan.GeoCalcGeodeticUtils.calculateTotalDistance;

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
    private Button bDelete, bGsd, bLocate, bAdd, bIsSimulating, bConfig, bRun;
    private TextView tPathDistance, tPathDistanceDJI, tEstimatedTimeDJI, tQuantityPhoto;
    private RadioGroup rgSpeed, rgActionAfterFinished, rgAlgorithm, rgPhoto;
    private LinearLayout lSettings, lMetrics;
    private AlertDialog adSetting, adMetrics;
    private Marker markerSelected;
    private int adding = 0;
    private boolean isSimulating = false;
    private float mSpeed = 4.0f;
    private int mFinishedAction = 1;
    private int algorithm = 0;
    private boolean isCovering = false;
    private DecimalFormat decimalFormatter = new DecimalFormat("0.00");
    private AreaOfInterest aoi;
    private JTSGeometryUtils jtsgu;
    private GeoCalcGeodeticUtils gcgu;
    private FlightControllerDJI dji;
    private MissionOperatorDJI mission;
    private MissionOperatorDJICallback missionCallback;
    private Fork graph;
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
                        bRun.setText("Parar");
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

                        if (!isCovering && i == 0
                                && bRun.getText().equals("Parar")
                                && executionEvent.getProgress().executeState == WaypointMissionExecuteState.BEGIN_ACTION) {
                            isCovering = true;
                            showToast("Caminho de cobertura iniciado ");
                        } else if (isCovering && i == executionEvent.getProgress().totalWaypointCount - 1
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
        } else if (id == R.id.locate) {
            updateDroneLocation.execute();
            cameraUpdate(); // Locate the drone's place
        } else if (id == R.id.add)
            addPath();
        else if (id == R.id.config)
            if (bRun.getText().equals("Upload"))
                showSettingDialog();
            else
                showToast("Missão em execução, não pode configurar opções");
        else if (id == R.id.run)
            runMission();
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
        bDelete.setOnClickListener(this);
        bGsd.setOnClickListener(this);
        bIsSimulating.setOnClickListener(this);
        bLocate.setOnClickListener(this);
        bAdd.setOnClickListener(this);
        bConfig.setOnClickListener(this);
        bRun.setOnClickListener(this);

        lSettings = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_setting, null);
        rgSpeed = lSettings.findViewById(R.id.speed);
        rgActionAfterFinished = lSettings.findViewById(R.id.actionAfterFinished);
        rgAlgorithm = lSettings.findViewById(R.id.algorithm);
        rgPhoto = lSettings.findViewById(R.id.takePhoto);
        rgSpeed.setOnCheckedChangeListener(this);
        rgActionAfterFinished.setOnCheckedChangeListener(this);
        rgAlgorithm.setOnCheckedChangeListener(this);
        rgPhoto.setOnCheckedChangeListener(this);

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
        tEstimatedTimeDJI = lMetrics.findViewById(R.id.estimatedTimeDJI);
        tQuantityPhoto = lMetrics.findViewById(R.id.quantityPhoto);

        adMetrics = new AlertDialog.Builder(this)
                .setTitle("")
                .setView(lMetrics)
                .setPositiveButton("Iniciar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mission.startMission();
                    }

                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
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


        if (!mission.setPathWaypoint(aoi.getPathPoint()))
            return;

        DJIError error = mission.loadMission(mFinishedAction, mSpeed);
        if (error == null) {
            mission.uploadMission();
            showToast("Missão carregada com sucesso");
        } else
            showToast("Falha em carregar a missão, tente novamente... Erro: " + error.getDescription());
    }

    private void cameraUpdate() {
        LatLng latlng = dji.getLocation(); // Melhoria?

        if (latlng == null)
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

            aoi.setPathPlanning();
            aoi.setInitialPath();
            aoi.setFinalPath();
        }
    }

    private void showSettingDialog() {
        // Definir opção de velocidade com base no valor de mSpeed
        if (mSpeed == 2.0f)
            rgSpeed.check(R.id.lowSpeed);
        else if (mSpeed == 4.0f)
            rgSpeed.check(R.id.midSpeed);
        else if (mSpeed == 8.0f)
            rgSpeed.check(R.id.highSpeed);

        // Definir opção de ação após finalizar com base no valor de mFinishedAction
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

        // Definir opção de algoritmo com base no valor de algorithm
        switch (algorithm) {
            case 0:
                rgAlgorithm.check(R.id.bcd);
                break;
            case 1:
                rgAlgorithm.check(R.id.stc);
                break;
        }

        // Definir opção de tirar foto com base no valor de mission.isTakePhoto()
        rgPhoto.check(mission.isTakePhoto() ? R.id.yes : R.id.no);

        adSetting.show();
    }

    private void showMetricsDialog() {
        tPathDistance.setText("Distância total do caminho: " + decimalFormatter.format(calculateTotalDistance(aoi.getGridPoints())) + " m");
        tPathDistanceDJI.setText("Distância total do caminho (DJI): " + decimalFormatter.format(mission.calculateTotalDistance()) + " m");
        // Tempo Estimado
        tQuantityPhoto.setText("Quantidade de fotos: " + mission.getWaypointCount());

        adMetrics.show();
    }

    private boolean onProductConnectionChange() {
        boolean isConnected = dji.setProduct(MainActivity.getProductInstance(), isSimulating, updateDroneLocation);
        if (isConnected)
            mission.setMissionOperator(MainActivity.getMissionOperatorInstance(), missionCallback);
        return isConnected;
    }

    StateCallback updateDroneLocation = () -> runOnUiThread(new Runnable() {
        @Override
        public void run() {
            LatLng latlng = dji.getLocation();
            aoi.setVant(latlng, dji.getAttitudeYaw());
        }
    });

    private void showToast(final String toastMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        });
    }
}