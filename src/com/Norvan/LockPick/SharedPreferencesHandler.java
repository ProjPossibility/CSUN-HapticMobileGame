package com.Norvan.LockPick;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi
 * Date: 2/5/12
 * Time: 1:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class SharedPreferencesHandler {
    final public static int USER_NORMAL = 0;
    final public static int USER_BLIND = 1;
    final public static int USER_DEAFBLIND = 2;

    public static void setUserType(Context context, int userType) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt("userType", userType);
        edit.commit();
    }

    public static int getUserType(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt("userType", 0);
    }

    public static void clearUserType(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove("userType");
        edit.commit();
    }

    public static boolean isFirstRun(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return !prefs.contains("userType");
    }

    public static void setHighScore(Context context, int score) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt("puzzleHighScore", score);
        edit.commit();
    }

    public static int getHighScore(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt("puzzleHighScore", 0);
    }
}
