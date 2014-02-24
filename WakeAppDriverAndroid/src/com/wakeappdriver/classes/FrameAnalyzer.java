package com.wakeappdriver.classes;

import java.util.Queue;

import org.opencv.core.Mat;

import android.util.Log;
public abstract class FrameAnalyzer implements Runnable{
    private static final String TAG = "AWD";

	private FrameQueue frameQueue;
	private ResultQueue resultQueue;
	private FrameQueueManager queueManager;
	
	public abstract double analyze(Mat mat);
	
	public FrameAnalyzer(FrameQueueManager queueManager, FrameQueue frameQueue,ResultQueue resultQueue){
		this.frameQueue = frameQueue;
		this.resultQueue = resultQueue;
		this.queueManager = queueManager;
	}
	
	@Override
	public void run() {
		Log.d(TAG, Thread.currentThread().getName() + "::starting frame analyzer ");

		Double value = null;
		while(true){
			CapturedFrame capturedFrame = queueManager.PopFrame(frameQueue);
			value = this.analyze(capturedFrame.gray());
			this.resultQueue.add(new FrameAnalyzerResult(value, capturedFrame.getTimestamp()));
			
		}
	}

}
