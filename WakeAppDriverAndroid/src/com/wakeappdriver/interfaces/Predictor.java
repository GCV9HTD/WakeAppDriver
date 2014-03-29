package com.wakeappdriver.interfaces;

import java.util.HashMap;
import com.wakeappdriver.enums.Enums.*;

public interface Predictor {

	public Double predictDrowsiness(HashMap<IndicatorType,Indicator> indicators);
}
