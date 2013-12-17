package com.wakeappdriver.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.wakeappdriver.interfaces.*;

public class WindowAnalyzer {
	private List<ResultQueue> resultQueues;
	private List<Indicator> indicators;

	public WindowAnalyzer(List<ResultQueue> resultQueues, List<Indicator> indicators){
		this.resultQueues = resultQueues;
		this.indicators = new ArrayList<Indicator>();
	}
	public List<Indicator> calculateIndicators(){

		for(ResultQueue queue : resultQueues){
			Queue<FrameAnalyzerResult> results = queue.getAll(); 
			for(Indicator indicator : indicators){
				if(indicator.interested(queue.getType())){
					indicator.calculate(results);
				}
			}
		}
		return indicators;
	}
}
