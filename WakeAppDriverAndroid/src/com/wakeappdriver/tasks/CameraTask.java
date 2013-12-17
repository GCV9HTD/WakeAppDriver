package com.wakeappdriver.tasks;

import android.hardware.Camera;

import com.wakeappdriver.classes.FrameQueueManager;

public class CameraTask implements Runnable {
	private int cameraSleep;
	private FrameQueueManager queueManager;
	private Camera camera;

	public CameraTask(int cameraSleep, FrameQueueManager queueManager){
		this.cameraSleep = cameraSleep;
		this.queueManager = queueManager;
		camera = Camera.open(1);
	}
	
	@Override
	public void run() {
		while(true){
			try {
				Thread.sleep(cameraSleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
	}
}
