package com.example.ota.selfstatedetector;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ota on 17/07/26.
 */

public class GlobalValues extends Application{
    private float[] pressure = {0};
    private float[] illuminance = {0};
    private float[] gravity = {0, 0, 0};

    private ClsModel clsModel = new ClsModel();

    public void setValue(String sensorType, float[] sensorValue) {
        switch (sensorType) {
            case "Pressure":
                pressure = new float[] {sensorValue[0]};
                break;
            case "Illuminance":
                illuminance = new float[] {sensorValue[0]};
                break;
            case "Gravity":
                gravity = sensorValue;
                break;
        }
    }

    public List<float[]> getSensorValues() {
        return new ArrayList<>(Arrays.asList(pressure, illuminance, gravity));
    }

    public void setModel(int label) {
        float[] data = new float[5];
        System.arraycopy(pressure, 0, data, 0, 1);
        System.arraycopy(illuminance, 0, data, 1, 1);
        System.arraycopy(gravity, 0, data, 2, 3);
        clsModel.addLabel(label, data);
    }

    public ClsModel getClsModel() {
        return clsModel;
    }
}
