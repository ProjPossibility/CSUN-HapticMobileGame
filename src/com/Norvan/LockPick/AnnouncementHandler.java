package com.Norvan.LockPick;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
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
//        tts.shutDownTTS(false);
    }


    public void masterShutDown() {
        tts.shutDownTTS();
    }

    public void shutUp() {
        tts.shutUp();
        vibrationHandler.stopVibrate();
    }


    public void mainActivityLaunch() {
        userType = SharedPreferencesHandler.getUserType(context);
        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            tts.speakPhrase(context.getResources().getString(R.string.mainactivityBlind));
        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
            vibrationHandler.stopVibrate();
            vibrationHandler.playString(context.getResources().getString(R.string.mainactivityDeafBlind));
        }
    }

    public void playPuzzleDescription() {
        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            tts.speakPhrase(context.getResources().getString(R.string.puzzleBlind));
        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
            vibrationHandler.stopVibrate();
            vibrationHandler.playString(context.getResources().getString(R.string.puzzleDeafBlind));
        }
    }

    public void playTimeAttackDescription() {
        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            tts.speakPhrase(context.getResources().getString(R.string.timeattackBlind));
        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
            vibrationHandler.stopVibrate();
            vibrationHandler.playString(context.getResources().getString(R.string.timeattackDeafBlind));
        }
    }


    public void puzzlelevelStart(int level, int picksLeft) {
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


    public void puzzlelevelWon(int wonLevel) {

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

    public void puzzlelevelLost(int level, int picksLeft) {


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

    public void gameOver(int score, boolean isHighScore) {

        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts
            if (isHighScore) {
                tts.speakPhrase("Game Over. Your scored " + String.valueOf((score)) + " points. A new high score! Press volume for a new game.");
            } else {
                tts.speakPhrase("Game Over. Your scored " + String.valueOf((score)) + " points. Press volume for a new game.");
            }

        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
            if (isHighScore) {
                vibrationHandler.playString("Game over high score " + String.valueOf(score) + " points");

            } else {
                vibrationHandler.playString("Game over score " + String.valueOf(score) + " points");
            }
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

    public void announceTimeLeft(int secondsLeft) {
        if (secondsLeft <= 60 && secondsLeft > 25) {
            if (secondsLeft % 10 == 0) { //Every 10 seconds between 60 and 25 seconds
                tts.speakFast(String.valueOf(secondsLeft) + " seconds remaining");
            }
        } else if (secondsLeft <= 25 && secondsLeft > 10) {
            if (secondsLeft % 5 == 0) { //Every 5 seconds between 25 and 10 seconds 
                tts.speakPhrase(String.valueOf(secondsLeft) + " seconds");
            }
        } else if (secondsLeft <= 10 && secondsLeft > 1) {
            tts.speakPhrase(String.valueOf(secondsLeft));
        }
    }


    public void confirmBackButton() {
        if (userType == SharedPreferencesHandler.USER_BLIND) {
            tts.speakPhrase("Press back again to exit");
        }
        Toast.makeText(context, "Press back again to quit", Toast.LENGTH_SHORT).show();

    }

    public void speakQuadrant(int quadrant) {
        if (userType == SharedPreferencesHandler.USER_BLIND) {
            //tts

            switch (quadrant) {
                case 3:
                    tts.speakPhrase("Game Instructions. Long press to select.");
                    break;
                case 2:
                    tts.speakPhrase("Puzzle Mode. Long press to select.");
                    break;
                case 4:
                    tts.speakPhrase("Reset User Type. Long press to select.");
                    break;
                case 1:
                    tts.speakPhrase("Time Attack Mode. Long press to select.");
                    break;
            }
        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            //morse
            vibrationHandler.stopVibrate();
            switch (quadrant) {
                case 3:
                    vibrationHandler.playString("Game Instructions. Long press to select.");
                    break;
                case 2:
                    vibrationHandler.playString("Puzzle Mode. Long press to select.");
                    break;
                case 4:
                    vibrationHandler.playString("Reset User Type. Long press to select.");
                    break;
                case 1:
                    vibrationHandler.playString("Time Attack Mode. Long press to select.");
                    break;
            }
        }
    }
}
