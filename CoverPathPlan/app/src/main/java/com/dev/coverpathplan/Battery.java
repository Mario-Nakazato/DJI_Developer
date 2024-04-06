package com.dev.coverpathplan;

import dji.common.battery.BatteryState;
import dji.sdk.base.BaseProduct;

interface BatteryStateCallback {
    void execute(int chargeRemaining, int chargeRemainingInPercent, int voltage, int current);
}

public class Battery {
    private BaseProduct mProduct;

    boolean setProduct(BaseProduct Baseproduct, BatteryStateCallback callback) {
        mProduct = Baseproduct;

        if (mProduct != null && mProduct.isConnected()) {
            mProduct.getBattery().setStateCallback(new BatteryState.Callback() {
                @Override
                public void onUpdate(BatteryState batteryState) {
                    callback.execute(batteryState.getChargeRemaining(),
                            batteryState.getChargeRemainingInPercent(),
                            batteryState.getVoltage(),
                            batteryState.getCurrent());
                }
            });
        }
        return mProduct != null;
    }
}