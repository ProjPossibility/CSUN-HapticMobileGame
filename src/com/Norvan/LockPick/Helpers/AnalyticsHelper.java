package com.Norvan.LockPick.Helpers;

import android.content.Context;
import com.Norvan.LockPick.SensorHandler;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi-dev
 * Date: 2/14/12
 * Time: 6:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class AnalyticsHelper {
    GoogleAnalyticsTracker tracker;
    boolean DEV_MODE = true;
    boolean hasGyro;

    public AnalyticsHelper(Context context) {
        tracker = GoogleAnalyticsTracker.getInstance();
        if (DEV_MODE) {
            tracker.startNewSession("UA-29206443-1", context);

        } else {
            tracker.startNewSession("UA-29193154-2", context);
        }
        hasGyro = SensorHandler.hasGyro(context);
    }

    public void startApp(int userType) {
        tracker.trackPageView("/mainPage_" + String.valueOf(userType));
        tracker.dispatch();
        tracker.stopSession();
    }

    public void startSurvivalActivity() {
        tracker.trackPageView("/survivalMode");
    }

    public void startTimeTrialActivity() {
        tracker.trackPageView("/timetrialMode");
    }

    public void winSurvivalLevel(int level, int time, int picksLeft) {
        tracker.trackEvent(
                "SurvivalWin",  // Category
                "Level: " + String.valueOf(level),  // Action
                "Picks: " + String.valueOf(picksLeft),
                time);       // Value
    }

    public void loseSurvivalLevel(int level, int time, int picksLeftAfter) {
        tracker.trackEvent(
                "SurvivalWin",  // Category
                "Level: " + String.valueOf(level),  // Action
                "Picks: " + String.valueOf(picksLeftAfter),
                time);       // Value
    }

    public void gameOverSurvival(int score, int maxlevel) {
        tracker.trackEvent(
                "Survival",  // Category
                "GAME OVER", String.valueOf(maxlevel) + " " + String.valueOf(hasGyro), score);       // Value
    }

    public void winTimeTrialLevel(int level, int timeLeft) {
        tracker.trackEvent(
                "TimeTrial",  // Category
                "WIN",  // Action
                String.valueOf(level), // Label
                timeLeft);       // Value
    }

    public void loseTimeTrialLevel(int level, int timeLeft) {
        tracker.trackEvent(
                "TimeTrial",  // Category
                "LOSE",  // Action
                String.valueOf(level), // Label
                timeLeft);       // Value
    }

    public void gameOverTimeTrial(int score, int maxlevel) {
        tracker.trackEvent(
                "TimeTrial",  // Category
                "GAME OVER", String.valueOf(maxlevel) + " " + String.valueOf(hasGyro), score);       // Value
    }

    public void exitTimeTrial() {
        tracker.dispatch();
        tracker.stopSession();
    }

    public void exitSurvival() {
        tracker.dispatch();
        tracker.stopSession();
    }

    public void newSurvivalGame() {
        tracker.trackEvent(
                "TimeTrial",   // Category
                "New Game", "", 0);       // Value
    }

    public void newTimeTrialGame() {
        tracker.trackEvent(
                "TimeTrial",  // Category
                "New Game", "", 0);       // Value
    }

    public void startHelpScreenActivity() {
        tracker.trackPageView("/helpScreen");
    }

    public void exitHelpScreen() {
        tracker.dispatch();
        tracker.stopSession();
    }
}
