package com.wakeappdriver.framework;

import java.text.DecimalFormat;

import org.opencv.core.Core;

public class FpsCounter {
    private static final int    STEP              = 20;
    private static final DecimalFormat FPS_FORMAT = new DecimalFormat("0.00");

    private int                 mFramesCouner;
    private double              mFrequency;
    private long                mprevFrameTime;
    private String              mStrfps;
    boolean                     mIsInitialized = false;


    public void init() {
        mFramesCouner = 0;
        mFrequency = Core.getTickFrequency();
        mprevFrameTime = Core.getTickCount();
        mStrfps = "";

    }

    public void measure() {
        if (!mIsInitialized) {
            init();
            mIsInitialized = true;
        } else {
            mFramesCouner++;
            if (mFramesCouner % STEP == 0) {
                long time = Core.getTickCount();
                double fps = STEP * mFrequency / (time - mprevFrameTime);
                mprevFrameTime = time;
                mStrfps = FPS_FORMAT.format(fps) + " FPS";
            }
        }
    }
    
    public String getFps(){
    	return mStrfps;
    }
}
