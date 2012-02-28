package com.Norvan.LockPick;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import com.Norvan.LockPick.Helpers.UserType;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi
 * Date: 2/5/12
 * Time: 1:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class FirstRunActivity extends Activity {
    private  Button butNormalMode, butBlindMode, butBlindDeafMode;
    private   TTSHandler tts;
    private   Context context;
    private  Vibrator vibrator;
    private    Handler mHandler;
    private boolean hasSelectedUserType =false;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstrun);
        context = this;
        tts = new TTSHandler(context);
        vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        butNormalMode = (Button) findViewById(R.id.butNormalMode);
        butNormalMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferencesHandler.setUserType(context, UserType.USER_NORMAL);
                setResult(RESULT_OK);
                finish();
            }
        });

        butBlindMode = (Button) findViewById(R.id.butBlindMode);
        butBlindMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferencesHandler.setUserType(context, UserType.USER_BLIND);
                setResult(RESULT_OK);
                finish();
            }
        });
        butBlindDeafMode = (Button) findViewById(R.id.butBlindDeafMode);
        butBlindDeafMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferencesHandler.setUserType(context, UserType.USER_DEAFBLIND);
                setResult(RESULT_OK);
                finish();
            }
        });

          mHandler = new Handler();
        mHandler.postDelayed(playAccessibleInfo, 5000);
    }

    private   Runnable playAccessibleInfo = new Runnable() {
        @Override
        public void run() {
            tts.speakPhrase(context.getResources().getString(R.string.firstrunBlind));
            vibrator.vibrate(MorseCodeConverter.pattern(context.getResources().getString(R.string.firstrunDeafBlind)), -1);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (event.isLongPress()) {
                hasSelectedUserType = true;
                SharedPreferencesHandler.setUserType(context, UserType.USER_BLIND);
                setResult(RESULT_OK);
                KeyEvent keyReleasedEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_UP);
                onKeyUp(KeyEvent.KEYCODE_VOLUME_UP, keyReleasedEvent);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (event.isLongPress()) {
                hasSelectedUserType=true;
                SharedPreferencesHandler.setUserType(context, UserType.USER_DEAFBLIND);
                setResult(RESULT_OK);
                KeyEvent keyReleasedEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_DOWN);
                onKeyUp(KeyEvent.KEYCODE_VOLUME_DOWN, keyReleasedEvent)  ;
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (hasSelectedUserType) {
                finish();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        vibrator.cancel();

        mHandler.removeCallbacks(playAccessibleInfo);

    }
}