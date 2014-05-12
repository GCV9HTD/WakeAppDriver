package com.wakeappdriver.framework.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.objdetect.CascadeClassifier;

import com.wakeappdriver.R;
import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.configuration.Enums.Action;
import com.wakeappdriver.configuration.Enums.*;
import com.wakeappdriver.framework.EmergencyHandler;
import com.wakeappdriver.framework.FrameQueue;
import com.wakeappdriver.framework.FrameQueueManager;
import com.wakeappdriver.framework.ResultQueue;
import com.wakeappdriver.framework.WindowAnalyzer;
import com.wakeappdriver.framework.datacollection.DataCollector;
import com.wakeappdriver.framework.implementations.analyzers.PercentCoveredFrameAnalyzer;
import com.wakeappdriver.framework.implementations.indicators.BlinkDurationIndicator;
import com.wakeappdriver.framework.implementations.indicators.PerclosIndicator;
import com.wakeappdriver.framework.implementations.predictors.WakeAppPredictor;
import com.wakeappdriver.framework.interfaces.FrameAnalyzer;
import com.wakeappdriver.framework.interfaces.Indicator;
import com.wakeappdriver.framework.interfaces.Predictor;
import com.wakeappdriver.framework.tasks.DetectorTask;
import com.wakeappdriver.framework.tasks.JavaCameraTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.Settings.Secure;
import android.util.Log;

public class GoService extends ListenerService{
	private static final String TAG = "WAD";

	private JavaCameraTask mCameraRunnable;
	
	private CascadeClassifier mFaceDetector;
	private CascadeClassifier mRightEyeDetector;

	private FrameQueueManager queueManager;
	private List<FrameAnalyzer> frameAnalyzers;
	private DetectorTask detector;

	private Thread detectionTask;
	CameraHandlerThread cameraHandlerThread;
	private List<Thread> frameAnalyzerTasks;

	private int frameWidth;
	private int frameHeight;

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
					((PercentCoveredFrameAnalyzer)frameAnalyzers.get(0)).setmFaceDetector(mFaceDetector);
					((PercentCoveredFrameAnalyzer)frameAnalyzers.get(0)).setmRightEyeDetector(mRightEyeDetector);

