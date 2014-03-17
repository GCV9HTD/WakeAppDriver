package services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.objdetect.CascadeClassifier;

import com.wakeappdriver.R;
import com.wakeappdriver.classes.AlerterContainer;
import com.wakeappdriver.classes.EmergencyHandler;
import com.wakeappdriver.classes.FrameAnalyzer;
import com.wakeappdriver.classes.FrameQueue;
import com.wakeappdriver.classes.FrameQueueManager;
import com.wakeappdriver.classes.IntentMessenger;
import com.wakeappdriver.classes.ResultQueue;
import com.wakeappdriver.classes.WindowAnalyzer;
import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.implementations.BlinkDurationIndicator;
import com.wakeappdriver.implementations.GuiAlerter;
import com.wakeappdriver.implementations.PercentCoveredFrameAnalyzer;
import com.wakeappdriver.implementations.PerclosIndicator;
import com.wakeappdriver.implementations.ServiceIntentHandler;
import com.wakeappdriver.implementations.SimpleAlerter;
import com.wakeappdriver.implementations.SpeechAlerter;
import com.wakeappdriver.implementations.WakeAppPredictor;
import com.wakeappdriver.interfaces.Alerter;
import com.wakeappdriver.interfaces.Indicator;
import com.wakeappdriver.interfaces.IntentHandler;
import com.wakeappdriver.interfaces.Predictor;
import com.wakeappdriver.tasks.DetectorTask;
import com.wakeappdriver.tasks.JavaCameraTask;
import com.wakeappdriver.enums.Enums.*;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

public class GoService  extends Service{
	private static final String TAG = "WAD";


	private CascadeClassifier mFaceDetector;
	private CascadeClassifier mRightEyeDetector;

	private FrameQueueManager queueManager;
	private List<FrameAnalyzer> frameAnalyzers;
	private DetectorTask detector;

	private Thread detectionTask;
	private List<Thread> frameAnalyzerTasks;

	private int frameWidth;
	private int frameHeight;

	private IntentMessenger intentMessenger;
	private Action[] actions = {Action.WAD_ACTION_GET_PREDICITON};
	private IntentHandler intentHandler;

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
		Log.d(TAG, "staring service");

		this.intentHandler = new ServiceIntentHandler();
		this.intentMessenger = new IntentMessenger(this, actions, intentHandler);	
		this.intentMessenger.register();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		frameWidth = intent.getIntExtra("frameWidth", 800);
		frameHeight = intent.getIntExtra("frameHeight", 600);
		this.init(); //initialize WAD data structures
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
		return Service.START_NOT_STICKY;

	}


	@Override
	public void onDestroy() {
		Log.d(TAG, Thread.currentThread().getName() + " :: service has been closed");

		super.onDestroy();
		this.queueManager.killManager();
		for (Thread t : frameAnalyzerTasks) {
			try {
				t.join(1000);
			} catch (InterruptedException e) {
				Log.e(TAG, Thread.currentThread().getName() + " :: couldn't kill thread " + t.getName());
			}
		}

		detector.killDetector();
		try {
			detectionTask.join(detector.getWindowSize());
		} catch (InterruptedException e) {
			Log.e(TAG, Thread.currentThread().getName() + " :: couldn't kill thread " + detectionTask.getName());
		}
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

		Alerter alerter;
		String alertActivity = ConfigurationParameters.ALERT_ACTIVITY;
		try {
			Class<?> clazz = Class.forName(ConfigurationParameters.getAlertType());
			Constructor<?> constructor = clazz.getConstructor(Context.class);
			alerter = (Alerter)(constructor.newInstance(this));
		} catch (Exception e) {
			alerter = new SimpleAlerter(this);
		}
		Alerter noIdenAlerter = new SpeechAlerter(this,this.getString(R.string.no_iden_message));
		Alerter EmeregencyAlerter = new SpeechAlerter(this,this.getString(R.string.emergency_message));

		Alerter guiNoIdenAlerter = new GuiAlerter(this, alertActivity, intentMessenger, noIdenAlerter);
		Alerter guiEmeAlerterAlerter = new GuiAlerter(this, alertActivity, intentMessenger, EmeregencyAlerter);
		Alerter guiAlerter = new GuiAlerter(this, alertActivity, intentMessenger, alerter);

		WindowAnalyzer windowAnalyzer = new WindowAnalyzer(resultQueueList, indicatorList);
		Predictor predictor = new WakeAppPredictor();

		AlerterContainer alerterContainer = new AlerterContainer(guiAlerter, guiNoIdenAlerter, guiEmeAlerterAlerter);

		this.detector = new DetectorTask(alerterContainer, windowAnalyzer, predictor, false, null, null);

		this.detectionTask = new Thread(this.detector);
		detectionTask.setName("DetectionTask");
		detectionTask.start();

		EmergencyHandler emergencyHandler = new EmergencyHandler(detector);
		frameAnalyzer1.setEmergencyHandler(emergencyHandler);

		JavaCameraTask cameraRunnable = new JavaCameraTask(frameWidth, frameHeight, queueManager);
		new CameraHandlerThread().openCamera(cameraRunnable);
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
}
