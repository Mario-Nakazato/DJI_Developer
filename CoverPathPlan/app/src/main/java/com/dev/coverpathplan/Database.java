package com.dev.coverpathplan;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

interface OnCompleteListenerCallback {
    void execute(Task<DataSnapshot> task);
}

public class Database {
    private DatabaseReference databaseReference, record, cover, planning, path, metrics;
    private List<String> paths;
    private int nPath = 0;

    Database(DatabaseReference firebaseDatabase) {
        databaseReference = firebaseDatabase;
        paths = new ArrayList<>();
    }

    void recordIn(boolean isSimulating) {
        if (databaseReference == null)
            return;

        if (isSimulating)
            record = databaseReference.child("SimulatorState");
        else
            record = databaseReference.child("FlightControllerState");

        if (record == null)
            return;

        String hash = record.push().getKey();
        cover = record.child(hash);

        if (cover == null)
            return;

        planning = cover.child("planning");
        metrics = cover.child("metrics");
    }

    void planningRecord(List<LatLng> vertex, double bearing, float speed, int finishedAction, String algorithm,
                        boolean isTakePhoto, String aspectRatio, double gsdLargura, double gsdAltura, double overlapLargura,
                        double overlapAltura, double footprintLargura, double footprintAltura) {
        if (planning == null)
            return;

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("vertex", vertex);
        dataMap.put("bearing", bearing);
        dataMap.put("speed", speed);
        dataMap.put("finishedAction", finishedAction);
        dataMap.put("algorithm", algorithm);
        dataMap.put("isTakePhoto", isTakePhoto);
        dataMap.put("aspectRatio", aspectRatio);
        dataMap.put("gsdLargura", gsdLargura);
        dataMap.put("gsdAltura", gsdAltura);
        dataMap.put("overlapLargura", overlapLargura);
        dataMap.put("overlapAltura", overlapAltura);
        dataMap.put("footprintLargura", footprintLargura);
        dataMap.put("footprintAltura", footprintAltura);
        planning.updateChildren(dataMap);
    }

    void pathRecord(FlightState flightState, int chargeRemaining, int chargeRemainingInPercent, int voltage, int current) {
        String h = cover.child("path").push().getKey();
        if (h == null)
            h = cover.child("path").push().getKey();
        path = cover.child("path").child(h);

        if (path == null)
            return;

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("currentDateTime", flightState.currentDateTime);
        dataMap.put("areMotorsOn", flightState.areMotorsOn);
        dataMap.put("isFlying", flightState.isFlying);
        dataMap.put("latitude", flightState.latitude);
        dataMap.put("longitude", flightState.longitude);
        dataMap.put("altitude", flightState.altitude);
        dataMap.put("positionX", flightState.positionX);
        dataMap.put("positionY", flightState.positionY);
        dataMap.put("positionZ", flightState.positionZ);
        dataMap.put("takeoffLocationAltitude", flightState.takeoffLocationAltitude);
        dataMap.put("pitch", flightState.pitch);
        dataMap.put("roll", flightState.roll);
        dataMap.put("yaw", flightState.yaw);
        dataMap.put("velocityX", flightState.velocityX);
        dataMap.put("velocityY", flightState.velocityY);
        dataMap.put("velocityZ", flightState.velocityZ);
        dataMap.put("flightTimeInSeconds", flightState.flightTimeInSeconds);
        dataMap.put("flightMode", flightState.flightMode);
        dataMap.put("satelliteCount", flightState.satelliteCount);
        dataMap.put("ultrasonicHeight", flightState.ultrasonicHeight);
        dataMap.put("flightCount", flightState.flightCount);
        dataMap.put("aircraftHeadDirection", flightState.aircraftHeadDirection);
        dataMap.put("chargeRemaining", chargeRemaining);
        dataMap.put("chargeRemainingInPercent", chargeRemainingInPercent);
        dataMap.put("voltage", voltage);
        dataMap.put("current", current);
        path.updateChildren(dataMap);
    }

    void metricsRecord(double pathDistance, double pathDistanceDJI, String estimatedTime, String estimatedTimeDJI,
                       int quantityPhoto, String initialDateTime, String finalDateTime, String elapsedTime, double distanceTraveled,
                       double velocityAverageX, double velocityAverageY, double velocityAverageZ, double velocityAverage,
                       int chargeConsumption, int chargeConsumptionInPercent) {
        if (metrics == null)
            return;

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("pathDistance", pathDistance);
        dataMap.put("pathDistanceDJI", pathDistanceDJI);
        dataMap.put("estimatedTime", estimatedTime);
        dataMap.put("estimatedTimeDJI", estimatedTimeDJI);
        dataMap.put("quantityPhoto", quantityPhoto);
        dataMap.put("initialDateTime", initialDateTime);
        dataMap.put("finalDateTime", finalDateTime);
        dataMap.put("elapsedTime", elapsedTime);
        dataMap.put("distanceTraveled", distanceTraveled);
        dataMap.put("velocityAverageX", velocityAverageX);
        dataMap.put("velocityAverageY", velocityAverageY);
        dataMap.put("velocityAverageZ", velocityAverageZ);
        dataMap.put("velocityAverage", velocityAverage);
        dataMap.put("chargeConsumption", chargeConsumption);
        dataMap.put("chargeConsumptionInPercent", chargeConsumptionInPercent);
        metrics.updateChildren(dataMap);
    }

    void updateCoveragePaths() {
        paths.clear();
        databaseReference.child("SimulatorState").get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DataSnapshot snapshot : task.getResult().getChildren()) {
                                paths.add("SimulatorState/" + snapshot.getKey());
                            }
                        }
                    }
                });

        databaseReference.child("FlightControllerState").get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DataSnapshot snapshot : task.getResult().getChildren()) {
                                paths.add("FlightControllerState/" + snapshot.getKey());
                            }
                        }
                    }
                });
    }

    void iterateBetweenCoveragePaths(OnCompleteListenerCallback callback) {
        if (databaseReference != null) {
            if (!paths.isEmpty()) {
                databaseReference.child(paths.get(nPath) + "/planning/vertex").get()
                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (task.isSuccessful()) {
                                    callback.execute(task);
                                }
                            }
                        });
                nPath++;
                nPath = nPath < paths.size() ? nPath : 0;
            }
        }
    }
}