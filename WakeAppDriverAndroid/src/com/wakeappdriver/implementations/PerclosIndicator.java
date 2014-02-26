package com.wakeappdriver.implementations;

import java.util.Queue;

import android.util.Log;

import com.wakeappdriver.classes.FrameAnalyzerResult;
import com.wakeappdriver.enums.FrameAnalyzerType;
import com.wakeappdriver.interfaces.Indicator;

public class PerclosIndicator implements Indicator{
    private static final String TAG = "WAD";

	private Double value;
	private int minSamples = 50;
	
	public PerclosIndicator (){
	}
	
	@Override
	public Double getValue() {
		return value;
	}

	@Override
	public void calculate(Queue<FrameAnalyzerResult> results) {
		int countValidFrames = 0;
		int countClosedFrames = 0;
		for (FrameAnalyzerResult result : results) {
			if (result.getValue() != null && !result.getValue().isInfinite() && !result.getValue().isNaN()) {
				countValidFrames++;
				if (result.getValue() <= 0.2) {
					countClosedFrames++;
				}
			}
		}
		if (countValidFrames < minSamples) {
			value = null;
		}
		else {
			value = (double)countClosedFrames / countValidFrames;
		}
		Log.d(TAG, Thread.currentThread().getName() + " :: calculated PERCLOS: " + value + ", ValidFrames = " + countValidFrames +", ClosedFrames = " + countClosedFrames);
	}

	@Override
	public boolean interested(FrameAnalyzerType type) {
		return true;
	}

}
