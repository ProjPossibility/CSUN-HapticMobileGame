package com.Norvan.LockPick;

import android.util.Log;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi-dev
 * Date: 2/4/12
 * Time: 7:20 PM
 * To change this template use File | Settings | File Templates.
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
    int[] levelData;


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
                levelData[i] = (int) (10 * (Math.pow(percentage+2, curvePower)));
            } else {
                levelData[i] = (int) (100 * percentage);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i : levelData) {
            sb.append(i);
            sb.append(" ");
        }
        Log.i("AMP", "level "+ String.valueOf(sb.toString()));
    }


    public void keyDown(int position) {
        keyPressPosition = position;
        buttonPressProximity = ((float) Math.abs(keyPressPosition - targetLocation)) / difficulty;
        currentTryWinnable = isCurrentTryWinnable();
        Log.i("AMP", "location "+ String.valueOf(((float) Math.abs(keyPressPosition - targetLocation)) / difficulty));
    }

    public int getUnlockedState(int currentPosition) {
        if (Math.abs(keyPressPosition - currentPosition) > difficulty * unlockDistanceMultiplier) {
            if (((float) Math.abs(keyPressPosition - targetLocation)) / difficulty < sweetSpot) {
                return 1;
            } else {
                return -1;
            }
        } else if (isPickBroken(currentPosition)) {
            return -1;
        }
        return 0;
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

    public float getCurrentTryResult() {
        return (float) (Math.abs(keyPressPosition - targetLocation)) / difficulty;
    }

    public int getIntensityForPosition(int tilt) {
        int distanceFromTarget = Math.abs(targetLocation - tilt);


//        int distanceFromTarget = tilt-targetLocation;
//        if (distanceFromTarget < 0) {
//            return -1;
//        }

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


    public int[] getLevelData() {
        //TODO remove
        return levelData;
    }

    public int getTargetLocation() {
        return targetLocation;
    }
}
