package com.example;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi
 * Date: 2/5/12
 * Time: 12:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class GameHandler {
    int numberOfPicksLeft = 5;
    int currentLevel = 0;
    VibrationHandler vibrationHandler;
    SensorHandler sensorHandler;
    LevelHandler levelHandler;
    int lastPressedPosition = -1;
    boolean keyPressed = false;
    boolean isPolling = false;
    boolean gameInProgress = false;
    int keyPressedPosition = -1;
    private Handler mHandler = new Handler();
    GameStatusInterface gameStatusInterface;
    Context context;

    int gameState = 0;
    public static final int STATE_FRESHLOAD = 0;
    public static final int STATE_INGAME = 1;
    public static final int STATE_MIDLEVEL = 2;
    public static final int STATE_GAMEOVER = 3;
    public GameHandler(Context context,GameStatusInterface gameStatusInterface , VibrationHandler vibrationHandler) {
        this.context = context;
        this.gameStatusInterface = gameStatusInterface;
        this.vibrationHandler = vibrationHandler;
        levelHandler = new LevelHandler(0);
        sensorHandler = new SensorHandler(context, sensorHandlerInterface);
        currentLevel = 0;
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

    public void setGameState(int gameState) {
        this.gameState = gameState;
    }

    public void playCurrentLevel() {


        levelHandler = new LevelHandler(currentLevel);
        gameInProgress = true;
        keyPressed = false;
        gameStatusInterface.levelStart(currentLevel, numberOfPicksLeft);
        if (!isPolling) {
            sensorHandler.startPolling();
        }
        gameState = STATE_INGAME;
    }



    public int getLevelNumber() {
        return currentLevel;
    }

    public int getNumberOfPicks() {
        return numberOfPicksLeft;
    }

    public void gotKeyDown() {
        keyPressed = true;
        keyPressedPosition = lastPressedPosition;
        levelHandler.keyDown(keyPressedPosition);
        Log.i("AMP", "odds "+ String.valueOf(levelHandler.getCurrentTryResult()));
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
                            gameState = STATE_MIDLEVEL;
                            levelLost();
                            break;
                        case 0: //In Progress
                            lastPressedPosition = tilt;
                            int intensity = levelHandler.getIntensityForPositionWhileUnlocking(tilt);
                            if ((angularVelocity * 100) > 10) {
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
                            gameState = STATE_MIDLEVEL;
                            break;
                    }
                } else {
                    lastPressedPosition = tilt;
                    int intensity = levelHandler.getIntensityForPosition(tilt);
                    if ((angularVelocity * 100) > 10) {
                        if (intensity == -1) {
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
        gameState = STATE_MIDLEVEL;
        if(numberOfPicksLeft ==0) {
            gameState = STATE_GAMEOVER;

            gameStatusInterface.gameOver(currentLevel);
            if (SharedPreferencesHandler.getHighScore(context) < currentLevel) {
                SharedPreferencesHandler.setHighScore(context,currentLevel);
            }
            currentLevel = 0;
            numberOfPicksLeft = 5;
        }else {
            numberOfPicksLeft --;
            gameStatusInterface.levelLost(currentLevel, numberOfPicksLeft);
        }
    }

    private void levelWon() {
        gameState = STATE_MIDLEVEL;
        vibrationHandler.stopVibrate();
        gameStatusInterface.levelWon( currentLevel, numberOfPicksLeft);
        currentLevel++;

    }
    
    public interface GameStatusInterface{
        public void levelStart(int level, int picksLeft);
        public void levelWon( int newLevel, int picksLeft);
        public void levelLost(int level, int picksLeft);
        public void gameOver(int maxLevel);
    }

    private long getTime() {
        return SystemClock.elapsedRealtime();
    }


}
