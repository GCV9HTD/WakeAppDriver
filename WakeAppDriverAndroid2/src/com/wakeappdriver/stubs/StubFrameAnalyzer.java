package com.wakeappdriver.stubs;

import java.util.Queue;

import org.opencv.core.Mat;

import com.wakeappdriver.classes.FrameAnalyzer;
import com.wakeappdriver.classes.FrameAnalyzerResult;
import com.wakeappdriver.classes.FrameQueue;

public class StubFrameAnalyzer extends FrameAnalyzer {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double analyze(Mat rawData) {
		return 0;
	}

}
