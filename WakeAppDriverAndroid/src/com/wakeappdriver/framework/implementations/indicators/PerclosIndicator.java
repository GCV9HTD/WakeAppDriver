package com.wakeappdriver.framework.implementations.indicators;

import java.util.Queue;

import android.util.Log;

import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.configuration.Enums.*;
import com.wakeappdriver.framework.dto.FrameAnalyzerResult;
import com.wakeappdriver.framework.interfaces.Indicator;

public class PerclosIndicator implements Indicator{
    private static final String TAG = "WAD";

    public static final double PERCLOS_THRESHOLD = 0.2;
    
	private Double value;
	private int minSamples;
	
	public PerclosIndicator(){
		this.minSamples = ConfigurationParameters.getMinSamples();
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
				if (result.getValue() <= PERCLOS_THRESHOLD) {
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
		if (type.equals(FrameAnalyzerType.PERCENT_COVERED))
			return true;
		return false;
	}

}
