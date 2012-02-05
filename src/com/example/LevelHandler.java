package com.example;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi-dev
 * Date: 2/4/12
 * Time: 7:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class LevelHandler {
    VibrationHandler vibrationHandler;
    int startSeed;
    int difficulty;
    int keyPressPosition;

    public LevelHandler(VibrationHandler vibrationHandler) {
        this.vibrationHandler = vibrationHandler;


    }

    public void newLevel() {
        Random rand = new Random();
        startSeed = rand.nextInt(600) + 200;
        difficulty = 100;
    }

    public void newLevel(int difficulty) {
        Random rand = new Random();
        startSeed = rand.nextInt(600) + 200;
        this.difficulty = difficulty;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getStartSeed() {
        return startSeed;
    }

    public void keyDown(int position) {
        keyPressPosition = position;
    }

    public int getUnlockedState(int currentPosition) {
        if (Math.abs(keyPressPosition - currentPosition) > difficulty) {
            if (Math.abs(keyPressPosition - startSeed) / difficulty < 0.25) {
                return 1;
            } else {
                return -1;
            }
        }
        return 0;
    }

    public int getIntensityForPosition(int tilt) {


        if (tilt > startSeed - difficulty && tilt < startSeed + difficulty) {
            int intensity = 100 - (100 * Math.abs(startSeed - tilt) / difficulty);
            return intensity;
        } else {
            return -1;
        }

    }


}
