package com.wakeappdriver.tasks;

import java.util.HashMap;

import android.util.Log;

import com.wakeappdriver.classes.AlerterContainer;
import com.wakeappdriver.classes.WindowAnalyzer;
import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.interfaces.Alerter;
import com.wakeappdriver.interfaces.Indicator;
import com.wakeappdriver.interfaces.Predictor;
import com.wakeappdriver.enums.Enums.*;

public class DetectorTask implements Runnable{
    private static final String TAG = "WAD";
	
	private WindowAnalyzer windowAnalyzer;
	private Alerter alerter;
	private Predictor predictor;
	private double alertThreshold;
	private int windowSize;
	private Alerter noIdenAlerter;
	private Alerter emergencyAlerter;
	
	private int learningModeDuration;
	private int durationBetweenAlerts;
	
	private boolean alertMode = false;
	private volatile boolean isAlive;
	private boolean emergencyMode = false;
	private Object detectorLock;
	
	public DetectorTask(AlerterContainer alerters, WindowAnalyzer windowsAnalyzer, 
						Predictor predictor){
		Log.d(TAG, Thread.currentThread().getName() + ":: starting Detector Task");
		this.windowAnalyzer = windowsAnalyzer;
		this.alerter = alerters.getGeneralAlerter();
		this.predictor = predictor;
		this.alertThreshold = ConfigurationParameters.getAlertThreshold();
		this.windowSize = ConfigurationParameters.getWindowSize();
		this.isAlive = true;
		this.noIdenAlerter = alerters.getNoIdenAlerter();
		this.learningModeDuration = ConfigurationParameters.getLearningModeDuration();
		this.durationBetweenAlerts = ConfigurationParameters.getDurationBetweenAlerts();
		this.emergencyAlerter = alerters.getEmergencyAlerter();
		this.detectorLock = new Object();
	}
	@Override
	public void run() {	
		Log.d(TAG, Thread.currentThread().getName() + ":: running Detector Task");

		HashMap<IndicatorType,Indicator> indicators = null;
		int windowsSinceLastAlert = 0;
		Double prediction = 0.0;
		boolean isEmergency = false;
		long startSleepTS = 0;
		long sleepDuration = 0;
		long nextSleepTime = this.windowSize;
		while(isAlive){
			
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
			
			if(!isAlive) return;
			
			if(isEmergency){
				Log.i(TAG, Thread.currentThread().getName() + ":: emergency alert");
				emergencyAlerter.alert();
				
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
			
			if (alertMode) {
				if (prediction == null) {	// system can't identify the driver
					Log.i(TAG, Thread.currentThread().getName() + ":: did not recognize driver");
					noIdenAlerter.alert();
					this.alertMode = false;
					windowsSinceLastAlert = this.durationBetweenAlerts;
				}
				else if (prediction > alertThreshold){	//driver is sleepy -> alert him
					Log.i(TAG, Thread.currentThread().getName() + ":: reached threshhold! : " + prediction + " > " + alertThreshold);
					alerter.alert();
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
		}
		nextSleepTime = this.windowSize;
	}
	
	public void killDetector() {
		Log.v(TAG, Thread.currentThread().getName() + ":: killed detector");
		alerter.destroy();
		noIdenAlerter.destroy();
		emergencyAlerter.destroy();
		this.isAlive = false;
	}
	
	public int getWindowSize() {
		return windowSize;
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
