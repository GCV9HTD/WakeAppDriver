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
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import com.wakeappdriver.R;
import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.configuration.Constants;
import com.wakeappdriver.configuration.Enums.Rotation;

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
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;

public class CalibrationActivity extends Activity implements CvCameraViewListener2 {

	private static final String TAG = "WAD";
	private final String CLASS_NAME = "CalibrationActivity";

	private final int FRAME_CHUNK = 3;
	private int ident_eye = 0;

	private CascadeClassifier mFaceDetector;
	private CascadeClassifier mRightEyeDetector;

	private int mAbsoluteFaceSize = 0;

	private static enum Status {ANALYZE, WAIT, SUCCESS, FAIL};
	private Status mStatus;

	/** Number of frames which the drive's face can be detected in */
	private float mIdent_frames = 0;
	/** Number of frames which the drive's face can NOT be detected in */
	private float mNo_ident_frames = 0;

	private CameraBridgeViewBase   mOpenCvCameraView;

	private Dialog mCalibFailedDialog;

	//private Mat mRgbaF;
	//private Mat mRgbaT;
	//private Rotation rotation;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibration);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		init();
		/*if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			rotation = Rotation.PORTRAIT;
		}
		else{
			rotation = Rotation.LANDSCAPE;
		}*/
	}

	@Override
	public void onConfigurationChanged(Configuration _newConfig){
		super.onConfigurationChanged(_newConfig);
		/*if(_newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
			rotation = Rotation.PORTRAIT;
		}
		else{
			rotation = Rotation.LANDSCAPE;
		}*/

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
		this.mIdent_frames = 0;
		this.mNo_ident_frames = 0;
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


	public void toMonitoring(View view) {
		Log.i(TAG, CLASS_NAME + ": Moving to monitoring activity");
		this.onStop();
		Intent intent = new Intent(this, MonitorActivity.class);
		startActivity(intent);
		finish();
	}

	/**
	 * Stop this activity and start StartScreenActivity
	 */
	public void toStartScreen(View view) {
		Log.i(TAG, CLASS_NAME + ": Moving to startScreen activity");
		// Stop camera task and analyzer
		this.onStop();

		Intent intent = new Intent(this, StartScreenActivity.class);
		this.startActivity(intent);
		finish();
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
		Log.i(TAG, CLASS_NAME + " onStop() started.");
		super.onStop();

		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();

		finish();
		Log.i(TAG, CLASS_NAME + " onStop() finished.");
	}


	@Override
	public void onCameraViewStarted(int width, int height) {
		//mRgbaF = new Mat(height, width, CvType.CV_8UC4);
		//mRgbaT = new Mat(width, width, CvType.CV_8UC4);
	}

	@Override
	public void onCameraViewStopped() {
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		/* 1. Check the process' status at each frame
		 * 2. Mark detected faces with a rectangle - user feedback. */
		checkCalibrationStatus();

		if(mStatus == Status.ANALYZE)
			Log.d(TAG, CLASS_NAME + ": #Asa ident frames: " + mIdent_frames   + "   no ident frames: " + mNo_ident_frames);

		Mat input_frame_rgba = inputFrame.rgba();
		Mat input_frame_gray = inputFrame.gray();

		/*if(this.rotation == Rotation.PORTRAIT){
			//transpose and flip both Mats according to the screen orientation
			rotateMat(input_frame_gray);
			rotateMat(input_frame_rgba);
		}*/

		if(mStatus == Status.ANALYZE) {
			Rect face_rect = detectFace(input_frame_gray);
			//draw face area
			if(face_rect != null) {
				Core.rectangle(input_frame_rgba, face_rect.tl(), face_rect.br(), new Scalar(255, 0, 0, 255), 2);

				Rect[] eyes = detectEyes(input_frame_gray, face_rect);
				if(eyes[0] != null)
					Core.rectangle(input_frame_rgba, eyes[0].tl(), eyes[0].br(), new Scalar(0, 0, 255, 255), 2);

				// Count identified frames (face & right eye)
				if(eyes[0] != null) {
					mIdent_frames++;
					mNo_ident_frames--;
					ident_eye = ident_eye < FRAME_CHUNK ? ident_eye+1 : FRAME_CHUNK;
				}
			}
			else {
				// No identified faces
				mIdent_frames--;
				mNo_ident_frames++;
				ident_eye = ident_eye > 0 ? ident_eye-1 : 0;
			}
			updateUserFeedback();
		}
		// Else - status is WAIT (means don't analyze frames)
		else {
			// Do nothing
		}

		return input_frame_rgba;
	}


	private void updateUserFeedback() {
		final View green_light = findViewById(R.id.CalibrationGreenLight);
		final View red_light = findViewById(R.id.CalibrationRedLight);
		// Identify
		if(mIdent_frames >= FRAME_CHUNK) {
			green_light.post(new Runnable() {
				public void run() {
					green_light.setVisibility(View.VISIBLE);
				}
			});
			red_light.post(new Runnable() {
				public void run() {
					red_light.setVisibility(View.INVISIBLE);

				}
			});
		}
		else {
			green_light.post(new Runnable() {
				public void run() {
					green_light.setVisibility(View.INVISIBLE);

				}
			});
			red_light.post(new Runnable() {
				public void run() {
					red_light.setVisibility(View.VISIBLE);

				}
			});
		}
	}

	/*private void rotateMat(Mat mat){
		Core.transpose(mat, mRgbaT);
		Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0, 0);
		Core.flip(mRgbaF, mat, -1 );
	}*/



	/**
	 * Checks if the calibration process succeeded or failed.
	 * If succeeded - calls to toMonitoring() method.
	 * If failed - calls popupFailMessage().
	 * Otherwise returns.
	 */
	private void checkCalibrationStatus() {
		if(this.mIdent_frames >= Constants.MIN_CALIB_FRAMES) {
			onCalibrationSuccess();
		}
		if(this.mNo_ident_frames >= Constants.MIN_NO_IDENT_CALIB_FRAMES) {
			onCalibrationFail();
		}
		// Reset negative counters values
		this.mIdent_frames = this.mIdent_frames < 0 ? 0 : this.mIdent_frames;
		this.mNo_ident_frames = this.mNo_ident_frames < 0 ? 0 : this.mNo_ident_frames;
	}



	private void onCalibrationSuccess() {
		mStatus = Status.WAIT;
		Log.i(TAG, CLASS_NAME + ": Calibration succeeded! ident_frames = " + this.mIdent_frames);
		// Show button "start" and disable button "stop":
		final View button_start = findViewById(R.id.ButtonStart);
		button_start.post(new Runnable() {
			public void run() {
				//button_start.setVisibility(View.VISIBLE);
				//button_start.bringToFront();

				/* #Asa
				 * I dont know why, but calling toMonitoring() from the "main" scope
				 * of this method didnt work (the app got stuck).
				 * This call is basically a patch and should be refactored.
				 */
				toMonitoring(null);
			}
		});
		//		final View button_stop = findViewById(R.id.ButtonStop);
		//		button_stop.post(new Runnable() {
		//			public void run() {
		//				button_stop.setVisibility(View.INVISIBLE);
		//			}
		//		});
	}


	private void onCalibrationFail() {
		mStatus = Status.WAIT;
		Log.i(TAG, CLASS_NAME + ": Calibration failed. no_ident_frames = " + this.mNo_ident_frames);
		this.mIdent_frames = 0;
		this.mNo_ident_frames = 0;
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

	/**
	 * Returns rectangles for detected eyes.
	 * <p>Rect[0] - right eye</p>
	 * <p>Rect[1] - left eye</p>
	 */
	private Rect[] detectEyes(Mat frame_gray, Rect face_rect) {
		if(face_rect == null) {
			return null;
		}

		Rect eyearea_right = new Rect(face_rect.x + face_rect.width / 16,
				(int) (face_rect.y + (face_rect.height / 4.5)),
				(face_rect.width - 2 * face_rect.width / 16) / 2, (int) (face_rect.height / 3.0));
//		Rect eyearea_left = new Rect(face_rect.x + face_rect.width / 16
//				+ (face_rect.width - 2 * face_rect.width / 16) / 2,
//				(int) (face_rect.y + (face_rect.height / 4.5)),
//				(face_rect.width - 2 * face_rect.width / 16) / 2, (int) (face_rect.height / 3.0));

		Rect[] eyes = new Rect[2];

		//detect eyes with classifier
		eyes[0] = getEyeRect(frame_gray, mRightEyeDetector, eyearea_right);
//		eyes[1] = getEyeRect(frame_gray, mRightEyeDetector, eyearea_left);
		return eyes;
	}

	private Rect getEyeRect(Mat faceGray, CascadeClassifier clasificator, Rect eye_area){
		try{
			Mat mROI = faceGray.submat(eye_area);
			MatOfRect eye = new MatOfRect();
			clasificator.detectMultiScale(mROI, eye, 1.15, 2,
					Objdetect.CASCADE_SCALE_IMAGE | Objdetect.CASCADE_FIND_BIGGEST_OBJECT, new Size(10, 10),
					new Size());

			Rect[] eyes_rects = eye.toArray();

			// Coordinates correction
			Rect detected_eye = eyes_rects[0];
			detected_eye.x = eye_area.x + detected_eye.x;
			detected_eye.y = eye_area.y + detected_eye.y;
			// Expand the eye rectangle because the classifier detects the pupil
			Rect eye_rect = new Rect((int) detected_eye.tl().x,
					(int) (detected_eye.tl().y + detected_eye.height * 0.4),
					detected_eye.width,
					(int) (detected_eye.height * 0.6));

			eye.release();
			mROI.release();

			return eye_rect;

		}
		catch(Exception e){
			return null;
		}
	}

	private Rect detectFace(Mat matGray) {
		//detect faces
		MatOfRect faces = new MatOfRect();
		if (mFaceDetector != null){
			mFaceDetector.detectMultiScale(matGray, faces, 1.1, 2, 2 | Objdetect.CASCADE_FIND_BIGGEST_OBJECT, 
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
		Builder b = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
		b.setTitle(R.string.dialog_calibration_failed_title);
		b.setIcon(R.drawable.ic_calibration);
		b.setMessage(R.string.dialog_calibration_failed_message);
		b.setNegativeButton(R.string.dialog_calibration_failed_neg_button, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				toStartScreen(null);
			}
		});
		b.setPositiveButton(R.string.dialog_calibration_failed_pos_button, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				init();
			}
		});
		mCalibFailedDialog = b.create();
	}

}
