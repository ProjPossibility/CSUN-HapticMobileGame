package com.Norvan.LockPick.TimeTrialMode;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import com.Norvan.LockPick.*;
import com.Norvan.LockPick.Helpers.AnalyticsHelper;
import com.Norvan.LockPick.Helpers.ResponseHelper;
import com.Norvan.LockPick.Helpers.VolumeToggleHelper;
import com.Norvan.LockPick.SurvivalMode.SurvivalGameHandler;

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
    TextView textLevelLabel, textGameOver, textHighScore, textTime, textLevelResult, textModeDescription, textCurrentScore;
    ImageButton imgbutToggleVolume, imgbutTogglePause;
    Button butGameButton;
    Chronometer chronoTimer;
    AnnouncementHandler announcementHandler;
    SharedPreferencesHandler prefs;
    Context context;
    ResponseHelper responseHelper;
    TimingHandler timingHandler;
     AnalyticsHelper analyticsHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetrialgamelayout);
        context = this;
        vibrationHandler = new VibrationHandler(this);
        textLevelLabel = (TextView) findViewById(R.id.textLevelLabel);
        textTime = (TextView) findViewById(R.id.textTime);
        textGameOver = (TextView) findViewById(R.id.textGameOverLabel);
        textModeDescription = (TextView) findViewById(R.id.textModeDescription);
        textLevelResult = (TextView) findViewById(R.id.textLevelResult);
        textCurrentScore = (TextView) findViewById(R.id.textCurrentScore);
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

        analyticsHelper = new AnalyticsHelper(this);
        analyticsHelper.startTimeTrialActivity();
        timingHandler.setUpdateTimeLeftInterface(updateTimeLeftInterface);
        ScoreHandler scoreHandler = new ScoreHandler(prefs, ScoreHandler.MODE_TIMETRIAL);
        gameHandler.setScoreHandler(scoreHandler);
        announcementHandler.playTimeAttackDescription();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (!event.isLongPress() && event.getRepeatCount() == 0) {
                if (gameHandler.getGameState() == TimeTrialGameHandler.STATE_INGAME) {
                    gameHandler.gotKeyDown();
                } else if (gameHandler.getGameState() == TimeTrialGameHandler.STATE_FRESHLOAD || gameHandler.getGameState() == TimeTrialGameHandler.STATE_GAMEOVER) {
                    textModeDescription.setVisibility(View.GONE);
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
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (backButtonPressed) {
                finish();
            } else if (gameHandler.getGameState() != TimeTrialGameHandler.STATE_FRESHLOAD && gameHandler.getGameState() != TimeTrialGameHandler.STATE_GAMEOVER) {
                if (gameHandler.getGameState() == TimeTrialGameHandler.STATE_INGAME) {
                    gameHandler.pauseGame();
                    setTogglePauseImage(true);
                }
                showBackButtonConfirmation();
            }  else{
                finish();
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    boolean backButtonPressed = false;

    public void showBackButtonConfirmation() {
        Handler mHandler = new Handler();
        mHandler.postDelayed(backButtonConfirm, 5000);
        announcementHandler.confirmBackButton();
        backButtonPressed = true;
    }

    Runnable backButtonConfirm = new Runnable() {
        @Override
        public void run() {
            backButtonPressed = false;
        }
    };

    View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (butGameButton.equals(view)) {
                textModeDescription.setVisibility(View.GONE);
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
            setHighScore(gameHandler.getHighScore());
             analyticsHelper.newTimeTrialGame();
        }

        @Override
        public void levelStart(int level, long timeLeft) {
            setUiGameState(TimeTrialGameHandler.STATE_INGAME);
//            chronoTimer.setBase(SystemClock.elapsedRealtime());
//            chronoTimer.start();
            setLevelLabel(level);
            setCurrentScore(gameHandler.getCurrentScore());
//            announcementHandler.levelStart(level, picksLeft);
        }

        @Override
        public void levelWon(int level, long levelTime, int bonus) {
            textLevelResult.setText("Lock Picked!");
            setUiGameState(TimeTrialGameHandler.STATE_BETWEENLEVELS);
             setScoreBonus(bonus);
            analyticsHelper.winTimeTrialLevel(level, gameHandler.getSecondsLeft());
//            butGameButton.setText("Next Level");
//            chronoTimer.stop();
//            float levelTime = SystemClock.elapsedRealtime() - chronoTimer.getBase();

//            announcementHandler.levelWon(levelTime, levelWon);
        }

        @Override
        public void levelLost(int level) {
            textLevelResult.setText("Pick Broke");
            textCurrentScore.setVisibility(View.GONE);
            setUiGameState(TimeTrialGameHandler.STATE_BETWEENLEVELS);
            analyticsHelper.loseTimeTrialLevel(level, gameHandler.getSecondsLeft());

//            butGameButton.setText("Try Again");
//            chronoTimer.stop();

//            announcementHandler.levelLost(level, picksLeft);
        }


        @Override
        public void gameOver(int maxLevel, boolean isHighScore, int currentScore) {
            setUiGameState(TimeTrialGameHandler.STATE_GAMEOVER);
            Log.i("AMP", "gameOver");
            butGameButton.setText("New Game");
            analyticsHelper.gameOverTimeTrial(maxLevel);
            if (isHighScore) {
                textGameOver.setText("GAME OVER\n\nScore: " + String.valueOf(currentScore) + "\n\nNEW RECORD!");
                setHighScore(gameHandler.getHighScore());
            } else {
                textGameOver.setText("GAME OVER\n\nScore: " + String.valueOf(currentScore) + "\n\nRecord: " + String.valueOf(gameHandler.getHighScore()));
            }

            announcementHandler.gameOver(currentScore, isHighScore);

//            announcementHandler.gameOver(maxLevel);

        }

        @Override
        public void updateTimeLeft(long timeLeft) {
            setTimeLeft(timeLeft);
        }
    };

    void setScoreBonus(int bonus) {
        textCurrentScore.setText("Score Bonus: "+String.valueOf(bonus));
    }
    
    void setCurrentScore(int score){
        textCurrentScore.setText("Score: "+String.valueOf(score));
    }
    
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
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
        gameHandler.setSensorPollingState(true);
        if (gameHandler.getGameState() == TimeTrialGameHandler.STATE_PAUSED) {
            setTogglePauseImage(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        Log.i("AMP", "onpause timetrial");
        gameHandler.setSensorPollingState(false);
        if (gameHandler.getGameState() == TimeTrialGameHandler.STATE_INGAME) {
            gameHandler.pauseGame();
        }
        vibrationHandler.stopVibrate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        Log.i("AMP", "ondestroy timetrial");
        announcementHandler.shutDown();
        vibrationHandler.stopVibrate();
        announcementHandler = null;
        vibrationHandler = null;
        gameHandler = null;
        prefs = null;
        analyticsHelper.exitTimeTrial();
        analyticsHelper = null;

    }

    private void setUiGameState(int gameState) {
        switch (gameState) {
            case TimeTrialGameHandler.STATE_FRESHLOAD: {
                butGameButton.setVisibility(View.VISIBLE);
                textGameOver.setVisibility(View.GONE);
                textLevelResult.setVisibility(View.GONE);
                textTime.setVisibility(View.GONE);
                textCurrentScore.setVisibility(View.GONE);
                imgbutTogglePause.setVisibility(View.GONE);
                textHighScore.setVisibility(View.GONE);
                textLevelLabel.setVisibility(View.VISIBLE);
//                setLevelLabel(0);
            }
            break;
            case TimeTrialGameHandler.STATE_INGAME: {
                textLevelLabel.setVisibility(View.VISIBLE);
                butGameButton.setVisibility(View.GONE);
                textGameOver.setVisibility(View.GONE);
                textCurrentScore.setVisibility(View.VISIBLE);
                textLevelResult.setVisibility(View.GONE);
                textTime.setVisibility(View.VISIBLE);
                imgbutTogglePause.setVisibility(View.VISIBLE);
                textHighScore.setVisibility(View.VISIBLE);

            }
            break;
            case TimeTrialGameHandler.STATE_BETWEENLEVELS: {
                textLevelLabel.setVisibility(View.VISIBLE);
                butGameButton.setVisibility(View.GONE);
                textCurrentScore.setVisibility(View.VISIBLE);
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
                textCurrentScore.setVisibility(View.GONE);
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

    TimingHandler.UpdateTimeLeftInterface updateTimeLeftInterface = new TimingHandler.UpdateTimeLeftInterface() {
        @Override
        public void updateTimeLeft(long timeLeft) {
            int secondsLeft = (int) (timeLeft / 1000);
            announcementHandler.announceTimeLeft(secondsLeft);
        }
    };

}