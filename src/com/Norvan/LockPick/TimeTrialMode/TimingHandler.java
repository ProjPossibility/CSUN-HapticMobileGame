package com.Norvan.LockPick.TimeTrialMode;

import android.os.SystemClock;
import android.widget.Chronometer;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi-dev
 * Date: 2/14/12
 * Time: 4:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimingHandler {
    private static long startTime = 30000;
    private static long levelWinTime = 10000;

    Chronometer chronoTimer;

    public void setTimingHandlerInterface(TimingHandlerInterface timingHandlerInterface) {
        this.timingHandlerInterface = timingHandlerInterface;
    }

    TimingHandlerInterface timingHandlerInterface;
    private long pauseChronoElapsed = 0;

    public TimingHandler(Chronometer chronoTimer) {
        this.chronoTimer = chronoTimer;
        chronoTimer.setOnChronometerTickListener(onChronometerTickListener);

    }


    public void startTimerNew() {
        chronoTimer.stop();
        chronoTimer.setBase(getCurrentTime());
        chronoTimer.start();
        pauseChronoElapsed = 0;
    }

    public long pauseTimer() {
        chronoTimer.stop();
        pauseChronoElapsed = getCurrentTime() - chronoTimer.getBase();
        return startTime - pauseChronoElapsed;
    }

    public long resumeTimer() {
        chronoTimer.setBase(getCurrentTime() - pauseChronoElapsed);
        chronoTimer.start();
        long timeLeft = startTime - pauseChronoElapsed;
        pauseChronoElapsed = 0;
        return timeLeft;
    }


    public long addLevelWinTime() {
        if (pauseChronoElapsed == 0) {
            chronoTimer.setBase(chronoTimer.getBase() + levelWinTime);
            return startTime - getCurrentTime() - chronoTimer.getBase();
        } else {
            pauseChronoElapsed = pauseChronoElapsed - levelWinTime;
            return startTime - pauseChronoElapsed;
        }
    }

    public long addLevelWinTime(int winLevel) {

        long customWinTime = 1000 * (winLevel + 3);
        if (customWinTime > 10000) {
            customWinTime = 10000;
        }
        if (pauseChronoElapsed == 0) {
            chronoTimer.setBase(chronoTimer.getBase() + customWinTime);
            return startTime - getCurrentTime() - chronoTimer.getBase();
        } else {
            pauseChronoElapsed = pauseChronoElapsed - customWinTime;
            return startTime - pauseChronoElapsed;
        }
    }

    public long getTimeLeft() {
        if (pauseChronoElapsed == 0) {
            return startTime - getCurrentTime() - chronoTimer.getBase();
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
            }

        }
    };

    public interface TimingHandlerInterface {
        public void gotSecondsTick(long timeLeft);

        public void timeIsUp();
    }

    private long getCurrentTime() {
        return SystemClock.elapsedRealtime();
    }

}
