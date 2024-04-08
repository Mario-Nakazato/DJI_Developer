package com.dev.coverpathplan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import dji.common.error.DJIError;
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
import dji.common.util.CommonCallbacks;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;

interface MissionOperatorDJICallback {
    void uploadMission(DJIError error);

    void startMission(DJIError error);

    void stopMission(DJIError error);

    void onExecutionUpdate(WaypointMissionExecutionEvent executionEvent);

    void onExecutionFinish(DJIError error);
}

@SuppressWarnings("Convert2Lambda")
public class MissionOperatorDJI {
    private WaypointMissionOperator missionOperator;
    private WaypointMissionOperatorListener eventNotificationListener;
    private MissionOperatorDJICallback callback;
    private WaypointMission.Builder waypointMissionBuilder;
    private List<Waypoint> pathWaypoint;
    private WaypointAction actionPhoto, actionRotate, actionStay;
    private boolean takePhoto = true;

    MissionOperatorDJI() {
        pathWaypoint = new ArrayList<>();
        waypointMissionBuilder = new WaypointMission.Builder();
        actionPhoto = new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 4);
        actionStay = new WaypointAction(WaypointActionType.STAY, 2);
    }

    boolean setMissionOperator(WaypointMissionOperator WaypointMissionOperator, MissionOperatorDJICallback missionOperatorDJICallback) {
        missionOperator = WaypointMissionOperator;
        callback = missionOperatorDJICallback;

        if (missionOperator != null) {
            eventNotificationListener = new WaypointMissionOperatorListener() {
                @Override
                public void onDownloadUpdate(@NonNull WaypointMissionDownloadEvent downloadEvent) {
                }

                @Override
                public void onUploadUpdate(@NonNull WaypointMissionUploadEvent uploadEvent) {
                }

                @Override
                public void onExecutionUpdate(@NonNull WaypointMissionExecutionEvent executionEvent) {
                    callback.onExecutionUpdate(executionEvent);
                }

                @Override
                public void onExecutionStart() {
                }

                @Override
                public void onExecutionFinish(@Nullable final DJIError error) {
                    callback.onExecutionFinish(error);
                }
            };
            missionOperator.addListener(eventNotificationListener);
        }
        return true;
    }

    void removeListener() {
        if (missionOperator != null) {
            missionOperator.removeListener(eventNotificationListener);
        }
    }

    boolean setPathWaypoint(List<LatLng> path, int bearing) {
        if (missionOperator == null || path.isEmpty())
            return false;

        for (Waypoint waypoint : pathWaypoint) {
            waypointMissionBuilder.removeWaypoint(waypoint);
        }
        pathWaypoint.clear();
        try {
            actionRotate = new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT, (bearing + 180) % 360 - 180);
            for (LatLng waypoint : path) {
                Waypoint mWaypoint = new Waypoint(waypoint.latitude, waypoint.longitude, (float) CaptureArea.getAltitude());
                if (pathWaypoint.isEmpty())
                    mWaypoint.addAction(actionRotate);
                mWaypoint.addAction(actionStay);
                if (takePhoto)
                    mWaypoint.addAction(actionPhoto);
                pathWaypoint.add(mWaypoint);
                waypointMissionBuilder.addWaypoint(mWaypoint);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    DJIError loadMission(int finishedAction, float speed) {
        if (missionOperator == null || pathWaypoint.isEmpty())
            return null;

        WaypointMissionHeadingMode mHeadingMode = WaypointMissionHeadingMode.USING_INITIAL_DIRECTION;
        WaypointMissionFinishedAction mFinishedAction;
        switch (finishedAction) {
            case 0:
                mFinishedAction = WaypointMissionFinishedAction.NO_ACTION;
                break;
            case 1:
                mFinishedAction = WaypointMissionFinishedAction.GO_HOME;
                break;
            case 2:
                mFinishedAction = WaypointMissionFinishedAction.AUTO_LAND;
                break;
            case 3:
                mFinishedAction = WaypointMissionFinishedAction.GO_FIRST_WAYPOINT;
                break;
            default:
                mFinishedAction = WaypointMissionFinishedAction.NO_ACTION;
        }
        waypointMissionBuilder.finishedAction(mFinishedAction)
                .headingMode(mHeadingMode)
                .autoFlightSpeed(speed)
                .maxFlightSpeed(speed)
                .flightPathMode(WaypointMissionFlightPathMode.NORMAL);
        return missionOperator.loadMission(waypointMissionBuilder.build());
    }

    void uploadMission() {
        if (missionOperator != null)
            missionOperator.uploadMission(new CommonCallbacks.CompletionCallback<DJIError>() {
                DJIError e;

                @Override
                public void onResult(DJIError error) {
                    if (error != null)
                        missionOperator.retryUploadMission(new CommonCallbacks.CompletionCallback<DJIError>() {
                            @Override
                            public void onResult(DJIError error) {
                                e = error;
                            }
                        });
                    callback.uploadMission(e);
                }
            });
    }

    void startMission() {
        if (missionOperator != null)
            missionOperator.startMission(new CommonCallbacks.CompletionCallback<DJIError>() {
                @Override
                public void onResult(DJIError error) {
                    callback.startMission(error);
                }
            });
    }

    void stopMission() {
        if (missionOperator != null)
            missionOperator.stopMission(new CommonCallbacks.CompletionCallback<DJIError>() {
                @Override
                public void onResult(DJIError error) {
                    callback.stopMission(error);
                }
            });
    }

    void setTakePhoto(boolean takePhoto) {
        this.takePhoto = takePhoto;
    }

    boolean isTakePhoto() {
        return takePhoto;
    }

    float calculateTotalDistance() {
        return waypointMissionBuilder.calculateTotalDistance();
    }

    Float calculateTotalTime() {
        return waypointMissionBuilder.calculateTotalTime();
    }

    int getWaypointCount() {
        return waypointMissionBuilder.getWaypointCount();
    }
}