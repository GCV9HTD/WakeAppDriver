package com.wakeappdriver.stubs;

import java.util.Queue;

import com.wakeappdriver.classes.FrameAnalyzerResult;
import com.wakeappdriver.interfaces.Indicator;
import com.wakeappdriver.enums.Enums.*;

public class StubIndicator implements Indicator{
    private Double value = 0.0;
	
	public StubIndicator (double value){
		this.value = value;
	}
	@Override
	public Double getValue() {
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
