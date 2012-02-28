package com.Norvan.LockPick.TutorialMode;

import android.content.Context;
import android.os.Handler;
import com.Norvan.LockPick.Helpers.GameVariables;
import com.Norvan.LockPick.LevelHandler;
import com.Norvan.LockPick.SensorHandler;
import com.Norvan.LockPick.VibrationHandler;


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
            angularVelocityMinimumThreshold = angularVelocityMinimumThreshold * GameVariables.nonGyroSensativityScalar;
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

    public void gotKeyDown() {
        if (currentStep == STEP_PREFORMUNLOCK) {
            keyPressed = true;
            levelHandler.keyDown(lastPressedPosition);
            vibrationHandler.stopVibrate();
        }
    }

    public void gotKeyUp() {
        keyPressed = false;

    }


    private void processSensorValuesNormal(float angularVelocity, int tilt) {
        lastPressedPosition = tilt;
        if (currentStep == STEP_preFINDSWEETSPOT || currentStep == STEP_PREFORMUNLOCK) {
            if ((angularVelocity * 100) > angularVelocityMinimumThreshold) {
                int intensity = levelHandler.getIntensityForPosition(tilt);
                if (!gyroExists) {
                    intensity = (int) (intensity * 0.7);
                }

                if (intensity < 0) {
                    vibrationHandler.stopVibrate();
                } else {
                    vibrationHandler.pulsePWM(intensity);
                }
            } else {
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
        } else {
            return;
        }

        if ((angularVelocity * 100) > angularVelocityMinimumThreshold) {
            int intensity = levelHandler.getIntensityForPosition(tilt);
            if (!gyroExists) {
                intensity = (int) (intensity * 0.7);
            }

            if (intensity < 0) {
                vibrationHandler.stopVibrate();
            } else {
                vibrationHandler.pulsePWM(intensity);
            }
        } else {
            vibrationHandler.stopVibrate();
        }
    }


    private void processSensorValuesUnlocking(float angularVelocity, int tilt) {
        switch (levelHandler.getUnlockedState(tilt)) {
            case -1://Pick Broken
                levelLost();
                break;
            case 0: //In Progress
                lastPressedPosition = tilt;
                int intensity = levelHandler.getIntensityForPositionWhileUnlocking(tilt);
                if ((angularVelocity * 100) > angularVelocityMinimumThreshold) {
                    if (intensity == -1) {
                        vibrationHandler.stopVibrate();
                    } else {
                        vibrationHandler.pulsePWM(intensity);
                    }
                } else {
                    vibrationHandler.stopVibrate();
                }
                break;
            case 1: //A WINRAR IS YOU!!!

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
        if (!scheduledPhoneAtSweetSpot) {
            handler.postDelayed(phoneAtSweetSpot, 1000);
            scheduledPhoneAtSweetSpot = true;
        }
    }

    private void phoneNotAtSweetSpot() {
        if (scheduledPhoneAtSweetSpot) {
            handler.removeCallbacks(phoneAtSweetSpot);
            scheduledPhoneAtSweetSpot = false;
        }
    }

    private void phoneNotOnSide() {
        if (scheduledPhoneAtSweetSpot) {
            handler.removeCallbacks(phoneOnSide);
            scheduledPhoneOnSide = false;
        }
    }

    private void phoneOnSide() {
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
