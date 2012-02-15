package com.Norvan.LockPick.TimeTrialMode;

import android.content.Context;
import android.util.Log;
import com.Norvan.LockPick.LevelHandler;
import com.Norvan.LockPick.SensorHandler;
import com.Norvan.LockPick.VibrationHandler;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi-dev
 * Date: 2/14/12
 * Time: 3:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimeTrialGameHandler {
    int currentLevel = 0;
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
    boolean gyroExists;
    TimingHandler timingHandler;

    boolean isPaused = false;

    public boolean isPaused() {
        return isPaused;
    }

    public TimeTrialGameHandler(Context context, GameStatusInterface gameStatusInterface, VibrationHandler vibrationHandler, TimingHandler timingHandler) {
        this.context = context;
        this.gameStatusInterface = gameStatusInterface;
        this.timingHandler = timingHandler;
        this.vibrationHandler = vibrationHandler;
        this.vibrationHandler.setVibrationCompletedInterface(vibrationCompletedInterface);
        levelHandler = new LevelHandler(0);
        sensorHandler = new SensorHandler(context, sensorHandlerInterface);
        currentLevel = 0;
        gyroExists = SensorHandler.hasGyro(context);

        timingHandler.setTimingHandlerInterface(timingHandlerInterface);
        if (!gyroExists) {
            angularVelocityMinimumThreshold = angularVelocityMinimumThreshold * 500;
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
        if (gameState == STATE_BETWEENLEVELS) {

            levelHandler = new LevelHandler(currentLevel);
            keyPressed = false;
            gameStatusInterface.levelStart(currentLevel, timingHandler.getTimeLeft());
            gameState = STATE_INGAME;
            if (!isPolling) {
                sensorHandler.startPolling();
            }
        } else if (gameState == STATE_FRESHLOAD || gameState == STATE_GAMEOVER) {

            levelHandler = new LevelHandler(currentLevel);
            keyPressed = false;
            timingHandler.startTimerNew();
            gameStatusInterface.levelStart(currentLevel, timingHandler.getTimeLeft());
            gameState = STATE_INGAME;
            if (!isPolling) {
                sensorHandler.startPolling();
            }
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

    VibrationHandler.VibrationCompletedInterface vibrationCompletedInterface = new VibrationHandler.VibrationCompletedInterface() {
        @Override
        public void vibrationCompleted() {
            if (gameState == STATE_BETWEENLEVELS) {
                playCurrentLevel();
                timingHandler.resumeTimer();
            }
        }
    };

    TimingHandler.TimingHandlerInterface timingHandlerInterface = new TimingHandler.TimingHandlerInterface() {
        @Override
        public void gotSecondsTick(long timeLeft) {
            gameStatusInterface.updateTimeLeft(timeLeft);
        }

        @Override
        public void timeIsUp() {
            gameOver();
        }
    };

    private void gameOver() {
        gameState = STATE_GAMEOVER;
        timingHandler.pauseTimer();
        gameStatusInterface.gameOver(currentLevel);
        currentLevel = 0;
    }

    private void levelLost() {
        vibrationHandler.stopVibrate();
        timingHandler.pauseTimer();
        vibrationHandler.playSadNotified();
        gameState = STATE_BETWEENLEVELS;
        gameStatusInterface.levelLost(currentLevel);

    }

    private void levelWon() {
        gameState = STATE_BETWEENLEVELS;
        vibrationHandler.stopVibrate();
        timingHandler.pauseTimer();
        timingHandler.addLevelWinTime(currentLevel);
        vibrationHandler.playHappyNotified();
        gameStatusInterface.levelWon(currentLevel);
        currentLevel++;

    }

    public interface GameStatusInterface {
        public void newGameStart();

        public void levelStart(int level, long timeLeft);

        public void levelWon(int level);

        public void levelLost(int level);

        public void gameOver(int maxLevel);

        public void updateTimeLeft(long timeLeft);
    }

    public void pauseGame() {
        isPaused = true;
        timingHandler.pauseTimer();
    }

    public void resumeGame() {
        isPaused = false;
        timingHandler.resumeTimer();
    }

    public boolean togglePause() {
        if (isPaused) {
            isPaused = false;
            timingHandler.resumeTimer();
            return false;
        } else {
            isPaused = true;
            timingHandler.pauseTimer();
            return true;

        }
    }


}
