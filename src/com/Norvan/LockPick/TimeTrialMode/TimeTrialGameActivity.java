package com.Norvan.LockPick.TimeTrialMode;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import com.Norvan.LockPick.*;
import com.Norvan.LockPick.Helpers.AnalyticsHelper;
import com.Norvan.LockPick.Helpers.ResponseHelper;
import com.Norvan.LockPick.Helpers.UserType;
import com.Norvan.LockPick.Helpers.VolumeToggleHelper;
import com.Norvan.LockPick.SurvivalMode.SurvivalGameHandler;


public class TimeTrialGameActivity extends Activity {
    private VolumeToggleHelper volumeToggleHelper;
    private VibrationHandler vibrationHandler;
    private TimeTrialGameHandler gameHandler;
    private TextView textLevelLabel, textGameOver, textHighScore, textTime, textLevelResult, textModeDescription, textCurrentScore, textScoreBonus;
    private ImageButton imgbutToggleVolume, imgbutTogglePause;
    private Button butGameButton;
    private Chronometer chronoTimer;
    private AnnouncementHandler announcementHandler;
    private SharedPreferencesHandler prefs;
    private Context context;
    private TimingHandler timingHandler;
    private AnalyticsHelper analyticsHelper;
    private int userType;
    private RelativeLayout quad1, quad2, quad4;
    private int lastLevelReached = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        vibrationHandler = new VibrationHandler(this);
        prefs = new SharedPreferencesHandler(this);
        userType = prefs.getUserType();
        if (userType == UserType.USER_NORMAL) {
            setUpNormalUI();
        } else if (userType == UserType.USER_DEAFBLIND || userType == UserType.USER_BLIND) {
            setUpAccessibleUI();
        }


