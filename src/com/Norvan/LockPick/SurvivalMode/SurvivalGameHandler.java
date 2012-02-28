package com.Norvan.LockPick.SurvivalMode;

import android.content.Context;
import com.Norvan.LockPick.LevelHandler;
import com.Norvan.LockPick.SensorHandler;
import com.Norvan.LockPick.VibrationHandler;

/**
 * @author Norvan Gorgi
 *         The state machine for the Puzzle game mode.
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
    private int angularVelocityMinimumThreshold = 10;
    private int gameState = 0;
    public static final int STATE_FRESHLOAD = 0;
    public static final int STATE_INGAME = 1;
    public static final int STATE_BETWEENLEVELS = 2;
    public static final int STATE_GAMEOVER = 3;
    public static final int STATE_PAUSED = 4;
    private boolean gyroExists;

    public SurvivalGameHandler(Context context, GameStatusInterface gameStatusInterface, VibrationHandler vibrationHandler) {
        this.gameStatusInterface = gameStatusInterface;
        this.vibrationHandler = vibrationHandler;
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

    /**
     * Starts the current level.
     */
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

    /**
     * The user pressed the volume button
     */
    public void gotKeyDown() {
        keyPressed = true;
        levelHandler.keyDown(lastPressedPosition);
        vibrationHandler.stopVibrate();
    }

    /**
     * The user released the volume button
     */
    public void gotKeyUp() {
        keyPressed = false;

    }

    private SensorHandler.SensorHandlerInterface sensorHandlerInterface = new SensorHandler.SensorHandlerInterface() {
        @Override
        public void newValues(float angularVelocity, int tilt) {
            if (gameState == STATE_INGAME) {
                if (keyPressed) {
                    //If the user is trying to open the lock

                    switch (levelHandler.getUnlockedState(tilt)) {
                        case LevelHandler.STATE_FAILED://Pick Broken
                            levelLost();
                            break;
                        case LevelHandler.STATE_IN_PROGRESS: //In Progress
                            lastPressedPosition = tilt;
                            if ((angularVelocity * 100) > angularVelocityMinimumThreshold) {
                                //Only vibrate if the user is rotating the phone so that it isn't too easy.
                                int intensity = levelHandler.getIntensityForPositionWhileUnlocking(tilt);

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
                } else {
                    lastPressedPosition = tilt;
                    //If the user is still searching for the sweet spot.
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
                }
            }


        }

    };

    private void levelLost() {
        gameState = STATE_BETWEENLEVELS;
        vibrationHandler.stopVibrate();
        vibrationHandler.pulseLose();
        if (numberOfPicksLeft == 0) {
            //If the user is all out of picks, GAME OVER!
            gameState = STATE_GAMEOVER;
            gameStatusInterface.gameOver(currentLevel);
            //Set things up for the next game.
            currentLevel = 0;
            numberOfPicksLeft = 5;
        } else {
            //Take away a pick.
            numberOfPicksLeft--;
            gameStatusInterface.levelLost(currentLevel, numberOfPicksLeft);
        }
    }

    private void levelWon() {
        gameState = STATE_BETWEENLEVELS;
        vibrationHandler.stopVibrate();
        vibrationHandler.pulseWin();
        gameStatusInterface.levelWon(currentLevel, numberOfPicksLeft);
        currentLevel++;

    }


    /**
     * Interface to communicate game state changes with the Activity
     */
    public interface GameStatusInterface {

        public void levelStart(int level, int picksLeft);

        public void levelWon(int newLevel, int picksLeft);

        public void levelLost(int level, int picksLeft);

        public void gameOver(int maxLevel);
    }


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
