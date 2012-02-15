package com.Norvan.LockPick;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.Norvan.LockPick.Helpers.AnalyticsHelper;
import com.Norvan.LockPick.R;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi-dev
 * Date: 2/9/12
 * Time: 5:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class Instructions extends Activity {
    VibrationHandler vibrationHandler;
    AnalyticsHelper analyticsHelper;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instructionslayout);
        TextView textInstructions = (TextView) findViewById(R.id.textInstructions);
        textInstructions.setText(getResources().getString(R.string.instructionsNormal));
        if (SharedPreferencesHandler.getUserType(this) == SharedPreferencesHandler.USER_BLIND) {
            TTSHandler tts = new TTSHandler(this);
            tts.speakPhrase(getResources().getString(R.string.instructionsBlind));

        } else if (SharedPreferencesHandler.getUserType(this) == SharedPreferencesHandler.USER_DEAFBLIND) {
            vibrationHandler = new VibrationHandler(this);
            vibrationHandler.playString(getResources().getString(R.string.instructionsDeafBlind));

        }
        analyticsHelper = new AnalyticsHelper(this);
        analyticsHelper.startHelpScreenActivity();
    }

    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        if (vibrationHandler != null) {
            vibrationHandler.stopVibrate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        analyticsHelper.exitHelpScreen();
    }
}