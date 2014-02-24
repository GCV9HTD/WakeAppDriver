package com.wakeappdriver.tasks;

import java.util.List;

import android.util.Log;

import com.wakeappdriver.classes.WindowAnalyzer;
import com.wakeappdriver.interfaces.Alerter;
import com.wakeappdriver.interfaces.Indicator;
import com.wakeappdriver.interfaces.Predictor;

public class DetectorTask implements Runnable{
    private static final String TAG = "AWD";
	
	private WindowAnalyzer windowsAnalyzer;
	private Alerter alerter;
	private Predictor predictor;
	private double alertThreshold;
	private int windowSize;
	
	public DetectorTask(Alerter alerter, WindowAnalyzer windowsAnalyzer, 
			Predictor predictor, double alertThreshold, int windowSize ){
		Log.e(TAG, Thread.currentThread().getName() + ":: starting Detector Task");
		this.windowsAnalyzer = windowsAnalyzer;
		this.alerter = alerter;
		this.predictor = predictor;
		this.alertThreshold = alertThreshold;
		this.windowSize = windowSize;
		
	}
	@Override
	public void run() {	
		Log.e(TAG, Thread.currentThread().getName() + ":: running Detector Task");

		List<Indicator> indicators = null;
		double prediction = 0;
		while(true){
			
			//sleep for windowsize miliseconds
			try {
				Log.e(TAG, Thread.currentThread().getName() + ":: sleeping for " +  windowSize +" ms");

				Thread.sleep(windowSize);
			} catch (InterruptedException e) {
				e.printStackTrace();
				
				return;
			}
			
			//get indicators from window analyzer and predict drowsiness using
			//the predictor
			Log.e(TAG, Thread.currentThread().getName() + ":: getting indicators");

			indicators = this.windowsAnalyzer.calculateIndicators();

			Log.e(TAG, Thread.currentThread().getName() + ":: predicting drowsiness");

			prediction = predictor.predictDrowsiness(indicators);
			
			if (prediction > alertThreshold){
				Log.e(TAG, Thread.currentThread().getName() + ":: reached threshhold! : " + prediction +" > " + alertThreshold);

				//driver is sleepy -> alert him
				alerter.alert();
			} else {
				Log.e(TAG, Thread.currentThread().getName() + ":: did not reach threshhold");

			}
			

		}
	}

}
