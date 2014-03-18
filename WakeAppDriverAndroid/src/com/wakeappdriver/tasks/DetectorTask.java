package com.wakeappdriver.tasks;

import java.util.HashMap;
import java.util.Calendar;
import java.util.Date;
import android.util.Log;

import android.os.Environment;
import android.os.Handler;
import com.wakeappdriver.classes.WadFTPClient;
import com.wakeappdriver.classes.AlerterContainer;
import com.wakeappdriver.classes.WindowAnalyzer;
import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.interfaces.Alerter;
import com.wakeappdriver.interfaces.Indicator;
import com.wakeappdriver.interfaces.Predictor;
import com.wakeappdriver.enums.Enums.*;
import com.wakeappdriver.gui.GoActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;



public class DetectorTask implements Runnable {
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
	private boolean collectMode;
	private final String collectFolder = "WakeUpDriver";
	private final String ftpStorePath  = "/public_html/WakeAppDriver/";
	private OutputStreamWriter logFile;
	private int numOfWindowsBetweenTwoQueries;
	private int windowNumber;
	private String android_id;

	private HashMap<IndicatorType,Indicator> indicators;
	private volatile boolean isAlive;
	private boolean emergencyMode = false;
	private Object detectorLock;
	private Handler uiHandler;

	public DetectorTask(AlerterContainer alerters, WindowAnalyzer windowsAnalyzer, 
			Predictor predictor, boolean collectMode, Handler uiHandler, String android_id){
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

		this.collectMode = collectMode;
		this.windowNumber = 0;
		this.numOfWindowsBetweenTwoQueries = ConfigurationParameters.getNumOfWindowsBetweenTwoQueries();
		this.android_id = android_id;
		this.indicators = null;
		this.uiHandler = uiHandler;
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

			if(!isAlive){
				try {
					logFile.close();
				} 
				catch (IOException e) {
					Log.e(TAG, "File close failed: " + e.toString());
				}
				return;
			}

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
			if(collectMode){
				writeToFile();
			}
			nextSleepTime = this.windowSize;
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

	private void writeToFile() {
		String log = "";
		String delimiter = ",";
		try {
			for(Indicator currIndicator : indicators.values()){
				log += currIndicator.getValue() + delimiter;
			}
			log+= new SimpleDateFormat("HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
			if(windowNumber>=numOfWindowsBetweenTwoQueries){
				windowNumber = 0;

				triggerVoiceRecognition();
				//create new thread to update drowsiness measure
				int drowsinessAssumption  = ConfigurationParameters.getDrowsinessAssumption();
				if(drowsinessAssumption != -1){
					log += delimiter + drowsinessAssumption;
				}
			}
			log+="\r\n";
			logFile.write(log);
		}
		catch (IOException e) {
			Log.e(TAG, "File write failed: " + e.toString());
		} 

	}


	private void triggerVoiceRecognition(){
		uiHandler.sendEmptyMessage(GoActivity.VOICE_RECOGNITION_CODE);		
	}
}
