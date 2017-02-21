package com.example.curtaineffectdemo.velocity;

import android.view.MotionEvent;

public interface VelocityTrackerInterface {
    public void addMovement(MotionEvent event);
    public void computeCurrentVelocity(int units);
    public float getXVelocity();
    public float getYVelocity();
    public void recycle();
}
