package com.Norvan.LockPick;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.Norvan.LockPick.Helpers.AnalyticsHelper;
import com.Norvan.LockPick.Helpers.UserType;
import com.Norvan.LockPick.Helpers.VolumeToggleHelper;
import com.Norvan.LockPick.SurvivalMode.SurvivalGameActivity;
import com.Norvan.LockPick.TimeTrialMode.TimeTrialGameActivity;
import com.Norvan.LockPick.TutorialMode.TutorialActivity;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi
 * Date: 2/8/12
 * Time: 9:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainActivity extends Activity {
    private static final int REQ_FIRSTRUNACTIVITY = 1;
    private static final int REQ_SURVIVALGAMEACTIVITY = 2;
    private static final int REQ_TIMETRIALGAMEACTIVITY = 4;
    private static final int REQ_INSTRUCTIONS = 3;

    private  int userType = 0;


    private  Button butNewSurvivalGame, butNewTimeTrialGame, butHelp, butSettings;
    private  VolumeToggleHelper volumeToggleHelper;
    private  ImageButton imgbutToggleVolume;
    private  boolean hasDoneTutorial = false;
    private  Context context;
    private  VibrationHandler vibrationHandler;
    private  AnnouncementHandler announcementHandler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userType = SharedPreferencesHandler.getUserType(this);
        boolean didGlobalLayout = false;
        context = this;
        vibrationHandler = new VibrationHandler(context);
        if (!vibrationHandler.hasVibrator()) {
            showUnsuportedDialog();
            return;
        }
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        AnalyticsHelper analyticsHelper = new AnalyticsHelper(this);
        analyticsHelper.startApp(userType);
        analyticsHelper = null;
        if (SharedPreferencesHandler.isFirstRun(this)) {
            startFirstRunActivity();
            return;
        } else {
            announcementHandler = new AnnouncementHandler(this, vibrationHandler);

        }
        if (userType == UserType.USER_NORMAL) {
            setContentView(R.layout.mainlayout);
            butNewSurvivalGame = (Button) findViewById(R.id.butMainNewSurvivalGame);
            butNewTimeTrialGame = (Button) findViewById(R.id.butMainNewTimeTrialGame);
            butHelp = (Button) findViewById(R.id.butMainHelp);
            butSettings = (Button) findViewById(R.id.butMainSettings);
            butNewSurvivalGame.setOnClickListener(onClickListener);
            butNewTimeTrialGame.setOnClickListener(onClickListener);
            butHelp.setOnClickListener(onClickListener);
            butSettings.setOnClickListener(onClickListener);
            butSettings.setText("Reset User Type");
            imgbutToggleVolume = (ImageButton) findViewById(R.id.imgbutMainVolume);
            imgbutToggleVolume.setOnClickListener(onClickListener);
            volumeToggleHelper = new VolumeToggleHelper(this, imgbutToggleVolume);

        } else if (userType == UserType.USER_DEAFBLIND || userType == UserType.USER_BLIND) {
            setContentView(R.layout.diagonalmainmenu);
            butNewSurvivalGame = (Button) findViewById(R.id.butSurvivalMode);
            butNewTimeTrialGame = (Button) findViewById(R.id.butTimeAttack);
            butHelp = (Button) findViewById(R.id.butInstructions);
            butSettings = (Button) findViewById(R.id.butResetUser);
            butNewSurvivalGame.setOnClickListener(blindOnClickListener);
            butNewTimeTrialGame.setOnClickListener(blindOnClickListener);
            butHelp.setOnClickListener(blindOnClickListener);
            butSettings.setOnClickListener(blindOnClickListener);
            butNewSurvivalGame.setOnLongClickListener(blindOnLongClickListener);
            butNewTimeTrialGame.setOnLongClickListener(blindOnLongClickListener);
            butHelp.setOnLongClickListener(blindOnLongClickListener);
            butSettings.setOnLongClickListener(blindOnLongClickListener);

        }
        Log.i("AMP", "hasGyro " + String.valueOf(SensorHandler.hasGyro(this)));

        announcementHandler.mainActivityLaunch();


    }


    private   View.OnClickListener blindOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (butNewTimeTrialGame.equals(v)) {
                announcementHandler.speakQuadrant(1);
            } else if (butNewSurvivalGame.equals(v)) {
                announcementHandler.speakQuadrant(2);
            } else if (butHelp.equals(v)) {
                announcementHandler.speakQuadrant(3);
            } else if (butSettings.equals(v)) {
                announcementHandler.speakQuadrant(4);
            }
            //To change body of implemented methods use File | Settings | File Templates.
        }
    };
    private   View.OnLongClickListener blindOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (butNewTimeTrialGame.equals(v)) {
                startTimeTrialGameActivity();
            } else if (butNewSurvivalGame.equals(v)) {
                startSurvivalGameActivity();
            } else if (butHelp.equals(v)) {
                startInstructionsActivity();
            } else if (butSettings.equals(v)) {
                showResetUserTypeDialog();
            }
            return true;  //To change body of implemented methods use File | Settings | File Templates.
        }
    };

    @Override
    protected void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
        hasDoneTutorial = SharedPreferencesHandler.hasDoneTutorial(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_FIRSTRUNACTIVITY: {
                if (resultCode == RESULT_OK) {
                    userType = SharedPreferencesHandler.getUserType(context);
                    if (userType == UserType.USER_NORMAL) {
                        setContentView(R.layout.mainlayout);
                        announcementHandler = new AnnouncementHandler(context, vibrationHandler);
                        butNewSurvivalGame = (Button) findViewById(R.id.butMainNewSurvivalGame);
                        butNewTimeTrialGame = (Button) findViewById(R.id.butMainNewTimeTrialGame);
                        butHelp = (Button) findViewById(R.id.butMainHelp);
                        butSettings = (Button) findViewById(R.id.butMainSettings);
                        butNewSurvivalGame.setOnClickListener(onClickListener);
                        butNewTimeTrialGame.setOnClickListener(onClickListener);
                        butHelp.setOnClickListener(onClickListener);
                        butSettings.setOnClickListener(onClickListener);
                        butSettings.setText("Reset User Type");
                        imgbutToggleVolume = (ImageButton) findViewById(R.id.imgbutMainVolume);
                        imgbutToggleVolume.setOnClickListener(onClickListener);
                        volumeToggleHelper = new VolumeToggleHelper(this, imgbutToggleVolume);

                    } else if (userType == UserType.USER_DEAFBLIND || userType == UserType.USER_BLIND) {
//                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        setContentView(R.layout.diagonalmainmenu);
                        announcementHandler = new AnnouncementHandler(context, vibrationHandler);
                        butNewSurvivalGame = (Button) findViewById(R.id.butSurvivalMode);
                        butNewTimeTrialGame = (Button) findViewById(R.id.butTimeAttack);
                        butHelp = (Button) findViewById(R.id.butInstructions);
                        butSettings = (Button) findViewById(R.id.butResetUser);
                        butNewSurvivalGame.setOnClickListener(blindOnClickListener);
                        butNewTimeTrialGame.setOnClickListener(blindOnClickListener);
                        butHelp.setOnClickListener(blindOnClickListener);
                        butSettings.setOnClickListener(blindOnClickListener);
                        butNewSurvivalGame.setOnLongClickListener(blindOnLongClickListener);
                        butNewTimeTrialGame.setOnLongClickListener(blindOnLongClickListener);
                        butHelp.setOnLongClickListener(blindOnLongClickListener);
                        butSettings.setOnLongClickListener(blindOnLongClickListener);
                    }

                    announcementHandler.mainActivityLaunch();
                } else {
                    startFirstRunActivity();
                }
            }
            break;
            case REQ_SURVIVALGAMEACTIVITY: {

                announcementHandler.mainActivityLaunch();

            }
            break;
            case REQ_TIMETRIALGAMEACTIVITY: {
                announcementHandler.mainActivityLaunch();
            }
            case REQ_INSTRUCTIONS: {
                announcementHandler.mainActivityLaunch();
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);    //To change body of overridden methods use File | Settings | File Templates.
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        vibrationHandler.stopVibrate();
        announcementHandler.masterShutDown();
    }

    private   View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (imgbutToggleVolume.equals(v)) {
                volumeToggleHelper.toggleMute();
            } else if (butNewSurvivalGame.equals(v)) {
                startSurvivalGameActivity();
            } else if (butNewTimeTrialGame.equals(v)) {
                startTimeTrialGameActivity();
            } else if (butHelp.equals(v)) {
                startInstructionsActivity();
            } else if (butSettings.equals(v)) {
                showResetUserTypeDialog();
            }
        }
    };


    private void startSurvivalGameActivity() {
        if (hasDoneTutorial) {
            announcementHandler.shutUp();
            startActivityForResult(new Intent(context, SurvivalGameActivity.class), REQ_SURVIVALGAMEACTIVITY);
        } else {
            showTutorialSuggestionDialog(true);
        }
    }

    private void startTimeTrialGameActivity() {
        if (hasDoneTutorial) {
            announcementHandler.shutUp();
            startActivityForResult(new Intent(context, TimeTrialGameActivity.class), REQ_TIMETRIALGAMEACTIVITY);
        } else {
            showTutorialSuggestionDialog(false);
        }
    }

    private void startFirstRunActivity() {
        startActivityForResult(new Intent(context, FirstRunActivity.class), REQ_FIRSTRUNACTIVITY);
    }

    private void startInstructionsActivity() {
        announcementHandler.shutUp();
        startActivityForResult(new Intent(context, TutorialActivity.class), REQ_INSTRUCTIONS);
    }

    private void showUnsuportedDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Error!");
        adb.setMessage("Your device does not have a vibrator, which is required for the game.");
        adb.setCancelable(false);
        adb.setPositiveButton("Quit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        adb.create().show();
    }

    private void showResetUserTypeDialog() {

        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle("Alert");
        adb.setMessage("Reset the user type? You will have to restart the app if you do.");
        adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferencesHandler.clearUserType(context);
                finish();
            }
        });
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


            }
        });

        if (userType != UserType.USER_NORMAL) {
            announcementHandler.announceResetUserType();
            adb.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {

                        if (event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.ACTION_MULTIPLE) {

                        } else if (event.getAction() == KeyEvent.ACTION_UP) {
                            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                                dialog.dismiss();
                                SharedPreferencesHandler.clearUserType(context);
                                finish();
                            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                                dialog.dismiss();

                            }

                        }
                        return true;
                    }
                    return false;  //To change body of implemented methods use File | Settings | File Templates.
                }
            });

        }
        adb.create().show();


    }


    private void showTutorialSuggestionDialog(boolean isSurvival) {
        final boolean survivalMode = isSurvival;
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle("Warning");
        adb.setMessage("This game is based entirely on haptic feedback. Would you like to go through the brief (recommended) tutorial?");
        adb.setCancelable(true);
        adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startInstructionsActivity();
            }
        });

        adb.setNegativeButton("Skip", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                announcementHandler.shutUp();
                if (survivalMode) {
                    startActivityForResult(new Intent(context, SurvivalGameActivity.class), REQ_SURVIVALGAMEACTIVITY);
                } else {
                    startActivityForResult(new Intent(context, TimeTrialGameActivity.class), REQ_TIMETRIALGAMEACTIVITY);
                }
            }
        });
        if (userType != UserType.USER_NORMAL) {
            announcementHandler.announceTutorialSuggestion();
            adb.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {

                        if (event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.ACTION_MULTIPLE) {

                        } else if (event.getAction() == KeyEvent.ACTION_UP) {
                            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                                dialog.dismiss();
                                startInstructionsActivity();
                            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                                dialog.dismiss();
                                announcementHandler.shutUp();
                                if (survivalMode) {
                                    startActivityForResult(new Intent(context, TimeTrialGameActivity.class), REQ_TIMETRIALGAMEACTIVITY);
                                } else {
                                    startActivityForResult(new Intent(context, TimeTrialGameActivity.class), REQ_TIMETRIALGAMEACTIVITY);
                                }
                            }

                        }
                        return true;
                    }
                    return false;  //To change body of implemented methods use File | Settings | File Templates.
                }
            });

        }
        adb.create().show();
    }


}