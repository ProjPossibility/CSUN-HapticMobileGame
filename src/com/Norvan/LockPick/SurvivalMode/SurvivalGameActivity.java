package com.Norvan.LockPick.SurvivalMode;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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
import com.Norvan.LockPick.TimeTrialMode.TimeTrialGameHandler;

public class SurvivalGameActivity extends Activity

{
    boolean wonLastLevel = false;
    int lastLevelReached = 0;
    LinearLayout linearChrono;
    VolumeToggleHelper volumeToggleHelper;
    VibrationHandler vibrationHandler;
    SurvivalGameHandler gameHandler;
    TextView textPicksLeft, textLevelLabel, textGameOver, textHighScore, textModeDescription, textCurrentScore, textScoreBonus;
    ImageButton imgbutToggleVolume, imgbutTogglePause;
    Button butGameButton;
    Chronometer chronoTimer;
    AnnouncementHandler announcementHandler;
    SharedPreferencesHandler prefs;
    Context context;
    ResponseHelper responseHelper;
    long gamePausedChronoProgress;
    AnalyticsHelper analyticsHelper;
    ScoreHandler scoreHandler;
    int userType;
    RelativeLayout quad1, quad2, quad4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        vibrationHandler = new VibrationHandler(this);
        gameHandler = new SurvivalGameHandler(this, gameStatusInterface, vibrationHandler);


        prefs = new SharedPreferencesHandler(this);
        userType = prefs.getUserType();

        if (userType == UserType.USER_NORMAL) {
            setUpNormalUI();
        } else if (userType == UserType.USER_DEAFBLIND || userType == UserType.USER_BLIND) {
            setUpAccessibleUI();
        }


        chronoTimer.setKeepScreenOn(true);

        setUiGameState(SurvivalGameHandler.STATE_FRESHLOAD);
        announcementHandler = new AnnouncementHandler(context, vibrationHandler);

        chronoTimer.setOnChronometerTickListener(onTick);
        responseHelper = new ResponseHelper(context);

        analyticsHelper = new AnalyticsHelper(this);
        analyticsHelper.startSurvivalActivity();

        scoreHandler = new ScoreHandler(prefs, ScoreHandler.MODE_SURVIVAL);

