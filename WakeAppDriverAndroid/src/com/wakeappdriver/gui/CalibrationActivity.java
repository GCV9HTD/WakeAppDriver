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
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import com.wakeappdriver.R;
import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.configuration.Constants;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

public class CalibrationActivity extends Activity implements CvCameraViewListener2 {

	private static final String TAG = "WAD";
	private final String CLASS_NAME = "CalibrationActivity";

	private CascadeClassifier mFaceDetector;
	private CascadeClassifier mRightEyeDetector;
	private Mat mGray;
	private Rect eyearea_right;
	private int mAbsoluteFaceSize = 0;

	private static enum Status {ANALYZE, WAIT};
	private Status mStatus;

	/** Number of frames which the drive's face can be detected in */
	private float ident_frames = 0;
	/** Number of frames which the drive's face can NOT be detected in */
	private float no_ident_frames = 0;

	private CameraBridgeViewBase   mOpenCvCameraView;

	private Dialog mCalibFailedDialog;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibration);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		init();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}


	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, StartScreenActivity.class);
		startActivity(intent);
		finish();
	}
	
	

	private void init() {
		this.ident_frames = 0;
		this.no_ident_frames = 0;
		mStatus = Status.ANALYZE;

		Thread.currentThread().setName("CameraThread");
		//java camera view works alot faster than native camera view
		if(ConfigurationParameters.getCameraMode()){
			mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.native_camera_surface_view);
		} else {
			mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_camera_surface_view);
		}
		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.setCameraIndex(1);
		mOpenCvCameraView.enableFpsMeter();
		
		initCalibFailedDialog();
	}



	/**
	 * NOTICE: You must call onStop() before invoking this method 
	 * @param view
	 */
	public void toMonitoring(View view) {
		Log.i(TAG, CLASS_NAME + ": going to monitoring screen");
		Intent intent = new Intent(this, MonitorActivity.class);
		startActivity(intent);
		finish();
	}

	/**
	 * Stop this activity and start StartScreenActivity
	 */
	public void toStartScreen(View view) {
		// Stop camera task and analyzer
		this.onStop();

		Intent intent = new Intent(this, StartScreenActivity.class);
		this.startActivity(intent);
	}


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

					// set classifiers to frame-analyzers

					cascadeDir.delete();
					cascadeDirER.delete();

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
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();

		finish();
	}


	@Override
	public void onCameraViewStarted(int width, int height) {		
	}

	@Override
	public void onCameraViewStopped() {
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		/* Analyze camera frames.
		 * Check the process' status at each frame  */
		checkCalibrationStatus();
		Log.d(TAG, CLASS_NAME + ": #Asa  " + ident_frames   + "   " + no_ident_frames);

		Mat rgba = inputFrame.rgba();
		mGray = inputFrame.gray();

		if(mStatus == Status.ANALYZE) {
			analyzeFrame();
		}
		// Else - status is WAIT (means don't analyze frames)
		else {
			// Do nothing
		}

		return rgba;
	}


	private void analyzeFrame() {
		Rect r = detectFace();
		if(r == null) {
			// No face detection
			this.no_ident_frames++;
		}
//		else if(! detectEye(r)) {
//			// No eye detection
//			//this.ident_frames--;
//		}
		else {
			this.ident_frames++;
			this.no_ident_frames--;
		}
	}


	/**
	 * Checks if the calibration process succeeded or failed.
	 * If succeeded - calls to toMonitoring() method.
	 * If failed - calls popupFailMessage().
	 * Otherwise returns.
	 */
	private void checkCalibrationStatus() {
		if(this.ident_frames >= Constants.MIN_CALIB_FRAMES) {
			onCalibrationSuccess();
			return;
		}
		if(this.no_ident_frames >= Constants.MIN_NO_IDENT_CALIB_FRAMES) {
			onCalibrationFail();
		}
		// Reset negative counters values
		this.ident_frames = this.ident_frames < 0 ? 0 : this.ident_frames;
		this.no_ident_frames = this.no_ident_frames < 0 ? 0 : this.no_ident_frames;
	}



	private void onCalibrationSuccess() {
		mStatus = Status.WAIT;
		Log.i(TAG, CLASS_NAME + ": Calibration succeeded! ident_frames = " + this.ident_frames);
		// Show button "start" and disable button "stop":
		final View button_start = findViewById(R.id.ButtonStart);
		button_start.post(new Runnable() {
			  public void run() {
				  button_start.setVisibility(View.VISIBLE);
				  button_start.bringToFront();
			  }
			});
		final View button_stop = findViewById(R.id.ButtonStop);
		button_stop.post(new Runnable() {
			  public void run() {
				  button_stop.setVisibility(View.INVISIBLE);
			  }
			});
	}
	
	
	private void onCalibrationFail() {
		mStatus = Status.WAIT;
		Log.i(TAG, CLASS_NAME + ": Calibration failed. no_ident_frames = " + this.no_ident_frames);
		this.ident_frames = 0;
		this.no_ident_frames = 0;
		popupFailMessage();
	}


	/**
	 * Shows a message on the screen, saying the calibration process failed.
	 * The user can choose between "retry" and "exit".
	 */
	private void popupFailMessage() {
		Log.i(TAG, CLASS_NAME + ": Calibration failed. Showing message");
		this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mCalibFailedDialog.show();
			}
		});
	}


	private boolean detectEye(Rect r) {
		if(r == null) {
			return false;
		}

		eyearea_right = new Rect(r.x + r.width / 16,
				(int) (r.y + (r.height / 4.5)),
				(r.width - 2 * r.width / 16) / 2, (int) (r.height / 3.0));
		//		eyearea_left = new Rect(r.x + r.width / 16
		//				+ (r.width - 2 * r.width / 16) / 2,
		//				(int) (r.y + (r.height / 4.5)),
		//				(r.width - 2 * r.width / 16) / 2, (int) (r.height / 3.0));

		//detect eyes with classifier
		return getEyeRect(mRightEyeDetector, eyearea_right);
	}

	private boolean getEyeRect(CascadeClassifier clasificator, Rect area){
		try{
			Mat mROI = mGray.submat(area);
			MatOfRect eyes = new MatOfRect();
			clasificator.detectMultiScale(mROI, eyes, 1.15, 2,
					Objdetect.CASCADE_SCALE_IMAGE | Objdetect.CASCADE_FIND_BIGGEST_OBJECT, new Size(10, 10),
					new Size());

			if(eyes.toArray().length == 0) {
				return false;
			}

			eyes.release();
			mROI.release();
		}
		catch(Exception e){
		}
		return true;
	}

	private Rect detectFace() {
		//detect faces
		MatOfRect faces = new MatOfRect();
		if (mFaceDetector != null){
			mFaceDetector.detectMultiScale(mGray, faces, 1.1, 2, 2 | Objdetect.CASCADE_FIND_BIGGEST_OBJECT, 
					new Size(mAbsoluteFaceSize, mAbsoluteFaceSize),
					new Size());
		}
		Rect[] facesArray = faces.toArray();
		faces.release();
		Log.d(TAG, Thread.currentThread().getName() + " :: num of faces: " + faces.size());

		//handle face area
		if(facesArray.length == 0) {
			return null;
		}
		return facesArray[0];
	}
	
	
	private void initCalibFailedDialog() {
		Builder b = new AlertDialog.Builder(this);
		b.setTitle("Well...");
		b.setMessage("The calibration process failed. In order to success, the system must "+
				"detect your face and eyes. Please try again.");
				b.setNegativeButton("Exit", null);
				b.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						init();
					}
				});
		mCalibFailedDialog = b.create();
	}

}
