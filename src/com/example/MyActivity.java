package com.example;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public class MyActivity extends Activity

{
    VibrationHandler vibrationHandler;
    GameHandler gameHandler;
    TextView textPicksLeft, textLevel, textGameOver;
    Button butNextLevel;
    Chronometer chronoTimer;
    AnnouncementHandler announcementHandler;
    boolean keyPressed = false;
     Context context;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        context = this;
//        if (!hasRequiredSensors(this)) {
//
//            showUnsuportedDialog();
//            return;
//        }
        vibrationHandler = new VibrationHandler(this);
        gameHandler = new GameHandler(this, gameStatusInterface, vibrationHandler);
        textPicksLeft = (TextView) findViewById(R.id.textPicksLeft);
        textLevel = (TextView) findViewById(R.id.textLevelNumber);
        textGameOver = (TextView) findViewById(R.id.textGameOver);
        butNextLevel = (Button) findViewById(R.id.butNextLevel);
        butNextLevel.setOnClickListener(onClick);
        chronoTimer = (Chronometer) findViewById(R.id.chronoTimer);
        if (SharedPreferencesHandler.isFirstRun(this)) {
            Intent i = new Intent(this, FirstRunActivity.class);
            startActivityForResult(i, 1);
        } else {
            announcementHandler = new AnnouncementHandler(this, vibrationHandler);
            announcementHandler.newLaunch();
        }

    }
    boolean pressedOutsideGame = true;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (gameHandler.getGameState() == GameHandler.STATE_INGAME) {
                if (!keyPressed) {
                    keyPressed = true;
                    gameHandler.gotKeyDown();
                }

            }   return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            keyPressed = false;
            gameHandler.gotKeyUp();
            if (gameHandler.getGameState() != GameHandler.STATE_INGAME) {
                if(pressedOutsideGame){
                    pressedOutsideGame = false;
                    gameHandler.playCurrentLevel();
                }                else{
                    pressedOutsideGame = true;
                }
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            gameHandler.playCurrentLevel();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (SharedPreferencesHandler.isFirstRun(this)) {
                Intent i = new Intent(this, FirstRunActivity.class);
                startActivityForResult(i, 1);
            } else {
                announcementHandler = new AnnouncementHandler(this, vibrationHandler);
                announcementHandler.newLaunch();

            }
        }
        super.onActivityResult(requestCode, resultCode, data);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public GameHandler.GameStatusInterface gameStatusInterface = new GameHandler.GameStatusInterface() {
        @Override
        public void levelStart(int level, int picksLeft) {
            butNextLevel.setVisibility(View.GONE);
            textGameOver.setVisibility(View.GONE);

            chronoTimer.setBase(SystemClock.elapsedRealtime());
            chronoTimer.start();
            setPicksLeft(picksLeft);
            textLevel.setText("Level " + String.valueOf(level + 1));
            announcementHandler.levelStart(level, picksLeft);

        }

        @Override
        public void levelWon(int levelWon, int picksLeft) {
            butNextLevel.setText("Next Level");
            chronoTimer.stop();
            float levelTime = SystemClock.elapsedRealtime() - chronoTimer.getBase();
            butNextLevel.setVisibility(View.VISIBLE);
            setPicksLeft(picksLeft);
            announcementHandler.levelWon(levelTime, levelWon);

        }

        @Override
        public void levelLost(int level, int picksLeft) {
            butNextLevel.setText("Try Again");
            chronoTimer.stop();
            butNextLevel.setVisibility(View.VISIBLE);
            setPicksLeft(picksLeft);
            announcementHandler.levelLost(level,picksLeft);
        }

        @Override
        public void gameOver(int maxLevel) {
            butNextLevel.setText("New Game");
            if (SharedPreferencesHandler.getHighScore(context) < maxLevel) {
                textGameOver.setText("GAME OVER\nHIGH SCORE!");
            }
            else
            {
                textGameOver.setText("GAME OVER");
            }
            textGameOver.setVisibility(View.VISIBLE);

            chronoTimer.stop();
            butNextLevel.setVisibility(View.VISIBLE);
            announcementHandler.gameOver(maxLevel);

        }
    };

    private void showUnsuportedDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Error!");
        adb.setMessage("Your device does not have a gyroscope. Gyroscope substitution with the accelerometer has not been implemented yet");
        adb.setCancelable(false);
        adb.setPositiveButton("Quit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        adb.create().show();
    }


    void setPicksLeft(int picks) {
        textPicksLeft.setText("Picks Left: " + String.valueOf(picks));
    }


    @Override
    protected void onResume() {
        keyPressed = false;
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void onPause() {
        gameHandler.setSensorPollingState(false);
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void onDestroy() {
        announcementHandler.shutDown();
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public static boolean hasRequiredSensors(Context context) {
        try {
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            List<Sensor> sensors = sensorManager.getSensorList(
                    Sensor.TYPE_GYROSCOPE);

            Sensor sensor = sensors.get(0);
            Log.i("AMP", "hasGyro");
            return true;
        } catch (Exception e) {
            Log.i("AMP", "noGryo");
            return false;
        }
    }
}

