package com.dev.coverpathplan;

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

interface ErrorCallback {
    void execute(DJIError error);
}

public class MissionOperatorDJI {
    private WaypointMissionOperator missionOperator;
    private WaypointMission.Builder waypointMissionBuilder;
    private List<Waypoint> pathWaypoint;
    private float mSpeed = 8.0f;
    private WaypointMissionFinishedAction mFinishedAction = WaypointMissionFinishedAction.GO_HOME;
    private WaypointMissionHeadingMode mHeadingMode = WaypointMissionHeadingMode.USING_INITIAL_DIRECTION;
    private WaypointMissionOperatorListener eventNotificationListener;
    WaypointAction actionPhoto, actionRotate;
    private boolean takePhoto = false;

    MissionOperatorDJI() {
        pathWaypoint = new ArrayList<>();
        waypointMissionBuilder = new WaypointMission.Builder();
        actionPhoto = new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1);
    }

    boolean setMissionOperator(WaypointMissionOperator WaypointMissionOperator, ErrorCallback callback) {
        missionOperator = WaypointMissionOperator;
        if (missionOperator != null) {
            eventNotificationListener = new WaypointMissionOperatorListener() {
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
                    callback.execute(error);
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

    boolean setPathWaypoint(List<LatLng> path) {
        if (path.isEmpty())
            return false;
        for (Waypoint waypoint : pathWaypoint) {
            waypointMissionBuilder.removeWaypoint(waypoint);
        }
        pathWaypoint.clear();
        try {
            actionRotate = new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT, ((int) GeoCalcGeodeticUtils.mBearingLargura + 180) % 360 - 180);
            for (LatLng waypoint : path) {
                Waypoint mWaypoint = new Waypoint(waypoint.latitude, waypoint.longitude, (float) CaptureArea.getAltitude());
                if (pathWaypoint.isEmpty())
                    mWaypoint.addAction(actionRotate);
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
        mSpeed = speed;

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
                .autoFlightSpeed(mSpeed)
                .maxFlightSpeed(mSpeed)
                .flightPathMode(WaypointMissionFlightPathMode.NORMAL);
        return missionOperator.loadMission(waypointMissionBuilder.build());
    }

    void uploadMission(ErrorCallback callback) {
        if (missionOperator != null)
            missionOperator.uploadMission(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError error) {
                    callback.execute(error);
                    if (error != null)
                        missionOperator.retryUploadMission(null);
                }
            });
    }

    void startMission(ErrorCallback callback) {
        if (missionOperator != null)
            missionOperator.startMission(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError error) {
                    callback.execute(error);
                }
            });
    }

    void stopMission(ErrorCallback callback) {
        if (missionOperator != null)
            missionOperator.stopMission(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError error) {
                    callback.execute(error);
                }
            });
    }

    void setTakePhoto(boolean takePhoto) {
        this.takePhoto = takePhoto;
    }
}