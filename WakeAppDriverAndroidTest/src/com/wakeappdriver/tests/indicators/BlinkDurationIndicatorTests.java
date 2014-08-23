package com.wakeappdriver.tests.indicators;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.wakeappdriver.framework.dto.FrameAnalyzerResult;
import com.wakeappdriver.framework.implementations.indicators.BlinkDurationIndicator;

import android.test.AndroidTestCase;

public class BlinkDurationIndicatorTests  extends AndroidTestCase{
	BlinkDurationIndicator indicator;
    Random rand = new Random();


	@Before
	public void setUp() throws Exception {
		indicator = new BlinkDurationIndicator(2);
	}

	@Test
	public void testBlinkDurationIndicatorUnderMinSamples() {
		//test blink duration indicator after getting less than "min smaples" blinks (2)
		Queue <FrameAnalyzerResult> results = new ArrayDeque<FrameAnalyzerResult>();
		for (int i = 0; i < 100; i ++){
			results.add(new FrameAnalyzerResult(getRandom(0.41,1), 0));
		}
		indicator.calculate(results);
		
		assertEquals(indicator.getValue(), null);
	}

	@Test
	public void testBlinkDurationIndicatorCalc() {
		//test blink duration - simulate 3 blinks - 2 sec, 3 sec, 4 sec 
		Queue <FrameAnalyzerResult> results = new ArrayDeque<FrameAnalyzerResult>();
		
		//not a blink
		for (int i = 0; i < 20; i ++){
			results.add(new FrameAnalyzerResult(getRandom(0.41, 1), i));
		}
		
		//2 sec blink
		for (int i = 20; i < 22; i ++){
			results.add(new FrameAnalyzerResult(getRandom(0, 0.4), i));
		}
		
		//not a blink
		for (int i = 22; i < 42; i ++){
			results.add(new FrameAnalyzerResult(getRandom(0.41, 1), i));
		}
		
		//3 sec blink
		for (int i = 42; i < 45; i ++){
			results.add(new FrameAnalyzerResult(getRandom(0, 0.4), i));
		}
		
		//not a blink
		for (int i = 45; i < 65; i ++){
			results.add(new FrameAnalyzerResult(getRandom(0.41, 1), i));
		}
		
		//4 sec blink
		for (int i = 65; i < 69; i ++){
			results.add(new FrameAnalyzerResult(getRandom(0, 0.4), i));
		}
		
		//not a blink
		for (int i = 69; i < 89; i ++){
			results.add(new FrameAnalyzerResult(getRandom(0.41, 1), i));
		}
		indicator.calculate(results);
		
		//result is (2 + 3 + 4)/3 = 3
		assertEquals(3.0 , indicator.getValue());
	}
	
	private double getRandom(double min, double max) {
		return (min + (max - min) * rand.nextDouble());
	}
}
