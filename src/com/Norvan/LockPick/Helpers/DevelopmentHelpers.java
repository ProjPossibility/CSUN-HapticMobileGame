package com.Norvan.LockPick.Helpers;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi
 * Date: 2/8/12
 * Time: 10:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class DevelopmentHelpers {

    public static void toastUnsupported(Context context) {
        Toast.makeText(context, "unsupported", Toast.LENGTH_SHORT).show();
    }
}
