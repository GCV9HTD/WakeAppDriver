package com.wakeappdriver.tasks;

import java.util.List;

import org.opencv.android.CameraBridgeViewBase.ListItemAccessor;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import android.util.Log;
import com.wakeappdriver.classes.CapturedFrame;
import com.wakeappdriver.classes.FpsCounter;
import com.wakeappdriver.classes.FrameQueueManager;

public class CameraTask implements Runnable {
	/** tag for logging*/
	public static final String TAG = "wad";

	/** queueManager for handling received frames*/
	private FrameQueueManager queueManager;

	/** time in milliseconds for camera to sleep between captures*/
	private int cameraSleep;

	/** indicating whether the task should stop*/
	private boolean stopThread;

	/** videoCapture object for capturing images from camera*/
	private VideoCapture mCamera;

	private FpsCounter fpsCounter;
	
	private int cameraId;
	private int frameWidth;
	private int frameHeight;

	public CameraTask(int cameraSleep, int cameraId, FrameQueueManager queueManager, int frameWidth, int frameHeight) {
		this.cameraId = cameraId;
		this.cameraSleep = cameraSleep;
		this.queueManager = queueManager;
		this.frameWidth = frameWidth;
		this.frameHeight = frameHeight;
		
		this.fpsCounter = new FpsCounter();
	}

	private boolean connectCamera() {

		/* 1. We need to instantiate camera
		 * 2. We need to start thread which will be getting frames
		 */
		/* First step - initialize camera connection */

		//    	Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		//    	frameWidth = d.getWidth();
		//    	frameHeight = d.getHeight();
		//    	
		if (!initializeCamera(frameWidth, frameHeight))
			return false;

		return true;
	}


	@SuppressWarnings("unused")
	private void disconnectCamera() {

		stopThread = true;
		releaseCamera();
	}

	public static class OpenCvSizeAccessor implements ListItemAccessor {

		public int getWidth(Object obj) {
			Size size  = (Size)obj;
			return (int)size.width;
		}

		public int getHeight(Object obj) {
			Size size  = (Size)obj;
			return (int)size.height;
		}

	}

	private boolean initializeCamera(int width, int height) {
		Size frameSize;
		synchronized (this) {

			if (cameraId == -1)
				mCamera = new VideoCapture(Highgui.CV_CAP_ANDROID);
			else
				mCamera = new VideoCapture(Highgui.CV_CAP_ANDROID + cameraId);

			if (mCamera == null)
				return false;

			if (mCamera.isOpened() == false)
				return false;

			java.util.List<Size> sizes = mCamera.getSupportedPreviewSizes();

			/* Select the size that fits surface considering maximum size allowed */
			frameSize = calculateCameraFrameSize(sizes, new OpenCvSizeAccessor(), width, height);

			mCamera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, frameSize.width);
			mCamera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, frameSize.height);
		}

		Log.i(TAG, "Selected camera frame size = (" + frameSize.width + ", " + frameSize.height + ")");

		return true;
	}

	private Size calculateCameraFrameSize(List<?> supportedSizes, ListItemAccessor accessor, int surfaceWidth, int surfaceHeight) {
		int calcWidth = 0;
		int calcHeight = 0;

		for (Object size : supportedSizes) {
			int width = accessor.getWidth(size);
			int height = accessor.getHeight(size);

			if (width <= surfaceWidth && height <= surfaceHeight) {
				if (width >= calcWidth && height >= calcHeight) {
					calcWidth = (int) width;
					calcHeight = (int) height;
				}
			}
		}

		return new Size(calcWidth, calcHeight);
	}


	private void releaseCamera() {
		synchronized (this) {
			if (mCamera != null) {
				mCamera.release();
			}
		}
	}

	@Override
	public void run() {

		Mat rgbaDummy = new Mat();
		Mat grayDummy = new Mat();

		Log.e(TAG, "running");

		if (!this.connectCamera()){
			Log.e(TAG, "Count not connect camera, stopping");
			return;
		}
		long time = 0;
		while(true){
		/**
			try {
				Thread.sleep(cameraSleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
			**/
			long currentTime = System.nanoTime() ;
			long newTime = currentTime - time;
			Log.d(TAG, 1000000/(newTime) + "");
			time = currentTime ;

			/**
			if(!stopThread){
				//take picture
				if (!mCamera.grab()) {
					Log.e(TAG, "Camera frame grab failed");
					break;
				}
				mCamera.retrieve(rgbaDummy, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
				mCamera.retrieve(grayDummy, Highgui.CV_CAP_ANDROID_GREY_FRAME);
				fpsCounter.measure();
				
				Log.d(TAG, fpsCounter.getFps());
				
				CapturedFrame frame = new CapturedFrame(System.nanoTime(),rgbaDummy.clone(),grayDummy.clone());
				//queueManager.putFrame(frame);
			}
			**/
		}
	}
}
