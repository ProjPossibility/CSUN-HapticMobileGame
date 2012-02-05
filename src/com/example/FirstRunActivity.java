package com.example;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi
 * Date: 2/5/12
 * Time: 1:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class FirstRunActivity extends Activity {
    Button butNotBlind;
    TTSHandler tts;
    Context context;
    Vibrator vibrator;
    int confirmType = -1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstrun);
        context = this;
        tts = new TTSHandler(context);
        vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        butNotBlind = (Button) findViewById(R.id.butNotBlind);
        butNotBlind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferencesHandler.setUserType(context, SharedPreferencesHandler.USER_NORMAL);
                finish();
            }
        });
        tts.speakPhrase(context.getResources().getString(R.string.firstrunBlind));
        vibrator.vibrate(MorseCodeConverter.pattern(context.getResources().getString(R.string.firstrunDeafBlind)), -1);

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (confirmType == SharedPreferencesHandler.USER_BLIND) {
                SharedPreferencesHandler.setUserType(context, SharedPreferencesHandler.USER_BLIND);
                finish();
            } else {
                confirmType = SharedPreferencesHandler.USER_BLIND;
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (confirmType == SharedPreferencesHandler.USER_DEAFBLIND) {
                SharedPreferencesHandler.setUserType(context, SharedPreferencesHandler.USER_DEAFBLIND);
                finish();
            } else {
                confirmType = SharedPreferencesHandler.USER_DEAFBLIND;
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        tts.shutDownTTS();
        vibrator.cancel();
    }
}