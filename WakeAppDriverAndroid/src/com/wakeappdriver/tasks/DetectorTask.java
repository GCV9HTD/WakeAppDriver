package com.wakeappdriver.tasks;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebView.FindListener;

import com.wakeappdriver.R;
import com.wakeappdriver.classes.NoIdentificationAlerter;
import com.wakeappdriver.classes.WadFTPClient;
import com.wakeappdriver.classes.WindowAnalyzer;
import com.wakeappdriver.enums.IndicatorType;
import com.wakeappdriver.interfaces.Alerter;
import com.wakeappdriver.interfaces.Indicator;
import com.wakeappdriver.interfaces.Predictor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;


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
	
	private boolean collectMode;
	private final String collectFolder = "WakeUpDriver";
	private final String ftpStorePath  = "/public_html/WakeAppDriver/";
	private OutputStreamWriter logFile;
	private int numOfWindowsBetweenTwoQueries;
	private int windowNumber;
	private int drowsinessMeasure;
	private String android_id;
	
	private volatile boolean isAlive;
	
	public DetectorTask(Alerter alerter, WindowAnalyzer windowsAnalyzer, 
			Predictor predictor, double alertThreshold, int windowSize, Alerter noIdenAlerter,
			int learningModeDuration, int durationBetweenAlerts, boolean collectMode, int numOfWindowsBetweenTwoQueries, String android_id){
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
		this.collectMode = collectMode;
		this.windowNumber = 0;
		this.numOfWindowsBetweenTwoQueries = numOfWindowsBetweenTwoQueries;
		this.drowsinessMeasure = 0;
		this.android_id = android_id;
	}
	@Override
	public void run() {	
		
		//remove this - only for debugging
		collectMode = true;
		
		Log.d(TAG, Thread.currentThread().getName() + ":: running Detector Task");

		HashMap<IndicatorType,Indicator> indicators = null;
		int windowsSinceLastAlert = 0;
		Double prediction = 0.0;
		
		if(collectMode){

				File externalDirectoryStorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File outDir=new File(externalDirectoryStorage,collectFolder);
				if (!outDir.exists()){
					outDir.mkdir();
				}
				else{
					WadFTPClient ftpclient = new WadFTPClient();
					boolean status = ftpclient.ftpConnect();
					if (status == true) {
						for(File currFile : outDir.listFiles()){
							try{
								FileInputStream fis = new FileInputStream(currFile);
								status = ftpclient.storeFile(ftpStorePath + currFile.getName(), fis);
								fis.close();
								if (status == true){
									currFile.delete();
								}
							}
							catch(Exception e){
								Log.d(TAG, "Error: could not send file " + currFile.getName() );
							}
						}
						ftpclient.ftpDisconnect();
					} 
				}
				
				String filename =this.android_id + "_" + new SimpleDateFormat("yyyy-MM-dd hh-mm-ss").format(new Date()) + ".csv";
				File outFile=new File(externalDirectoryStorage,collectFolder + "/"+filename);
				if (outFile.exists()) {
					outFile.delete();
				}
				try {
					logFile = new OutputStreamWriter(new FileOutputStream(outFile.getPath()));
				} catch (FileNotFoundException e) {
					Log.d(TAG, "Error: could not open log file " + outFile.getName() );
					collectMode = false;
				}

		}
		
		while(isAlive){
			windowNumber++;
			//sleep for windowsize miliseconds
			try {
				Log.v(TAG, Thread.currentThread().getName() + ":: sleeping for " + windowSize + " ms");
				Thread.sleep(windowSize);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
			
			if(!isAlive){
				try {
					logFile.close();
				} 
				catch (IOException e) {
					Log.e(TAG, "File close failed: " + e.toString());
				}
				return;
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
			if(collectMode){
				writeToFile(indicators.values());
			}
		}
		try {
			logFile.close();
		} 
		catch (IOException e) {
			Log.e(TAG, "File close failed: " + e.toString());
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
	
	private void writeToFile(Collection<Indicator> indicators) {
		String log = "";
		String delimiter = ",";
        try {
        	for(Indicator currIndicator : indicators){
        		log += currIndicator.getValue() + delimiter;
        	}
        	log+= new SimpleDateFormat("HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
        	if(windowNumber>=numOfWindowsBetweenTwoQueries){
        		windowNumber = 0;
        		//create new thread to update drowsiness measure
        		log += delimiter + drowsinessMeasure;
        	}
        	log+="\r\n";
        	logFile.write(log);
        }
        catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
        } 
         
    }

}
