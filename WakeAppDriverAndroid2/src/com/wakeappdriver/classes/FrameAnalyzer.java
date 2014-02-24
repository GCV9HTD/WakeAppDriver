package com.wakeappdriver.classes;

import java.util.Queue;

import org.opencv.core.Mat;

import com.wakeappdriver.classes.*;

public abstract class FrameAnalyzer implements Runnable{
	FrameQueue frameQueue;
	ResultQueue resultQueue;
	
	public abstract double analyze(Mat rawData);
	
	public FrameAnalyzer(){
	}
	
	public void registerIncomingQueue(FrameQueue frameQueue){
		this.frameQueue = frameQueue;
	}
	public void registerOutgoingQueue(ResultQueue resultQueue){
		this.resultQueue = resultQueue;
	}
	
	@Override
	public void run() {
//		Double value = null;
//		while(true){
//			CapturedFrame capturedFrame = frameQueue.poll();
//			value = this.analyze(capturedFrame.getData());
//			this.resultQueue.add(new FrameAnalyzerResult(value, capturedFrame.getTimestamp()));
//			
//		}
	}
}
