package com.example;

import android.app.Activity;
import android.content.Context;
import android.hardware.*;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class MyActivity extends Activity 
    
{
    SensorHandler sensorHandler;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        sensorHandler = new SensorHandler(this);
     }

    

   
}

