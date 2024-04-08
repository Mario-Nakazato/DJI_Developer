package com.dev.coverpathplan;

import dji.common.camera.SettingsDefinitions;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.sdk.base.BaseProduct;

public class Camera {
    private BaseProduct mProduct;
    private int mAspectRadio = 0, mOrientation = 0;

    boolean setProduct(BaseProduct Baseproduct) {
        mProduct = Baseproduct;

        if (mProduct != null && mProduct.isConnected()) {
            mProduct.getCamera().setMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO, null);
            setPhotoAspectRatio(0);
            setOrientation(0);
            setGimbal();
        }
        return mProduct != null;
    }

    void setPhotoAspectRatio(int aspectRatio) {
        if (mProduct == null)
            return;
        mAspectRadio = aspectRatio;
        mProduct.getCamera().setPhotoAspectRatio(mAspectRadio == 0 ? SettingsDefinitions.PhotoAspectRatio.RATIO_4_3 :
                SettingsDefinitions.PhotoAspectRatio.RATIO_16_9, null);
    }

    int getAspectRadio() {
        return mAspectRadio;
    }

    void setOrientation(int orientation) {
        if (mProduct == null)
            return;
        mOrientation = orientation;
        mProduct.getCamera().setOrientation(mOrientation == 0 ? SettingsDefinitions.Orientation.LANDSCAPE :
                SettingsDefinitions.Orientation.PORTRAIT, null);
    }

    int getOrientation() {
        return mOrientation;
    }

    private void setGimbal() {
        if (mProduct == null)
            return;

        mProduct.getGimbal()
                .rotate(new Rotation.Builder().pitch(-90)
                        .mode(RotationMode.ABSOLUTE_ANGLE)
                        .yaw(Rotation.NO_ROTATION)
                        .roll(Rotation.NO_ROTATION)
                        .build(), null);
    }
}