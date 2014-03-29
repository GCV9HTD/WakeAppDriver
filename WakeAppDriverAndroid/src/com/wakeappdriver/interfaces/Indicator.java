package com.wakeappdriver.interfaces;

import java.util.Queue;

import com.wakeappdriver.classes.FrameAnalyzerResult;
import com.wakeappdriver.enums.Enums.*;

public interface Indicator {
	public Double getValue();
	public void calculate(Queue<FrameAnalyzerResult> results);
	public boolean interested(FrameAnalyzerType type);
}
