package com.example.datacollector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class TestSensorListener implements SensorEventListener {

    String name= "defaultSensor";
    int samples = 0;

    public TestSensorListener(String name){
        this.name = name;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        samples++;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public String getName() {
        return name;
    }

    public int getSamples() {
        return samples;
    }
}
