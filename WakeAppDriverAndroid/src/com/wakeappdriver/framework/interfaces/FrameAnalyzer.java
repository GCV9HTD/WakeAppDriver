package com.wakeappdriver.framework.interfaces;

import org.opencv.core.Mat;

import com.wakeappdriver.framework.FrameQueue;
import com.wakeappdriver.framework.FrameQueueManager;
import com.wakeappdriver.framework.ResultQueue;
import com.wakeappdriver.framework.dto.CapturedFrame;
import com.wakeappdriver.framework.dto.FrameAnalyzerResult;

import android.util.Log;

public abstract class FrameAnalyzer implements Runnable{
    private static final String TAG = "WAD";

	private FrameQueue frameQueue;
	private ResultQueue resultQueue;
	private FrameQueueManager queueManager;
	
	public abstract Double analyze(CapturedFrame capturedFrame);
	public abstract Mat visualAnalyze(CapturedFrame capturedFrame);	// debugging method
	
	public FrameAnalyzer(FrameQueueManager queueManager, FrameQueue frameQueue,ResultQueue resultQueue){
		this.frameQueue = frameQueue;
		this.resultQueue = resultQueue;
		this.queueManager = queueManager;
	}
	
	@Override
	public void run() {
		Log.d(TAG, Thread.currentThread().getName() + " :: starting frame analyzer ");

		Double value = null;
		while(queueManager.isAlive()){
			CapturedFrame capturedFrame = queueManager.PopFrame(frameQueue);
			value = this.analyze(capturedFrame);
			FrameAnalyzerResult frameResult = new FrameAnalyzerResult(value, capturedFrame.getTimestamp());
			this.resultQueue.add(frameResult);
			this.logFrameResult(frameResult);
			capturedFrame.destroy();
		}
	}
	
	protected void logFrameResult(FrameAnalyzerResult frameResult) {
		Log.v(TAG, Thread.currentThread().getName() + " :: Result: " + frameResult.getTimestamp() + " " + frameResult.getValue());
	}

}
