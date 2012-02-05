package com.example;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.security.PublicKey;
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
    int lastTiltReading = -1, lastAngularVelocity = -1;
    SensorHandlerInterface sensorHandlerInterface;
    long lastTimestampGyro = 0;
    long lastTimestampAccel = 0;

    public SensorHandler(Context context, SensorHandlerInterface sensorHandlerInterface) {
        this.context = context;
        this.sensorHandlerInterface = sensorHandlerInterface;
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(
                Sensor.TYPE_ACCELEROMETER);

        Sensor sensor = sensors.get(0);
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME);
        List<Sensor> sensors2 = sensorManager.getSensorList(
                Sensor.TYPE_GYROSCOPE);

        Sensor sensor2 = sensors2.get(0);
        sensorManager.registerListener(sensorEventListener, sensor2, SensorManager.SENSOR_DELAY_GAME);

    }


    public int getTilt() {
        return lastTiltReading;
    }

    public int getAngularVelocity() {
        return lastAngularVelocity;
    }

    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            long timestamp = sensorEvent.timestamp;

            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                if(timestamp-lastTimestampAccel > 500000000){
                    lastTiltReading = (int) (50 * sensorEvent.values[2]) + 500;
                    lastTimestampAccel = timestamp;
                }
            } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                if(timestamp-lastTimestampGyro > 500000000){
                    lastAngularVelocity = (int) sensorEvent.values[2];
                    sensorHandlerInterface.newValues(lastAngularVelocity, lastTiltReading);
                    lastTimestampGyro = timestamp;
                }




            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    };

    public interface SensorHandlerInterface {
        public void newValues(int angularVelocity, int tilt);
    }


}
