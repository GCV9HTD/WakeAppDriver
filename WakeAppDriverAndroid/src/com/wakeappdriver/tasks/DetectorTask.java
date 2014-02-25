package com.wakeappdriver.tasks;

import java.util.HashMap;

import android.util.Log;

import com.wakeappdriver.classes.WindowAnalyzer;
import com.wakeappdriver.enums.IndicatorType;
import com.wakeappdriver.interfaces.Alerter;
import com.wakeappdriver.interfaces.Indicator;
import com.wakeappdriver.interfaces.Predictor;

public class DetectorTask implements Runnable{
    private static final String TAG = "WAD";
	
	private WindowAnalyzer windowsAnalyzer;
	private Alerter alerter;
	private Predictor predictor;
	private double alertThreshold;
	private int windowSize;
	private volatile boolean isAlive;
	
	public DetectorTask(Alerter alerter, WindowAnalyzer windowsAnalyzer, 
			Predictor predictor, double alertThreshold, int windowSize ){
		Log.i(TAG, Thread.currentThread().getName() + ":: starting Detector Task");
		this.windowsAnalyzer = windowsAnalyzer;
		this.alerter = alerter;
		this.predictor = predictor;
		this.alertThreshold = alertThreshold;
		this.windowSize = windowSize;
		this.isAlive = true;
	}
	@Override
	public void run() {	
		Log.i(TAG, Thread.currentThread().getName() + ":: running Detector Task");

		HashMap<IndicatorType,Indicator> indicators = null;
		double prediction = 0;
		while(isAlive){
			Log.v(TAG, Thread.currentThread().getName() + ":: isAlive? " + isAlive);
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

			indicators = this.windowsAnalyzer.calculateIndicators();

			Log.v(TAG, Thread.currentThread().getName() + ":: predicting drowsiness");

			prediction = predictor.predictDrowsiness(indicators);
			
			if (prediction > alertThreshold){
				Log.i(TAG, Thread.currentThread().getName() + ":: reached threshhold! : " + prediction + " > " + alertThreshold);
				//driver is sleepy -> alert him
				alerter.alert();
			} else {
				Log.i(TAG, Thread.currentThread().getName() + ":: did not reach threshhold : " + prediction + " < " + alertThreshold);
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
