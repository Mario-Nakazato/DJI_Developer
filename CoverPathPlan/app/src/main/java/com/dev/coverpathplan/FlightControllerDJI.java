package com.dev.coverpathplan;

import static com.dev.coverpathplan.FlightState.timeStamp2Date;

import androidx.annotation.NonNull;

import dji.common.error.DJIError;
import dji.common.flightcontroller.Attitude;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.flightcontroller.simulator.InitializationData;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.Simulator;
import dji.sdk.products.Aircraft;

interface StateCallback {
    void execute(FlightState flightState);
}

@SuppressWarnings("Convert2Lambda")
public class FlightControllerDJI {
    private BaseProduct mProduct;
    private FlightController mFlightController;
    private Simulator mFlightControllerSimulator;
    private boolean isSimulating = true;
    private FlightState flightState = new FlightState();

    boolean setProduct(BaseProduct Baseproduct, boolean simulate, StateCallback callback) {
        mProduct = Baseproduct;
        isSimulating = simulate;

        if (mProduct != null && mProduct.isConnected()) {
            if (mProduct instanceof Aircraft) {
                mFlightController = ((Aircraft) mProduct).getFlightController();
                if (isSimulating)
                    mFlightControllerSimulator = mFlightController.getSimulator();
            }
        } else
            return false;

        if (mFlightController != null) {
            onDestroyController();
            onDestroySimulator();
            mFlightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(@NonNull FlightControllerState djiFlightControllerCurrentState) {
                    flightState.currentDateTime = timeStamp2Date("dd/MM/yyyy HH:mm:ss.SSS");
                    flightState.areMotorsOn = djiFlightControllerCurrentState.areMotorsOn();
                    flightState.isFlying = djiFlightControllerCurrentState.isFlying();
                    LocationCoordinate3D aircraftLocation = djiFlightControllerCurrentState.getAircraftLocation();
                    flightState.latitude = aircraftLocation.getLatitude();
                    flightState.longitude = aircraftLocation.getLongitude();
                    flightState.altitude = aircraftLocation.getAltitude();
                    flightState.takeoffLocationAltitude = djiFlightControllerCurrentState.getTakeoffLocationAltitude();
                    Attitude attitude = djiFlightControllerCurrentState.getAttitude();
                    flightState.pitch = attitude.pitch;
                    flightState.roll = attitude.roll;
                    flightState.yaw = attitude.yaw;
                    flightState.velocityX = djiFlightControllerCurrentState.getVelocityX();
                    flightState.velocityY = djiFlightControllerCurrentState.getVelocityY();
                    flightState.velocityZ = djiFlightControllerCurrentState.getVelocityZ();
                    flightState.flightTimeInSeconds = djiFlightControllerCurrentState.getFlightTimeInSeconds();
                    flightState.flightMode = djiFlightControllerCurrentState.getFlightMode().name();
                    flightState.satelliteCount = djiFlightControllerCurrentState.getSatelliteCount();
                    flightState.ultrasonicHeight = djiFlightControllerCurrentState.getUltrasonicHeightInMeters();
                    flightState.flightCount = djiFlightControllerCurrentState.getFlightCount();
                    flightState.aircraftHeadDirection = String.valueOf(djiFlightControllerCurrentState.getAircraftHeadDirection());
                    callback.execute(flightState);
                }
            });
        }

        if (isSimulating) {
            if (mFlightControllerSimulator != null)
                mFlightControllerSimulator.start(InitializationData.createInstance(
                        new LocationCoordinate2D(-23.1858535, -50.6574255), 10, 12), null);
        }

        return true;
    }

    void onDestroyController() {
        if (mFlightController != null)
            mFlightController.setStateCallback(null);
    }

    void onDestroySimulator() {
        if (mFlightControllerSimulator != null) {
            mFlightControllerSimulator.stop(new CommonCallbacks.CompletionCallback<DJIError>() {
                @Override
                public void onResult(DJIError djiError) {

                }
            });
            mFlightControllerSimulator.setStateCallback(null);
        }
    }

    boolean checkGpsCoordination(double locationLat, double locationLng) {
        return (locationLat > -90 && locationLat < 90 && locationLng > -180 && locationLng < 180) && (locationLat != 0f && locationLng != 0f);
    }
}