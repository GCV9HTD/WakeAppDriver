package com.wakeappdriver.classes;

import java.util.Queue;

public class FrameQueue {
	
	Queue<CapturedFrame> frames;
	int maxCapacity;
	int micCapacity;
	FrameQueueManager queueManager;
	
	public CapturedFrame poll(){
		return frames.poll();
	}
}
