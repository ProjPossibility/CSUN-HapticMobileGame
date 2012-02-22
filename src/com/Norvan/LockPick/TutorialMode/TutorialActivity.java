package com.Norvan.LockPick.TutorialMode;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.Norvan.LockPick.AnnouncementHandler;
import com.Norvan.LockPick.Helpers.UserType;
import com.Norvan.LockPick.R;
import com.Norvan.LockPick.SharedPreferencesHandler;
import com.Norvan.LockPick.TimeTrialMode.TimeTrialGameHandler;
import com.Norvan.LockPick.VibrationHandler;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi
 * Date: 2/20/12
 * Time: 11:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class TutorialActivity extends Activity {
    TutorialHandler tutorialHandler;
    Context context;
    VibrationHandler vibrationHandler;
    AnnouncementHandler announcementHandler;
    int userType;

    ImageButton imgbutContinue;
    RelativeLayout quad12, quad4;
    TextView textStepInstructions, textCurrentStep;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        userType = SharedPreferencesHandler.getUserType(context);
        if (userType == UserType.USER_NORMAL) {
            setUpNormalUI();
        } else {
            setUpAccessibleUI();
        }
        vibrationHandler = new VibrationHandler(context);
        vibrationHandler.setVibrationCompletedInterface(vibrationCompletedInterface);
        announcementHandler = new AnnouncementHandler(context, vibrationHandler);
        tutorialHandler = new TutorialHandler(context, tutorialStatusInterface, vibrationHandler);

    }

    void setUpNormalUI() {
        setContentView(R.layout.tutoriallayout);
    }

    void setUpAccessibleUI() {
        setContentView(R.layout.diagonaltutuoriallayout);
        imgbutContinue = (ImageButton) findViewById(R.id.imgbutNextStep);
        quad12 = (RelativeLayout) findViewById(R.id.relquad12);
        quad4 = (RelativeLayout) findViewById(R.id.relquad4);
        textCurrentStep = (TextView) findViewById(R.id.textTutorialStep);
        textStepInstructions = (TextView) findViewById(R.id.textTutorialInstructions);
        quad12.setOnClickListener(onClickAccessible);
        quad4.setOnClickListener(onClickAccessible);
        imgbutContinue.setOnClickListener(onClickAccessible);
        imgbutContinue.setOnLongClickListener(onLongClickAccessible);
        setStepNumberText(1);
    }

    View.OnClickListener onClickAccessible = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (quad12.equals(view)) {
                announcementHandler.speakPhrase(String.valueOf(textStepInstructions.getText()));
            }   else if (quad4.equals(view)) {
                announcementHandler.speakPhrase(String.valueOf(textCurrentStep.getText()));
            }   else if (imgbutContinue.equals(view)) {
                announcementHandler.speakPhrase("Start tutorial. Hold to select");
            }

        }
    };
    View.OnLongClickListener onLongClickAccessible = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            if (imgbutContinue.equals(view)) {
                announcementHandler.tutorialTurnPhoneOnSide();
                if (userType != UserType.USER_DEAFBLIND) {
                    goToStep(TutorialHandler.STEP_preTURNPHONEONSIDE);
                }
                imgbutContinue.setVisibility(View.GONE);
                return true;
            }
            return false;
        }
    };
    View.OnClickListener onClickNormal = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };

    void setStepNumberText(int step) {
        if (step > 0) {
            textCurrentStep.setText("Step " + String.valueOf(step) + " of 3");
        } else {
            textCurrentStep.setText("Done!");
        }
    }

    void setTextStepInstruction(String text) {
        textStepInstructions.setText(text);
    }

    void goToStep(int step) {
        switch (step) {
            case TutorialHandler.STEP_START:

                break;
            case TutorialHandler.STEP_preTURNPHONEONSIDE:
                setStepNumberText(1);
                setTextStepInstruction("Hold the phone on its side with the top facing forward, like it is a key.");
                break;
            case TutorialHandler.STEP_postTURNPHONEONSIDE:
                setStepNumberText(2);
                setTextStepInstruction("Turn the phone back and forth like a key until you feel it vibrate. Try to find the spot with the strongest vibration and hold it there. ");
                break;
            case TutorialHandler.STEP_preFINDSWEETSPOT:
                break;
            case TutorialHandler.STEP_postFINDSWEETSPOT:
                setStepNumberText(3);
                setTextStepInstruction("When you are at the point with the strongest vibration, hold down either volume button and turn the phone to open the lock.");
                break;
            case TutorialHandler.STEP_PREFORMUNLOCK:
                break;
            case TutorialHandler.STEP_postPREFORMUNLOCKwin:
                break;
            case TutorialHandler.STEP_postPREFORMUNLOCKlose:
                setTextStepInstruction("Be careful, you can break your pick if you try to open the lock in the wrong place! If you feel the phone vibrate a lot AFTER pressing the volume button, that means you are going to break the pick. Let go of the button and try again.");
                break;
            case TutorialHandler.STEP_FINISHED:
                setTextStepInstruction("Congratulations! Now go play the game!");
                goToStep(-1);
                break;
        }
        tutorialHandler.goToStep(step);

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (tutorialHandler.getCurrentStep()) {
                case TutorialHandler.STEP_START:
                    announcementHandler.tutorialTurnPhoneOnSide();
                    if (userType != UserType.USER_DEAFBLIND) {
                        goToStep(TutorialHandler.STEP_preTURNPHONEONSIDE);
                    }
                    break;
                case TutorialHandler.STEP_preTURNPHONEONSIDE:
                    break;
                case TutorialHandler.STEP_postTURNPHONEONSIDE:
                    break;
                case TutorialHandler.STEP_preFINDSWEETSPOT:
                    break;
                case TutorialHandler.STEP_postFINDSWEETSPOT:
                    break;
                case TutorialHandler.STEP_PREFORMUNLOCK:
                    break;
                case TutorialHandler.STEP_FINISHED:
                    break;


            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        if (tutorialHandler.isPolling) {
            tutorialHandler.setSensorPollingState(false);
        }
        vibrationHandler.stopVibrate();

    }

    @Override
    protected void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
        if (tutorialHandler.getCurrentStep() > TutorialHandler.STEP_START) {
            tutorialHandler.setSensorPollingState(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        if (tutorialHandler.isPolling) {
            tutorialHandler.setSensorPollingState(false);
        }
        vibrationHandler.stopVibrate();
        tutorialHandler = null;
        vibrationHandler = null;
        announcementHandler = null;

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (!event.isLongPress() && event.getRepeatCount() == 0) {
                tutorialHandler.gotKeyDown();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            tutorialHandler.gotKeyUp();
            return true;
        }
        return super.onKeyUp(keyCode, event);    //To change body of overridden methods use File | Settings | File Templates.
    }

    TutorialHandler.TutorialStatusInterface tutorialStatusInterface = new TutorialHandler.TutorialStatusInterface() {
        @Override
        public void isOnSide() {
            goToStep(TutorialHandler.STEP_postTURNPHONEONSIDE);
            announcementHandler.tutorialFindSweetSpot();
            if (userType != UserType.USER_DEAFBLIND) {
                goToStep(TutorialHandler.STEP_preFINDSWEETSPOT);
            }
        }

        @Override
        public void sweetSpotFound() {
            goToStep(TutorialHandler.STEP_postFINDSWEETSPOT);
            announcementHandler.tutorialPreformUnlock();
            if (userType != UserType.USER_DEAFBLIND) {
                goToStep(TutorialHandler.STEP_PREFORMUNLOCK);
            }
        }

        @Override
        public void preformedUnlock(boolean success) {
            if (success) {
                goToStep(TutorialHandler.STEP_postPREFORMUNLOCKwin);
                vibrationHandler.playHappyNotified();
                vibrationHandler.playHappyNotified();
                if (userType != UserType.USER_DEAFBLIND) {
                    goToStep(TutorialHandler.STEP_FINISHED);
                    announcementHandler.tutorialWin();
                }
            } else {
                goToStep(TutorialHandler.STEP_postPREFORMUNLOCKlose);

                vibrationHandler.playSadNotified();
                if (userType != UserType.USER_DEAFBLIND) {
                    announcementHandler.tutorialLose();
                    goToStep(TutorialHandler.STEP_PREFORMUNLOCK);
                }
            }
            //To change body of implemented methods use File | Settings | File Templates.
        }
    };
    VibrationHandler.VibrationCompletedInterface vibrationCompletedInterface = new VibrationHandler.VibrationCompletedInterface() {
        @Override
        public void vibrationCompleted() {
            switch (tutorialHandler.getCurrentStep()) {
                case TutorialHandler.STEP_START:
                    if (userType == UserType.USER_DEAFBLIND) {
                        goToStep(TutorialHandler.STEP_preTURNPHONEONSIDE);
                    }
                    break;
                case TutorialHandler.STEP_preTURNPHONEONSIDE:
                    break;
                case TutorialHandler.STEP_postTURNPHONEONSIDE:
                    if (userType == UserType.USER_DEAFBLIND) {
                        goToStep(TutorialHandler.STEP_preFINDSWEETSPOT);
                    }
                    break;
                case TutorialHandler.STEP_preFINDSWEETSPOT:
                    break;
                case TutorialHandler.STEP_postFINDSWEETSPOT:
                    if (userType == UserType.USER_DEAFBLIND) {
                        goToStep(TutorialHandler.STEP_PREFORMUNLOCK);
                    }
                    break;
                case TutorialHandler.STEP_PREFORMUNLOCK:
                    break;
                case TutorialHandler.STEP_FINISHED:
                    break;
                case TutorialHandler.STEP_postPREFORMUNLOCKwin:
                    if (userType == UserType.USER_DEAFBLIND) {
                        goToStep(TutorialHandler.STEP_FINISHED);
                        announcementHandler.tutorialWin();
                    }
                    break;
                case TutorialHandler.STEP_postPREFORMUNLOCKlose:
                    if (userType == UserType.USER_DEAFBLIND) {
                        announcementHandler.tutorialLose();
                        goToStep(TutorialHandler.STEP_PREFORMUNLOCK);
                    }
                    break;
            }
        }
    };
}