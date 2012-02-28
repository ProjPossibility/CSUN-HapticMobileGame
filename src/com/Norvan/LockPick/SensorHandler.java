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
    private int lastTiltReading = -1;
    private float lastAngularVelocity = -1;
    private SensorHandlerInterface sensorHandlerInterface;
    private long lastTimestampGyro = 0;
    private long lastTimestampAccel = 0;
    private SensorManager sensorManager;
    private boolean gyroExists;
    private ArrayList<Float> angularVelocityBuffer;
    private int initialSideFacingUp = 0;
    private static final int LEFT_FACING_UP = -1;
    private static final int RIGHT_FACING_UP = 1;
    private boolean isPolling = false;

    private static final int refreshDelay = 10000000;
    private static final int refreshDelayNoGyro = 10000000;

    public SensorHandler(Context context, SensorHandlerInterface sensorHandlerInterface) {
        this.sensorHandlerInterface = sensorHandlerInterface;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        gyroExists = hasGyro(context);
        angularVelocityBuffer = new ArrayList<Float>();
        angularVelocityBuffer.add(0f);
        angularVelocityBuffer.add(0f);
        if (!gyroExists) {
            angularVelocityBuffer.add(0f);

        }

    }

    public boolean isPolling() {
        return isPolling;
    }

    public void startPolling() {
        try {
            if (gyroExists) {
                Sensor sensorOrientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
                Sensor sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                sensorManager.registerListener(sensorEventListenerWithGyro, sensorOrientation, SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(sensorEventListenerWithGyro, sensorGyroscope, SensorManager.SENSOR_DELAY_GAME);
            } else {
                //Some devices will not give orientation readings unless gravity sensor is also registered. Its values are not used.
                Sensor sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
                sensorManager.registerListener(sensorEventListenerNoGyro, sensorGravity, SensorManager.SENSOR_DELAY_GAME);

                Sensor sensorOrientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
                sensorManager.registerListener(sensorEventListenerNoGyro, sensorOrientation, SensorManager.SENSOR_DELAY_GAME);
            }
            isPolling = true;
        } catch (Exception e) {

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
        isPolling = false;
    }

    private SensorEventListener sensorEventListenerWithGyro = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            synchronized (this) {
                long timestamp = sensorEvent.timestamp;

                if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                    if (timestamp - lastTimestampAccel > refreshDelay) {

                        boolean isFacingDown = (Math.abs(sensorEvent.values[1]) > 90);
                        float phoneRightSideHeading = sensorEvent.values[2] + 90;
                        if (isFacingDown) {
                            phoneRightSideHeading = 360 - phoneRightSideHeading;
                        }
                        lastTiltReading = (int) ((phoneRightSideHeading * 10000) / 360);

                        if (initialSideFacingUp == 0) {
                            if ((lastTiltReading > 4000 && lastTiltReading < 4950) || (lastTiltReading > 5050 && lastTiltReading < 6000)) {
                                initialSideFacingUp = RIGHT_FACING_UP;
                                logInitialSideUp(initialSideFacingUp);
                            } else if ((lastTiltReading > 9000 && lastTiltReading < 9050) || (lastTiltReading < 1000 && lastTiltReading > 50)) {
                                initialSideFacingUp = LEFT_FACING_UP;
                                logInitialSideUp(initialSideFacingUp);
                            }
                        }

                        if (initialSideFacingUp == LEFT_FACING_UP) {
                            if (lastTiltReading > 5000) {
                                lastTiltReading = lastTiltReading - 5000;
                            } else {
                                lastTiltReading = lastTiltReading + 5000;
                            }
                        }
                        lastTimestampAccel = timestamp;
                    }
                } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    if (timestamp - lastTimestampGyro > refreshDelay) {
                        if (initialSideFacingUp != 0) {
                            lastAngularVelocity = Math.abs(sensorEvent.values[1]);
                            angularVelocityBuffer.remove(0);
                            angularVelocityBuffer.add(lastAngularVelocity);
                            lastAngularVelocity = 0;
                            for (float val : angularVelocityBuffer) {
                                lastAngularVelocity = lastAngularVelocity + val;
                            }
                            lastAngularVelocity = lastAngularVelocity / angularVelocityBuffer.size();
                            sensorHandlerInterface.newValues(lastAngularVelocity, lastTiltReading);
                        }
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

    private SensorEventListener sensorEventListenerNoGyro = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            synchronized (this) {

                if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                    long timestamp = sensorEvent.timestamp;

                    if (timestamp - lastTimestampAccel > refreshDelayNoGyro) {
                        int currentTiltReading;
                        boolean isFacingDown = (Math.abs(sensorEvent.values[1]) > 90);
                        float phoneRightSideHeading = sensorEvent.values[2] + 90;
                        if (isFacingDown) {
                            phoneRightSideHeading = 360 - phoneRightSideHeading;
                        }
                        currentTiltReading = (int) ((phoneRightSideHeading * 10000) / 360);
                        if (initialSideFacingUp == 0) {
                            if ((lastTiltReading > 4000 && lastTiltReading < 4950) || (lastTiltReading > 5050 && lastTiltReading < 6000)) {
                                initialSideFacingUp = RIGHT_FACING_UP;
                                logInitialSideUp(initialSideFacingUp);
                            } else if ((lastTiltReading > 9000 && lastTiltReading < 9050) || (lastTiltReading < 1000 && lastTiltReading > 50)) {
                                initialSideFacingUp = LEFT_FACING_UP;
                                logInitialSideUp(initialSideFacingUp);
                            }
                        }
                        if (initialSideFacingUp == LEFT_FACING_UP) {
                            if (currentTiltReading > 5000) {
                                currentTiltReading = currentTiltReading - 5000;
                            } else {
                                currentTiltReading = currentTiltReading + 5000;
                            }
                        }
                        int delta = Math.abs(lastTiltReading - currentTiltReading);

                        angularVelocityBuffer.remove(0);
                        angularVelocityBuffer.add((float) delta);
                        delta = 0;
                        for (float val : angularVelocityBuffer) {
                            delta = (int) (delta + val);
                        }
                        delta = delta / angularVelocityBuffer.size();
                        lastTiltReading = currentTiltReading;
                        lastTimestampAccel = timestamp;
                        if (initialSideFacingUp != 0) {
                            sensorHandlerInterface.newValues(delta, currentTiltReading);
                        }

                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    };
    
    private void logInitialSideUp(int side) {
        if (side == LEFT_FACING_UP) {
            Log.i("AMP", "LEFT FACING UP");
        }   else if (side == RIGHT_FACING_UP) {
            Log.i("AMP", "RIGHT FACING UP");
        }
    }

    public interface SensorHandlerInterface {
        public void newValues(float angularVelocity, int tilt);


    }

    public static boolean hasGyro(Context context) {
        PackageManager paM = context.getPackageManager();
        boolean hasGyro = paM.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
//        hasGyro = false;
        return hasGyro;
    }


}
