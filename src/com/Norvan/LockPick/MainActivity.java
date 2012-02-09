package com.Norvan.LockPick;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.Norvan.LockPick.Helpers.DevelopmentHelpers;
import com.Norvan.LockPick.Helpers.VolumeToggleHelper;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi
 * Date: 2/8/12
 * Time: 9:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainActivity extends Activity {
    private static final int REQ_FIRSTRUNACTIVITY = 1;
    private static final int REQ_GAMEACTIVITY = 2;

    int userType = 0;

    Button butNewGame, butHelp, butSettings;
    VolumeToggleHelper volumeToggleHelper;
    ImageButton imgbutToggleVolume;
    Context context;
    TTSHandler ttsHandler;
    VibrationHandler vibrationHandler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainlayout);
        context = this;
        butNewGame = (Button) findViewById(R.id.butMainNewGame);
        butHelp = (Button) findViewById(R.id.butMainHelp);
        butSettings = (Button) findViewById(R.id.butMainSettings);
        butNewGame.setOnClickListener(onClickListener);
        butHelp.setOnClickListener(onClickListener);
        butSettings.setOnClickListener(onClickListener);
        imgbutToggleVolume = (ImageButton) findViewById(R.id.imgbutMainVolume);
        imgbutToggleVolume.setOnClickListener(onClickListener);
        volumeToggleHelper = new VolumeToggleHelper(this, imgbutToggleVolume);
        if (SharedPreferencesHandler.isFirstRun(context)) {
            startActivityForResult(new Intent(context, FirstRunActivity.class), REQ_FIRSTRUNACTIVITY);
            return;
        }

        setUpAccessibility();

    }

    private void setUpAccessibility() {
        userType = SharedPreferencesHandler.getUserType(context);
        if (userType == SharedPreferencesHandler.USER_BLIND) {
            ttsHandler = new TTSHandler(context);
            ttsHandler.speakPhrase(getResources().getString(R.string.mainactivityBlind));
        } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
            vibrationHandler = new VibrationHandler(context);
            vibrationHandler.playString(getResources().getString(R.string.mainactivityDeafBlind));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_FIRSTRUNACTIVITY: {
                if (resultCode == RESULT_OK) {
                    setUpAccessibility();
                } else {
                    startFirstRunActivity();
                }
            }
            break;
            case REQ_GAMEACTIVITY: {
                if (SharedPreferencesHandler.isFirstRun(context)) {
                    startFirstRunActivity();
                } else {
                    if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
                        finish();
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (event.getRepeatCount() == 0 && !event.isLongPress()) {
                if (userType == SharedPreferencesHandler.USER_BLIND) {
                    startGameActivity();
                } else if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                        startGameActivity();
                    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                        vibrationHandler.playString(getResources().getString(R.string.instructionsDeafBlind));
                    }
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return true;
        }
        return super.onKeyUp(keyCode, event);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (imgbutToggleVolume.equals(v)) {
                volumeToggleHelper.toggleMute();
            } else if (butNewGame.equals(v)) {
                startGameActivity();
            } else if (butHelp.equals(v)) {
                DevelopmentHelpers.toastUnsupported(context);
            } else if (butSettings.equals(v)) {
                DevelopmentHelpers.toastUnsupported(context);
            }
        }
    };


    private void startGameActivity() {
        startActivityForResult(new Intent(context, GameActivity.class), REQ_GAMEACTIVITY);
    }

    private void startFirstRunActivity() {
        startActivityForResult(new Intent(context, FirstRunActivity.class), REQ_FIRSTRUNACTIVITY);
    }
}