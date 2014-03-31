package com.wakeappdriver.framework.interfaces;

import java.util.HashMap;

import com.wakeappdriver.configuration.Enums.*;

public interface Predictor {

	public Double predictDrowsiness(HashMap<IndicatorType,Indicator> indicators);
}
