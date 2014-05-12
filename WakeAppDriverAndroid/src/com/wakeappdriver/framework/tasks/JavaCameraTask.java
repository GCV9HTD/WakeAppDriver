package com.wakeappdriver.framework.tasks;

import java.util.List;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.ListItemAccessor;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.wakeappdriver.framework.FrameQueueManager;
import com.wakeappdriver.framework.dto.CapturedFrame;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.util.Log;

public class JavaCameraTask implements Runnable,PreviewCallback {

	public static final int CAMERA_ID_ANY   = -1;
	public static final int CAMERA_ID_BACK  = 99;
	public static final int CAMERA_ID_FRONT = 98;

	private static final int MAGIC_TEXTURE_ID = 10;
	private static final String TAG = "JavaCameraTask";

	private byte mBuffer[];
	private Mat[] mFrameChain;
	private int mChainIdx = 0;
	private boolean mStopThread;

	protected Camera mCamera;
	protected int mCameraIndex = 1;//CAMERA_ID_FRONT;

	protected JavaCameraFrame[] mCameraFrame;
	private SurfaceTexture mSurfaceTexture;

	private int mFrameWidth;
	private int mFrameHeight;
	protected float mScale = 0;

	FrameQueueManager queueManager;

	public JavaCameraTask(int mFrameWidth, int mFrameHeight, FrameQueueManager queueManager){
		this.mFrameWidth = mFrameWidth;
		this.mFrameHeight = mFrameHeight;
		this.queueManager = queueManager;
	}
	public static class JavaCameraSizeAccessor implements ListItemAccessor {

		public int getWidth(Object obj) {
			Camera.Size size = (Camera.Size) obj;
			return size.width;
		}

