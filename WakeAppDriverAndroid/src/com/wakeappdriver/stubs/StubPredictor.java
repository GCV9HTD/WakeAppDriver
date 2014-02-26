package com.wakeappdriver.stubs;

import java.util.HashMap;
import java.util.List;

import com.wakeappdriver.classes.FrameAnalyzerResult;
import com.wakeappdriver.enums.IndicatorType;
import com.wakeappdriver.interfaces.Indicator;
import com.wakeappdriver.interfaces.Predictor;

public class StubPredictor implements Predictor {
    private static final String TAG = "WAD";

	@Override
	public Double predictDrowsiness(HashMap<IndicatorType,Indicator> indicators) {
		return 1.0;
	}

}
