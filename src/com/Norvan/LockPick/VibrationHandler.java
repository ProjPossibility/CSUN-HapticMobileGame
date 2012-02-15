package com.Norvan.LockPick;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi-dev
 * Date: 2/4/12
 * Time: 6:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class VibrationHandler {

    Vibrator vibrator;
    private int PWMsegmentLength = 50;
    private static final int PWMsegmentLengthNoGyro = 15;
    VibrationCompletedInterface vibrationCompletedInterface;
    Handler mHandler;
    boolean gyroExists;

    public void setVibrationCompletedInterface(VibrationCompletedInterface vibrationCompletedInterface) {
        this.vibrationCompletedInterface = vibrationCompletedInterface;
    }

    public VibrationHandler(Context context) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        gyroExists = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
        if (!gyroExists) {
            PWMsegmentLength = PWMsegmentLengthNoGyro;
        }
        mHandler = new Handler();


    }

    public void stopVibrate() {
        vibrator.cancel();
    }

    public void playHappy() {
        long[] happyPattern = {0, 100, 200, 100, 100, 100, 100, 400};
        vibrator.vibrate(happyPattern, -1);
    }

    public void playNewLevel() {
        long[] pattern = {0, 150, 100, 150, 100, 150};
        vibrator.vibrate(pattern, -1);
    }

    public void playSad() {
        long[] asadPattern = {0, 100, 200, 7, 3, 7, 3, 7, 3, 7, 3, 7, 3, 7, 3, 7, 3, 7, 3, 7, 3, 7, 3, 7, 3, 7, 3, 7, 3, 7, 3, 7, 3, 7, 3, 7, 3, 7, 3, 7, 3, 7, 403, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6, 4, 6};
        long[] sadPattern = {0, 500, 200, 500, 200, 450};
        vibrator.vibrate(sadPattern, -1);
    }

    public void pulsePWM(int intensity) {
        int onTime = (int) (PWMsegmentLength * ((float) intensity / 100));
        int offTime = PWMsegmentLength - onTime;
        if (offTime < 0) {
            offTime = 0;
        }
        if (onTime < 2) {
            onTime = 1;
        }
        long[] pattern = {0, onTime, offTime};


        vibrator.vibrate(pattern, 0);
    }

    public void playString(String text) {

        vibrator.vibrate(MorseCodeConverter.pattern(text), -1);
    }

    public boolean hasVibrator() {
        if (Build.VERSION.SDK_INT > 10) {
            return vibrator.hasVibrator();
        } else {
            return true;
        }
    }


    public void playSadNotified() {
        long[] sadPattern = {0, 500, 200, 500, 200, 450};
        long duration = 0;
        for (long l : sadPattern) {
            duration = duration + l;
        }
        mHandler.postDelayed(vibrationComplete, duration);
        vibrator.vibrate(sadPattern, -1);
    }

    public void playHappyNotified() {
        long[] happyPattern = {0, 100, 200, 100, 100, 100, 100, 400};
        long duration = 0;
        for (long l : happyPattern) {
            duration = duration + l;
        }
        mHandler.postDelayed(vibrationComplete, duration);
        vibrator.vibrate(happyPattern, -1);
    }

    private Runnable vibrationComplete = new Runnable() {
        @Override
        public void run() {
            //To change body of implemented methods use File | Settings | File Templates.
            if (vibrationCompletedInterface != null) {
                vibrationCompletedInterface.vibrationCompleted();
            }
        }
    };

    public interface VibrationCompletedInterface {
        public void vibrationCompleted();
    }
}