		public int getHeight(Object obj) {
			Camera.Size size = (Camera.Size) obj;
			return size.height;
		}
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
	protected boolean initializeCamera(int width, int height) {
		Log.d(TAG, "Initialize java camera");
		boolean result = true;
		synchronized (this) {
			mCamera = null;

			if (mCameraIndex == CAMERA_ID_ANY) {
				Log.d(TAG, "Trying to open camera with old open()");
				try {
					mCamera = Camera.open();
				}
				catch (Exception e){
					Log.e(TAG, "Camera is not available (in use or does not exist): " + e.getLocalizedMessage());
				}

				if(mCamera == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
					boolean connected = false;
					for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); ++camIdx) {
						Log.d(TAG, "Trying to open camera with new open(" + Integer.valueOf(camIdx) + ")");
						try {
							mCamera = Camera.open(camIdx);
							connected = true;
						} catch (RuntimeException e) {
							Log.e(TAG, "Camera #" + camIdx + "failed to open: " + e.getLocalizedMessage());
						}
						if (connected) break;
					}
				}
			} else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
					int localCameraIndex = mCameraIndex;
					if (mCameraIndex == CAMERA_ID_BACK) {
						Log.i(TAG, "Trying to open back camera");
						Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
						for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); ++camIdx) {
							Camera.getCameraInfo( camIdx, cameraInfo );
							if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
								localCameraIndex = camIdx;
								break;
							}
						}
					} else if (mCameraIndex == CAMERA_ID_FRONT) {
						Log.i(TAG, "Trying to open front camera");
						Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
						for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); ++camIdx) {
							Camera.getCameraInfo( camIdx, cameraInfo );
							if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
								localCameraIndex = camIdx;
								break;
							}
						}
					}
					if (localCameraIndex == CAMERA_ID_BACK) {
						Log.e(TAG, "Back camera not found!");
					} else if (localCameraIndex == CAMERA_ID_FRONT) {
						Log.e(TAG, "Front camera not found!");
					} else {
						Log.d(TAG, "Trying to open camera with new open(" + Integer.valueOf(localCameraIndex) + ")");
						try {
							mCamera = Camera.open(localCameraIndex);
						} catch (RuntimeException e) {
							Log.e(TAG, "Camera #" + localCameraIndex + "failed to open: " + e.getLocalizedMessage());
						}
					}
				}
			}

			if (mCamera == null)
				return false;

			/* Now set camera parameters */
			try {
				Camera.Parameters params = mCamera.getParameters();
				Log.d(TAG, "getSupportedPreviewSizes()");
				List<android.hardware.Camera.Size> sizes = params.getSupportedPreviewSizes();

				if (sizes != null) {
					/* Select the size that fits surface considering maximum size allowed */
					Size frameSize = calculateCameraFrameSize(sizes, new JavaCameraSizeAccessor(), width, height);

					params.setPreviewFormat(ImageFormat.NV21);
					Log.d(TAG, "Set preview size to " + Integer.valueOf((int)frameSize.width) + "x" + Integer.valueOf((int)frameSize.height));
					params.setPreviewSize((int)frameSize.width, (int)frameSize.height);

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
						params.setRecordingHint(true);

					List<String> FocusModes = params.getSupportedFocusModes();
					if (FocusModes != null && FocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
					{
						params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
					}

					mCamera.setParameters(params);
					params = mCamera.getParameters();

					mFrameWidth = params.getPreviewSize().width;
					mFrameHeight = params.getPreviewSize().height;

					//                    if ((getLayoutParams().width == LayoutParams.MATCH_PARENT) && (getLayoutParams().height == LayoutParams.MATCH_PARENT))
					mScale = Math.min(((float)height)/mFrameHeight, ((float)width)/mFrameWidth);
					//                    else
					//                        mScale = 0;

					//                    if (mFpsMeter != null) {
					//                        mFpsMeter.setResolution(mFrameWidth, mFrameHeight);
					//                    }

					int size = mFrameWidth * mFrameHeight;
					size  = size * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
					mBuffer = new byte[size];

					mCamera.addCallbackBuffer(mBuffer);
					mCamera.setPreviewCallbackWithBuffer(this);

					mFrameChain = new Mat[2];
					mFrameChain[0] = new Mat(mFrameHeight + (mFrameHeight/2), mFrameWidth, CvType.CV_8UC1);
					mFrameChain[1] = new Mat(mFrameHeight + (mFrameHeight/2), mFrameWidth, CvType.CV_8UC1);

					//                    AllocateCache();

					mCameraFrame = new JavaCameraFrame[2];
					mCameraFrame[0] = new JavaCameraFrame(mFrameChain[0], mFrameWidth, mFrameHeight);
					mCameraFrame[1] = new JavaCameraFrame(mFrameChain[1], mFrameWidth, mFrameHeight);

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						mSurfaceTexture = new SurfaceTexture(MAGIC_TEXTURE_ID);
						mCamera.setPreviewTexture(mSurfaceTexture);
					} else
						mCamera.setPreviewDisplay(null);

					/* Finally we are ready to start the preview */
					Log.d(TAG, "startPreview");
					mCamera.startPreview();
				}
				else
					result = false;
			} catch (Exception e) {
				result = false;
				e.printStackTrace();
			}
		}

		return result;
	}

	protected void releaseCamera() {
		synchronized (this) {
			Log.e(TAG, "camera released entered");
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.setPreviewCallback(null);
				mCamera.release();
			}
			mCamera = null;
			if (mFrameChain != null) {
				mFrameChain[0].release();
				mFrameChain[1].release();
			}
			if (mCameraFrame != null) {
				mCameraFrame[0].release();
				mCameraFrame[1].release();
			}
		}
	}

	protected boolean connectCamera(int width, int height) {

		/* 1. We need to instantiate camera
		 * 2. We need to start thread which will be getting frames
		 */
		/* First step - initialize camera connection */
		Log.d(TAG, "Connecting to camera");
		if (!initializeCamera(width, height))
			return false;

		/* now we can start update thread */
		Log.d(TAG, "Starting processing thread");
		mStopThread = false;
		//		mThread = new Thread(new CameraWorker());
		//		mThread.start();

		return true;
	}

	public void onPreviewFrame(byte[] frame, Camera arg1) {
		//Log.e(TAG, "Preview Frame received. Frame size: " + Thread.currentThread().getName());
		mFrameChain[1 - mChainIdx].put(0, 0, frame);
		mChainIdx = 1 - mChainIdx;

		Long timestamp = System.currentTimeMillis();
		Mat rgbaDummy = mCameraFrame[mChainIdx].rgba();
		Mat grayDummy = mCameraFrame[mChainIdx].gray();
		CapturedFrame capturedFrame = new CapturedFrame(timestamp,rgbaDummy.clone(),grayDummy.clone());
		queueManager.putFrame(capturedFrame);
		
		mChainIdx = 1 - mChainIdx;
		if (mCamera != null)
			mCamera.addCallbackBuffer(mBuffer);
		
		if(mStopThread){
			this.releaseCamera();
		}
	}

	private class JavaCameraFrame implements CvCameraViewFrame {
		public Mat gray() {
			return mYuvFrameData.submat(0, mHeight, 0, mWidth);
		}

		public Mat rgba() {
			Imgproc.cvtColor(mYuvFrameData, mRgba, Imgproc.COLOR_YUV2BGR_NV12, 4);
			return mRgba;
		}

		public JavaCameraFrame(Mat Yuv420sp, int width, int height) {
			super();
			mWidth = width;
			mHeight = height;
			mYuvFrameData = Yuv420sp;
			mRgba = new Mat();
		}

		public void release() {
			mRgba.release();
		}

		private Mat mYuvFrameData;
		private Mat mRgba;
		private int mWidth;
		private int mHeight;
	};

	@Override
	public void run() {

		Log.e(TAG, "Preview Frame received. Frame size: " + Thread.currentThread().getName());

		if(!connectCamera(mFrameWidth, mFrameHeight)){
			Log.e(TAG,"failed to connect camera");
			mCamera.release();
			return;
		}
	}
	public void kill() {
		mStopThread = true;
	}
}
