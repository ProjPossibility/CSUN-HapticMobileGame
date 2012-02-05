package com.example;

import java.util.HashMap;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

public class TTSHandler {
    private TextToSpeech mTts;
    Context context;
    boolean goodToGo = false;
    String notReadyBuffer = null;

    public TTSHandler(Context context) {
        this.context = context;
        mTts = new TextToSpeech(context, mInitListener);

    }

    private TextToSpeech.OnInitListener mInitListener = new TextToSpeech.OnInitListener() {

        public void onInit(int status) {

            // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
            if (status == TextToSpeech.SUCCESS) {

                int result = mTts.setLanguage(Locale.US);

                // Try this someday for some interesting results.
                // int result mTts.setLanguage(Locale.FRANCE);
                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {


                } else {
                    goodToGo = true;
                    if (notReadyBuffer != null) {
                        speakPhrase(notReadyBuffer);
                    }
                }
            } else {
                // Initialization failed.
                Log.i("AMP", "Could not initialize TextToSpeech.");
            }
        }

    };



    public void speakPhrase(String phrase) {
        if (goodToGo) {
            mTts.speak(phrase, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            notReadyBuffer = phrase;

        }
    }

    public void shutUp(){
        mTts.stop();
    }

    public void shutDownTTS() {
        try {
            mTts.shutdown();
        } catch (Exception e) {
        }
    }


}
