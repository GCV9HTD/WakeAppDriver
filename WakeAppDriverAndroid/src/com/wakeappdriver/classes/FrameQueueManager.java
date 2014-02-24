package com.wakeappdriver.classes;

import java.util.List;

import android.util.Log;

public class FrameQueueManager  {
    private static final String TAG = "AWD";

	private List<FrameQueue> frameQueues;

	public FrameQueueManager(List<FrameQueue> frameQueues){
		Log.d(TAG, Thread.currentThread().getName() + "::creating new queue manager");

		this.frameQueues = frameQueues;
	}

	public synchronized CapturedFrame PopFrame(FrameQueue queue){
		CapturedFrame frame = null;
		Log.d(TAG, Thread.currentThread().getName() + "::trying to get a frame from queue " + queue.getType());

		try {
			while(queue.isEmpty()){
				Log.d(TAG, Thread.currentThread().getName() + "::queue " + queue.getType()+"is empty, going to sleep ");
				this.wait();
			}
			frame = queue.remove();
			Log.d(TAG, Thread.currentThread().getName() + "::got frame from queue" + queue.getType());

			if (queue.shouldNotifyWriter()){
				Log.d(TAG, Thread.currentThread().getName() + "::notifying all");
				this.notifyAll();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return frame;

	}
	public synchronized void putFrame(CapturedFrame frame){
		try{
			Log.d(TAG, Thread.currentThread().getName() + "::trying to put a frame in all queues ");
			while(allQueuesFull()){
				Log.d(TAG, Thread.currentThread().getName() + "::all queues full, going to sleep ");

				this.wait();
			}
			
			Log.d(TAG, Thread.currentThread().getName() + "::adding frame to all frame queues ");
			for (FrameQueue queue : frameQueues){
				queue.tryAdd(frame);
			}
			
			if(this.shouldNotifyReaders()){
				Log.d(TAG, Thread.currentThread().getName() + "::notiying analyzers");

				this.notifyAll();
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private boolean allQueuesFull(){
		boolean allFull = true;
		for (FrameQueue queue : frameQueues){
			if(!queue.isFull()){
				allFull = false;
				break;
			}
		}
		return allFull;
	}
	
	private boolean shouldNotifyReaders() {
		boolean notify = false;
		for (FrameQueue queue : frameQueues){
			if(queue.shouldNotifyReader()){
				notify = true;
				break;
			}
		}
		return notify;
	}

}
