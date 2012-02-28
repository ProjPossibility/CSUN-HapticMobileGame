package com.Norvan.LockPick;



/**
 * @author Norvan Gorgi
 *         Abstracts out all the functions relating to a game's score and high score.
 */
public class ScoreHandler {
    private SharedPreferencesHandler sharedPreferencesHandler;
    public static final int MODE_SURVIVAL = 0;
    public static final int MODE_TIMETRIAL = 1;
    private int score, gameMode;


    /**
     * @param sharedPreferencesHandler SharedPreferencesHandler to get and set high scores
     * @param gameMode                 Game mode, either MODE_SURVIVAL or MODE_TIMETRIAL
     */
    public ScoreHandler(SharedPreferencesHandler sharedPreferencesHandler, int gameMode) {
        this.sharedPreferencesHandler = sharedPreferencesHandler;
        this.gameMode = gameMode;
        score = 0;
    }

    public void newGame() {
        score = 0;
    }

    /**
     * Adds the points from winning a level to the current score. Also calculates in the score bonus based on the
     * level time.
     *
     * @param time The time, in ms, that it took to beat the level. Used to calculate the score bonus.
     * @return the score bonus.
     */
    public int wonLevel(float time) {
        score = score + 100;
        if (time <= 10000) {
            int bonus = (int) (50 * ((10000 - time) / 10000));
            score = score + bonus;
            return bonus;
        }
        return 0;
    }


    public int getCurrentScore() {
        return score;
    }

    /**
     * Called when the game is over. Calculates whether the current score is the new High Score and returns that.
     *
     * @return Whether the game broke the high score.
     */
    public boolean gameOver() {
        int highScore = 0;
        if (gameMode == MODE_SURVIVAL) {
            highScore = sharedPreferencesHandler.getSurvivalHighScore();
        } else if (gameMode == MODE_TIMETRIAL) {

            highScore = sharedPreferencesHandler.getTimeTrialHighScore();
        }
        if (score > highScore) {
            if (gameMode == MODE_SURVIVAL) {
                sharedPreferencesHandler.setSurvivalHighScore(score);
            } else if (gameMode == MODE_TIMETRIAL) {
                sharedPreferencesHandler.setTimeTrialHighScore(score);
            }
            return true;
        }
        return false;
    }

    public int getHighScore() {
        if (gameMode == MODE_SURVIVAL) {
            return sharedPreferencesHandler.getSurvivalHighScore();
        } else if (gameMode == MODE_TIMETRIAL) {
            return sharedPreferencesHandler.getTimeTrialHighScore();
        }
        return -1;
    }

}
