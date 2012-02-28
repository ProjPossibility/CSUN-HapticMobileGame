package com.Norvan.LockPick;


import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/**
 * @author Norvan Gorgi
 *         Abstracts out all the functions related with communicating with the text to speech engine.
 */
public class TTSHandler {
    private TextToSpeech mTts;
    private Context context;
    private boolean goodToGo = false;
    private String notReadyBuffer = null;

    public TTSHandler(Context context) {
        this.context = context;
        mTts = new TextToSpeech(context, mInitListener);

    }

    private TextToSpeech.OnInitListener mInitListener = new TextToSpeech.OnInitListener() {

        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {

                int result = mTts.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                } else {
                    goodToGo = true;
                    if (notReadyBuffer != null) {
                        speakPhrase(notReadyBuffer);
                    }
                }
            } else {

            }
        }

    };

    /**
     * Speaks a phrase at the normal pace.
     *
     * @param phrase The phrase to be spoken
     */

    public void speakPhrase(String phrase) {
        if (goodToGo) {
            mTts.setSpeechRate(1);
            mTts.speak(phrase, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            notReadyBuffer = phrase;
        }
    }

    /**
     * Stops any active utternace
     */

    public void shutUp() {
        mTts.stop();
    }

    public void shutDownTTS() {
        try {
            mTts.shutdown();
        } catch (Exception e) {
        }
    }

    /**
     * Speaks a phrase at 1.3x the normal pace.
     *
     * @param phrase The phrase to be spoken
     */
    public void speakFast(String phrase) {
        if (goodToGo) {
            mTts.setSpeechRate(1.3f);
            mTts.speak(phrase, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            notReadyBuffer = phrase;

        }
    }


}
