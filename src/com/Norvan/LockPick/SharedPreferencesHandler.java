package com.Norvan.LockPick;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author Norvan Gorgi
 *         Abstracts out all the functions relating to storing and retrieving data from device storage. Some of the
 *         methods are static because a class may only need to use them once; no sense in creating a whole instance of
 *         the object for that.
 */

public class SharedPreferencesHandler {

    private SharedPreferences prefs;

    public SharedPreferencesHandler(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Sets the initial user type.
     *
     * @param userType The user type. See UserType.class for descriptions.
     * @param context  A Context object to communicate with the SharedPreferences.
     */
    public static void setUserType(Context context, int userType) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt("userType", userType);
        edit.commit();
    }

    /**
     * Gets the user type.
     *
     * @param context A Context object to communicate with the SharedPreferences.
     * @return the stored user type, or -1 if it is not set.
     */
    public static int getUserType(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt("userType", -1);
    }

    /**
     * Gets the user type.
     *
     * @return the stored user type, or -1 if it is not set.
     */
    public int getUserType() {
        return prefs.getInt("userType", -1);
    }

    /**
     * Resets the user type.
     *
     */
    public static void clearUserType(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove("userType");
        edit.commit();
    }

    /**
     * Returns whether a usertype has been set.
     *
     * @return true if it has been set, false if it has not been.
     */
    public static boolean isFirstRun(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return !prefs.contains("userType");
    }

    public void setSurvivalHighScore(int score) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt("puzzleHighScore", score);
        edit.commit();
    }

    public int getSurvivalHighScore() {
        return prefs.getInt("puzzleHighScore", 0);
    }

    public void setTimeTrialHighScore(int score) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt("timetrialHighScore", score);
        edit.commit();
    }

    public int getTimeTrialHighScore() {
        return prefs.getInt("timetrialHighScore", 0);
    }

    /**
     * Returns whether the user has completed the tutorial before.
     *
     * @return true if they have, false if they have not.
     */
    public static boolean hasDoneTutorial(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.contains("prefDoneTutorial");
    }

    /**
     * Sets that the user has completed (or decided to skip) the tutorial.
     *
     */
    public static void didTutorial(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("prefDoneTutorial", true);
        edit.commit();
    }
}
