package com.wakeappdriver.framework;


import java.util.ArrayDeque;
import java.util.Queue;

import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.configuration.Enums.*;
import com.wakeappdriver.framework.dto.CapturedFrame;

import android.util.Log;

public class FrameQueue {
    private static final String TAG = "WAD";
    private Queue<CapturedFrame> frames;
    private FrameQueueType type;
    private int maxCapacity;	
	
	public FrameQueue(FrameQueueType type){
		Log.d(TAG, Thread.currentThread().getName() + " :: creating frame queue: capacity " + maxCapacity + ", type "+ type.name());
		this.maxCapacity = ConfigurationParameters.getMaxFrameQueueSize();
		this.type = type;
		this.frames = new ArrayDeque<CapturedFrame>();
	}
	public CapturedFrame remove(){
		CapturedFrame frame = this.frames.remove();
		Log.d(TAG, Thread.currentThread().getName() + " :: removing frame from queue: new size " + this.frames.size());
		return frame;
	}

	public boolean tryAdd(CapturedFrame frame){
		if(!isFull()){
			this.frames.add(frame);
			Log.d(TAG, Thread.currentThread().getName() + " :: add new frame to queue " +  this.type.name() + ", new size " + this.frames.size());

			return true;
		}
		Log.d(TAG, Thread.currentThread().getName() + " :: cannot add to frame queue, queue is full");
		return false;
	}

	public boolean isEmpty() {
		return frames.isEmpty();
	}
	
	public boolean isFull() {
		return this.frames.size() >= this.maxCapacity;
	}
	public boolean shouldNotifyWriter() {
		return this.frames.size() == this.maxCapacity - 1;
	}
	public boolean shouldNotifyReader() {
		return this.frames.size() == 1;
	}
	
	public String getType(){
		return this.type.name();
	}
}
