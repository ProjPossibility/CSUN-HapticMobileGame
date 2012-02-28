package com.Norvan.LockPick;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;

/**
 * @author Norvan Gorgi
 *         Abstracts out all the functions relating to communicating with the device's sensors.
 */
public class SensorHandler {
    public static final int nonGyroSensativityScalar = 110;
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


    /**
     * @return whether the sensors are currently being polled.
     */
    public boolean isPolling() {
        return isPolling;
    }

    /**
     * Starts polling the sensors for values.
     */
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


    /**
     * Stops polling the sensors for values.
     */
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


    /**
     *
     * Handles sensor data for devices with a gyroscope.
     *
     */
    private SensorEventListener sensorEventListenerWithGyro = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            synchronized (this) {
                long timestamp = sensorEvent.timestamp;
                if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                    //if the refresh delay has passed since the last used reading
                    if (timestamp - lastTimestampAccel > refreshDelay) {
                        //Tilt readings are reported as the angular distance to the horizon. We need check which way the
                        //phones screen is facing to add direction to the distance
                        boolean isFacingDown = (Math.abs(sensorEvent.values[1]) > 90);
                        float phoneRightSideHeading = sensorEvent.values[2] + 90;

                        //If needed, mirrors the value on the Y axis
                        if (isFacingDown) {
                            phoneRightSideHeading = 360 - phoneRightSideHeading;
                        }
                        //Converts to a 10,000 unit scale
                        lastTiltReading = (int) ((phoneRightSideHeading * 10000) / 360);


                        //Auto-detection for the initial side facing up.
                        if (initialSideFacingUp == 0) {
                            if ((lastTiltReading > 4000 && lastTiltReading < 4950) || (lastTiltReading > 5050 && lastTiltReading < 6000)) {
                                initialSideFacingUp = RIGHT_FACING_UP;
                                logInitialSideUp(initialSideFacingUp);
                            } else if ((lastTiltReading > 9000 && lastTiltReading < 9050) || (lastTiltReading < 1000 && lastTiltReading > 50)) {
                                initialSideFacingUp = LEFT_FACING_UP;
                                logInitialSideUp(initialSideFacingUp);
                            }
                        }

                        //Reverses the readings to compensate for "lefty mode" by mirroring on the X axis.
                        if (initialSideFacingUp == LEFT_FACING_UP) {
                            if (lastTiltReading > 5000) {
                                lastTiltReading = lastTiltReading - 5000;
                            } else {
                                lastTiltReading = lastTiltReading + 5000;
                            }
                        }

                        //Tilt readings are stored

                        lastTimestampAccel = timestamp;
                    }
                } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    if (timestamp - lastTimestampGyro > refreshDelay) {

                        //Don't do anything if the phone hasn't gone upright yet.
                        if (initialSideFacingUp != 0) {
                            lastAngularVelocity = Math.abs(sensorEvent.values[1]);

                            //Buffers out angular velocity and then averages it.
                            angularVelocityBuffer.remove(0);
                            angularVelocityBuffer.add(lastAngularVelocity);
                            lastAngularVelocity = 0;
                            for (float val : angularVelocityBuffer) {
                                lastAngularVelocity = lastAngularVelocity + val;
                            }
                            lastAngularVelocity = lastAngularVelocity / angularVelocityBuffer.size();

                            //Notifies of new values with the current angular velocity and the last stored tilt.
                            sensorHandlerInterface.newValues(lastAngularVelocity, lastTiltReading);
                        }
                        lastTimestampGyro = timestamp;
                    }


                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
         }
    };


    /**
     *
     * Handles sensor data for devices WITHOUT a gyroscope.
     *
     */
    private SensorEventListener sensorEventListenerNoGyro = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            synchronized (this) {

                if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                    long timestamp = sensorEvent.timestamp;

                    //if the refresh delay has passed since the last used reading
                    if (timestamp - lastTimestampAccel > refreshDelayNoGyro) {
                        int currentTiltReading;

                        //Tilt readings are reported as the angular distance to the horizon. We need check which way the
                        //phones screen is facing to add direction to the distance
                        boolean isFacingDown = (Math.abs(sensorEvent.values[1]) > 90);
                        float phoneRightSideHeading = sensorEvent.values[2] + 90;

                        //If needed, mirrors the value on the Y axis
                        if (isFacingDown) {
                            phoneRightSideHeading = 360 - phoneRightSideHeading;
                        }

                        //Converts to a 10,000 unit scale
                        currentTiltReading = (int) ((phoneRightSideHeading * 10000) / 360);

                        //Auto-detection for the initial side facing up.
                        if (initialSideFacingUp == 0) {
                            if ((lastTiltReading > 4000 && lastTiltReading < 4950) || (lastTiltReading > 5050 && lastTiltReading < 6000)) {
                                initialSideFacingUp = RIGHT_FACING_UP;
                                logInitialSideUp(initialSideFacingUp);
                            } else if ((lastTiltReading > 9000 && lastTiltReading < 9050) || (lastTiltReading < 1000 && lastTiltReading > 50)) {
                                initialSideFacingUp = LEFT_FACING_UP;
                                logInitialSideUp(initialSideFacingUp);
                            }
                        }

                        //Reverses the readings to compensate for "lefty mode" by mirroring on the X axis.
                        if (initialSideFacingUp == LEFT_FACING_UP) {
                            if (currentTiltReading > 5000) {
                                currentTiltReading = currentTiltReading - 5000;
                            } else {
                                currentTiltReading = currentTiltReading + 5000;
                            }
                        }

                        //No gyroscope to get angular velocity from? Just use delta orientation!
                        int delta = Math.abs(lastTiltReading - currentTiltReading);

                        //Buffers out delta and then averages it.
                        angularVelocityBuffer.remove(0);
                        angularVelocityBuffer.add((float) delta);
                        delta = 0;
                        for (float val : angularVelocityBuffer) {
                            delta = (int) (delta + val);
                        }
                        delta = delta / angularVelocityBuffer.size();

                        lastTiltReading = currentTiltReading;
                        lastTimestampAccel = timestamp;

                        //Don't do anything if the phone hasn't gone upright yet.
                        if (initialSideFacingUp != 0) {
                            sensorHandlerInterface.newValues(delta, currentTiltReading);
                        }

                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
         }
    };


    //For debugging purposes
    private void logInitialSideUp(int side) {
        if (side == LEFT_FACING_UP) {
            Log.i("AMP", "LEFT FACING UP");
        } else if (side == RIGHT_FACING_UP) {
            Log.i("AMP", "RIGHT FACING UP");
        }
    }

    public interface SensorHandlerInterface {
        /**
         * The sensors have received new values since the last delay threshold.
         *
         * @param angularVelocity at what speed the phone is turning
         * @param tilt            heading of the side of the phone initially facing up. 0-10,000, starting at 6 oclock
         *                        and going counter-clockwise.
         */
        public void newValues(float angularVelocity, int tilt);


    }


    /**
     * @return whether the device has a built in gyroscope.
     */
    public static boolean hasGyro(Context context) {
        PackageManager paM = context.getPackageManager();
        boolean hasGyro = paM.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
//        hasGyro = false;
        return hasGyro;
    }


}
