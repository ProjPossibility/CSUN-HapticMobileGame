package com.Norvan.LockPick.TutorialMode;

import android.content.Context;
import android.os.Handler;
import com.Norvan.LockPick.LevelHandler;
import com.Norvan.LockPick.SensorHandler;
import com.Norvan.LockPick.VibrationHandler;

/**
 * @author Norvan Gorgi
 *         The state machine for the tutorial "game".
 */
public class TutorialHandler {
    public static final int STEP_START = 0;
    public static final int STEP_preTURNPHONEONSIDE = 1;
    public static final int STEP_postTURNPHONEONSIDE = 2;
    public static final int STEP_preFINDSWEETSPOT = 3;
    public static final int STEP_postFINDSWEETSPOT = 4;
    public static final int STEP_PREFORMUNLOCK = 5;
    public static final int STEP_postPREFORMUNLOCKwin = 6;
    public static final int STEP_postPREFORMUNLOCKlose = 7;

    public static final int STEP_FINISHED = 8;

    public int getCurrentStep() {
        return currentStep;
    }

    private int currentStep = 0;
    private int angularVelocityMinimumThreshold = 10;
    private boolean gyroExists;
    private int lastPressedPosition = -1;
    private boolean keyPressed = false;


    public boolean isPolling() {
        return isPolling;
    }

    private boolean isPolling = false;
    private SensorHandler sensorHandler;
    private boolean scheduledPhoneOnSide = false;
    private boolean scheduledPhoneAtSweetSpot = false;
    private Handler handler;
    private LevelHandler levelHandler;
    private TutorialStatusInterface tutorialStatusInterface;
    private VibrationHandler vibrationHandler;

    public TutorialHandler(Context context, TutorialStatusInterface tutorialStatusInterface, VibrationHandler vibrationHandler) {
        levelHandler = new LevelHandler(0, true);
        this.vibrationHandler = vibrationHandler;

        sensorHandler = new SensorHandler(context, new SensorHandler.SensorHandlerInterface() {
            @Override
            public void newValues(float angularVelocity, int tilt) {

                if (keyPressed) {
                    processSensorValuesUnlocking(angularVelocity, tilt);
                } else {
                    processSensorValuesNormal(angularVelocity, tilt);
                }
            }


        });
        this.tutorialStatusInterface = tutorialStatusInterface;
        gyroExists = SensorHandler.hasGyro(context);
        if (!gyroExists) {
            angularVelocityMinimumThreshold = angularVelocityMinimumThreshold * SensorHandler.nonGyroSensativityScalar;
        }
        handler = new Handler();

    }


    public void setSensorPollingState(boolean state) {
        if (state) {
            sensorHandler.startPolling();
        } else {
            sensorHandler.stopPolling();
        }
        isPolling = state;
    }


    /**
     * The user pressed the volume button
     */
    public void gotKeyDown() {
        if (currentStep == STEP_PREFORMUNLOCK) {
            keyPressed = true;
            levelHandler.keyDown(lastPressedPosition);
            vibrationHandler.stopVibrate();
        }
    }

    /**
     * The user released the volume button
     */
    public void gotKeyUp() {
        keyPressed = false;

    }


    private void processSensorValuesNormal(float angularVelocity, int tilt) {
        lastPressedPosition = tilt;
        if (currentStep == STEP_preFINDSWEETSPOT || currentStep == STEP_PREFORMUNLOCK) {
            if ((angularVelocity * 100) > angularVelocityMinimumThreshold) {

                //Only vibrate if the user is rotating the phone so that it isn't too easy.

                int intensity = levelHandler.getIntensityForPosition(tilt);

                if (intensity <= 0) {
                    vibrationHandler.stopVibrate();
                } else {
                    vibrationHandler.pulsePWM(intensity);
                }
            } else {
                //If the user isn't rotating the phone, don't vibrate
                vibrationHandler.stopVibrate();
            }
            if (currentStep == STEP_preFINDSWEETSPOT) {
                if (levelHandler.tiltIsInSweetSpot(tilt)) {
                    phoneAtSweetSpot();
                } else {
                    phoneNotAtSweetSpot();
                }
            }
        } else if (currentStep == STEP_preTURNPHONEONSIDE) {
            if (tilt > 4000 && tilt < 6000) {
                phoneOnSide();
            } else {
                phoneNotOnSide();
            }
            return;
        }

    }


