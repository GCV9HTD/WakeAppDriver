package com.wakeappdriver.framework.implementations.indicators;

import java.util.Queue;

import com.wakeappdriver.configuration.Enums.*;
import com.wakeappdriver.framework.dto.FrameAnalyzerResult;
import com.wakeappdriver.framework.interfaces.Indicator;

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
