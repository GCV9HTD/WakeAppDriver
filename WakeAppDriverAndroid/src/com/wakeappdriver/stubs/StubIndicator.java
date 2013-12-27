package com.wakeappdriver.stubs;

import java.util.List;
import java.util.Queue;

import com.wakeappdriver.classes.FrameAnalyzerResult;
import com.wakeappdriver.enums.FrameAnalyzerType;
import com.wakeappdriver.interfaces.Indicator;

public class StubIndicator implements Indicator{

	@Override
	public double getValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void calculate(Queue<FrameAnalyzerResult> results) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean interested(FrameAnalyzerType type) {
		// TODO Auto-generated method stub
		return false;
	}

}