        setUiGameState(SurvivalGameHandler.STATE_FRESHLOAD);
        scoreHandler.newGame();
        setHighScore(scoreHandler.getHighScore());
        analyticsHelper.newSurvivalGame();
        announcementHandler.playPuzzleDescription();
    }

    private void setUpAccessibleUI() {
        setContentView(R.layout.diagonalsurvivallayout);
        textPicksLeft = (TextView) findViewById(R.id.textPicksLeft);
        textLevelLabel = (TextView) findViewById(R.id.textCurrentLevel);
        textCurrentScore = (TextView) findViewById(R.id.textScore);
        textGameOver = (TextView) findViewById(R.id.textGameOver);  //
        textModeDescription = (TextView) findViewById(R.id.textGameMode);
        textHighScore = (TextView) findViewById(R.id.textHighScore);
        textScoreBonus = (TextView) findViewById(R.id.textScoreBonus);
        linearChrono = (LinearLayout) findViewById(R.id.linearTime);
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
        imgbutTogglePause.setContentDescription("Start game");
//        volumeToggleHelper = new VolumeToggleHelper(this, imgbutToggleVolume);
    }


    private void setUpNormalUI() {
        setContentView(R.layout.survivalgamelayout);
        textPicksLeft = (TextView) findViewById(R.id.textPicksLeft);
        textLevelLabel = (TextView) findViewById(R.id.textLevelLabel);
        textCurrentScore = (TextView) findViewById(R.id.textCurrentScore);
        textGameOver = (TextView) findViewById(R.id.textGameOverLabel);
        textModeDescription = (TextView) findViewById(R.id.textModeDescription);
        textHighScore = (TextView) findViewById(R.id.textHighScore);
        linearChrono = (LinearLayout) findViewById(R.id.linearChrono);
        imgbutToggleVolume = (ImageButton) findViewById(R.id.imgbutGameVolume);
        imgbutToggleVolume.setOnClickListener(onClickNormal);
        imgbutTogglePause = (ImageButton) findViewById(R.id.imgbutTogglePause);
        imgbutTogglePause.setOnClickListener(onClickNormal);
        butGameButton = (Button) findViewById(R.id.butGameButton);
        butGameButton.setOnClickListener(onClickNormal);
        chronoTimer = (Chronometer) findViewById(R.id.chronoTimer);
        volumeToggleHelper = new VolumeToggleHelper(this, imgbutToggleVolume);

    }

    Chronometer.OnChronometerTickListener onTick = new Chronometer.OnChronometerTickListener() {
        @Override
        public void onChronometerTick(Chronometer chronometer) {
            long timeElapsed = getCurrentTime() - chronometer.getBase();
            if (timeElapsed > 10000) {
                if (timeElapsed % 10000 < 1000 && gameHandler.getCurrentLevel() > 6) {
                    announcementHandler.userTakingTooLong();
                }
            }
        }
    };


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (!event.isLongPress() && event.getRepeatCount() == 0) {
                if (gameHandler.getGameState() == SurvivalGameHandler.STATE_INGAME) {
                    gameHandler.gotKeyDown();
                } else if (gameHandler.getGameState() != SurvivalGameHandler.STATE_PAUSED) {
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
                showBackButtonConfirmation();
                if (gameHandler.getGameState() == TimeTrialGameHandler.STATE_INGAME) {
                    pauseGame();
                }

            } else {
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


    View.OnClickListener onClickNormal = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (butGameButton.equals(view)) {
                gameHandler.playCurrentLevel();
            } else if (imgbutToggleVolume.equals(view)) {
                volumeToggleHelper.toggleMute();
            } else if (imgbutTogglePause.equals(view)) {
                boolean isPaused = gameHandler.togglePause();
                if (isPaused) {
                    pauseGame();
                } else {
                    resumeGame();
                }

            }
        }
    };

    View.OnClickListener onClickAccessible = new View.OnClickListener() {
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
                        case SurvivalGameHandler.STATE_BETWEENLEVELS:
                            gameHandler.playCurrentLevel();
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
                        case SurvivalGameHandler.STATE_BETWEENLEVELS:
                            announcementHandler.gameNextLevel(wonLastLevel);
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
                        announcementHandler.readPicksLeft(gameHandler.getNumberOfPicksLeft());
                        break;
                    case SurvivalGameHandler.STATE_INGAME:
                        announcementHandler.readPicksLeft(gameHandler.getNumberOfPicksLeft());
                        break;
                    case SurvivalGameHandler.STATE_BETWEENLEVELS:
                        announcementHandler.readPicksLeft(gameHandler.getNumberOfPicksLeft());
                        break;
                    case SurvivalGameHandler.STATE_FRESHLOAD:
                        announcementHandler.playPuzzleDescription();
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
                    case SurvivalGameHandler.STATE_BETWEENLEVELS:
                        announcementHandler.readLevelResult(wonLastLevel, gameHandler.getCurrentLevel());
                        break;
                    case SurvivalGameHandler.STATE_FRESHLOAD:
                        announcementHandler.pressBottomLeft();
                        break;
                    case SurvivalGameHandler.STATE_GAMEOVER:
                        announcementHandler.readLevelLabel(lastLevelReached + 1, true);
                        break;

                }
            } else if (quad4.equals(view)) {
                announcementHandler.readScores(scoreHandler.getCurrentScore(), scoreHandler.getHighScore());
            }
            //To change body of implemented methods use File | Settings | File Templates.
        }
    };
    View.OnLongClickListener onLongClickAccessible = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            if (imgbutTogglePause.equals(view)) {
                switch (gameHandler.getGameState()) {
                    case SurvivalGameHandler.STATE_PAUSED:
                        resumeGame();
                        announcementHandler.confirmGameResume();
                        break;
                    case SurvivalGameHandler.STATE_INGAME:
                        pauseGame();
                        announcementHandler.confirmGamePause();
                        break;
                    case SurvivalGameHandler.STATE_BETWEENLEVELS:
                        gameHandler.playCurrentLevel();
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
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }
    };


    public SurvivalGameHandler.GameStatusInterface gameStatusInterface = new SurvivalGameHandler.GameStatusInterface() {
        @Override
        public void newGameStart() {
            setUiGameState(SurvivalGameHandler.STATE_FRESHLOAD);
            scoreHandler.newGame();
            setHighScore(scoreHandler.getHighScore());
            setScore();
            analyticsHelper.newSurvivalGame();
        }

        @Override
        public void levelStart(int level, int picksLeft) {
            setUiGameState(SurvivalGameHandler.STATE_INGAME);
            chronoTimer.setBase(SystemClock.elapsedRealtime());
            chronoTimer.start();
            setPicksLeft(picksLeft);
            setLevelLabel(level);
            setScore();
            announcementHandler.puzzlelevelStart(level, picksLeft);

        }

        @Override
        public void levelWon(int levelWon, int picksLeft) {
            setUiGameState(SurvivalGameHandler.STATE_BETWEENLEVELS);
            if (userType == UserType.USER_NORMAL) {
                butGameButton.setText("Next Level");
            } else {
                textLevelLabel.setText("Level Complete!");
                imgbutTogglePause.setContentDescription("Start next level");
            }
            wonLastLevel = true;
            chronoTimer.stop();
            float levelTime = SystemClock.elapsedRealtime() - chronoTimer.getBase();
            setTimeBonus(scoreHandler.wonLevel(levelTime));
            analyticsHelper.winSurvivalLevel(levelWon, (int) levelTime, picksLeft);
            setPicksLeft(picksLeft);
            setScore();
            announcementHandler.puzzlelevelWon(levelWon);

        }

        @Override
        public void levelLost(int level, int picksLeft) {
            setUiGameState(SurvivalGameHandler.STATE_BETWEENLEVELS);
            if (userType == UserType.USER_NORMAL) {
                textCurrentScore.setVisibility(View.GONE);
                butGameButton.setText("Try Again");
            } else {
                textScoreBonus.setVisibility(View.GONE);
                imgbutTogglePause.setContentDescription("Try level again");
                textLevelLabel.setText("Broke Pick\nTry Again");
            }
            chronoTimer.stop();
            wonLastLevel = false;
            analyticsHelper.loseSurvivalLevel(level, (int) (SystemClock.elapsedRealtime() - chronoTimer.getBase()), picksLeft);
            setPicksLeft(picksLeft);
            announcementHandler.puzzlelevelLost(level, picksLeft);
        }

        @Override
        public void gameOver(int maxLevel) {
            setUiGameState(SurvivalGameHandler.STATE_GAMEOVER);
            Log.i("AMP", "gameOver");
            if (userType == UserType.USER_NORMAL) {
                butGameButton.setText("New Game");
            }
            lastLevelReached = maxLevel;
            analyticsHelper.gameOverSurvival(scoreHandler.getCurrentScore(), maxLevel);
            boolean isHighScore = scoreHandler.gameOver();
            if (userType == UserType.USER_NORMAL) {
                if (isHighScore) {
                    textGameOver.setText("GAME OVER\n\nScore: " + String.valueOf(scoreHandler.getCurrentScore()) + "\n\nNEW RECORD!");

                } else {
                    textGameOver.setText("GAME OVER\n\nScore: " + String.valueOf(scoreHandler.getCurrentScore()) + "\n\nRecord: " + String.valueOf(scoreHandler.getHighScore()));
                }
            } else {
                textGameOver.setText("Game Over");
            }


            chronoTimer.stop();

            setHighScore(scoreHandler.getHighScore());
            announcementHandler.gameOver(scoreHandler.getCurrentScore(), isHighScore);

        }
    };

    void setTimeBonus(int bonus) {
        if (userType == UserType.USER_NORMAL) {
            textCurrentScore.setText("Score Bonus: " + String.valueOf(bonus));
        } else {
            textScoreBonus.setText("Score Bonus\n" + String.valueOf(bonus));
        }
    }

    void setScore() {

        if (userType == UserType.USER_NORMAL) {
            textCurrentScore.setText("Score: " + String.valueOf(scoreHandler.getCurrentScore()));
        } else {
            textCurrentScore.setText("Score\n" + String.valueOf(scoreHandler.getCurrentScore()));
        }
    }

    void setPicksLeft(int picks) {
        if (userType == UserType.USER_NORMAL) {
            textPicksLeft.setText("Picks Left: " + String.valueOf(picks));
        } else {
            textPicksLeft.setText("Picks Left\n" + String.valueOf(picks));
        }

    }

    void setLevelLabel(int level) {
        textLevelLabel.setText("Level " + String.valueOf(level + 1));
    }

    void setHighScore(int highScore) {
        if (userType == UserType.USER_NORMAL) {
            textHighScore.setText("High Score: " + String.valueOf(highScore));
        } else {
            textHighScore.setText("High Score\n" + String.valueOf(highScore));
        }
    }

    private void pauseGame() {
        gameHandler.pauseGame();
        gamePausedChronoProgress = getCurrentTime() - chronoTimer.getBase();
        chronoTimer.stop();
        setTogglePauseImage(true);
        announcementHandler.confirmGamePause();
        imgbutTogglePause.setContentDescription("Resume game");

    }

    private void resumeGame() {    
        gameHandler.resumeGame();
        chronoTimer.setBase(getCurrentTime() - gamePausedChronoProgress);
        chronoTimer.start();
        setTogglePauseImage(false);
        announcementHandler.confirmGameResume();
        imgbutTogglePause.setContentDescription("Pause Game");
    }

    @Override
    protected void onResume() {
        if (gameHandler.getGameState() != SurvivalGameHandler.STATE_FRESHLOAD) {
            gameHandler.setSensorPollingState(true);
        }
        if (gameHandler.getGameState() == TimeTrialGameHandler.STATE_PAUSED) {
            setTogglePauseImage(true);
        }


        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void onPause() {
        super.onPause();

        gameHandler.setSensorPollingState(false);
        if (gameHandler.getGameState() == TimeTrialGameHandler.STATE_INGAME) {
            pauseGame();
        }
        vibrationHandler.stopVibrate();

    }

    @Override
    protected void onDestroy() {
        announcementHandler.shutDown();
        vibrationHandler.stopVibrate();
        announcementHandler = null;
        vibrationHandler = null;
        gameHandler = null;
        prefs = null;
        analyticsHelper.exitSurvival();
        analyticsHelper = null;
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
    }

    private void setUiGameState(int gameState) {
        switch (gameState) {
            case SurvivalGameHandler.STATE_FRESHLOAD: {

                textGameOver.setVisibility(View.GONE);
                linearChrono.setVisibility(View.GONE);
                textPicksLeft.setVisibility(View.GONE);

                textLevelLabel.setVisibility(View.VISIBLE);

                textModeDescription.setVisibility(View.VISIBLE);

                if (userType == UserType.USER_NORMAL) {
                    butGameButton.setVisibility(View.VISIBLE);
                    textHighScore.setVisibility(View.GONE);
                    textCurrentScore.setVisibility(View.GONE);
                    imgbutTogglePause.setVisibility(View.GONE);
                } else {
                    textScoreBonus.setVisibility(View.GONE);
                    textHighScore.setVisibility(View.VISIBLE);
                    textCurrentScore.setVisibility(View.VISIBLE);
                    imgbutTogglePause.setVisibility(View.VISIBLE);
                    imgbutTogglePause.setImageResource(R.drawable.ic_media_play);
                }
            }
            break;
            case SurvivalGameHandler.STATE_INGAME: {
                textLevelLabel.setVisibility(View.VISIBLE);
                textGameOver.setVisibility(View.GONE);
                linearChrono.setVisibility(View.VISIBLE);
                textPicksLeft.setVisibility(View.VISIBLE);
                textCurrentScore.setVisibility(View.VISIBLE);
                imgbutTogglePause.setVisibility(View.VISIBLE);
                textHighScore.setVisibility(View.VISIBLE);
                textModeDescription.setVisibility(View.GONE);
                if (userType == UserType.USER_NORMAL) {
                    butGameButton.setVisibility(View.GONE);
                } else {
                    textScoreBonus.setVisibility(View.GONE);
                    imgbutTogglePause.setContentDescription("Pause game");
                    imgbutTogglePause.setImageResource(R.drawable.ic_media_pause);
                }
            }
            break;
            case SurvivalGameHandler.STATE_BETWEENLEVELS: {
                textLevelLabel.setVisibility(View.VISIBLE);
                textGameOver.setVisibility(View.GONE);
                linearChrono.setVisibility(View.GONE);
                textCurrentScore.setVisibility(View.VISIBLE);
                textModeDescription.setVisibility(View.GONE);
                if (userType == UserType.USER_NORMAL) {
                    textPicksLeft.setVisibility(View.GONE);
                    butGameButton.setVisibility(View.VISIBLE);
                    textHighScore.setVisibility(View.GONE);
                    imgbutTogglePause.setVisibility(View.GONE);
                } else {
                    textPicksLeft.setVisibility(View.VISIBLE);
                    textScoreBonus.setVisibility(View.VISIBLE);
                    textHighScore.setVisibility(View.VISIBLE);
                    imgbutTogglePause.setVisibility(View.VISIBLE);
                    imgbutTogglePause.setImageResource(R.drawable.ic_media_play);
                }
            }
            break;
            case SurvivalGameHandler.STATE_GAMEOVER: {
                textGameOver.setVisibility(View.VISIBLE);

                linearChrono.setVisibility(View.GONE);
                textPicksLeft.setVisibility(View.GONE);
                textModeDescription.setVisibility(View.GONE);
                if (userType == UserType.USER_NORMAL) {

                    butGameButton.setVisibility(View.VISIBLE);
                    textHighScore.setVisibility(View.GONE);
                    textLevelLabel.setVisibility(View.GONE);
                    textCurrentScore.setVisibility(View.GONE);
                    imgbutTogglePause.setVisibility(View.GONE);
                } else {
                    textHighScore.setVisibility(View.VISIBLE);
                    textLevelLabel.setVisibility(View.VISIBLE);
                    textCurrentScore.setVisibility(View.VISIBLE);
                    imgbutTogglePause.setContentDescription("Start new game");
                    imgbutTogglePause.setVisibility(View.VISIBLE);
                    imgbutTogglePause.setImageResource(R.drawable.ic_media_play);
                }
            }
            break;
        }
    }

    private long getCurrentTime() {
        return SystemClock.elapsedRealtime();
    }

    private void setTogglePauseImage(boolean isPaused) {
        imgbutTogglePause.setImageResource(isPaused ? R.drawable.ic_media_play : R.drawable.ic_media_pause);
    }
}

