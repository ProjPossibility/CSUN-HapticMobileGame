package com.Norvan.LockPick;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
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
     SensorManager sensorManager;
    boolean gyroExists;

    public SensorHandler(Context context, SensorHandlerInterface sensorHandlerInterface) {
        this.context = context;
        this.sensorHandlerInterface = sensorHandlerInterface;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        PackageManager paM = context.getPackageManager();
        gyroExists = paM.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);

        

    }

    public void startPolling() {
        if (gyroExists) {
            Sensor sensorOrientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            Sensor sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sensorManager.registerListener(sensorEventListenerWithGyro, sensorOrientation, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(sensorEventListenerWithGyro, sensorGyroscope, SensorManager.SENSOR_DELAY_GAME);
        }
        else
        {
            Sensor sensorOrientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            sensorManager.registerListener(sensorEventListenerNoGyro, sensorOrientation, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void stopPolling() {
        try {
            sensorManager.unregisterListener(sensorEventListenerNoGyro);
        } catch (Exception e) {
            
        }
        try {
            sensorManager.unregisterListener(sensorEventListenerWithGyro);
        } catch (Exception e) {
            
        }
    }

    SensorEventListener sensorEventListenerWithGyro = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            synchronized (this) {
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
                        lastAngularVelocity = Math.abs(sensorEvent.values[1]);
                        sensorHandlerInterface.newValues(lastAngularVelocity, lastTiltReading);
                        lastTimestampGyro = timestamp;
                    }


                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    };

    SensorEventListener sensorEventListenerNoGyro = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            synchronized (this) {
                long timestamp = sensorEvent.timestamp;

                if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                    if (timestamp - lastTimestampAccel > 50000000) {
                        int currentTiltReading = 0;
                        if (Math.abs(sensorEvent.values[1]) > 100) {
                            currentTiltReading = (int) (sensorEvent.values[2] * 10);
                        } else {
                            currentTiltReading = 1800 - (int) (sensorEvent.values[2] * 10);
                        }
                        currentTiltReading = Math.abs((int) (currentTiltReading / 1.8));

                        int delta = Math.abs(lastTiltReading - currentTiltReading);
                        lastTiltReading = currentTiltReading;
                        
                        lastTimestampAccel = timestamp;
                        Log.i("AMP", "delta "+ String.valueOf(delta)+" tilt "+String.valueOf(currentTiltReading));
                        sensorHandlerInterface.newValues(delta, currentTiltReading);
                    }
                } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    if (timestamp - lastTimestampGyro > 50000000) {
//
                        lastAngularVelocity = Math.abs(sensorEvent.values[1]);
                        sensorHandlerInterface.newValues(lastAngularVelocity, lastTiltReading);
                        lastTimestampGyro = timestamp;
                    }


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


    }


}
