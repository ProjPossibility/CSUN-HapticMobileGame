package com.Norvan.LockPick.SurvivalMode;

import android.content.Context;
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
    public int getNumberOfPicksLeft() {
        return numberOfPicksLeft;
    }

    private int numberOfPicksLeft = 5;

    public int getCurrentLevel() {
        return currentLevel;
    }

    private int currentLevel = 0;
    private VibrationHandler vibrationHandler;
    private SensorHandler sensorHandler;
    private LevelHandler levelHandler;
    private int lastPressedPosition = -1;
    private boolean keyPressed = false;
    private boolean isPolling = false;
    private GameStatusInterface gameStatusInterface;
    private Context context;
    private int angularVelocityMinimumThreshold = 10;
    private int gameState = 0;
    public static final int STATE_FRESHLOAD = 0;
    public static final int STATE_INGAME = 1;
    public static final int STATE_BETWEENLEVELS = 2;
    public static final int STATE_GAMEOVER = 3;
    public static final int STATE_PAUSED = 4;
    private boolean gyroExists;

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
            angularVelocityMinimumThreshold = angularVelocityMinimumThreshold * SensorHandler.nonGyroSensativityScalar;
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
        if (!sensorHandler.isPolling()) {
            setSensorPollingState(true);
        }
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
        levelHandler.keyDown(lastPressedPosition);
        vibrationHandler.stopVibrate();
    }

    public void gotKeyUp() {
        keyPressed = false;

    }

    private SensorHandler.SensorHandlerInterface sensorHandlerInterface = new SensorHandler.SensorHandlerInterface() {
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

    };

    private void levelLost() {
        vibrationHandler.stopVibrate();
        vibrationHandler.playSad();
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
        vibrationHandler.playHappy();
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

    private VibrationHandler.VibrationCompletedInterface vibrationCompletedInterface = new VibrationHandler.VibrationCompletedInterface() {
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
