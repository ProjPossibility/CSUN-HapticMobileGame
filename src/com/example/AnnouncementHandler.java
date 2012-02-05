package com.example;

import android.content.Context;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi
 * Date: 2/5/12
 * Time: 2:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class AnnouncementHandler {
    int userType;
    VibrationHandler vibrationHandler;
    TTSHandler tts;

    public AnnouncementHandler(Context context, VibrationHandler vibrationHandler) {
        userType = SharedPreferencesHandler.getUserType(context);
        this.vibrationHandler = vibrationHandler;
        tts = new TTSHandler(context);
    }

    public void shutDown() {
        tts.shutDownTTS();
    }

    public void cancelAnnouncement() {
        tts.shutUp();
        vibrationHandler.stopVibrate();

    }

    public void newLaunch() {
        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            tts.speakPhrase("Turn the phone on its side with the screen facing left. Press the volume button to begin.");
        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
        }

    }

    public void levelStart(int level, int picksLeft) {
        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            tts.speakPhrase("Level " + String.valueOf(level + 1) + ", " + String.valueOf(picksLeft) + " picks left.");
        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
        }
    }

    public void levelWon(float time, int newLevel) {
        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            tts.speakPhrase("You beat level "+String.valueOf(newLevel)+" in "+ getTimeString(time)+". Press volume to continue.");
        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
        }
    }

    public void levelLost(int level, int picksLeft) {
        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            tts.speakPhrase("You lost. Press volume to try again");
        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
        }
    }

    public void gameOver(int maxLevel) {
        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            tts.speakPhrase("Game Over. Press volume for a new game.");
        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
        }
        
    }
    
    private String getTimeString(float time){
        return null;
    }
}
