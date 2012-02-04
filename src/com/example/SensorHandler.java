package com.example;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi-dev
 * Date: 2/4/12
 * Time: 2:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class SensorHandler {
    Context context;

    public SensorHandler(Context context) {
        this.context = context;
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(
                Sensor.TYPE_ORIENTATION);

        Sensor sensor = sensors.get(0);
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME);
    }


    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    };


}