					cascadeDir.delete();
					cascadeDirER.delete();

				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, Thread.currentThread().getName() + " :: Failed to load cascade. Exception thrown: " + e);
				}

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
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "starting service");

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "GoService: onStartCommand() called");
		frameWidth = intent.getIntExtra("frameWidth", 800);
		frameHeight = intent.getIntExtra("frameHeight", 600);
		this.init(); //initialize WAD data structures
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
		return Service.START_NOT_STICKY;

	}


	@Override
	public void onDestroy() {
		Log.e(TAG, Thread.currentThread().getName() + " :: service has been closed");

		super.onDestroy();
		this.mCameraRunnable.kill();
		this.queueManager.killManager();
		for (Thread t : frameAnalyzerTasks) {
			try {
				t.join(1000);
			} catch (InterruptedException e) {
				Log.e(TAG, Thread.currentThread().getName() + " :: couldn't kill thread " + t.getName());
			}
		}

		try {
			detector.killDetector();
			detectionTask.join(detector.getWindowSize());
		} catch (InterruptedException e) {
			Log.e(TAG, Thread.currentThread().getName() + " :: couldn't kill thread " + detectionTask.getName());
		}
		catch (NullPointerException e) {
			Log.e(TAG, "GoService: detector is null");
		}

		cameraHandlerThread.kill();
	}



	//init data structures
	private void init(){
		FrameQueue frameQueue1 = new FrameQueue(FrameQueueType.PERCENT_COVERED_QUEUE);
		//		FrameQueue frameQueue2 = new FrameQueue(10,FrameQueueType.HEAD_INCLINATION_QUEUE);
		//		FrameQueue frameQueue3 = new FrameQueue(10,FrameQueueType.YAWN_SIZE_QUEUE);

		List<FrameQueue> frameQueueList = new ArrayList<FrameQueue>();
		frameQueueList.add(frameQueue1);
		//		frameQueueList.add(frameQueue2);
		//		frameQueueList.add(frameQueue3);

		ResultQueue resultQueue1 = new ResultQueue(FrameAnalyzerType.PERCENT_COVERED);
		//		ResultQueue resultQueue2 = new ResultQueue(FrameAnalyzerType.HEAD_INCLINATION);
		//		ResultQueue resultQueue3 = new ResultQueue(FrameAnalyzerType.YAWN_SIZE);

		List<ResultQueue> resultQueueList = new ArrayList<ResultQueue>();
		resultQueueList.add(resultQueue1);
		//		resultQueueList.add(resultQueue2);
		//		resultQueueList.add(resultQueue3);

		queueManager = new FrameQueueManager(frameQueueList);

		PercentCoveredFrameAnalyzer frameAnalyzer1 = new PercentCoveredFrameAnalyzer(queueManager,frameQueue1,resultQueue1);
		//		FrameAnalyzer frameAnalyzer2 = new StubFrameAnalyzer(queueManager,frameQueue2,resultQueue2);
		//		FrameAnalyzer frameAnalyzer3 = new StubFrameAnalyzer(queueManager,frameQueue3,resultQueue3);
		this.frameAnalyzers = new ArrayList<FrameAnalyzer>();
		this.frameAnalyzers.add(frameAnalyzer1);

		Indicator indicator1 = new PerclosIndicator();
		Indicator indicator2 = new BlinkDurationIndicator(5);
		//		Indicator indicator3 = new StubIndicator(7);

		HashMap<IndicatorType,Indicator> indicatorList = new HashMap<IndicatorType,Indicator>();
		indicatorList.put(IndicatorType.PERCLOS, indicator1);
		indicatorList.put(IndicatorType.BLINK_DURATION, indicator2);
		//		indicatorList.add(indicator3);

		this.frameAnalyzerTasks = new ArrayList<Thread>();
		for (FrameAnalyzer frameAnalyzer : this.frameAnalyzers) {
			Thread t = new Thread(frameAnalyzer);
			t.setName(frameAnalyzer.getClass().getSimpleName());
			frameAnalyzerTasks.add(t);
			t.start();
		}
		
		WindowAnalyzer windowAnalyzer = new WindowAnalyzer(resultQueueList, indicatorList);
		Predictor predictor = new WakeAppPredictor();
		
		//create a dataCollector if the collection feature is on
		DataCollector dataCollector = null;
		if(ConfigurationParameters.getCollectMode()){
			String android_id = Secure.getString(getBaseContext().getContentResolver(),Secure.ANDROID_ID);
			dataCollector = new DataCollector(android_id); 
		}
		this.detector = new DetectorTask(windowAnalyzer, predictor, dataCollector, this);

		this.detectionTask = new Thread(this.detector);
		detectionTask.setName("DetectionTask");
		detectionTask.start();

		EmergencyHandler emergencyHandler = new EmergencyHandler(detector);
		frameAnalyzer1.setEmergencyHandler(emergencyHandler);

		mCameraRunnable = new JavaCameraTask(frameWidth, frameHeight, queueManager);
		cameraHandlerThread = new CameraHandlerThread();
		cameraHandlerThread.openCamera(mCameraRunnable);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		//no support for binding
		return null;
	}


	private static class CameraHandlerThread extends HandlerThread {
		Handler mHandler = null;
		JavaCameraTask cameraTask;
		
		CameraHandlerThread() {
			super("CameraThread");
			start();
			mHandler = new Handler(getLooper());
		}

		void openCamera(JavaCameraTask cameraTask) {
			this.cameraTask = cameraTask;
			mHandler.post(cameraTask);
		}
		
		void kill(){
			cameraTask.kill();
		}
	}


	@Override
	public Action[] getActions() {
		/*
		 * If we want the service to listen to actions -
		 * return a list of those actions here 
		 * 
		 */
		return new Action[0];
	}

	@Override
	public void onListenEvent(Intent intent) {
		
		switch(Action.toAction(intent.getAction())){
		/*
		 * If we want the service to listen to actions -
		 * implement what to do in the case of each action 
		 * (this runs on UI thread - if interaction with the service
		 * threads is needed then we need to synchronize)
		 */
		default:
			break;
		
		}
		return;
	}
}
