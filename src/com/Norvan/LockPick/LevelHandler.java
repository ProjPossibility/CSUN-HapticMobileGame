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
    private static final int startingDifficulty = 120;
    private int targetLocation;
    private int difficulty;
    private int keyPressPosition;
    private float buttonPressProximity;
    private boolean currentTryWinnable = false;

    public LevelHandler(int levelNumber) {
        difficulty = startingDifficulty - levelNumber * 10;
        if (difficulty < 10) {
            difficulty = 10;
        }
        Random rand = new Random();
        targetLocation = rand.nextInt(400) + 300;
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

        float intensityPercentage = (float) distanceFromTarget / difficulty;


        int intensity = (int) (100 * (1.0 - intensityPercentage));
        return intensity;


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


}
