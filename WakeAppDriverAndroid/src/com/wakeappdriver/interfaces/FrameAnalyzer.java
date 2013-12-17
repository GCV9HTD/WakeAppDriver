package com.wakeappdriver.interfaces;

import java.util.Queue;

import com.wakeappdriver.classes.*;

public interface FrameAnalyzer extends Runnable{
	public void registerIncomingQueue(FrameQueue frameQueue);
	public void registerOutgoingQueue(ResultQueue resultQueue);
}
