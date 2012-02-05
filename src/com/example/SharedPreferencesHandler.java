package com.example;

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
    
    public static int getUserType(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt("userType", 0);
    }

    public static boolean isFirstRun(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return !prefs.contains("userType");
    }
}
