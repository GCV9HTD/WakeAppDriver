package com.wakeappdriver.implementations;

import java.util.Queue;

import android.util.Log;

import com.wakeappdriver.classes.FrameAnalyzerResult;
import com.wakeappdriver.enums.FrameAnalyzerType;
import com.wakeappdriver.interfaces.Indicator;

public class PerclosIndicator implements Indicator{
    private static final String TAG = "WAD";

	private double value;	// avg eye-closure ratio
	
	public PerclosIndicator (double value){
		this.value = value;
	}
	@Override
	public double getValue() {
		return value;
	}

	@Override
	public void calculate(Queue<FrameAnalyzerResult> results) {
		int sum = 0;
		int count = 0;
		for (FrameAnalyzerResult result : results) {
			if (result.getValue() != null) {
				count++;
				sum += result.getValue();
			}
		}
		if (count == 0) {
			value = 0;
		}
		else {
			value = sum / count;
		}
		Log.d(TAG, Thread.currentThread().getName() + " :: calculated PERCLOS");
	}

	@Override
	public boolean interested(FrameAnalyzerType type) {
		return true;
	}

}
