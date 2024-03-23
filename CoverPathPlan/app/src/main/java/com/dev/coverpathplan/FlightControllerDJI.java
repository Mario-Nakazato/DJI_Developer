package com.dev.coverpathplan;

import com.google.android.gms.maps.model.LatLng;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.simulator.InitializationData;
import dji.common.flightcontroller.simulator.SimulatorState;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.Simulator;
import dji.sdk.products.Aircraft;

interface StateCallback {
    void execute();
}

public class FlightControllerDJI {
    private BaseProduct product;
    private FlightController mFlightController;
    private Simulator mFlightControllerSimulator;
    private boolean isSimulating = true;
    private double locationLat = 181, locationLng = 181, attitudeYaw;

    boolean setProduct(BaseProduct Baseproduct, boolean simulate, StateCallback callback) {
        product = Baseproduct;
        isSimulating = simulate;
        locationLat = 181;
        locationLng = 181;

        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mFlightController = ((Aircraft) product).getFlightController();
                if (isSimulating)
                    mFlightControllerSimulator = mFlightController.getSimulator();
            }
        } else {
            return false;
        }

        if (!isSimulating) {
            if (mFlightController != null) {
                mFlightController.setStateCallback(new FlightControllerState.Callback() {
                    @Override
                    public void onUpdate(FlightControllerState djiFlightControllerCurrentState) {
                        locationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                        locationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                        attitudeYaw = djiFlightControllerCurrentState.getAttitude().yaw;
                        callback.execute();
                    }
                });
            }
            onDestroySimulator();
        } else {
            if (mFlightControllerSimulator != null) {
                mFlightControllerSimulator.start(InitializationData.createInstance(new LocationCoordinate2D(-23.1858535, -50.6574255), 10, 12),
                        new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {

                            }
                        });
                mFlightControllerSimulator.setStateCallback(new SimulatorState.Callback() {
                    @Override
                    public void onUpdate(SimulatorState simulatorState) {
                        locationLat = simulatorState.getLocation().getLatitude();
                        locationLng = simulatorState.getLocation().getLongitude();
                        attitudeYaw = simulatorState.getYaw();
                        callback.execute();
                    }
                });
            }
            onDestroyController();
        }
        return true;
    }

    void onDestroyController() {
        if (mFlightController != null)
            mFlightController.setStateCallback(null);
    }

    void onDestroySimulator() {
        if (mFlightControllerSimulator != null) {
            mFlightControllerSimulator.stop(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {

                }
            });
            mFlightControllerSimulator.setStateCallback(null);
        }
    }

    LatLng getLocation() {
        if (!checkGpsCoordination())
            return null;
        return new LatLng(locationLat, locationLng);
    }

    boolean checkGpsCoordination() {
        return (locationLat > -90 && locationLat < 90 && locationLng > -180 && locationLng < 180) && (locationLat != 0f && locationLng != 0f);
    }

    double getAttitudeYaw() {
        return attitudeYaw;
    }
}
