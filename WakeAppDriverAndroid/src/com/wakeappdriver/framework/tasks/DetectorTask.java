package com.wakeappdriver.framework.tasks;

import java.util.HashMap;

import android.content.Intent;
import android.util.Log;
import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.configuration.Constants;
import com.wakeappdriver.configuration.Enums.*;
import com.wakeappdriver.framework.WindowAnalyzer;
import com.wakeappdriver.framework.datacollection.DataCollector;
import com.wakeappdriver.framework.interfaces.Indicator;
import com.wakeappdriver.framework.interfaces.Predictor;
import com.wakeappdriver.framework.services.ListenerService;



public class DetectorTask implements Runnable {
	private static final String TAG = "WAD";

	private WindowAnalyzer windowAnalyzer;
	private Predictor predictor;
	private double alertThreshold;
	private int windowSize;

	private int learningModeDuration;
	private int durationBetweenAlerts;

	private boolean alertMode = false;

	private HashMap<IndicatorType,Indicator> indicators;
	private volatile boolean isAlive;
	private boolean emergencyMode = false;
	private Object detectorLock;
	private int windowNumber;
	private int numOfWindowsBetweenTwoQueries;
	private boolean collectMode;
	private DataCollector dataCollector;
	private ListenerService listenerService;
	
	public DetectorTask(WindowAnalyzer windowsAnalyzer, 
			Predictor predictor, DataCollector dataCollector, ListenerService listenerService){
		Log.d(TAG, Thread.currentThread().getName() + "#Asa :: starting Detector Task");
		this.windowAnalyzer = windowsAnalyzer;
		this.predictor = predictor;
		this.alertThreshold = ConfigurationParameters.getAlertThreshold();
		this.windowSize = ConfigurationParameters.getWindowSize();
		this.isAlive = true;
		this.learningModeDuration = ConfigurationParameters.getLearningModeDuration();
		this.durationBetweenAlerts = ConfigurationParameters.getDurationBetweenAlerts();
		this.detectorLock = new Object();
		this.indicators = null;
		this.windowNumber = 0;
		this.numOfWindowsBetweenTwoQueries = ConfigurationParameters.getNumOfWindowsBetweenTwoQueries();
		this.dataCollector = dataCollector;
		this.listenerService = listenerService;
		this.collectMode = ConfigurationParameters.getCollectMode();
		Log.d(TAG, Thread.currentThread().getName() + "#Asa :: done");
	}
	@Override
	public void run() {	
		Log.d(TAG, Thread.currentThread().getName() + ":: running Detector Task");


		int windowsSinceLastAlert = 0;
		Double prediction = 0.0;
		boolean isEmergency = false;
		long startSleepTS = 0;
		long sleepDuration = 0;
		long nextSleepTime = this.windowSize;


		if (this.dataCollector != null && !this.dataCollector.init()){
			this.dataCollector = null;
		}
		while(isAlive){
			windowNumber++;
			synchronized(detectorLock){
				try {
					Log.v(TAG, Thread.currentThread().getName() + ":: sleeping for " + windowSize + " ms");

					if(!this.emergencyMode){

						startSleepTS = System.currentTimeMillis();
						detectorLock.wait(nextSleepTime);
						sleepDuration = System.currentTimeMillis() - startSleepTS;
					}
					isEmergency = this.emergencyMode;
					this.emergencyMode = false;

				} catch (InterruptedException e) {
					continue;
				}
			}

			if(isEmergency){
				Log.i(TAG, Thread.currentThread().getName() + ":: emergency alert");
				
				this.listenerService.forceStartActivity(Constants.MONITOR_ACTIVITY);
				Intent intent = new Intent(Action.WAD_ACTION_ALERT.name());
				this.listenerService.sendBroadcast(intent);

				this.alertMode = false;
				windowsSinceLastAlert = this.durationBetweenAlerts;

				if(this.windowSize - sleepDuration > 0){
					nextSleepTime = this.windowSize - sleepDuration;
				}
				else {
					nextSleepTime = this.windowSize;
				}
				continue;
			}
			//get indicators from window analyzer and predict drowsiness using
			//the predictor
			Log.v(TAG, Thread.currentThread().getName() + ":: getting indicators");

			indicators = this.windowAnalyzer.calculateIndicators();

			Log.v(TAG, Thread.currentThread().getName() + ":: predicting drowsiness");

			prediction = predictor.predictDrowsiness(indicators);
			
			if(prediction != null){
				Intent intent = new Intent(Action.WAD_ACTION_UPDATE_PREDICITON.name());
				intent.putExtra(Constants.UPDATE_PRED_KEY, prediction);
				this.listenerService.sendBroadcast(intent);
			}
			
			if (alertMode) {
				if (prediction == null) {	// system can't identify the driver
					Log.i(TAG, Thread.currentThread().getName() + ":: did not recognize driver");
					
					this.listenerService.forceStartActivity(Constants.MONITOR_ACTIVITY);
					Intent intent = new Intent(Action.WAD_ACTION_NO_IDEN.name());
					this.listenerService.sendBroadcast(intent);
					
					this.alertMode = false;
					windowsSinceLastAlert = this.durationBetweenAlerts;
				}
				else if (prediction > alertThreshold){	//driver is sleepy -> alert him
					Log.i(TAG, Thread.currentThread().getName() + ":: reached threshhold! : " + prediction + " > " + alertThreshold);
					
					this.listenerService.forceStartActivity(Constants.MONITOR_ACTIVITY);
					Intent intent = new Intent(Action.WAD_ACTION_ALERT.name());
					this.listenerService.sendBroadcast(intent);
					
					this.alertMode = false;
					windowsSinceLastAlert = this.durationBetweenAlerts;
				} else {	// driver is aware
					Log.i(TAG, Thread.currentThread().getName() + ":: did not reach threshhold : " + prediction + " < " + alertThreshold);
				}
			}
			else {
				if (learningModeDuration > 0)
					learningModeDuration--;
				else if (windowsSinceLastAlert > 0)
					windowsSinceLastAlert--;
				else {
					alertMode = true;
				}
			}
			if(windowNumber == numOfWindowsBetweenTwoQueries){
				windowNumber = 0;
			}
			if(this.dataCollector!=null){
				this.dataCollector.logCurrWindow(indicators.values(), windowNumber);
				//prompt the user to insert his drowsiness level
				if(windowNumber + 1 == numOfWindowsBetweenTwoQueries){
					Intent intent = new Intent(Action.WAD_ACTION_PROMPT_USER.name());
					this.listenerService.forceStartActivity(Constants.MONITOR_ACTIVITY);
					this.listenerService.sendBroadcast(intent);
				}
			}
			
			
			nextSleepTime = this.windowSize;
		}
		
	}

	public void killDetector() {
		Log.v(TAG, Thread.currentThread().getName() + ":: killed detector");
		if(dataCollector != null) 
			dataCollector.destroy();
		this.isAlive = false;
	}
	
	public int getWindowSize() {
		return this.windowSize;
	}


	public void emergency(){
		synchronized(detectorLock){
			if (learningModeDuration == 0){
				this.emergencyMode = true;
				detectorLock.notifyAll();
			}
		}
	}	
}
