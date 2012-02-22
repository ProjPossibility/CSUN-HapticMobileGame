package com.Norvan.LockPick.Helpers;

import android.content.Context;
import android.os.SystemClock;
import com.Norvan.LockPick.R;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi-dev
 * Date: 2/9/12
 * Time: 4:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResponseHelper {
    private   Context context;
    private   Random rand;

    public ResponseHelper(Context context) {
        this.context = context;
        rand = new Random(SystemClock.elapsedRealtime());
    }

    public String getLevelWin0to10() {
        String[] responses = context.getResources().getStringArray(R.array.levelwin0to10);
        return responses[rand.nextInt(responses.length)];
    }

    public String getLevelWin10plus() {
        String[] responses = context.getResources().getStringArray(R.array.levelwintenplus);
        return responses[rand.nextInt(responses.length)];
    }

    public String getLevelLose5plus() {
        String[] responses = context.getResources().getStringArray(R.array.levellose5plus);
        return responses[rand.nextInt(responses.length)];
    }

    public String getTakingTooLong() {
        String[] responses = context.getResources().getStringArray(R.array.takingtoolong);
        return responses[rand.nextInt(responses.length)];
    }


    public String getLevelLoseFast() {
        String[] responses = context.getResources().getStringArray(R.array.levellosefast);
        return responses[rand.nextInt(responses.length)];
    }



}
