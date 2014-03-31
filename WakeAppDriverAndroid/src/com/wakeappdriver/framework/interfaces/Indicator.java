package com.wakeappdriver.framework.interfaces;

import java.util.Queue;

import com.wakeappdriver.configuration.Enums.*;
import com.wakeappdriver.framework.dto.FrameAnalyzerResult;

public interface Indicator {
	public Double getValue();
	public void calculate(Queue<FrameAnalyzerResult> results);
	public boolean interested(FrameAnalyzerType type);
}
