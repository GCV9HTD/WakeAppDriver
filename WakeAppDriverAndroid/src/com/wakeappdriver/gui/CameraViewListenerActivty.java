package com.wakeappdriver.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.opencv.imgproc.Imgproc;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import com.wakeappdriver.classes.CapturedFrame;
import com.wakeappdriver.classes.FrameAnalyzer;
import com.wakeappdriver.classes.FrameQueue;
import com.wakeappdriver.classes.FrameQueueManager;
import com.wakeappdriver.classes.ResultQueue;
import com.wakeappdriver.classes.WindowAnalyzer;
import com.wakeappdriver.enums.FrameAnalyzerType;
import com.wakeappdriver.enums.FrameQueueType;
import com.wakeappdriver.interfaces.Alerter;
import com.wakeappdriver.interfaces.Indicator;
import com.wakeappdriver.interfaces.Predictor;
import com.wakeappdriver.stubs.StubAlerter;
import com.wakeappdriver.stubs.StubFrameAnalyzer;
import com.wakeappdriver.stubs.StubIndicator;
import com.wakeappdriver.stubs.StubPredictor;
import com.wakeappdriver.tasks.DetectorTask;
import com.wakeappdriver.wakeappdriver.R;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.DashPathEffect;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.support.v4.app.NavUtils;
public class CameraViewListenerActivty extends Activity implements CvCameraViewListener2 {
	private static final String TAG = "AWD";

	private Mat                    mRgba;
	private Mat                    mGray;
	private CameraBridgeViewBase   mOpenCvCameraView;
	FrameQueueManager queueManager;


	private CascadeClassifier mFaceDetector;
	private CascadeClassifier mRightEyeDetector;
	private CascadeClassifier mLeftEyeDetector;

	private Mat mZoomWindow;
	private Mat mZoomWindow2;
	Rect eyearea_right;
	Rect eyearea_left;
	Rect eyeOnlyRect_right;
	private float mRelativeFaceSize = 0.2f;
	private int mAbsoluteFaceSize = 0;

