package com.wakeappdriver.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

import com.wakeappdriver.framework.dto.CapturedFrame;
import com.wakeappdriver.framework.implementations.analyzers.PercentCoveredFrameAnalyzer;
import com.wakeappdriver.framework.interfaces.FrameAnalyzer;
import com.wakeappdriver.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.WindowManager;


public class DebugActivity extends Activity implements CvCameraViewListener2 {
	private static final String TAG = "WAD";

	private CameraBridgeViewBase   mOpenCvCameraView;

	private FrameAnalyzer frameAnalyzer;

	private CascadeClassifier mFaceDetector;
	private CascadeClassifier mRightEyeDetector;
	

	private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				Log.d(TAG, Thread.currentThread().getName() + " :: OpenCV loaded successfully");
				try {
					// load face-classifier file from application resources
					InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
					File mFaceCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
					FileOutputStream os = new FileOutputStream(mFaceCascadeFile);

					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					is.close();
					os.close();

					// load right-eye-classifier file from application resources
					InputStream iser = getResources().openRawResource(R.raw.haarcascade_lefteye_2splits);
					File cascadeDirER = getDir("cascadeER", Context.MODE_PRIVATE);
					File cascadeFileER = new File(cascadeDirER, "haarcascade_eye_right.xml");
					FileOutputStream oser = new FileOutputStream(cascadeFileER);

					byte[] bufferER = new byte[4096];
					int bytesReadER;
					while ((bytesReadER = iser.read(bufferER)) != -1) {
						oser.write(bufferER, 0, bytesReadER);
					}
					iser.close();
					oser.close();

					// create face-classifier
					mFaceDetector = new CascadeClassifier(mFaceCascadeFile.getAbsolutePath());
					if (mFaceDetector.empty()) {
						Log.e(TAG, Thread.currentThread().getName() + " :: Failed to load cascade classifier");
						mFaceDetector = null;
					} else
						Log.d(TAG, Thread.currentThread().getName() + " :: Loaded cascade classifier from " + mFaceCascadeFile.getAbsolutePath());

					// create right-eye-classifier
					mRightEyeDetector = new CascadeClassifier(cascadeFileER.getAbsolutePath());
					if (mRightEyeDetector.empty()) {
						Log.e(TAG, Thread.currentThread().getName() + " :: Failed to load cascade classifier");
						mRightEyeDetector = null;
					} else
						Log.d(TAG, Thread.currentThread().getName() + " :: Loaded cascade classifier from " + cascadeFileER.getAbsolutePath());
					
					cascadeDir.delete();
					cascadeDirER.delete();
					
					frameAnalyzer = new PercentCoveredFrameAnalyzer(mFaceDetector, mRightEyeDetector);
										
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, Thread.currentThread().getName() + " :: Failed to load cascade. Exception thrown: " + e);
				}

				mOpenCvCameraView.enableView();
			}
			break;
			default:
			{
				super.onManagerConnected(status);
			} break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_view_listener_activty);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Show the Up button in the action bar.

		Resources res = getResources();
		SharedPreferences config = this.getSharedPreferences(res.getString(R.string.awd_config_fname), MODE_PRIVATE);
		
		Thread.currentThread().setName("CameraThread");

//		boolean nativeCam = false;
		boolean nativeCam = config.getBoolean(res.getString(R.string.awd_config_native_cam_key), false);
		//java camera view works alot faster than native camera view
		if(nativeCam){
			mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.native_camera_surface_view);
		} else {
			mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_camera_surface_view);
		}
		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.setCameraIndex(1);
		mOpenCvCameraView.enableFpsMeter();
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera_view_listener_activty, menu);
		return true;
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		this.finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if ((keyCode == KeyEvent.KEYCODE_BACK))
	    {
	        finish();
	    }
	    return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, Thread.currentThread().getName() + " :: service has been closed");
	}

	@Override
	public void onCameraViewStarted(int width, int height) {		
	}

	@Override
	public void onCameraViewStopped() {
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		
		Mat gray = inputFrame.gray().clone();
		Mat rgba = inputFrame.rgba().clone();
		Long timestamp = System.nanoTime();

		rgba = this.frameAnalyzer.visualAnalyze(new CapturedFrame(timestamp, rgba, gray));
		return rgba;
	}


}
