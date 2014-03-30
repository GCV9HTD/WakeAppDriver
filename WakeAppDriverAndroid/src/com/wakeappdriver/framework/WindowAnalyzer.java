package com.wakeappdriver.framework;

import java.util.HashMap;
import java.util.List;
import java.util.Queue;

import com.wakeappdriver.framework.dto.FrameAnalyzerResult;
import com.wakeappdriver.framework.interfaces.*;
import com.wakeappdriver.configuration.Enums.*;

public class WindowAnalyzer {
	private List<ResultQueue> resultQueues;
	private HashMap<IndicatorType,Indicator> indicators;

	public WindowAnalyzer(List<ResultQueue> resultQueues, HashMap<IndicatorType,Indicator> indicators){
		this.resultQueues = resultQueues;
		this.indicators = indicators;
	}
	public HashMap<IndicatorType,Indicator> calculateIndicators(){

		for(ResultQueue queue : resultQueues){
			Queue<FrameAnalyzerResult> results = queue.getAll(); 
			for(Indicator indicator : indicators.values()){
				if(indicator.interested(queue.getType())){
					indicator.calculate(results);
				}
			}
		}
		return indicators;
	}
}
