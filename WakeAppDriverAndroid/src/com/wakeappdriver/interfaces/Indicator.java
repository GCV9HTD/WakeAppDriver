package com.wakeappdriver.interfaces;

import java.util.List;
import java.util.Queue;

import com.wakeappdriver.classes.FrameAnalyzerResult;
import com.wakeappdriver.enums.FrameAnalyzerType;

public interface Indicator {
	public double getValue();
	public void calculate(Queue<FrameAnalyzerResult> results);
	public boolean interested(FrameAnalyzerType type);
}
