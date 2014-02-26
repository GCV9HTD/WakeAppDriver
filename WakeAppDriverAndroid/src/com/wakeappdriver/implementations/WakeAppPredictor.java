package com.wakeappdriver.implementations;

import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import android.util.Log;

import com.wakeappdriver.enums.IndicatorType;
import com.wakeappdriver.interfaces.Indicator;
import com.wakeappdriver.interfaces.Predictor;

public class WakeAppPredictor implements Predictor {
    private static final String TAG = "WAD";

	@Override
	public Double predictDrowsiness(HashMap<IndicatorType, Indicator> indicators) {
		logWindowResult(indicators);
		return indicators.get(IndicatorType.PERCLOS).getValue();
	}
	
	private void logWindowResult(HashMap<IndicatorType, Indicator> indicators) {
		String record = "Result: timestamp " + System.nanoTime();
		for (Entry<IndicatorType, Indicator> indicator : indicators.entrySet()) {
			record += (" " + indicator.getKey().toString() + " " + indicator.getValue().getValue());
		}
		Log.i(TAG, Thread.currentThread().getName() + " :: " + record);

	}

}
