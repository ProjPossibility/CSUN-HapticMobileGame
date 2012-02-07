package com.Norvan.LockPick;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi-dev
 * Date: 2/4/12
 * Time: 7:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class LevelHandler {
    private static final double sweetSpot = 0.25;
    private static final double curvePower = 2;
    private static final int startingDifficulty = 1200;
    private boolean exponential = false;
    private int targetLocation;
    private int difficulty;
    private int keyPressPosition;
    private float buttonPressProximity;
    private boolean currentTryWinnable = false;
    int[] levelData;
    int[][] falsePositiveData;
    int[] falsePositiveLocations;

    public LevelHandler(int levelNumber) {
        difficulty = startingDifficulty - levelNumber * 10;
        if (difficulty < 10) {
            difficulty = 10;
        }
        Random rand = new Random();
        targetLocation = rand.nextInt(4000) + 3000;
        levelData = new int[difficulty];
        for (int i = 0; i < difficulty; i++) {
            double percentage = 1.0f - (((double) i) / difficulty);
            if (exponential) {
                levelData[i] = (int) (100 * (Math.pow(percentage, curvePower)));
            } else {
                levelData[i] = (int) (100 * percentage);
            }
        }
    }


    public void keyDown(int position) {
        keyPressPosition = position;
        buttonPressProximity = ((float) Math.abs(keyPressPosition - targetLocation)) / difficulty;
        currentTryWinnable = isCurrentTryWinnable();
    }

    public int getUnlockedState(int currentPosition) {
        if (Math.abs(keyPressPosition - currentPosition) > difficulty * 2) {
            if (((float) Math.abs(keyPressPosition - targetLocation)) / difficulty < sweetSpot) {
                return 1;
            } else {
                return -1;
            }
        } else if (((float) Math.abs(keyPressPosition - targetLocation) * 2) / difficulty > 1) {
            return -1;
        }
        return 0;
    }

    private boolean isCurrentTryWinnable() {
        if ((float) (Math.abs(keyPressPosition - targetLocation)) / difficulty < sweetSpot) {
            return true;
        } else {
            return false;
        }
    }

    public float getCurrentTryResult() {
        return (float) (Math.abs(keyPressPosition - targetLocation)) / difficulty;
    }

    public int getIntensityForPosition(int tilt) {
        int distanceFromTarget = Math.abs(targetLocation - tilt);
        if (distanceFromTarget >= difficulty) {
            return -1;
        }

        return levelData[distanceFromTarget];


    }

    public int getIntensityForPositionWhileUnlocking(int tilt) {

        float diff = Math.abs(keyPressPosition - tilt);
        float numerator = buttonPressProximity * diff;
        float divided = numerator / difficulty;
        if (!currentTryWinnable) {
            divided = (float) (divided * 1.5);
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


    public int[] getLevelData() {
        //TODO remove
        return levelData;
    }

}
