package com.Norvan.LockPick.TimeTrialMode;

import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;


public class TimingHandler {
    private static long startTime = 30000;

    private long levelStartTimeLeft;


    private   Chronometer chronoTimer;


    public void setTimingHandlerInterface(TimingHandlerInterface timingHandlerInterface) {
        this.timingHandlerInterface = timingHandlerInterface;
    }

    UpdateTimeLeftInterface updateTimeLeftInterface;

    public void setUpdateTimeLeftInterface(UpdateTimeLeftInterface updateTimeLeftInterface) {
        this.updateTimeLeftInterface = updateTimeLeftInterface;
    }

    TimingHandlerInterface timingHandlerInterface;
    private long pauseChronoElapsed = -1;

    public TimingHandler(Chronometer chronoTimer) {
        this.chronoTimer = chronoTimer;
        chronoTimer.setOnChronometerTickListener(onChronometerTickListener);

    }


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

    public void resumeTimer() {
        chronoTimer.setBase(getCurrentTime() - pauseChronoElapsed);
        chronoTimer.start();
        pauseChronoElapsed = 0;

    }

    public void levelLost(){
        long timeLeft = startTime - (getCurrentTime() - chronoTimer.getBase());
        levelStartTimeLeft = timeLeft;
        pauseTimer();

    }

    public long levelWon(int winLevel) {
        long customWinTime = 1000 * (winLevel + 3);
        if (customWinTime > 10000) {
            customWinTime = 10000;
        }
        long timeLeft = startTime - (getCurrentTime() - chronoTimer.getBase());
        long levelTime = levelStartTimeLeft - timeLeft;
        levelStartTimeLeft = timeLeft;
        pauseTimer();
        pauseChronoElapsed = pauseChronoElapsed - customWinTime;
        return levelTime;
    }

    public long getTimeLeft() {

        if (pauseChronoElapsed == 0) {
            return startTime - (getCurrentTime() - chronoTimer.getBase());
        } else {
            return startTime - pauseChronoElapsed;
        }
    }


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

    public interface TimingHandlerInterface {
        public void gotSecondsTick(long timeLeft);

        public void timeIsUp();
    }

    public interface UpdateTimeLeftInterface {
        public void updateTimeLeft(long timeLeft);
    }

    private long getCurrentTime() {
        return SystemClock.elapsedRealtime();
    }

}
