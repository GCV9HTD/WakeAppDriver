package com.wakeappdriver.interfaces;

import java.util.List;

import com.wakeappdriver.classes.FrameAnalyzerResult;

public interface Predictor {

	public double predictDrowsiness(List<Indicator> indicators);
}
