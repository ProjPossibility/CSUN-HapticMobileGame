package com.example;

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

    int startSeed;
    int difficulty;
    int keyPressPosition;
    float buttonPressProximity;
    public LevelHandler(int levelNumber){
        difficulty = 100-levelNumber*10;
        Random rand = new Random();
        startSeed = rand.nextInt(600) + 200;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getStartSeed() {
        return startSeed;
    }

    public void keyDown(int position) {
        keyPressPosition = position;
        buttonPressProximity = ((float) Math.abs(keyPressPosition - startSeed)) / difficulty;
    }

    public int getUnlockedState(int currentPosition) {
        Log.i("AMP", String.valueOf(((float) Math.abs(keyPressPosition - startSeed)) / difficulty));
        if (Math.abs(keyPressPosition - currentPosition) > difficulty * 2) {
            if (((float) Math.abs(keyPressPosition - startSeed)) / difficulty < 0.25) {
                return 1;
            } else {
                return -1;
            }
        } else if (((float) Math.abs(keyPressPosition - startSeed) * 2) / difficulty > 1) {
            return -1;
        }
        return 0;
    }

    public boolean isCurrentTryWinnable() {
        if ((float) (Math.abs(keyPressPosition - startSeed)) / difficulty < 0.25) {
            return true;
        } else {
            return false;
        }
    }

    public float getCurrentTryResult() {
        return (float) (Math.abs(keyPressPosition - startSeed)) / difficulty;
    }

    public int getIntensityForPosition(int tilt) {


        if (tilt > startSeed - difficulty && tilt < startSeed + difficulty) {
            int intensity = 100 - (100 * Math.abs(startSeed - tilt) / difficulty);
            return intensity;
        } else {
            return -1;
        }

    }

    public int getIntensityForPositionWhileUnlocking(int tilt) {
        float diff = Math.abs(keyPressPosition-tilt);
        float numerator = buttonPressProximity* diff;
        float divided = numerator/difficulty;

        int intensity =  (int)(divided*100);
        Log.i("AMP", "tilt "+ String.valueOf(tilt));
        Log.i("AMP", "intensity "+ String.valueOf(intensity));
        if (intensity > 100) {
            return 100;
        } else if (intensity <= 0) {
            return -1;
        } else {
            return intensity;
        }
    }


}