    private void processSensorValuesUnlocking(float angularVelocity, int tilt) {
        switch (levelHandler.getUnlockedState(tilt)) {
            case LevelHandler.STATE_FAILED://Pick Broken
                levelLost();
                break;
            case LevelHandler.STATE_IN_PROGRESS: //In Progress
                lastPressedPosition = tilt;
                int intensity = levelHandler.getIntensityForPositionWhileUnlocking(tilt);
                if ((angularVelocity * 100) > angularVelocityMinimumThreshold) {
                    //Only vibrate if the user is rotating the phone so that it isn't too easy.

                    if (intensity <= 0) {
                        vibrationHandler.stopVibrate();
                    } else {
                        vibrationHandler.pulsePWM(intensity);
                    }
                } else {
                    //If the user isn't rotating the phone, don't vibrate
                    vibrationHandler.stopVibrate();
                }
                break;
            case LevelHandler.STATE_UNLOCKED: //A WINRAR IS YOU!!!

                levelWon();
                break;
        }
    }

    private void levelWon() {
        vibrationHandler.stopVibrate();
        currentStep = STEP_FINISHED;
        tutorialStatusInterface.preformedUnlock(true);
    }

    private void levelLost() {
        vibrationHandler.stopVibrate();
        currentStep = STEP_postFINDSWEETSPOT;
        tutorialStatusInterface.preformedUnlock(false);
    }

    /**
     * Go to the indicated step in the state machine.
     *
     * @param step the step to go to according to the public static final ints
     */
    public void goToStep(int step) {
        currentStep = step;
        if (step == STEP_preTURNPHONEONSIDE) {
            setSensorPollingState(true);
        } else if (step == STEP_FINISHED) {
            setSensorPollingState(false);
        }

    }

    public interface TutorialStatusInterface {
        public void isOnSide();

        public void sweetSpotFound();

        public void preformedUnlock(boolean success);
    }

    private void phoneAtSweetSpot() {
        //If the phone JUST came into the sweet spot, run the phoneAtSweetSpot runnable in 1 second.
        if (!scheduledPhoneAtSweetSpot) {
            handler.postDelayed(phoneAtSweetSpot, 1000);
            scheduledPhoneAtSweetSpot = true;
        }
    }

    private void phoneNotAtSweetSpot() {
        //If the phone was previously at the sweet spot but is no longer there, cancel the scheduled runnable.
        //This way the user has to hold the phone in the right spot for a full second.
        if (scheduledPhoneAtSweetSpot) {
            handler.removeCallbacks(phoneAtSweetSpot);
            scheduledPhoneAtSweetSpot = false;
        }
    }

    private void phoneNotOnSide() {
        //If the phone was previously on its side but is no longer there, cancel the scheduled runnable.
        //This way the user has to hold the phone on its side for a full second.
        if (scheduledPhoneAtSweetSpot) {
            handler.removeCallbacks(phoneOnSide);
            scheduledPhoneOnSide = false;
        }
    }

    private void phoneOnSide() {
        //If the phone JUST turned on its side, run the phoneOnSide runnable in 1 second.
        if (!scheduledPhoneOnSide) {
            handler.postDelayed(phoneOnSide, 1000);
            scheduledPhoneOnSide = true;
        }
    }

    private Runnable phoneOnSide = new Runnable() {
        @Override
        public void run() {
            tutorialStatusInterface.isOnSide();
        }
    };
    private Runnable phoneAtSweetSpot = new Runnable() {
        @Override
        public void run() {
            tutorialStatusInterface.sweetSpotFound();
        }
    };
}
