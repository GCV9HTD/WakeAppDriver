package com.wakeappdriver.tasks;

import java.util.List;

import com.wakeappdriver.classes.WindowAnalyzer;
import com.wakeappdriver.interfaces.Alerter;
import com.wakeappdriver.interfaces.Indicator;
import com.wakeappdriver.interfaces.Predictor;

public class DetectorTask implements Runnable{
	private WindowAnalyzer windowsAnalyzer;
	private Alerter alerter;
	private Predictor predictor;
	private double alertThreshold;
	private int windowSize;
	
	public DetectorTask(Alerter alerter, WindowAnalyzer windowsAnalyzer, 
			Predictor predictor, double alertThreshold, int windowSize ){
		
		this.windowsAnalyzer = windowsAnalyzer;
		this.alerter = alerter;
		this.predictor = predictor;
		this.alertThreshold = alertThreshold;
		this.windowSize = windowSize;
		
	}
	@Override
	public void run() {	
		List<Indicator> indicators = null;
		double prediction = 0;
		while(true){
			
			//sleep for windowsize miliseconds
			try {
				Thread.sleep(windowSize);
			} catch (InterruptedException e) {
				e.printStackTrace();
				
				return;
			}
			
			//get indicators from window analyzer and predict drowsiness using
			//the predictor
			indicators = this.windowsAnalyzer.calculateIndicators();
			prediction = predictor.predictDrowsiness(indicators);
			
			if (prediction > alertThreshold){
				//driver is sleepy -> alert him
				alerter.alert();
			}
			

		}
	}

}