	private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				Log.i(TAG, Thread.currentThread().getName() + ":: OpenCV loaded successfully");
				try {
					// load cascade file from application resources
					InputStream is = getResources().openRawResource(
							R.raw.lbpcascade_frontalface);
					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
					File mFaceCascadeFile = new File(cascadeDir,
							"lbpcascade_frontalface.xml");
					FileOutputStream os = new FileOutputStream(mFaceCascadeFile);

					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					is.close();
					os.close();

					// --------------------------------- load right eye
					// classificator -----------------------------------
					InputStream iser = getResources().openRawResource(
							R.raw.haarcascade_lefteye_2splits);
					File cascadeDirER = getDir("cascadeER",
							Context.MODE_PRIVATE);
					File cascadeFileER = new File(cascadeDirER,
							"haarcascade_eye_right.xml");
					FileOutputStream oser = new FileOutputStream(cascadeFileER);

					byte[] bufferER = new byte[4096];
					int bytesReadER;
					while ((bytesReadER = iser.read(bufferER)) != -1) {
						oser.write(bufferER, 0, bytesReadER);
					}
					iser.close();
					oser.close();

					// --------------------------------- load left eye
					// classificator -----------------------------------
					InputStream isel = getResources().openRawResource(
							R.raw.haarcascade_lefteye_2splits);
					File cascadeDirEL = getDir("cascadeEL",
							Context.MODE_PRIVATE);
					File cascadeFileEL = new File(cascadeDirEL,
							"haarcascade_eye_left.xml");
					FileOutputStream osel = new FileOutputStream(cascadeFileEL);

					byte[] bufferEL = new byte[4096];
					int bytesReadEL;
					while ((bytesReadEL = isel.read(bufferEL)) != -1) {
						osel.write(bufferEL, 0, bytesReadEL);
					}
					isel.close();
					osel.close();


					mFaceDetector = new CascadeClassifier(
							mFaceCascadeFile.getAbsolutePath());
					if (mFaceDetector.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mFaceDetector = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from "
								+ mFaceCascadeFile.getAbsolutePath());

					mRightEyeDetector = new CascadeClassifier(
							cascadeFileER.getAbsolutePath());
					if (mRightEyeDetector.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mRightEyeDetector = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from "
								+ cascadeFileER.getAbsolutePath());

					mLeftEyeDetector = new CascadeClassifier(
							cascadeFileER.getAbsolutePath());
					if (mLeftEyeDetector.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mLeftEyeDetector = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from "
								+ cascadeFileEL.getAbsolutePath());



					cascadeDir.delete();
					cascadeDirEL.delete();
					cascadeDirER.delete();

				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
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

		//this.init(config); //initilize WAD data structures

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

	public void onDestroy() {
		super.onDestroy();
		mOpenCvCameraView.disableView();
	}


	@Override
	public void onCameraViewStarted(int width, int height) {
		mGray = new Mat();
		mRgba = new Mat();		
	}

	@Override
	public void onCameraViewStopped() {
		mGray.release();
		mRgba.release();				
		mZoomWindow.release();
		mZoomWindow2.release();
	}

	private boolean nameSet = false;
	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		if(!nameSet){
			Thread.currentThread().setName("CameraThread");
			nameSet = true;
		}

		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();

		//create zoom areas
		CreateAuxiliaryMats();


		if (mAbsoluteFaceSize == 0) {
			int height = mGray.rows();
			if (Math.round(height * mRelativeFaceSize) > 0) {
				mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
			}
		}


		//detect faces
		MatOfRect faces = new MatOfRect();
		if (mFaceDetector != null){
			mFaceDetector.detectMultiScale(mGray, faces, 1.1, 2,
					2, 
					new Size(mAbsoluteFaceSize, mAbsoluteFaceSize),
					new Size());
		}

		Rect[] facesArray = faces.toArray();

		for(int i = 0; i < facesArray.length; i++){
			//draw face area
			Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), new Scalar(0,255,0),4);
			Rect r = facesArray[i];
			eyearea_right = new Rect(r.x + r.width / 16,
					(int) (r.y + (r.height / 4.5)),
					(r.width - 2 * r.width / 16) / 2, (int) (r.height / 3.0));
			eyearea_left = new Rect(r.x + r.width / 16
					+ (r.width - 2 * r.width / 16) / 2,
					(int) (r.y + (r.height / 4.5)),
					(r.width - 2 * r.width / 16) / 2, (int) (r.height / 3.0));

			//draw eye areas
			Core.rectangle(mRgba, eyearea_left.tl(), eyearea_left.br(),
					new Scalar(255, 0, 0, 255), 2);
			Core.rectangle(mRgba, eyearea_right.tl(), eyearea_right.br(),
					new Scalar(255, 0, 0, 255), 2);

