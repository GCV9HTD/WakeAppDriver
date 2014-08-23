package com.wakeappdriver.tests.indicators;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.wakeappdriver.framework.dto.FrameAnalyzerResult;
import com.wakeappdriver.framework.implementations.indicators.PerclosIndicator;

import android.test.AndroidTestCase;

public class PerclosIndicatorTests  extends AndroidTestCase{
	PerclosIndicator indicator;
    Random rand = new Random();


	@Before
	public void setUp() throws Exception {
		indicator = new PerclosIndicator();
	}

	@Test
	public void testPerclosIndicatorUnderMinSamples() {
		//test perclos indicator after getting less than "min smaples" valid frames (50)
		Queue <FrameAnalyzerResult> results = new ArrayDeque<FrameAnalyzerResult>();
		for (int i = 0; i < 40; i ++){
			results.add(new FrameAnalyzerResult(getRandom(0, 1), 0));
		}
		indicator.calculate(results);
		
		assertEquals(indicator.getValue(), null);
	}

	@Test
	public void testPerclosIndicatorCalc() {
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
		
		//result is closed/valid (60/80)
		assertEquals(indicator.getValue(), 0.75);
	}
	
	private double getRandom(double min, double max) {
		return (min + (max - min) * rand.nextDouble());
	}
}
