package com.wakeappdriver.framework.implementations.analyzers;

import org.opencv.core.Mat;

import android.util.Log;

import com.wakeappdriver.framework.FrameQueue;
import com.wakeappdriver.framework.FrameQueueManager;
import com.wakeappdriver.framework.ResultQueue;
import com.wakeappdriver.framework.dto.CapturedFrame;
import com.wakeappdriver.framework.interfaces.FrameAnalyzer;

public class StubFrameAnalyzer extends FrameAnalyzer {
    private static final String TAG = "WAD";

	public StubFrameAnalyzer(FrameQueueManager queueManager,
			FrameQueue frameQueue, ResultQueue resultQueue) {
		super(queueManager, frameQueue, resultQueue);
		Log.d(TAG, Thread.currentThread().getName() + "creating stub frame analyzer ");

	}

	@Override
	public Double analyze(CapturedFrame capturedFrame) {
		Log.d(TAG, Thread.currentThread().getName() + "analyzing!");

		return (double) 0;
	}

	@Override
	public Mat visualAnalyze(CapturedFrame capturedFrame) {
		// TODO Auto-generated method stub
		return null;
	}
}