			//detect eyes with classifier
			Rect tmp = getEyeRect(mRightEyeDetector, eyearea_right);
			if(tmp != null){
				eyeOnlyRect_right = tmp;
			}
			if(eyeOnlyRect_right != null){

				//images to display in zoom areas
				Mat toDisplayGray = mGray.submat(eyeOnlyRect_right);
				Mat toDisplayRBGA = mRgba.submat(eyeOnlyRect_right);

				//get binarization threshold
				double thresh = getThreshold(toDisplayGray);
				double maxval    = 255; 

				//print threshold
				//Log.e("WAD", "THRESH " + thresh);

				//binarize image
				Imgproc.threshold(toDisplayGray, toDisplayGray , thresh , maxval ,Imgproc.THRESH_BINARY);

				//count black pixels in binarized image to get iris size
				getIrisSize(toDisplayGray,toDisplayRBGA);

				//draw zoom areas
				displayMat(toDisplayGray,toDisplayRBGA);
			}

		}
		faces.release();

		/**
		 * use data structures flow
		Mat gray = inputFrame.gray().clone();
		Mat rgba = inputFrame.rgba().clone();
		Long timestamp = System.nanoTime();
		queueManager.putFrame(new CapturedFrame(timestamp, rgba, gray));
		 **/

		return mRgba;
	}

	private void getIrisSize(Mat toDisplayGray, Mat toDisplayRgba) {

		int midRow = (toDisplayGray.rows()/2)-3;
		int midCol = toDisplayGray.cols()/2;
		double startCountImgPercent = 0.3;
		double endCountImagePercent = 0.7;

		//last black pixel indices in edges (left right top bottom)
		int left = 0;
		int right = 0;
		int top = 0;
		int down = 0;

		//count black pixels from left to right in the middle row
		for(int i = (int)(toDisplayGray.cols()*startCountImgPercent); i < toDisplayGray.cols()*endCountImagePercent; i++){
			//if value is 0 -> black pixel
			if(toDisplayGray.get(midRow, i)[0] == 0){
				if(left == 0){ 
					left = i;
					right = i;
				} 
				else {
					right = i;
				}
			}
		}

		//count black pixels from top to bottom in the middle column
		for(int i = 0; i < toDisplayGray.rows(); i++){
			//if value is 0 -> black pixel
			if(toDisplayGray.get(i,midCol)[0] == 0){
				if(down == 0){
					down = i;
					top = i;
				} 
				else {
					top = i;
				}
			}
		}
		
		double width = right - left;
		double height = top - down;
		double ratio = height/ (width);
		if(ratio > 0.33){ // no blink, draw red lines
			Core.line(toDisplayRgba, new Point(left,midRow), new Point(right,midRow), new Scalar(255,0,0));
			Core.line(toDisplayRgba, new Point(midCol,down), new Point(midCol,top), new Scalar(255,0,0));
		}
		//else: Blink occurred		

		//print ratio
		//Log.e("WAD","ratio " +ratio);		//get average distance of last black pixel from middle (left and right)
		
//		double leftDiff = midCol - left;
//		double rightDiff = right - midCol;
//		int avgColDiff = (int)((leftDiff/2) + (rightDiff)/2);
//		left = midCol - avgColDiff;
//		right = midCol + avgColDiff;
//
//		//get average distance of last black pixel from middle (top and bottom)
//		double topDiff = top - midRow;
//		double downDiff = midRow - down;
//		int avgRowDiff = (int)((downDiff/2) + (topDiff)/2);
//		down = midRow - avgRowDiff;
//		top = midRow + avgRowDiff;
//
//		//ration between height and width
//		double ratio2 = (double)avgRowDiff/ (double)avgColDiff;
//		if(ratio > 0.33){ // no blink, draw red lines
//			Core.line(toDisplayRgba, new Point(left,midRow), new Point(right,midRow), new Scalar(255,0,0));
//			Core.line(toDisplayRgba, new Point(midCol,down), new Point(midCol,top), new Scalar(255,0,0));
//		}

	}

	private double getThreshold(Mat toDisplayGray) {
		//works good in daylight (indoors)
		//double thresh    = 75;

		//threshhold for low-light situtations (afternoon indoors)
		//double thresh    = 30;

		//attempt to make a robust threshold, we should find a better
		//formula that fits all light situations
		double mean = Core.mean(toDisplayGray).val[0];
		double thresh    = mean*(0.75);

		return thresh;
	}

	private void displayMat(Mat toDisplayGray, Mat toDisplayRgba) {

		//convert grayscale image to RBGA in order to display in zoom window
		Mat toDisplayTmp = new Mat(toDisplayGray.rows(),toDisplayGray.cols(),mRgba.type());
		Imgproc.cvtColor(toDisplayGray, toDisplayTmp, Imgproc.COLOR_GRAY2RGBA);
		Imgproc.resize(toDisplayTmp, toDisplayTmp,mZoomWindow.size());
		toDisplayTmp.copyTo(mZoomWindow);
		Imgproc.resize(toDisplayRgba, mZoomWindow2,mZoomWindow2.size());

		toDisplayTmp.release();
	}
	private Rect getEyeRect(CascadeClassifier clasificator, Rect area){
		Rect eye_only_rectangle = null;
		try{
			Mat mROI = mGray.submat(area);
			MatOfRect eyes = new MatOfRect();
			clasificator.detectMultiScale(mROI, eyes, 1.15, 2,
					Objdetect.CASCADE_FIND_BIGGEST_OBJECT
					| Objdetect.CASCADE_SCALE_IMAGE, new Size(10, 10),
					new Size());

			Rect e = eyes.toArray()[0];
			e.x = area.x + e.x;
			e.y = area.y + e.y;
			eye_only_rectangle = new Rect((int) e.tl().x,
					(int) (e.tl().y + e.height * 0.4), (int) e.width,
					(int) (e.height * 0.6));
			eyes.release();
		}
		catch(Exception e){
		}
		return eye_only_rectangle;
	}
	
	private void CreateAuxiliaryMats() {
		if (mGray.empty())
			return;

		int rows = mGray.rows();
		int cols = mGray.cols();

		//if (mZoomWindow == null) {
		mZoomWindow = mRgba.submat(rows / 2 + rows / 10, rows, cols / 2
				+ cols / 10, cols);
		mZoomWindow2 = mRgba.submat(0, rows / 2 - rows / 10, cols / 2
				+ cols / 10, cols);
		//}

	}

	//init data structures
	private void init(SharedPreferences config){

		FrameQueue frameQueue1 = new FrameQueue(10,FrameQueueType.PERCENT_COVERED_QUEUE);
		FrameQueue frameQueue2 = new FrameQueue(10,FrameQueueType.HEAD_INCLINATION_QUEUE);
		FrameQueue frameQueue3 = new FrameQueue(10,FrameQueueType.YAWN_SIZE_QUEUE);

		List<FrameQueue> frameQueueList = new ArrayList<FrameQueue>();
		frameQueueList.add(frameQueue1);
		frameQueueList.add(frameQueue2);
		frameQueueList.add(frameQueue3);

		ResultQueue resultQueue1 = new ResultQueue(FrameAnalyzerType.PERCENT_COVERED);
		ResultQueue resultQueue2 = new ResultQueue(FrameAnalyzerType.HEAD_INCLINATION);
		ResultQueue resultQueue3 = new ResultQueue(FrameAnalyzerType.YAWN_SIZE);

		List<ResultQueue> resultQueueList = new ArrayList<ResultQueue>();
		resultQueueList.add(resultQueue1);
		resultQueueList.add(resultQueue2);
		resultQueueList.add(resultQueue3);

		queueManager = new FrameQueueManager(frameQueueList);

		FrameAnalyzer frameAnalyzer1 = new StubFrameAnalyzer(queueManager,frameQueue1,resultQueue1);
		FrameAnalyzer frameAnalyzer2 = new StubFrameAnalyzer(queueManager,frameQueue2,resultQueue2);
		FrameAnalyzer frameAnalyzer3 = new StubFrameAnalyzer(queueManager,frameQueue3,resultQueue3);

		Indicator indicator1 = new StubIndicator(3);
		Indicator indicator2 = new StubIndicator(5);
		Indicator indicator3 = new StubIndicator(7);

		List<Indicator> indicatorList = new ArrayList<Indicator>();
		indicatorList.add(indicator1);
		indicatorList.add(indicator2);
		indicatorList.add(indicator3);

		Thread t1 = new Thread(frameAnalyzer1);
		t1.setName("FrameAnalyzer1");
		Thread t2 = new Thread(frameAnalyzer2);
		t2.setName("FrameAnalyzer2");
		Thread t3 = new Thread(frameAnalyzer3);
		t3.setName("FrameAnalyzer3");

		t1.start();
		t2.start();
		t3.start();

		Resources res = getResources();
		double alertThreshold = config.getFloat(res.getString(R.string.awd_config_threshold_key), (float) 7.5);
		int windowSize = config.getInt(res.getString(R.string.awd_config_window_sizw_key), 7000);
		Alerter alerter = new StubAlerter();
		WindowAnalyzer windowAnalyzer = new WindowAnalyzer(resultQueueList, indicatorList);
		Predictor predictor = new StubPredictor();
		DetectorTask detectorTask = new DetectorTask(alerter, windowAnalyzer, predictor, alertThreshold, windowSize);

		Thread detectionTask = new Thread(detectorTask);
		detectionTask.setName("DetectionTask");

		detectionTask.start();

	}

}
