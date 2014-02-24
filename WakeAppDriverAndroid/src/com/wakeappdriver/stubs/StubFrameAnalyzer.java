package com.wakeappdriver.stubs;

import org.opencv.core.Mat;

import android.util.Log;

import com.wakeappdriver.classes.FrameAnalyzer;
import com.wakeappdriver.classes.FrameQueue;
import com.wakeappdriver.classes.FrameQueueManager;
import com.wakeappdriver.classes.ResultQueue;

public class StubFrameAnalyzer extends FrameAnalyzer {
    private static final String TAG = "AWD";

	public StubFrameAnalyzer(FrameQueueManager queueManager,
			FrameQueue frameQueue, ResultQueue resultQueue) {
		super(queueManager, frameQueue, resultQueue);
		Log.d(TAG, Thread.currentThread().getName() + "creating stub frame analyzer ");

	}

	public double analyze(Mat mat) {
		Log.d(TAG, Thread.currentThread().getName() + "analyzing!");

		return 0;
	}
}
