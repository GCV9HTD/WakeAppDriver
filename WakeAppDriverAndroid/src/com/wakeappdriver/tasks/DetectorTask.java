package com.wakeappdriver.tasks;

import java.util.HashMap;

import android.util.Log;

import com.wakeappdriver.classes.NoIdentificationAlerter;
import com.wakeappdriver.classes.WindowAnalyzer;
import com.wakeappdriver.enums.IndicatorType;
import com.wakeappdriver.interfaces.Alerter;
import com.wakeappdriver.interfaces.Indicator;
import com.wakeappdriver.interfaces.Predictor;

public class DetectorTask implements Runnable{
    private static final String TAG = "WAD";
	
	private WindowAnalyzer windowAnalyzer;
	private Alerter alerter;
	private Predictor predictor;
	private double alertThreshold;
	private int windowSize;
	private Alerter noIdenAlerter;
	
	private int learningModeDuration;
	private int durationBetweenAlerts;
	
	private boolean alertMode = false;
	private volatile boolean isAlive;
	
	public DetectorTask(Alerter alerter, WindowAnalyzer windowsAnalyzer, 
			Predictor predictor, double alertThreshold, int windowSize, Alerter noIdenAlerter,
			int learningModeDuration, int durationBetweenAlerts){
		Log.d(TAG, Thread.currentThread().getName() + ":: starting Detector Task");
		this.windowAnalyzer = windowsAnalyzer;
		this.alerter = alerter;
		this.predictor = predictor;
		this.alertThreshold = alertThreshold;
		this.windowSize = windowSize;
		this.isAlive = true;
		this.noIdenAlerter = noIdenAlerter;
		this.learningModeDuration = learningModeDuration;
		this.durationBetweenAlerts = durationBetweenAlerts;
	}
	@Override
	public void run() {	
		Log.d(TAG, Thread.currentThread().getName() + ":: running Detector Task");

		HashMap<IndicatorType,Indicator> indicators = null;
		int windowsSinceLastAlert = 0;
		Double prediction = 0.0;
		while(isAlive){
			//sleep for windowsize miliseconds
			try {
				Log.v(TAG, Thread.currentThread().getName() + ":: sleeping for " + windowSize + " ms");
				Thread.sleep(windowSize);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
			
			if(!isAlive) return;
			
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
	}
	
	public void killDetector() {
		Log.v(TAG, Thread.currentThread().getName() + ":: killed detector");
		alerter.destroy();
		this.isAlive = false;
	}
	
	public int getWindowSize() {
		return windowSize;
	}

}
