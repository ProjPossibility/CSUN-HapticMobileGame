package com.Norvan.LockPick.TimeTrialMode;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import com.Norvan.LockPick.*;
import com.Norvan.LockPick.Helpers.ResponseHelper;
import com.Norvan.LockPick.Helpers.VolumeToggleHelper;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi-dev
 * Date: 2/14/12
 * Time: 3:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimeTrialGameActivity extends Activity {
    VolumeToggleHelper volumeToggleHelper;
    VibrationHandler vibrationHandler;
    TimeTrialGameHandler gameHandler;
    TextView textLevelLabel, textGameOver, textHighScore, textTime, textLevelResult;
    ImageButton imgbutToggleVolume, imgbutTogglePause;
    Button butGameButton;
    Chronometer chronoTimer;
    AnnouncementHandler announcementHandler;
    SharedPreferencesHandler prefs;
    Context context;
    ResponseHelper responseHelper;
    TimingHandler timingHandler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetrialgamelayout);
        context = this;
        vibrationHandler = new VibrationHandler(this);
        textLevelLabel = (TextView) findViewById(R.id.textLevelLabel);
        textTime = (TextView) findViewById(R.id.textTime);
        textGameOver = (TextView) findViewById(R.id.textGameOverLabel);
        textLevelResult = (TextView) findViewById(R.id.textLevelResult);
        textHighScore = (TextView) findViewById(R.id.textHighScore);
        imgbutToggleVolume = (ImageButton) findViewById(R.id.imgbutGameVolume);
        imgbutToggleVolume.setOnClickListener(onClick);
        imgbutTogglePause = (ImageButton) findViewById(R.id.imgbutTogglePause);
        imgbutTogglePause.setOnClickListener(onClick);
        butGameButton = (Button) findViewById(R.id.butGameButton);
        butGameButton.setOnClickListener(onClick);
        chronoTimer = (Chronometer) findViewById(R.id.chronoTimer);
        volumeToggleHelper = new VolumeToggleHelper(this, imgbutToggleVolume);
        imgbutToggleVolume.setKeepScreenOn(true);
        prefs = new SharedPreferencesHandler(this);
        timingHandler = new TimingHandler(chronoTimer);
        gameHandler = new TimeTrialGameHandler(this, gameStatusInterface, vibrationHandler, timingHandler);
        setHighScore(prefs.getTimeTrialHighScore() + 1);
        setUiGameState(TimeTrialGameHandler.STATE_FRESHLOAD);
        announcementHandler = new AnnouncementHandler(context, vibrationHandler);
        responseHelper = new ResponseHelper(context);


    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (!event.isLongPress() && event.getRepeatCount() == 0) {
                if (gameHandler.getGameState() == TimeTrialGameHandler.STATE_INGAME) {
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
            } else if (imgbutTogglePause.equals(view)) {
                boolean isPaused = gameHandler.togglePause();
                setTogglePauseImage(isPaused);
            }
        }
    };


    public TimeTrialGameHandler.GameStatusInterface gameStatusInterface = new TimeTrialGameHandler.GameStatusInterface() {
        @Override
        public void newGameStart() {
            setUiGameState(TimeTrialGameHandler.STATE_FRESHLOAD);
            setHighScore(prefs.getTimeTrialHighScore() + 1);
        }

        @Override
        public void levelStart(int level, long timeLeft) {
            setUiGameState(TimeTrialGameHandler.STATE_INGAME);
//            chronoTimer.setBase(SystemClock.elapsedRealtime());
//            chronoTimer.start();
            setLevelLabel(level);
//            announcementHandler.levelStart(level, picksLeft);
        }

        @Override
        public void levelWon(int level) {
            textLevelResult.setText("Lock Picked!");
            setUiGameState(TimeTrialGameHandler.STATE_BETWEENLEVELS);

//            butGameButton.setText("Next Level");
//            chronoTimer.stop();
//            float levelTime = SystemClock.elapsedRealtime() - chronoTimer.getBase();

//            announcementHandler.levelWon(levelTime, levelWon);
        }

        @Override
        public void levelLost(int level) {
            textLevelResult.setText("Pick Broke");
            setUiGameState(TimeTrialGameHandler.STATE_BETWEENLEVELS);

//            butGameButton.setText("Try Again");
//            chronoTimer.stop();

//            announcementHandler.levelLost(level, picksLeft);
        }


        @Override
        public void gameOver(int maxLevel) {
            setUiGameState(TimeTrialGameHandler.STATE_GAMEOVER);
            Log.i("AMP", "gameOver");
            butGameButton.setText("New Game");
            if (prefs.getTimeTrialHighScore() < maxLevel) {
                textGameOver.setText("GAME OVER\nScore: " + String.valueOf(maxLevel) + "\nNEW RECORD!");
                prefs.setTimeTrialHighScore(maxLevel);
                setHighScore(maxLevel + 1);
            } else {
                textGameOver.setText("GAME OVER\nScore: " + String.valueOf(maxLevel + 1) + "\nRecord: " + String.valueOf(prefs.getTimeTrialHighScore()));
            }


//            announcementHandler.gameOver(maxLevel);

        }

        @Override
        public void updateTimeLeft(long timeLeft) {
            setTimeLeft(timeLeft);
        }
    };


    void setTimeLeft(long time) {
        textTime.setText("Time: " + String.valueOf(time / 1000));
    }

    void setLevelLabel(int level) {
        textLevelLabel.setText("Level " + String.valueOf(level + 1));
    }

    void setHighScore(int highScore) {
        textHighScore.setText("High Score: " + String.valueOf(highScore));
    }


    @Override
    protected void onResume() {
        if (gameHandler.getGameState() == TimeTrialGameHandler.STATE_INGAME) {
            gameHandler.setSensorPollingState(true);
            gameHandler.resumeGame();
        }
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void onPause() {
        gameHandler.setSensorPollingState(false);
        if (gameHandler.getGameState() == TimeTrialGameHandler.STATE_INGAME) {
            gameHandler.pauseGame();
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
            case TimeTrialGameHandler.STATE_FRESHLOAD: {
                butGameButton.setVisibility(View.VISIBLE);
                textGameOver.setVisibility(View.GONE);
                textLevelResult.setVisibility(View.GONE);
                textTime.setVisibility(View.GONE);
                imgbutTogglePause.setVisibility(View.GONE);
                textHighScore.setVisibility(View.GONE);
                textLevelLabel.setVisibility(View.VISIBLE);
                setLevelLabel(0);
            }
            break;
            case TimeTrialGameHandler.STATE_INGAME: {
                textLevelLabel.setVisibility(View.VISIBLE);
                butGameButton.setVisibility(View.GONE);
                textGameOver.setVisibility(View.GONE);
                textLevelResult.setVisibility(View.GONE);
                textTime.setVisibility(View.VISIBLE);
                imgbutTogglePause.setVisibility(View.VISIBLE);
                textHighScore.setVisibility(View.VISIBLE);

            }
            break;
            case TimeTrialGameHandler.STATE_BETWEENLEVELS: {
                textLevelLabel.setVisibility(View.VISIBLE);
                butGameButton.setVisibility(View.GONE);
                textGameOver.setVisibility(View.GONE);
                textLevelResult.setVisibility(View.VISIBLE);
                textTime.setVisibility(View.GONE);
                imgbutTogglePause.setVisibility(View.GONE);
                textHighScore.setVisibility(View.GONE);
            }
            break;
            case TimeTrialGameHandler.STATE_GAMEOVER: {
                textLevelLabel.setVisibility(View.GONE);
                butGameButton.setVisibility(View.VISIBLE);
                textGameOver.setVisibility(View.VISIBLE);
                textLevelResult.setVisibility(View.GONE);
                textTime.setVisibility(View.GONE);
                imgbutTogglePause.setVisibility(View.GONE);
                textHighScore.setVisibility(View.GONE);
            }
            break;
        }
    }


    private void setTogglePauseImage(boolean isPaused) {
        imgbutTogglePause.setImageResource(isPaused ? R.drawable.ic_media_play : R.drawable.ic_media_pause);


    }

}