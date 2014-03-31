package com.wakeappdriver.framework.implementations.indicators;

import java.util.Queue;

import android.util.Log;

import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.configuration.Enums.*;
import com.wakeappdriver.framework.dto.FrameAnalyzerResult;
import com.wakeappdriver.framework.interfaces.Indicator;

public class BlinkDurationIndicator implements Indicator{
    private static final String TAG = "WAD";

	private Double value;
	private int minSamples;
	private double blinkLimit;
	
	public BlinkDurationIndicator (int minSamples){
		this.minSamples = minSamples;
		this.blinkLimit = ConfigurationParameters.getBlinkLimit();
	}
	
	@Override
	public Double getValue() {
		return value;
	}

	@Override
	public void calculate(Queue<FrameAnalyzerResult> results) {
		int blinkCounter = 0;
		long totalBlinkDuration = 0;
		long blinkStartTime = 0;
		boolean blinking = false;
		for (FrameAnalyzerResult result : results) {
			if (!blinking) {
				if (result.getValue() != null && !result.getValue().isInfinite() 
						&& !result.getValue().isNaN() && result.getValue() <= blinkLimit) {
					blinking = true;
					blinkCounter++;
					blinkStartTime = result.getTimestamp();
				}
			}
			else {
				if (result.getValue() == null || result.getValue().isInfinite() 
						|| result.getValue().isNaN() || result.getValue() > blinkLimit) {	// stop blinking
					blinking = false;
					totalBlinkDuration += result.getTimestamp() - blinkStartTime;
				}
			}
		}
		
		if (blinkCounter < minSamples) {
			value = null;
		}
		else {
			value = (double)totalBlinkDuration / blinkCounter;
		}
		Log.d(TAG, Thread.currentThread().getName() + " :: calculated AvgBlinkDuration: " + value + ", blinkCounter = " + blinkCounter + ", totalBlinkDuration = " + totalBlinkDuration);
	}

	@Override
	public boolean interested(FrameAnalyzerType type) {
		if (type.equals(FrameAnalyzerType.PERCENT_COVERED))
			return true;
		return false;
	}

}
