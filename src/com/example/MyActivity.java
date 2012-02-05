package com.example;

import android.app.Activity;
import android.content.Context;
import android.hardware.*;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

import java.util.List;

public class MyActivity extends Activity

{
    SensorHandler sensorHandler;
    Vibrator vibrator;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        sensorHandler = new SensorHandler(this, sensorHandlerInterface);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    SensorHandler.SensorHandlerInterface sensorHandlerInterface = new SensorHandler.SensorHandlerInterface() {
        @Override
        public void newValues(int angularVelocity, int tilt) {
            Log.i("AMP", "angvel "+ String.valueOf(angularVelocity)+ " tilt "+String.valueOf(tilt));
            if(tilt > 500 && tilt < 600){
               vibrator.vibrate(100);
            }
        }
    };

}

