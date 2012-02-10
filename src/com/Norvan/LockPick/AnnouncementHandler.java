package com.Norvan.LockPick;

import android.content.Context;
import com.Norvan.LockPick.Helpers.ResponseHelper;

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
    Context context;
    ResponseHelper responseHelper;

    public AnnouncementHandler(Context context, VibrationHandler vibrationHandler) {
        userType = SharedPreferencesHandler.getUserType(context);
        this.vibrationHandler = vibrationHandler;
        this.context = context;
        tts = new TTSHandler(context);
        responseHelper = new ResponseHelper(context);
    }

    public void shutDown() {
        tts.shutDownTTS();
    }

    public void cancelAnnouncement() {
        tts.shutUp();
        vibrationHandler.stopVibrate();

    }

    public void mainActivityLaunch() {
        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            tts.speakPhrase(context.getResources().getString(R.string.mainactivityBlind));
        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
            vibrationHandler.stopVibrate();
            vibrationHandler.playString(context.getResources().getString(R.string.mainactivityDeafBlind));
        }
    }

    public void playDeafBlindInstructions() {
        vibrationHandler.playString(context.getResources().getString(R.string.instructionsDeafBlind));
    }

    public void newLaunch() {
        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            tts.speakPhrase("Press either volume button to begin.");
        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
            vibrationHandler.stopVibrate();
            vibrationHandler.playString("press vol to begin");
        }

    }

    public void levelStart(int level, int picksLeft) {
        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            tts.speakPhrase("Level " + String.valueOf(level + 1) + ", " + String.valueOf(picksLeft) + " picks left.");
        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
            vibrationHandler.playString("lvl " + String.valueOf(level + 1));
        } else {
            tts.speakPhrase("Level " + String.valueOf(level + 1));
        }
    }

    public void levelWon(float time, int wonLevel) {
        vibrationHandler.playHappy();
        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            if (wonLevel > 8) {
                tts.speakPhrase(responseHelper.getLevelWin10plus() + " Press either volume key to continue.");
            } else {
                tts.speakPhrase(responseHelper.getLevelWin0to10() + " Press either volume key to continue.");
            }
        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
            vibrationHandler.playString("won lvl " + String.valueOf(wonLevel + 1));
        } else {
            if (wonLevel > 8) {
                tts.speakPhrase(responseHelper.getLevelWin10plus());
            } else {
                tts.speakPhrase(responseHelper.getLevelWin0to10());
            }
        }
    }

    public void levelLost(int level, int picksLeft) {
        vibrationHandler.playSad();

        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            if (level > 3) {
                tts.speakPhrase(responseHelper.getLevelLose5plus() + " Press either volume key to try again.");
            } else {
                tts.speakPhrase("You lost. Press volume to try again");
            }
        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
            vibrationHandler.playString("lvl lost " + String.valueOf(picksLeft) + " lives");
        } else {
            if (level > 3) {
                tts.speakPhrase(responseHelper.getLevelLose5plus());
            } else {
                tts.speakPhrase("You lost.");
            }
        }
    }

    public void gameOver(int maxLevel) {
        vibrationHandler.playSad();

        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            if (new SharedPreferencesHandler(context).getHighScore() < maxLevel) {
                tts.speakPhrase("Game Over. Your reached level " + String.valueOf((maxLevel + 1)) + ". A new high score! Press volume for a new game.");
            } else {
                tts.speakPhrase("Game Over. Your reached level " + String.valueOf((maxLevel + 1)) + ". Press volume for a new game.");
            }

        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
            vibrationHandler.playString("Game over lvl " + String.valueOf(maxLevel + 1));
        } else {
            tts.speakPhrase("Game Over");
        }

    }

    public void userTakingTooLong() {
        if (userType != SharedPreferencesHandler.USER_DEAFBLIND) {
            tts.speakPhrase(responseHelper.getTakingTooLong());
        }
    }

    private String getTimeString(float time) {
        int seconds = (int) time / 1000;
        return String.valueOf(seconds) + " seconds";

    }
}
