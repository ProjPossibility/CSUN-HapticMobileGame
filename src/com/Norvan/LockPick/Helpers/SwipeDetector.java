package com.Norvan.LockPick.Helpers;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi
 * Date: 2/19/12
 * Time: 7:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class SwipeDetector extends GestureDetector.SimpleOnGestureListener {
    private SwipeDetectorInterface swipeDetectorInterface;

    public SwipeDetector(SwipeDetectorInterface swipeDetectorInterface) {
        this.swipeDetectorInterface = swipeDetectorInterface;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        int X = (int) velocityX;
        int Y = (int) velocityY;
        int absX = Math.abs(X);
        int absY = Math.abs(Y);

        if (absX > absY && absX > 400) {
            if (X > 0) {
                swipeDetectorInterface.swipeRight();
            } else if (X < 0) {
                swipeDetectorInterface.swipeLeft();
            }
        } else if (absX < absY && absY > 400) {
            if (Y > 0) {
                swipeDetectorInterface.swipeDown();
            } else if (Y < 0) {
                swipeDetectorInterface.swipeUp();
            }
        }
        return true;
    }

    public interface SwipeDetectorInterface {
        public void swipeUp();

        public void swipeDown();

        public void swipeLeft();

        public void swipeRight();
    }
}
