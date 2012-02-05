package com.example;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.security.PublicKey;
import java.util.ArrayList;
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
    int lastTiltReading = -1;
    float lastAngularVelocity = -1;
    SensorHandlerInterface sensorHandlerInterface;
    long lastTimestampGyro = 0;
    long lastTimestampAccel = 0;

    public SensorHandler(Context context, SensorHandlerInterface sensorHandlerInterface) {
        this.context = context;
        this.sensorHandlerInterface = sensorHandlerInterface;
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(
                Sensor.TYPE_ORIENTATION);

        Sensor sensor = sensors.get(0);
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME);
        List<Sensor> sensors2 = sensorManager.getSensorList(
                Sensor.TYPE_GYROSCOPE);

        Sensor sensor2 = sensors2.get(0);
        sensorManager.registerListener(sensorEventListener, sensor2, SensorManager.SENSOR_DELAY_GAME);
    }




    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            long timestamp = sensorEvent.timestamp;

            if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                if (timestamp - lastTimestampAccel > 50000000) {
                    if (Math.abs(sensorEvent.values[1]) > 100) {
                        lastTiltReading = (int) (sensorEvent.values[2] * 10);
                    } else {
                        lastTiltReading = 1800 - (int) (sensorEvent.values[2] * 10);
                    }
                    lastTiltReading = Math.abs((int) (lastTiltReading / 1.8));
                    lastTimestampAccel = timestamp;
                }
            } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                if (timestamp - lastTimestampGyro > 50000000) {
//
                    lastAngularVelocity  = Math.abs(sensorEvent.values[1]);
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
        public void newValues(float angularVelocity, int tilt);

        public void showAngularVelocity(float[] values);
    }


}
