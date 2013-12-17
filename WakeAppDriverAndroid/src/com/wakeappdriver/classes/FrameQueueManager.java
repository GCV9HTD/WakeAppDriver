package com.wakeappdriver.classes;

import java.util.List;

public class FrameQueueManager  {
	private List<FrameQueue> frameQueue;
	private boolean isFull;



	public List<FrameQueue> getFrameQueue() {
		return frameQueue;
	}
	public void setFrameQueue(List<FrameQueue> frameQueue) {
		this.frameQueue = frameQueue;
	}
	public synchronized boolean isFull() {
		return isFull;
	}
	public synchronized void unsetFull() {
		this.isFull = false;
	}
	public synchronized void setFull() {
		this.isFull = true;
	}

	public synchronized void waitForAnalyzer(){
		try{
			while(isFull){
				this.wait();
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
}
