package com.Norvan.LockPick.TimeTrialMode;

import android.os.SystemClock;
import android.widget.Chronometer;

/**
 * @author Norvan Gorgi
 *         Since Time Attack mode needs a more robust timing system than Puzzle Mode, this class abstracts out all the
 *         functions related to timekeeping in that mode.
 */
public class TimingHandler {
    private static long startTime = 30000;

    /**
     * The time left when a new level starts. Used to calculate the time it took to beat a level.
     */
    private long levelStartTimeLeft;


    private Chronometer chronoTimer;


    public void setTimingHandlerInterface(TimingHandlerInterface timingHandlerInterface) {
        this.timingHandlerInterface = timingHandlerInterface;
    }

    UpdateTimeLeftInterface updateTimeLeftInterface;

    public void setUpdateTimeLeftInterface(UpdateTimeLeftInterface updateTimeLeftInterface) {
        this.updateTimeLeftInterface = updateTimeLeftInterface;
    }

    TimingHandlerInterface timingHandlerInterface;

    /**
     * How much time had elapsed when the timer was paused. This value is used to calculate the adjusted base for the
     * timer when it is resumed.
     */
    private long pauseChronoElapsed = -1;

    /**
     * @param chronoTimer The hidden Chronometer object in the Activity. Why use a hidden view? Because it's the easiest
     *                    way to be notified of 1 second clock ticks.
     */
    public TimingHandler(Chronometer chronoTimer) {
        this.chronoTimer = chronoTimer;
        chronoTimer.setOnChronometerTickListener(onChronometerTickListener);

    }

    /**
     * Starts a new game.
     */
    public void startTimerNew() {
        chronoTimer.stop();
        chronoTimer.setBase(getCurrentTime());
        chronoTimer.start();
        pauseChronoElapsed = 0;
        levelStartTimeLeft = startTime;

    }

    public void pauseTimer() {
        chronoTimer.stop();
        pauseChronoElapsed = getCurrentTime() - chronoTimer.getBase();
    }

    /**
     * Starts a new level because it is paused during the victory or loss vibration pattern (to preserve time).
     * Almost the same as resumeTimer() but also sets a new levelStartTimeLeft.
     */
    public void startLevel() {
        chronoTimer.setBase(getCurrentTime() - pauseChronoElapsed);
        chronoTimer.start();
        pauseChronoElapsed = 0;
        levelStartTimeLeft = startTime - (getCurrentTime() - chronoTimer.getBase());
    }


    public void resumeTimer() {
        chronoTimer.setBase(getCurrentTime() - pauseChronoElapsed);
        chronoTimer.start();
        pauseChronoElapsed = 0;

    }


    /**
     * Called when the the user breaks the pick (loses the level).
     */
    public void levelLost() {

        pauseTimer();

    }

    /**
     * Called when the user wins a level. Adds more time to the timer based on the level and returns the time it took to
     * complete the level.
     *
     * @param winLevel the level that was completed. Used to calculate the time bonus so that the user doesn't get too
     *                 much free time by completing the first few easy levels.
     * @return the time, in ms, that it took to complete the level.
     */
    public long levelWon(int winLevel) {
        long customWinTime = 1000 * (winLevel + 3);
        if (customWinTime > 10000) {
            customWinTime = 10000;
        }
        long timeLeft = startTime - (getCurrentTime() - chronoTimer.getBase());
        long levelTime = levelStartTimeLeft - timeLeft;

        pauseTimer();
        pauseChronoElapsed = pauseChronoElapsed - customWinTime;
        return Math.abs(levelTime);
    }

    /**
     * @return the time left before the game is over in ms.
     */
    public long getTimeLeft() {

        if (pauseChronoElapsed == 0) {
            return startTime - (getCurrentTime() - chronoTimer.getBase());
        } else {
            return startTime - pauseChronoElapsed;
        }
    }

    /**
     * Updates the time left every second. Notifies observers.
     */
    private Chronometer.OnChronometerTickListener onChronometerTickListener = new Chronometer.OnChronometerTickListener() {
        @Override
        public void onChronometerTick(Chronometer chronometer) {
            long timeleft = startTime - (getCurrentTime() - chronometer.getBase());
            if (timeleft <= 0) {
                timingHandlerInterface.timeIsUp();
            } else {
                timingHandlerInterface.gotSecondsTick(timeleft);
                updateTimeLeftInterface.updateTimeLeft(timeleft);
            }

        }
    };

    /**
     * Interface for the game handler.
     */
    public interface TimingHandlerInterface {
        public void gotSecondsTick(long timeLeft);

        public void timeIsUp();
    }

    /**
     * Interface for the Activity to update the time left view.
     */
    public interface UpdateTimeLeftInterface {
        public void updateTimeLeft(long timeLeft);
    }

    private long getCurrentTime() {
        return SystemClock.elapsedRealtime();
    }

}
