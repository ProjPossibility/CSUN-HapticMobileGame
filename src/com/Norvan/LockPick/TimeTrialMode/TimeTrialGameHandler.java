package com.Norvan.LockPick.TimeTrialMode;

import android.content.Context;
import android.util.Log;
import com.Norvan.LockPick.Helpers.GameVariables;
import com.Norvan.LockPick.LevelHandler;
import com.Norvan.LockPick.ScoreHandler;
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
    private int keyPressedPosition = -1;
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
    private TimingHandler timingHandler;
    private ScoreHandler scoreHandler;

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
        if (gameState == STATE_BETWEENLEVELS) {
            levelHandler = new LevelHandler(currentLevel);
            keyPressed = false;
            gameStatusInterface.levelStart(currentLevel, timingHandler.getTimeLeft());
            gameState = STATE_INGAME;

        } else if (gameState == STATE_FRESHLOAD || gameState == STATE_GAMEOVER) {
            if (!sensorHandler.isPolling()) {
                setSensorPollingState(true);
            }
            gameStatusInterface.newGameStart();
            scoreHandler.newGame();
            levelHandler = new LevelHandler(currentLevel);
            keyPressed = false;
            timingHandler.startTimerNew();
            gameStatusInterface.levelStart(currentLevel, timingHandler.getTimeLeft());
            gameState = STATE_INGAME;


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

    private SensorHandler.SensorHandlerInterface sensorHandlerInterface = new SensorHandler.SensorHandlerInterface() {
        @Override
        public void newValues(float angularVelocity, int tilt) {
            if (gameState == STATE_INGAME) {
                if (keyPressed) {
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

    private  VibrationHandler.VibrationCompletedInterface vibrationCompletedInterface = new VibrationHandler.VibrationCompletedInterface() {
        @Override
        public void vibrationCompleted() {
            if (gameState == STATE_BETWEENLEVELS) {
                timingHandler.resumeTimer();

                playCurrentLevel();
            }
        }
    };

    private  TimingHandler.TimingHandlerInterface timingHandlerInterface = new TimingHandler.TimingHandlerInterface() {
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
        boolean isHighScore = scoreHandler.gameOver();
        int currentScore = scoreHandler.getCurrentScore();
        gameStatusInterface.gameOver(currentLevel, isHighScore, currentScore);
        currentLevel = 0;
    }

    private void levelLost() {
        vibrationHandler.stopVibrate();
        timingHandler.levelLost();
        vibrationHandler.playSadNotified();
        gameState = STATE_BETWEENLEVELS;
        gameStatusInterface.levelLost(currentLevel);

    }

    private void levelWon() {
        gameState = STATE_BETWEENLEVELS;
        vibrationHandler.stopVibrate();
        long levelTime = timingHandler.levelWon(currentLevel);
        int bonus = scoreHandler.wonLevel(levelTime);
        vibrationHandler.playHappyNotified();
        Log.i("AMP", "leveltim " + String.valueOf(levelTime));
        gameStatusInterface.levelWon(currentLevel, levelTime, bonus);
        currentLevel++;

    }

    public interface GameStatusInterface {
        public void newGameStart();

        public void levelStart(int level, long levelTime);

        public void levelWon(int level, long time, int bonus);

        public void levelLost(int level);

        public void gameOver(int maxLevel, boolean isHighScore, int score);

        public void updateTimeLeft(long timeLeft);
    }

    public void pauseGame() {
        gameState = STATE_PAUSED;
        timingHandler.pauseTimer();
        vibrationHandler.stopVibrate();
    }

    public void resumeGame() {
        gameState = STATE_INGAME;
        timingHandler.resumeTimer();
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


    public int getSecondsLeft() {
        return (int) (timingHandler.getTimeLeft() / 1000);
    }


    public void setScoreHandler(ScoreHandler scoreHandler) {
        this.scoreHandler = scoreHandler;
    }

    public int getHighScore() {
        return scoreHandler.getHighScore();
    }

    public int getCurrentScore() {
        return scoreHandler.getCurrentScore();
    }
}
