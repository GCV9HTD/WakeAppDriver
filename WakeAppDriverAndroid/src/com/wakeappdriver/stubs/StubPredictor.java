package com.wakeappdriver.stubs;

import java.util.HashMap;
import com.wakeappdriver.interfaces.Indicator;
import com.wakeappdriver.interfaces.Predictor;
import com.wakeappdriver.enums.Enums.*;

public class StubPredictor implements Predictor {
    @Override
	public Double predictDrowsiness(HashMap<IndicatorType,Indicator> indicators) {
		return 1.0;
	}

}
