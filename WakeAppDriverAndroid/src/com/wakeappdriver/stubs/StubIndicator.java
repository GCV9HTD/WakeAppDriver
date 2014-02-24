package com.wakeappdriver.stubs;

import java.util.List;
import java.util.Queue;

import com.wakeappdriver.classes.FrameAnalyzerResult;
import com.wakeappdriver.enums.FrameAnalyzerType;
import com.wakeappdriver.interfaces.Indicator;

public class StubIndicator implements Indicator{
    private static final String TAG = "AWD";

	private double value;
	
	public StubIndicator (double value){
		this.value = value;
	}
	@Override
	public double getValue() {
		return value;
	}

	@Override
	public void calculate(Queue<FrameAnalyzerResult> results) {
		value += 10;
		
	}

	@Override
	public boolean interested(FrameAnalyzerType type) {
		return true;
	}

}
