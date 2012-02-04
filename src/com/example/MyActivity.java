package com.example;

import android.app.Activity;
import android.content.Context;
import android.hardware.*;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class MyActivity extends Activity 
    
{
    Sensor sensor, sensor2;
    Context context;
    SensorManager sensorManager;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        context = this;


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(
                Sensor.TYPE_ORIENTATION);
        
        sensor = sensors.get(0);

        List<Sensor> sensors2 = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        sensor2 = sensors2.get(0);
        boolean result = sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME);

        sensorManager.registerListener(sensorEventListener, sensor2, SensorManager.SENSOR_DELAY_GAME);
        Log.i("SENSORS", String.valueOf(result));
     }

    
    
    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {


                Log.i("SENSORS", String.valueOf((int)sensorEvent.values[2]));


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }     ;
   
}

