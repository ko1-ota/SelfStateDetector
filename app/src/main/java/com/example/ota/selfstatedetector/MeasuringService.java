package com.example.ota.selfstatedetector;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by ota on 17/07/24.
 */

public class MeasuringService extends IntentService implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor barometer;
    private Sensor illuminanceSensor;
    private Sensor gravitySensor;

    //    private float[] pressure = {0,0,0};
//    private float[] illuminance = {0,0,0};
//    private float[] gravity = {0,0,0};
    private GlobalValues globalValues;

    public MeasuringService() {
        super("IntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            globalValues = (GlobalValues) this.getApplication();
            handleActionMeasure();
        }
    }
    private void handleActionMeasure() {
        try {
            Log.d("Service", "in: handleActionMeasure");

            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            // get Sensor
            barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            illuminanceSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            // set SensorEventListener
            sensorManager.registerListener(this, barometer, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, illuminanceSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d("Sensor", "all SensorEventListeners have been registered");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Service", "out: handleActionMeasure");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // put sensor value on float variables
        switch (event.sensor.getType()) {
            case Sensor.TYPE_PRESSURE:
                float[] tmp = event.values.clone();
                for (int i = 0; i < 3; i++) { tmp[i] = tmp[i] / 1000; }
                globalValues.setValue("Pressure", tmp);
                break;
            case Sensor.TYPE_LIGHT:
                tmp = event.values.clone();
                for (int i = 0; i < 3; i++) { tmp[i] = tmp[i] / 10; }
                globalValues.setValue("Illuminance", tmp);
                break;
            case Sensor.TYPE_GRAVITY:
                globalValues.setValue("Gravity", event.values.clone());
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
