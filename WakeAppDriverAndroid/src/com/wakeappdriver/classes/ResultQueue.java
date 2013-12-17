package com.wakeappdriver.classes;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.wakeappdriver.enums.FrameAnalyzerType;

public class ResultQueue {
	private Queue<FrameAnalyzerResult> results;
	private FrameAnalyzerType type;
	
	public ResultQueue(FrameAnalyzerType type){
		this.type = type;
		this.results = new ConcurrentLinkedQueue<FrameAnalyzerResult>();
	}
	
	public FrameAnalyzerType getType(){
		return type;
	}
	public Queue<FrameAnalyzerResult> getAll(){
		ConcurrentLinkedQueue<FrameAnalyzerResult> currentResults =  new ConcurrentLinkedQueue<FrameAnalyzerResult>();
		while(!this.results.isEmpty()){
			currentResults.add(results.poll());
		}
		return currentResults;
	}
}

