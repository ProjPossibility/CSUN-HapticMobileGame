package com.Norvan.LockPick;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.Norvan.LockPick.Helpers.ResponseHelper;
import com.Norvan.LockPick.Helpers.VolumeToggleHelper;

public class GameActivity extends Activity

{
    LinearLayout linearChrono;
    VolumeToggleHelper volumeToggleHelper;
    VibrationHandler vibrationHandler;
    GameHandler gameHandler;
    TextView textPicksLeft, textLevelLabel, textGameOver, textHighScore;
    ImageButton imgbutToggleVolume;
    Button butGameButton;
    Chronometer chronoTimer;
    AnnouncementHandler announcementHandler;
    SharedPreferencesHandler prefs;
    Context context;
    ResponseHelper responseHelper;
    long gamePausedChronoProgress;
    GraphView graphView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gamelayout);
        context = this;
        vibrationHandler = new VibrationHandler(this);
        gameHandler = new GameHandler(this, gameStatusInterface, vibrationHandler);
        textPicksLeft = (TextView) findViewById(R.id.textPicksLeft);
        textLevelLabel = (TextView) findViewById(R.id.textLevelLabel);
        textGameOver = (TextView) findViewById(R.id.textGameOverLabel);
        textHighScore = (TextView) findViewById(R.id.textHighScore);
        linearChrono = (LinearLayout) findViewById(R.id.linearChrono);
        imgbutToggleVolume = (ImageButton) findViewById(R.id.imgbutGameVolume);
        imgbutToggleVolume.setOnClickListener(onClick);
        butGameButton = (Button) findViewById(R.id.butGameButton);
        butGameButton.setOnClickListener(onClick);
        chronoTimer = (Chronometer) findViewById(R.id.chronoTimer);
        volumeToggleHelper = new VolumeToggleHelper(this, imgbutToggleVolume);

        chronoTimer.setKeepScreenOn(true);
        prefs = new SharedPreferencesHandler(this);
        setHighScore(prefs.getHighScore() + 1);
        setUiGameState(GameHandler.STATE_FRESHLOAD);
        announcementHandler = new AnnouncementHandler(context, vibrationHandler);

        chronoTimer.setOnChronometerTickListener(onTick);
        responseHelper = new ResponseHelper(context);


    }

    Chronometer.OnChronometerTickListener onTick = new Chronometer.OnChronometerTickListener() {
        @Override
        public void onChronometerTick(Chronometer chronometer) {
            long timeElapsed = getCurrentTime() - chronometer.getBase();
            Log.i("AMP", "modulo " + String.valueOf(timeElapsed % 10000));
            if (timeElapsed > 10000) {
                if (timeElapsed % 10000 < 1000) {
                    announcementHandler.userTakingTooLong();
                }
            }
        }
    };




    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (!event.isLongPress() && event.getRepeatCount() == 0) {
                if (gameHandler.getGameState() == GameHandler.STATE_INGAME) {
                    gameHandler.gotKeyDown();
                } else {
                    gameHandler.playCurrentLevel();
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            gameHandler.gotKeyUp();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (butGameButton.equals(view)) {
                gameHandler.playCurrentLevel();
            } else if (imgbutToggleVolume.equals(view)) {
                volumeToggleHelper.toggleMute();
            }
        }
    };


    public GameHandler.GameStatusInterface gameStatusInterface = new GameHandler.GameStatusInterface() {
        @Override
        public void newGameStart() {
            setUiGameState(GameHandler.STATE_FRESHLOAD);
            setHighScore(prefs.getHighScore() + 1);
        }

        @Override
        public void levelStart(int level, int picksLeft) {
            setUiGameState(GameHandler.STATE_INGAME);
            chronoTimer.setBase(SystemClock.elapsedRealtime());
            chronoTimer.start();
            setPicksLeft(picksLeft);
            setLevelLabel(level);
            announcementHandler.levelStart(level, picksLeft);
//            boolean needsToAdd = false;
//            if (graphView == null) {
//                needsToAdd = true;
//            }
//            graphView = new GraphView(context, gameHandler.getLevelData(), GraphView.LINE);
//            if (needsToAdd) {
//                ((LinearLayout) findViewById(R.id.linearMain)).addView(graphView);
//            }
//            graphView.setVisibility(View.VISIBLE);

        }

        @Override
        public void levelWon(int levelWon, int picksLeft) {
            setUiGameState(GameHandler.STATE_BETWEENLEVELS);
            butGameButton.setText("Next Level");
            chronoTimer.stop();
            float levelTime = SystemClock.elapsedRealtime() - chronoTimer.getBase();

            setPicksLeft(picksLeft);
            announcementHandler.levelWon(levelTime, levelWon);

        }

        @Override
        public void levelLost(int level, int picksLeft) {
            setUiGameState(GameHandler.STATE_BETWEENLEVELS);

            butGameButton.setText("Try Again");
            chronoTimer.stop();

            setPicksLeft(picksLeft);
            announcementHandler.levelLost(level, picksLeft);
        }

        @Override
        public void gameOver(int maxLevel) {
            setUiGameState(GameHandler.STATE_GAMEOVER);
            Log.i("AMP", "gameOver");
            butGameButton.setText("New Game");
            if (prefs.getHighScore() < maxLevel) {
                textGameOver.setText("GAME OVER\nScore: " + String.valueOf(maxLevel) + "\nNEW RECORD!");
                prefs.setHighScore(maxLevel);
                setHighScore(maxLevel + 1);
            } else {
                textGameOver.setText("GAME OVER\nScore: " + String.valueOf(maxLevel + 1) + "\nRecord: " + String.valueOf(prefs.getHighScore()));
            }


            chronoTimer.stop();


            announcementHandler.gameOver(maxLevel);

        }
    };


    void setPicksLeft(int picks) {
        textPicksLeft.setText("Picks Left: " + String.valueOf(picks));
    }

    void setLevelLabel(int level) {
        textLevelLabel.setText("Level " + String.valueOf(level + 1));
    }

    void setHighScore(int highScore) {
        textHighScore.setText("High Score: " + String.valueOf(highScore));
    }


    @Override
    protected void onResume() {
        if (gameHandler.getGameState() == GameHandler.STATE_INGAME) {
            gameHandler.setSensorPollingState(true);
            chronoTimer.setBase(getCurrentTime() - gamePausedChronoProgress);
            chronoTimer.start();
        }
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void onPause() {
        gameHandler.setSensorPollingState(false);
        if (gameHandler.getGameState() == GameHandler.STATE_INGAME) {
            gamePausedChronoProgress = getCurrentTime() - chronoTimer.getBase();
            chronoTimer.stop();
        }
        vibrationHandler.stopVibrate();
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void onDestroy() {
        announcementHandler.shutDown();
        vibrationHandler.stopVibrate();
        announcementHandler = null;
        vibrationHandler = null;
        gameHandler = null;
        prefs = null;

        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
    }

    private void setUiGameState(int gameState) {
        switch (gameState) {
            case GameHandler.STATE_FRESHLOAD: {
                butGameButton.setVisibility(View.VISIBLE);
                textGameOver.setVisibility(View.GONE);
                linearChrono.setVisibility(View.GONE);
                textPicksLeft.setVisibility(View.GONE);
                textHighScore.setVisibility(View.GONE);
                textLevelLabel.setVisibility(View.VISIBLE);
                setLevelLabel(0);
            }
            break;
            case GameHandler.STATE_INGAME: {
                textLevelLabel.setVisibility(View.VISIBLE);
                butGameButton.setVisibility(View.GONE);
                textGameOver.setVisibility(View.GONE);
                linearChrono.setVisibility(View.VISIBLE);
                textPicksLeft.setVisibility(View.VISIBLE);
                textHighScore.setVisibility(View.VISIBLE);

            }
            break;
            case GameHandler.STATE_BETWEENLEVELS: {
                textLevelLabel.setVisibility(View.VISIBLE);
                butGameButton.setVisibility(View.VISIBLE);
                textGameOver.setVisibility(View.GONE);
                linearChrono.setVisibility(View.GONE);
                textPicksLeft.setVisibility(View.GONE);
                textHighScore.setVisibility(View.GONE);
            }
            break;
            case GameHandler.STATE_GAMEOVER: {
                textLevelLabel.setVisibility(View.GONE);
                butGameButton.setVisibility(View.VISIBLE);
                textGameOver.setVisibility(View.VISIBLE);
                linearChrono.setVisibility(View.GONE);
                textPicksLeft.setVisibility(View.GONE);
                textHighScore.setVisibility(View.GONE);
            }
            break;
        }
    }

    private long getCurrentTime() {
        return SystemClock.elapsedRealtime();
    }
}

