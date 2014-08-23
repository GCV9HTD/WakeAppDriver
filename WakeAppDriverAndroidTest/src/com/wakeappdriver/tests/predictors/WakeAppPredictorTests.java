package com.wakeappdriver.tests.predictors;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.wakeappdriver.configuration.Enums.IndicatorType;
import com.wakeappdriver.framework.dto.FrameAnalyzerResult;
import com.wakeappdriver.framework.implementations.indicators.PerclosIndicator;
import com.wakeappdriver.framework.implementations.predictors.WakeAppPredictor;
import com.wakeappdriver.framework.interfaces.Indicator;

import android.test.AndroidTestCase;

public class WakeAppPredictorTests  extends AndroidTestCase{
	WakeAppPredictor predictor;
    Random rand = new Random();

	@Before
	public void setUp() throws Exception {
		predictor = new WakeAppPredictor();
	}

	@Test
	public void testWakeAppPredictorNotNull() {
		HashMap<IndicatorType, Indicator> indicators = new HashMap<IndicatorType, Indicator>();
		
		PerclosIndicator perclos = new PerclosIndicator();
		addToIndicatorNotNull(perclos);
		
		indicators.put(IndicatorType.PERCLOS, perclos);
		Double val = predictor.predictDrowsiness(indicators);
		assertEquals(val.doubleValue(), (double)(10/3) * (double)(0.75));

	}

	@Test
	public void testWakeAppPredictorNull() {
		HashMap<IndicatorType, Indicator> indicators = new HashMap<IndicatorType, Indicator>();
		
		PerclosIndicator perclos = new PerclosIndicator();
		addToIndicatorNull(perclos);
		
		indicators.put(IndicatorType.PERCLOS, perclos);
		Double val = predictor.predictDrowsiness(indicators);
		assertNull(val);
	}
	
	private void addToIndicatorNotNull(Indicator indicator){
		Queue <FrameAnalyzerResult> results = new ArrayDeque<FrameAnalyzerResult>();
		for (int i = 0; i < 60; i ++){
			//60 closed frames
			results.add(new FrameAnalyzerResult(getRandom(0, 0.19), 0));
		}
		
		for (int i = 0; i < 20; i ++){
			//20 open frames
			results.add(new FrameAnalyzerResult(getRandom(0.2, 1), 0));
		}
		
		for (int i = 0; i < 50; i ++){
			//50 null frames
			results.add(new FrameAnalyzerResult(null, 0));
		}
		indicator.calculate(results);
	}
	
	public void addToIndicatorNull(Indicator indicator) {
		//test perclos indicator after getting less than "min smaples" valid frames (50)
		Queue <FrameAnalyzerResult> results = new ArrayDeque<FrameAnalyzerResult>();
		for (int i = 0; i < 40; i ++){
			results.add(new FrameAnalyzerResult(getRandom(0, 1), 0));
		}
		indicator.calculate(results);
	}

	private double getRandom(double min, double max) {
		return (min + (max - min) * rand.nextDouble());
	}

}
