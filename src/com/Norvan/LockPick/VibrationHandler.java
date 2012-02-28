package com.Norvan.LockPick;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;

/**
 * @author Norvan Gorgi
 *         Abstracts out all the functions relating to the vibrator.
 */
public class VibrationHandler {

    private Vibrator vibrator;
    private int PWMsegmentLength = 30;
    private static final int PWMsegmentLengthNoGyro = 15;
    private VibrationCompletedInterface vibrationCompletedInterface;
    private Handler mHandler;
    private boolean gyroExists;


    public VibrationHandler(Context context) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        gyroExists = SensorHandler.hasGyro(context);
        if (!gyroExists) {
            PWMsegmentLength = PWMsegmentLengthNoGyro;
        }
        mHandler = new Handler();


    }

    /**
     * Stops vibration
     */
    public void stopVibrate() {
        vibrator.cancel();
    }

    /**
     * Plays the 'victory' pattern.
     */
    public void pulseWin() {
        long[] happyPattern = {0, 100, 200, 100, 100, 100, 100, 400};
        vibrator.vibrate(happyPattern, -1);
    }

    /**
     * Plays the 'fail' pattern.
     */
    public void pulseLose() {
        long[] sadPattern = {0, 500, 200, 500, 200, 450};
        vibrator.vibrate(sadPattern, -1);
    }

    /**
     * Continuously vibrates at a given intensity using PWM techniques.
     *
     * @param intensity vibration intensity from 0 to 100
     */
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

    /**
     * Pulses out the given string as Morse Code.
     *
     * @param text String to pulse out
     */
    public void playString(String text) {
        vibrator.vibrate(MorseCodeConverter.pattern(text), -1);
    }


    /**
     * Pulses out the given string as Morse Code. Notifies the registered VibrationCompletedInterface when the duration
     * of the pattern has passed.
     *
     * @param text String to pulse out
     */
    public void playStringNotified(String text) {
        long[] pattern = MorseCodeConverter.pattern(text);
        long duration = 0;
        for (long l : pattern) {
            duration = duration + l;
        }
        mHandler.postDelayed(vibrationComplete, duration);
        vibrator.vibrate(pattern, -1);
    }

    public boolean hasVibrator() {
        if (Build.VERSION.SDK_INT > 10) {
            return vibrator.hasVibrator();
        } else {
            return true;
        }
    }

    /**
     * Plays the 'fail' pattern.  Notifies the registered VibrationCompletedInterface when the duration
     * of the pattern has passed.
     */
    public void playSadNotified() {
        long[] sadPattern = {0, 500, 200, 500, 200, 450};
        long duration = 0;
        for (long l : sadPattern) {
            duration = duration + l;
        }
        mHandler.postDelayed(vibrationComplete, duration + 200);
        vibrator.vibrate(sadPattern, -1);
    }

    /**
     * Plays the 'victory' pattern. Notifies the registered VibrationCompletedInterface when the duration
     * of the pattern has passed.
     */
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
            if (vibrationCompletedInterface != null) {
                vibrationCompletedInterface.vibrationCompleted();
            }
        }
    };

    public void setVibrationCompletedInterface(VibrationCompletedInterface vibrationCompletedInterface) {
        this.vibrationCompletedInterface = vibrationCompletedInterface;
    }

    public interface VibrationCompletedInterface {
        /**
         * The previously triggered notifiable vibration has completed.
         */
        public void vibrationCompleted();
    }
}
