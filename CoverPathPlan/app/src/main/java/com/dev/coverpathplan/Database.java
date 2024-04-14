package com.dev.coverpathplan;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

interface OnCompleteListenerCallback {
    void execute(DataSnapshot dataSnapshot);
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
        path = cover.child("path");
        metrics = cover.child("metrics");
    }

    void planningRecord(List<LatLng> vertex, double bearing, float speed, int finishedAction, String algorithm,
                        boolean isTakePhoto, String aspectRatio, double altitude, double gsdLargura, double gsdAltura, double overlapLargura,
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
        dataMap.put("altitude", altitude);
        dataMap.put("gsdLargura", gsdLargura);
        dataMap.put("gsdAltura", gsdAltura);
        dataMap.put("overlapLargura", overlapLargura);
        dataMap.put("overlapAltura", overlapAltura);
        dataMap.put("footprintLargura", footprintLargura);
        dataMap.put("footprintAltura", footprintAltura);
        planning.updateChildren(dataMap);
    }

    void pathRecord(FlightState flightState, int chargeRemaining, int chargeRemainingInPercent, int voltage, int current) {
        String h = path.push().getKey();

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("currentDateTime", flightState.currentDateTime);
        dataMap.put("areMotorsOn", flightState.areMotorsOn);
        dataMap.put("isFlying", flightState.isFlying);
        dataMap.put("latitude", isValidDouble(flightState.latitude) ? flightState.latitude : null);
        dataMap.put("longitude", isValidDouble(flightState.longitude) ? flightState.longitude : null);
        dataMap.put("altitude", isValidDouble(flightState.altitude) ? flightState.altitude : null);
        dataMap.put("positionX", isValidDouble(flightState.positionX) ? flightState.positionX : null);
        dataMap.put("positionY", isValidDouble(flightState.positionY) ? flightState.positionY : null);
        dataMap.put("positionZ", isValidDouble(flightState.positionZ) ? flightState.positionZ : null);
        dataMap.put("takeoffLocationAltitude", isValidDouble(flightState.takeoffLocationAltitude) ? flightState.takeoffLocationAltitude : null);
        dataMap.put("pitch", isValidDouble(flightState.pitch) ? flightState.pitch : null);
        dataMap.put("roll", isValidDouble(flightState.roll) ? flightState.roll : null);
        dataMap.put("yaw", isValidDouble(flightState.yaw) ? flightState.yaw : null);
        dataMap.put("velocityX", isValidDouble(flightState.velocityX) ? flightState.velocityX : null);
        dataMap.put("velocityY", isValidDouble(flightState.velocityY) ? flightState.velocityY : null);
        dataMap.put("velocityZ", isValidDouble(flightState.velocityZ) ? flightState.velocityZ : null);
        dataMap.put("flightTimeInSeconds", flightState.flightTimeInSeconds);
        dataMap.put("flightMode", flightState.flightMode);
        dataMap.put("satelliteCount", flightState.satelliteCount);
        dataMap.put("ultrasonicHeight", isValidDouble(flightState.ultrasonicHeight) ? flightState.ultrasonicHeight : null);
        dataMap.put("flightCount", flightState.flightCount);
        dataMap.put("aircraftHeadDirection", flightState.aircraftHeadDirection);
        dataMap.put("chargeRemaining", chargeRemaining);
        dataMap.put("chargeRemainingInPercent", chargeRemainingInPercent);
        dataMap.put("voltage", voltage);
        dataMap.put("current", current);
        path.child(h).updateChildren(dataMap);
    }

    // Método auxiliar para verificar se um valor double é válido
    private boolean isValidDouble(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    void metricsRecord(double pathDistance, double pathDistanceDJI, String estimatedTime, String estimatedTimeDJI,
                       int quantityPhoto, String initialDateTime, String finalDateTime, String elapsedTime, double distanceTraveled,
                       double velocityAverage, int chargeConsumption, int chargeConsumptionInPercent) {
        if (metrics == null)
            return;

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("pathDistance", isValidDouble(pathDistance) ? pathDistance : null);
        dataMap.put("pathDistanceDJI", isValidDouble(pathDistanceDJI) ? pathDistanceDJI : null);
        dataMap.put("estimatedTime", estimatedTime);
        dataMap.put("estimatedTimeDJI", estimatedTimeDJI);
        dataMap.put("quantityPhoto", quantityPhoto);
        dataMap.put("initialDateTime", initialDateTime);
        dataMap.put("finalDateTime", finalDateTime);
        dataMap.put("elapsedTime", elapsedTime);
        dataMap.put("distanceTraveled", isValidDouble(distanceTraveled) ? distanceTraveled : null);
        dataMap.put("velocityAverage", isValidDouble(velocityAverage) ? velocityAverage : null);
        dataMap.put("chargeConsumption", chargeConsumption);
        dataMap.put("chargeConsumptionInPercent", chargeConsumptionInPercent);
        metrics.updateChildren(dataMap);
    }

    void updateCoveragePaths() {
        paths.clear();
        databaseReference.child("SimulatorState")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren())
                            paths.add("SimulatorState/" + snapshot.getKey());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        databaseReference.child("FlightControllerState")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren())
                            paths.add("FlightControllerState/" + snapshot.getKey());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        // Configurar um ChildEventListener para manter a lista atualizada
        databaseReference.child("SimulatorState").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                // Adicionar a nova HashKey à lista
                String newHashKey = dataSnapshot.getKey();
                paths.add("SimulatorState/" + newHashKey);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                // Lidar com alterações, se necessário
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // Remover a HashKey removida da lista
                String removedHashKey = dataSnapshot.getKey();
                paths.remove(removedHashKey);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // Lidar com movimentações, se necessário
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Lidar com erros, se necessário
            }
        });

        databaseReference.child("FlightControllerState").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                // Adicionar a nova HashKey à lista
                String newHashKey = dataSnapshot.getKey();
                paths.add("FlightControllerState/" + newHashKey);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                // Lidar com alterações, se necessário
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // Remover a HashKey removida da lista
                String removedHashKey = dataSnapshot.getKey();
                paths.remove(removedHashKey);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // Lidar com movimentações, se necessário
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Lidar com erros, se necessário
            }
        });
    }

    void iterateBetweenCoveragePaths(OnCompleteListenerCallback callback) {
        if (databaseReference != null) {
            if (!paths.isEmpty()) {
                databaseReference.child(paths.get(nPath) + "/planning/vertex")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                callback.execute(dataSnapshot);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                nPath++;
                nPath = nPath < paths.size() ? nPath : 0;
            }
        }
    }
}