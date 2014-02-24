package com.wakeappdriver.stubs;

import java.util.List;

import com.wakeappdriver.classes.FrameAnalyzerResult;
import com.wakeappdriver.interfaces.Indicator;
import com.wakeappdriver.interfaces.Predictor;

public class StubPredictor implements Predictor {
    private static final String TAG = "AWD";

	@Override
	public double predictDrowsiness(List<Indicator> indicators) {
		return 1;
	}

}
