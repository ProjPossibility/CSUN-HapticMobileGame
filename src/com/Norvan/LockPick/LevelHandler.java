package com.Norvan.LockPick;

import android.util.Log;

import java.util.Random;

/**
 * @author Norvan Gorgi
 *         Abstracts out all the functions relating to the game level ("playing field").
 */
public class LevelHandler {
    private static final double sweetSpot = 0.3;
    private static final double curvePower = 1.5;
    private static final int startingDifficulty = 1000;
    private static final float unlockDistanceMultiplier = 1.5f;
    private boolean exponential = false;
    private int targetLocation;
    private int difficulty;
    private int keyPressPosition;
    private float buttonPressProximity;
    private boolean currentTryWinnable = false;
    private int[] levelData;

    public static int STATE_IN_PROGRESS = 0;
    public static int STATE_UNLOCKED = 1;
    public static int STATE_FAILED = -1;


    /**
     * Creates a new level with a random target location for the given levelnumber.
     *
     * @param levelNumber the # of the current level. Used to calculate difficulty.
     */
    public LevelHandler(int levelNumber) {
        difficulty = startingDifficulty - (levelNumber / 3) * 100;
        if (difficulty < 150) {
            difficulty = 150;
        }
        Random rand = new Random();
        targetLocation = rand.nextInt(4000) + 3000;
        levelData = new int[difficulty];
        for (int i = 0; i < difficulty; i++) {
            double percentage = 1.0f - (((double) i) / difficulty);
            if (exponential) {
                levelData[i] = (int) (10 * (Math.pow(percentage + 2, curvePower)));
            } else {
                levelData[i] = (int) (100 * percentage);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i : levelData) {
            sb.append(i);
            sb.append(" ");
        }
    }


    /**
     * Like the standard constructor, but allows you to specify that the target location cannot be in the center.
     * Used for tutorial.
     *
     * @param levelNumber the # of the current level. Used to calculate difficulty.
     * @param notInCenter if true, the target wont be at 12 oclock.
     */
    public LevelHandler(int levelNumber, boolean notInCenter) {
        difficulty = startingDifficulty - (levelNumber / 3) * 100;
        if (difficulty < 150) {
            difficulty = 150;
        }
        Random rand = new Random();
        targetLocation = rand.nextInt(4000) + 3000;
        if (notInCenter) {
            while (targetLocation > 4000 && targetLocation < 6000) {
                targetLocation = rand.nextInt(4000) + 3000;
            }
        }
        levelData = new int[difficulty];
        for (int i = 0; i < difficulty; i++) {
            double percentage = 1.0f - (((double) i) / difficulty);
            if (exponential) {
                levelData[i] = (int) (10 * (Math.pow(percentage + 2, curvePower)));
            } else {
                levelData[i] = (int) (100 * percentage);
            }
        }

    }


    /**
     * Notifies the level that the user has pressed the volume button
     *
     * @param position the position at which the button was pressed.
     */
    public void keyDown(int position) {
        keyPressPosition = position;
        buttonPressProximity = ((float) Math.abs(keyPressPosition - targetLocation)) / difficulty;
        currentTryWinnable = isCurrentTryWinnable();
        Log.i("AMP", "location " + String.valueOf(((float) Math.abs(keyPressPosition - targetLocation)) / difficulty));
    }


    /**
     * Returns the state of the current picking attempt based on the current position.
     *
     * @param currentPosition the current tilt reading of the device.
     * @return 0 for in Progress, 1 for success, and -1 for fail.
     */
    public int getUnlockedState(int currentPosition) {
        if (Math.abs(keyPressPosition - currentPosition) > difficulty * unlockDistanceMultiplier) {
            if (((float) Math.abs(keyPressPosition - targetLocation)) / difficulty < sweetSpot) {
                return STATE_UNLOCKED;
            } else {
                return STATE_FAILED;
            }
        } else if (isPickBroken(currentPosition)) {
            return STATE_FAILED;
        }
        return STATE_IN_PROGRESS;
    }

    private boolean isPickBroken(int currentPosition) {
        if (keyPressPosition - currentPosition < 20) {
            return false;
        } else if (((float) Math.abs(keyPressPosition - targetLocation) * unlockDistanceMultiplier) / difficulty > 1) {
            return true;
        }
        return false;

    }

    private boolean isCurrentTryWinnable() {
        if ((float) (Math.abs(keyPressPosition - targetLocation)) / difficulty < sweetSpot) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the intensity of vibration at the given tilt position
     *
     * @param tilt the current tilt position
     * @return intensity for vibration from 0-100
     */
    public int getIntensityForPosition(int tilt) {
        int distanceFromTarget = Math.abs(targetLocation - tilt);
        if (distanceFromTarget >= difficulty) {
            return -1;
        }
        return levelData[distanceFromTarget];
    }

    /**
     * Returns whether the tilt position is in the winnable "sweet spot"
     *
     * @param tilt the current tilt position
     * @return whether the given tilt is in the sweet spot
     */
    public boolean tiltIsInSweetSpot(int tilt) {
        if ((float) (Math.abs(tilt - targetLocation)) / difficulty < sweetSpot) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the intensity of vibration at the given tilt position when the volume button is depressed and unlocking
     * is in progress.
     *
     * @param tilt the current tilt position
     * @return intensity for vibration from 0-100
     */
    public int getIntensityForPositionWhileUnlocking(int tilt) {

        float diff = Math.abs(keyPressPosition - tilt);
        float numerator = buttonPressProximity * diff;
        float divided = numerator / difficulty;
        if (!currentTryWinnable) {
            divided = (float) (divided * 2);
        }
        int intensity = (int) (divided * 100);

        if (intensity > 100) {
            return 100;
        } else if (intensity <= 0) {
            return -1;
        } else {
            return intensity;
        }
    }
}