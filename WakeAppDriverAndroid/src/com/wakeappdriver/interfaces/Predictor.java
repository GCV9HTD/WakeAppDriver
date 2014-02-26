package com.wakeappdriver.interfaces;

import java.util.HashMap;
import java.util.List;

import com.wakeappdriver.classes.FrameAnalyzerResult;
import com.wakeappdriver.enums.IndicatorType;

public interface Predictor {

	public Double predictDrowsiness(HashMap<IndicatorType,Indicator> indicators);
}
