package com.wakeappdriver.framework.implementations.predictors;

import java.util.HashMap;

import com.wakeappdriver.framework.interfaces.Indicator;
import com.wakeappdriver.framework.interfaces.Predictor;
import com.wakeappdriver.configuration.Enums.*;

public class StubPredictor implements Predictor {
    @Override
	public Double predictDrowsiness(HashMap<IndicatorType,Indicator> indicators) {
		return 1.0;
	}

}
