package com.example;

import android.app.Activity;
import android.content.Context;
import android.hardware.*;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public class MyActivity extends Activity

{
    VibrationHandler vibrationHandler;
    SensorHandler sensorHandler;
    LevelHandler levelHandler;
    TextView textSeed, textButtonState, textDifficulty, textPressedLocation;
    EditText editDifficulty;
    Button butNewLevel;
    int lastPressedPosition = -1;
    boolean keyPressed = false;
    int keyPressedPosition = -1;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        vibrationHandler = new VibrationHandler(this);
        levelHandler = new LevelHandler(vibrationHandler);
        sensorHandler = new SensorHandler(this, sensorHandlerInterface);
        levelHandler.newLevel();
        textSeed = (TextView) findViewById(R.id.textSeed);
        textButtonState = (TextView) findViewById(R.id.textButtonState);
        textDifficulty = (TextView) findViewById(R.id.textDifficulty);
        textPressedLocation = (TextView) findViewById(R.id.textPressedLoc);
        butNewLevel = (Button) findViewById(R.id.butNewLevel);
        butNewLevel.setOnClickListener(onClick);
        editDifficulty = (EditText) findViewById(R.id.editTextDifficulty);
        textSeed.setText("Seed: " + String.valueOf(levelHandler.getStartSeed()));
        textDifficulty.setText("Difficulty: " + String.valueOf(levelHandler.getDifficulty()));
    }

    View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(butNewLevel.equals(view)) {
                try {
                    levelHandler.newLevel(Integer.valueOf(String.valueOf(editDifficulty.getText())));
                } catch (Exception e) {
                    levelHandler.newLevel();
                }
                textSeed.setText("Seed: " + String.valueOf(levelHandler.getStartSeed()));
                textDifficulty.setText("Difficulty: " + String.valueOf(levelHandler.getDifficulty()));

            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            keyPressed = true;
            keyPressedPosition = lastPressedPosition;
            textPressedLocation.setText("PressedLoc: " + String.valueOf(keyPressedPosition));
            textButtonState.setText("Button Pressed: TRUE");
            vibrationHandler.stopVibrate();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            keyPressed = false;
            textButtonState.setText("Button Pressed: FALSE");

            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    SensorHandler.SensorHandlerInterface sensorHandlerInterface = new SensorHandler.SensorHandlerInterface() {
        @Override
        public void newValues(float angularVelocity, int tilt) {
            if (keyPressed) {

            } else {
                lastPressedPosition = tilt;
                int intensity = levelHandler.getIntensityForPosition(tilt);
                if ((angularVelocity * 100) > 10) {
                    if (intensity == -1) {
                        vibrationHandler.stopVibrate();
                    } else {
                        vibrationHandler.pulsePWM(intensity);
                    }
                } else {
                    vibrationHandler.stopVibrate();
                }
            }


        }

        @Override
        public void showAngularVelocity(float[] values) {

        }
    };


}

