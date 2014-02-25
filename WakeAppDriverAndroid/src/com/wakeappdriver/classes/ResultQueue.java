package com.wakeappdriver.classes;

import java.util.ArrayDeque;
import java.util.Queue;

import android.util.Log;

import com.wakeappdriver.enums.FrameAnalyzerType;

public class ResultQueue {
    private static final String TAG = "WAD";

	private Queue<FrameAnalyzerResult> results;
	private FrameAnalyzerType type;
	
	public ResultQueue(FrameAnalyzerType type){
		Log.d(TAG, Thread.currentThread().getName() + " :: creating new result queue, type :"+ type.name());
		this.type = type;
		this.results = new ArrayDeque<FrameAnalyzerResult>();
	}
	
	public FrameAnalyzerType getType(){
		return type;
	}
	public synchronized Queue<FrameAnalyzerResult> getAll(){
		Log.d(TAG, Thread.currentThread().getName() + " :: retreiving all results from queue, got "+ this.results.size()+ " results");
		Queue <FrameAnalyzerResult> tmp = results;
		this.results = new ArrayDeque<FrameAnalyzerResult>();
		return tmp;
	}
	
	public synchronized void add(FrameAnalyzerResult result){
		if(this.results.size() < 100){			
			this.results.add(result);
			Log.d(TAG, Thread.currentThread().getName() + " :: adding result " + result.getValue() + " at " + result.getTimestamp() + " to queue " + this.type.name()+ ", new size :"+ results.size());
		}
	}
}