        timingHandler = new TimingHandler(chronoTimer);
        gameHandler = new TimeTrialGameHandler(this, gameStatusInterface, vibrationHandler, timingHandler);
        setHighScore(prefs.getTimeTrialHighScore());
        setUiGameState(TimeTrialGameHandler.STATE_FRESHLOAD);
        announcementHandler = new AnnouncementHandler(context, vibrationHandler);
        analyticsHelper = new AnalyticsHelper(this);
        analyticsHelper.startTimeTrialActivity();
        timingHandler.setUpdateTimeLeftInterface(updateTimeLeftInterface);
        ScoreHandler scoreHandler = new ScoreHandler(prefs, ScoreHandler.MODE_TIMETRIAL);
        gameHandler.setScoreHandler(scoreHandler);
        announcementHandler.playTimeAttackDescription();
    }

    private void setUpNormalUI() {
        setContentView(R.layout.timetrialgamelayout);
        textLevelLabel = (TextView) findViewById(R.id.textLevelLabel);
        textTime = (TextView) findViewById(R.id.textTime);
        textGameOver = (TextView) findViewById(R.id.textGameOverLabel);
        textModeDescription = (TextView) findViewById(R.id.textModeDescription);
        textLevelResult = (TextView) findViewById(R.id.textLevelResult);
        textCurrentScore = (TextView) findViewById(R.id.textCurrentScore);
        textHighScore = (TextView) findViewById(R.id.textHighScore);
        imgbutToggleVolume = (ImageButton) findViewById(R.id.imgbutGameVolume);
        imgbutToggleVolume.setOnClickListener(onClickNormal);
        imgbutTogglePause = (ImageButton) findViewById(R.id.imgbutTogglePause);
        imgbutTogglePause.setOnClickListener(onClickNormal);
        butGameButton = (Button) findViewById(R.id.butGameButton);
        butGameButton.setOnClickListener(onClickNormal);
        chronoTimer = (Chronometer) findViewById(R.id.chronoTimer);
        volumeToggleHelper = new VolumeToggleHelper(this, imgbutToggleVolume);
        imgbutToggleVolume.setKeepScreenOn(true);
    }

    private void setUpAccessibleUI() {
        setContentView(R.layout.diagonaltimetriallayout);
        textLevelLabel = (TextView) findViewById(R.id.textCurrentLevel);
        textCurrentScore = (TextView) findViewById(R.id.textScore);
        textTime = (TextView) findViewById(R.id.textTime);
        textGameOver = (TextView) findViewById(R.id.textGameOver);  //
        textModeDescription = (TextView) findViewById(R.id.textGameMode);
        textHighScore = (TextView) findViewById(R.id.textHighScore);
        textScoreBonus = (TextView) findViewById(R.id.textScoreBonus);
        imgbutTogglePause = (ImageButton) findViewById(R.id.imgbutTogglePause);
        imgbutTogglePause.setOnClickListener(onClickAccessible);
        imgbutTogglePause.setOnLongClickListener(onLongClickAccessible);
        chronoTimer = (Chronometer) findViewById(R.id.chronoTime);
        quad1 = (RelativeLayout) findViewById(R.id.relquad1);
        quad2 = (RelativeLayout) findViewById(R.id.relquad2);
        quad4 = (RelativeLayout) findViewById(R.id.relquad4);
        quad1.setOnClickListener(onClickAccessible);
        quad2.setOnClickListener(onClickAccessible);
        quad4.setOnClickListener(onClickAccessible);
        quad1.setOnLongClickListener(onLongClickAccessible);
        quad2.setOnLongClickListener(onLongClickAccessible);
        quad4.setOnLongClickListener(onLongClickAccessible);
        imgbutTogglePause.setKeepScreenOn(true);
        imgbutTogglePause.setContentDescription("Start game");

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
                    pauseGame();
                }
                showBackButtonConfirmation();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private boolean backButtonPressed = false;

    private void showBackButtonConfirmation() {
        Handler mHandler = new Handler();
        mHandler.postDelayed(backButtonConfirm, 5000);
        announcementHandler.confirmBackButton();
        backButtonPressed = true;
    }

    private Runnable backButtonConfirm = new Runnable() {
        @Override
        public void run() {
            backButtonPressed = false;
        }
    };

    private View.OnClickListener onClickAccessible = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (imgbutTogglePause.equals(view)) {
                if (userType == UserType.USER_BLIND) {
                    switch (gameHandler.getGameState()) {
                        case SurvivalGameHandler.STATE_PAUSED:
                            resumeGame();
                            break;
                        case SurvivalGameHandler.STATE_INGAME:
                            pauseGame();
                            break;
                        case SurvivalGameHandler.STATE_FRESHLOAD:
                            gameHandler.playCurrentLevel();
                            break;
                        case SurvivalGameHandler.STATE_GAMEOVER:
                            gameHandler.playCurrentLevel();
                            break;
                    }
                } else if (userType == UserType.USER_DEAFBLIND) {
                    switch (gameHandler.getGameState()) {
                        case SurvivalGameHandler.STATE_PAUSED:
                            announcementHandler.gameResumeGame();
                            break;
                        case SurvivalGameHandler.STATE_INGAME:
                            pauseGame();
                            break;
                        case SurvivalGameHandler.STATE_FRESHLOAD:
                            announcementHandler.gameStartFreshGame();
                            break;
                        case SurvivalGameHandler.STATE_GAMEOVER:
                            announcementHandler.gameStartNewGame();
                            break;
                    }
                }
            } else if (quad1.equals(view)) {
                switch (gameHandler.getGameState()) {
                    case SurvivalGameHandler.STATE_PAUSED:
                        announcementHandler.readSecondsLeft(gameHandler.getSecondsLeft());
                        break;
                    case SurvivalGameHandler.STATE_INGAME:
                        announcementHandler.readSecondsLeft(gameHandler.getSecondsLeft());
                        break;
                    case SurvivalGameHandler.STATE_BETWEENLEVELS:
                        announcementHandler.readSecondsLeft(gameHandler.getSecondsLeft());
                        break;
                    case SurvivalGameHandler.STATE_FRESHLOAD:
                        announcementHandler.playTimeAttackDescription();
                        break;
                    case SurvivalGameHandler.STATE_GAMEOVER:
                        announcementHandler.readGameOver();
                        break;

                }
            } else if (quad2.equals(view)) {
                switch (gameHandler.getGameState()) {
                    case SurvivalGameHandler.STATE_PAUSED:
                        announcementHandler.readLevelLabel(gameHandler.getCurrentLevel() + 1, false);
                        break;
                    case SurvivalGameHandler.STATE_INGAME:
                        announcementHandler.readLevelLabel(gameHandler.getCurrentLevel() + 1, false);
                        break;

                    case SurvivalGameHandler.STATE_FRESHLOAD:
                        announcementHandler.pressBottomLeft();
                        break;
                    case SurvivalGameHandler.STATE_GAMEOVER:
                        announcementHandler.readLevelLabel(lastLevelReached + 1, true);
                        break;

                }
            } else if (quad4.equals(view)) {
                announcementHandler.readScores(gameHandler.getCurrentScore(), gameHandler.getHighScore());
            }
        }
    };

    private View.OnLongClickListener onLongClickAccessible = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            if (imgbutTogglePause.equals(view)) {
                switch (gameHandler.getGameState()) {
                    case SurvivalGameHandler.STATE_PAUSED:
                        resumeGame();
                        break;
                    case SurvivalGameHandler.STATE_INGAME:
                        pauseGame();
                        break;

                    case SurvivalGameHandler.STATE_FRESHLOAD:
                        gameHandler.playCurrentLevel();
                        break;
                    case SurvivalGameHandler.STATE_GAMEOVER:
                        gameHandler.playCurrentLevel();
                        break;

                }
                return true;
            } else if (quad1.equals(view)) {

            } else if (quad2.equals(view)) {

            } else if (quad4.equals(view)) {

            }
            return false;
        }
    };

    private View.OnClickListener onClickNormal = new View.OnClickListener() {
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


    private TimeTrialGameHandler.GameStatusInterface gameStatusInterface = new TimeTrialGameHandler.GameStatusInterface() {
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
            if (userType == UserType.USER_NORMAL) {
                textLevelResult.setText("Lock Picked!");
            } else {
                textLevelLabel.setText("Lock Picked!");
                textScoreBonus.setVisibility(View.VISIBLE);
                imgbutTogglePause.setContentDescription("Start next level");
            }
            setUiGameState(TimeTrialGameHandler.STATE_BETWEENLEVELS);
            setScoreBonus(bonus);
            announcementHandler.timeTrialWin();
            setCurrentScore(gameHandler.getCurrentScore());
            analyticsHelper.winTimeTrialLevel(level, gameHandler.getSecondsLeft());
//            butGameButton.setText("Next Level");
//            chronoTimer.stop();
//            float levelTime = SystemClock.elapsedRealtime() - chronoTimer.getBase();

//            announcementHandler.levelWon(levelTime, levelWon);
        }

        @Override
        public void levelLost(int level) {
            if (userType == UserType.USER_NORMAL) {
                textLevelResult.setText("Pick Broke");
                textCurrentScore.setVisibility(View.GONE);
            } else {
                imgbutTogglePause.setContentDescription("Try level again");
                textLevelLabel.setText("Pick Broke");
            }
            announcementHandler.timeTrialLose();

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
            if (userType == UserType.USER_NORMAL) {
                butGameButton.setText("New Game");
            }
            lastLevelReached = maxLevel;
            analyticsHelper.gameOverTimeTrial(currentScore, maxLevel);
            if (userType == UserType.USER_NORMAL) {

                if (isHighScore) {
                    textGameOver.setText("GAME OVER\n\nScore: " + String.valueOf(currentScore) + "\n\nNEW RECORD!");
                    setHighScore(gameHandler.getHighScore());
                } else {
                    textGameOver.setText("GAME OVER\n\nScore: " + String.valueOf(currentScore) + "\n\nRecord: " + String.valueOf(gameHandler.getHighScore()));
                }
            } else {
                textGameOver.setText("Game Over");
            }


            announcementHandler.gameOver(currentScore, isHighScore);

//            announcementHandler.gameOver(maxLevel);

        }

        @Override
        public void updateTimeLeft(long timeLeft) {
            setTimeLeft(timeLeft);
        }
    };

    private void setScoreBonus(int bonus) {
        if (userType == UserType.USER_NORMAL) {
            textCurrentScore.setText("Score Bonus: " + String.valueOf(bonus));
        } else {
            textScoreBonus.setText("Score Bonus\n" + String.valueOf(bonus));
        }
    }

    private void setCurrentScore(int score) {
        if (userType == UserType.USER_NORMAL) {
            textCurrentScore.setText("Score: " + String.valueOf(score));
        } else {
            textCurrentScore.setText("Score\n" + String.valueOf(score));
        }
    }

    private void setTimeLeft(long time) {
        if (userType == UserType.USER_NORMAL) {
            textTime.setText("Time: " + String.valueOf(time / 1000));
        } else {
            textTime.setText("Time Left\n" + String.valueOf(time / 1000));
        }


    }

    private void setLevelLabel(int level) {
        textLevelLabel.setText("Level " + String.valueOf(level + 1));
    }

    private void setHighScore(int highScore) {
        if (userType == UserType.USER_NORMAL) {
            textHighScore.setText("High Score: " + String.valueOf(highScore));
        } else {
            textHighScore.setText("High Score\n" + String.valueOf(highScore));
        }
    }


    @Override
    protected void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
        if (gameHandler.getGameState() != TimeTrialGameHandler.STATE_FRESHLOAD) {
            gameHandler.setSensorPollingState(true);
        }
        if (gameHandler.getGameState() == TimeTrialGameHandler.STATE_PAUSED) {
            setTogglePauseImage(true);
        }
    }

    private void pauseGame() {
        gameHandler.pauseGame();
        imgbutTogglePause.setContentDescription("Resume game");
        setTogglePauseImage(true);
        announcementHandler.confirmGamePause();
    }

    private void resumeGame() {
        gameHandler.resumeGame();
        imgbutTogglePause.setContentDescription("Pause game");
        setTogglePauseImage(false);
        announcementHandler.confirmGameResume();
    }

    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        Log.i("AMP", "onpause timetrial");
        gameHandler.setSensorPollingState(false);
        if (gameHandler.getGameState() == TimeTrialGameHandler.STATE_INGAME) {
            pauseGame();
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
                textGameOver.setVisibility(View.GONE);
                textTime.setVisibility(View.GONE);
                textLevelLabel.setVisibility(View.VISIBLE);

                if (userType == UserType.USER_NORMAL) {
                    butGameButton.setVisibility(View.VISIBLE);
                    textCurrentScore.setVisibility(View.GONE);
                    textHighScore.setVisibility(View.GONE);
                    imgbutTogglePause.setVisibility(View.GONE);
                    textLevelResult.setVisibility(View.GONE);
                } else {
                    textCurrentScore.setVisibility(View.VISIBLE);
                    textHighScore.setVisibility(View.VISIBLE);
                    imgbutTogglePause.setVisibility(View.VISIBLE);
                    setTogglePauseImage(true);
                    textScoreBonus.setVisibility(View.GONE);

                }
            }
            break;
            case TimeTrialGameHandler.STATE_INGAME: {
                textLevelLabel.setVisibility(View.VISIBLE);

                textGameOver.setVisibility(View.GONE);
                textCurrentScore.setVisibility(View.VISIBLE);
                textModeDescription.setVisibility(View.GONE);

                textTime.setVisibility(View.VISIBLE);
                imgbutTogglePause.setVisibility(View.VISIBLE);
                textHighScore.setVisibility(View.VISIBLE);
                if (userType == UserType.USER_NORMAL) {
                    butGameButton.setVisibility(View.GONE);
                    textLevelResult.setVisibility(View.GONE);
                } else {
                    setTogglePauseImage(false);
                    imgbutTogglePause.setContentDescription("Pause Game");
                    textScoreBonus.setVisibility(View.GONE);

                }
            }
            break;
            case TimeTrialGameHandler.STATE_BETWEENLEVELS: {
                textLevelLabel.setVisibility(View.VISIBLE);
                textModeDescription.setVisibility(View.GONE);
                textCurrentScore.setVisibility(View.VISIBLE);
                textGameOver.setVisibility(View.GONE);
                if (userType == UserType.USER_NORMAL) {
                    butGameButton.setVisibility(View.GONE);
                    textLevelResult.setVisibility(View.VISIBLE);
                    textTime.setVisibility(View.GONE);
                    textHighScore.setVisibility(View.GONE);
                    imgbutTogglePause.setVisibility(View.GONE);

                } else {
                    textTime.setVisibility(View.VISIBLE);
                    textHighScore.setVisibility(View.VISIBLE);
                    imgbutTogglePause.setVisibility(View.VISIBLE);
//                    textScoreBonus.setVisibility(View.VISIBLE);

                }
            }
            break;
            case TimeTrialGameHandler.STATE_GAMEOVER: {
                textModeDescription.setVisibility(View.GONE);

                textGameOver.setVisibility(View.VISIBLE);

                textTime.setVisibility(View.GONE);

                if (userType == UserType.USER_NORMAL) {
                    textLevelLabel.setVisibility(View.GONE);
                    butGameButton.setVisibility(View.VISIBLE);
                    textCurrentScore.setVisibility(View.GONE);
                    textLevelResult.setVisibility(View.GONE);
                    imgbutTogglePause.setVisibility(View.GONE);
                    textHighScore.setVisibility(View.GONE);
                } else {
                    textLevelLabel.setVisibility(View.VISIBLE);
                    textCurrentScore.setVisibility(View.VISIBLE);
                    imgbutTogglePause.setVisibility(View.VISIBLE);
                    textHighScore.setVisibility(View.VISIBLE);
                    setTogglePauseImage(true);
                    imgbutTogglePause.setContentDescription("Start New Game");
                    textScoreBonus.setVisibility(View.GONE);
                }
            }
            break;
        }
    }


    private void setTogglePauseImage(boolean isPaused) {
        imgbutTogglePause.setImageResource(isPaused ? R.drawable.ic_media_play : R.drawable.ic_media_pause);


    }

    private TimingHandler.UpdateTimeLeftInterface updateTimeLeftInterface = new TimingHandler.UpdateTimeLeftInterface() {
        @Override
        public void updateTimeLeft(long timeLeft) {
            int secondsLeft = (int) (timeLeft / 1000);
            announcementHandler.announceTimeLeft(secondsLeft);
        }
    };

}