package com.Norvan.LockPick;

import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi-dev
 * Date: 2/15/12
 * Time: 6:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScoreHandler {
    SharedPreferencesHandler sharedPreferencesHandler;
    public static final int MODE_SURVIVAL = 0;
    public static final int MODE_TIMETRIAL = 1;

    int score, gameMode;

    public ScoreHandler(SharedPreferencesHandler sharedPreferencesHandler, int gameMode) {
        this.sharedPreferencesHandler = sharedPreferencesHandler;
        this.gameMode = gameMode;
        score = 0;
    }

    public void newGame() {
        score = 0;
    }

    public int wonLevel(float time) {
        score = score + 100;
        if (time <= 10000) {
            int bonus = (int) (50  * ((10000-time) / 10000));
            score = score + bonus;
        }
        return 0;
    }

    public int wonLevel() {
        score = score + 100;
        return score;
    }

    public int getCurrentScore() {
        return score;
    }


    public boolean gameOver() {
        int highScore;
        if (gameMode == MODE_SURVIVAL) {
            highScore = sharedPreferencesHandler.getSurvivalHighScore();
        } else {
            highScore = sharedPreferencesHandler.getTimeTrialHighScore();
        }
        if (score > highScore) {
            if (gameMode == MODE_SURVIVAL) {
                sharedPreferencesHandler.setSurvivalHighScore(score);
            } else {
                sharedPreferencesHandler.setTimeTrialHighScore(score);
            }
            return true;
        }
        return false;
    }

    public int getHighScore() {
        if (gameMode == MODE_SURVIVAL) {
            return sharedPreferencesHandler.getSurvivalHighScore();
        } else {
            return sharedPreferencesHandler.getTimeTrialHighScore();
        }
    }

}
