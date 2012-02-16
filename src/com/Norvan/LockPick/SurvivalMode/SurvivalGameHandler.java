package com.Norvan.LockPick.SurvivalMode;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import com.Norvan.LockPick.Helpers.GameVariables;
import com.Norvan.LockPick.LevelHandler;
import com.Norvan.LockPick.SensorHandler;
import com.Norvan.LockPick.VibrationHandler;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi
 * Date: 2/5/12
 * Time: 12:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class SurvivalGameHandler {
    private int numberOfPicksLeft = 5;

    public int getCurrentLevel() {
        return currentLevel;
    }

    private int currentLevel = 0;
    VibrationHandler vibrationHandler;
    SensorHandler sensorHandler;
    LevelHandler levelHandler;
    int lastPressedPosition = -1;
    boolean keyPressed = false;
    boolean isPolling = false;
    int keyPressedPosition = -1;
    GameStatusInterface gameStatusInterface;
    Context context;
    int angularVelocityMinimumThreshold = 10;
    int gameState = 0;
    public static final int STATE_FRESHLOAD = 0;
    public static final int STATE_INGAME = 1;
    public static final int STATE_BETWEENLEVELS = 2;
    public static final int STATE_GAMEOVER = 3;
    public static final int STATE_PAUSED = 4;
    boolean gyroExists;

    public SurvivalGameHandler(Context context, GameStatusInterface gameStatusInterface, VibrationHandler vibrationHandler) {
        this.context = context;
        this.gameStatusInterface = gameStatusInterface;
        this.vibrationHandler = vibrationHandler;
        this.vibrationHandler.setVibrationCompletedInterface(vibrationCompletedInterface);
        levelHandler = new LevelHandler(0);
        sensorHandler = new SensorHandler(context, sensorHandlerInterface);
        currentLevel = 0;
        gyroExists = SensorHandler.hasGyro(context);


        if (!gyroExists) {
            angularVelocityMinimumThreshold = angularVelocityMinimumThreshold * GameVariables.nonGyroSensativityScalar;
        }

    }

    public void setSensorPollingState(boolean state) {
        try {
            if (state) {
                sensorHandler.startPolling();
            } else {
                sensorHandler.stopPolling();
            }
            isPolling = state;

        } catch (Exception e) {

        }

    }

    public int getGameState() {
        return gameState;
    }


    public void playCurrentLevel() {
        levelHandler = new LevelHandler(currentLevel);
        keyPressed = false;
        gameStatusInterface.levelStart(currentLevel, numberOfPicksLeft);
        gameState = STATE_INGAME;
        if (!isPolling) {
            sensorHandler.startPolling();
        }
    }


    public void gotKeyDown() {
        keyPressed = true;
        keyPressedPosition = lastPressedPosition;
        levelHandler.keyDown(keyPressedPosition);
        vibrationHandler.stopVibrate();
    }

    public void gotKeyUp() {
        keyPressed = false;

    }

    SensorHandler.SensorHandlerInterface sensorHandlerInterface = new SensorHandler.SensorHandlerInterface() {
        @Override
        public void newValues(float angularVelocity, int tilt) {
            if (gameState == STATE_INGAME) {
                if (keyPressed) {
                    switch (levelHandler.getUnlockedState(tilt)) {
                        case -1://Pick Broken
                            gameState = STATE_BETWEENLEVELS;
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
                            gameState = STATE_BETWEENLEVELS;
                            break;
                    }
                } else {
                    lastPressedPosition = tilt;

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
            }


        }

        @Override
        public void notOnSide() {

        }
    };

    private void levelLost() {
        vibrationHandler.stopVibrate();
        vibrationHandler.playSadNotified();
        gameState = STATE_BETWEENLEVELS;
        if (numberOfPicksLeft == 0) {
            gameState = STATE_GAMEOVER;
            gameStatusInterface.gameOver(currentLevel);
            currentLevel = 0;
            numberOfPicksLeft = 5;
        } else {
            numberOfPicksLeft--;
            gameStatusInterface.levelLost(currentLevel, numberOfPicksLeft);
        }
    }

    private void levelWon() {
        gameState = STATE_BETWEENLEVELS;
        vibrationHandler.stopVibrate();
        vibrationHandler.playHappyNotified();
        gameStatusInterface.levelWon(currentLevel, numberOfPicksLeft);
        currentLevel++;

    }

    public interface GameStatusInterface {
        public void newGameStart();

        public void levelStart(int level, int picksLeft);

        public void levelWon(int newLevel, int picksLeft);

        public void levelLost(int level, int picksLeft);

        public void gameOver(int maxLevel);
    }

    public int[] getLevelData() {
        //TODO remove
        return levelHandler.getLevelData();
    }

    public int getTargetLocation() {
        return levelHandler.getTargetLocation();
    }

    VibrationHandler.VibrationCompletedInterface vibrationCompletedInterface = new VibrationHandler.VibrationCompletedInterface() {
        @Override
        public void vibrationCompleted() {
        }
    };

    public void pauseGame() {
        gameState = STATE_PAUSED;
        vibrationHandler.stopVibrate();

    }

    public void resumeGame() {
        gameState = STATE_INGAME;

    }

    public boolean togglePause() {
        if (gameState == STATE_PAUSED) {
            resumeGame();
            return false;
        } else {
            pauseGame();
            return true;

        }
    }
}