package com.wakeappdriver.framework.implementations.predictors;
import java.util.HashMap;
import java.util.Map.Entry;

import android.preference.PreferenceManager;
import android.util.Log;

import com.wakeappdriver.framework.interfaces.Indicator;
import com.wakeappdriver.framework.interfaces.Predictor;
import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.configuration.Enums.*;

public class WakeAppPredictor implements Predictor {
    private static final String TAG = "WAD";

	@Override
	public Double predictDrowsiness(HashMap<IndicatorType, Indicator> indicators) {
		logWindowResult(indicators);
		Double perclos_val = indicators.get(IndicatorType.PERCLOS).getValue();
		if(perclos_val != null)
			perclos_val = perclos_val * (double)(10/3);
		
		return perclos_val;
	}
	
	private void logWindowResult(HashMap<IndicatorType, Indicator> indicators) {
		String record = "Result: timestamp " + System.nanoTime();
		for (Entry<IndicatorType, Indicator> indicator : indicators.entrySet()) {
			record += (" " + indicator.getKey().toString() + " " + indicator.getValue().getValue());
		}
		Log.i(TAG, Thread.currentThread().getName() + " :: " + record);
	}

}
