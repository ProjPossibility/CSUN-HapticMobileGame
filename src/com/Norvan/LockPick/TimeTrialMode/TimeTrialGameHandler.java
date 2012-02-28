package com.Norvan.LockPick.TimeTrialMode;

import android.content.Context;
import com.Norvan.LockPick.LevelHandler;
import com.Norvan.LockPick.ScoreHandler;
import com.Norvan.LockPick.SensorHandler;
import com.Norvan.LockPick.VibrationHandler;

/**
 * @author Norvan Gorgi
 *         The state machine for the Time Attack game mode.
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
    private int keyPressedPosition = -1;
    private GameStatusInterface gameStatusInterface;
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
            angularVelocityMinimumThreshold = angularVelocityMinimumThreshold * SensorHandler.nonGyroSensativityScalar;
        }
    }

    public void setSensorPollingState(boolean state) {
        //Why try-catch? You never know when working with hardware.
        try {
            if (state) {
                sensorHandler.startPolling();
            } else {
                sensorHandler.stopPolling();
            }
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
        if (gameState == STATE_BETWEENLEVELS) {
            //If the user just beat or lost a level

            levelHandler = new LevelHandler(currentLevel);
            keyPressed = false;
            gameStatusInterface.levelStart(currentLevel, timingHandler.getTimeLeft());
            gameState = STATE_INGAME;

        } else if (gameState == STATE_FRESHLOAD || gameState == STATE_GAMEOVER) {
            //if this is a new game
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

    /**
     * The user pressed the volume button
     */
    public void gotKeyDown() {
        keyPressed = true;
        keyPressedPosition = lastPressedPosition;
        levelHandler.keyDown(keyPressedPosition);
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
                    //If the user is still searching for the sweet spot.
                    lastPressedPosition = tilt;

                    if ((angularVelocity * 100) > angularVelocityMinimumThreshold) {
                        //Only vibrate if the user is rotating the phone so that it isn't too easy.

                        int intensity = levelHandler.getIntensityForPosition(tilt);

                        if (intensity < 0) {
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

    private VibrationHandler.VibrationCompletedInterface vibrationCompletedInterface = new VibrationHandler.VibrationCompletedInterface() {
        /**
         * Called when the level win or lose vibration pattern has completed playing. This way they get to play out
         * fully before the game's vibrations cancel them out.
         */

        @Override
        public void vibrationCompleted() {
            if (gameState == STATE_BETWEENLEVELS) {
                timingHandler.startLevel();

                playCurrentLevel();
            }
        }
    };

    private TimingHandler.TimingHandlerInterface timingHandlerInterface = new TimingHandler.TimingHandlerInterface() {
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
        vibrationHandler.stopVibrate();
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
        gameStatusInterface.levelWon(currentLevel, levelTime, bonus);
        currentLevel++;

    }

    /**
     * Interface to communicate game state changes with the Activity
     */
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

    /**
     * The time left before the game ends
     *
     * @return the time left in seconds
     */
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
