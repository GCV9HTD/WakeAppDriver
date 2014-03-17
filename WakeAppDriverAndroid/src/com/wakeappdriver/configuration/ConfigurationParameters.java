package com.wakeappdriver.configuration;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;

import com.wakeappdriver.R;

public class ConfigurationParameters {
	
	private SharedPreferences sharedPref;
	
	public ConfigurationParameters(Context context) {
		Resources res = context.getResources();
		this.sharedPref = context.getSharedPreferences(res.getString(R.string.awd_config_fname), Activity.MODE_PRIVATE);
	}

	public int getWindowSize() {
		return sharedPref.getInt("windowSize", 15000);
	}
	

	public void setWindowSize(int windowSize) {
		Editor editor = this.sharedPref.edit();
		editor.putInt("windowSize", windowSize);
		editor.apply();
	}
	
	public int getDrowsinessAssumption(){
		return sharedPref.getInt("drowsinessAssumption", -1);
	}
	
	public void setDrosinessAssumption(int drowsinessAssumption){
		Editor editor = this.sharedPref.edit();
		editor.putInt("drowsinessAssumption", drowsinessAssumption);
		editor.apply();
	}
	
	public int getNumOfWindowsBetweenTwoQueries(){
		return sharedPref.getInt("numOfWindowsBetweenTwoQueries", 10);
	}
	
	public void setNumOfWindowsBetweenTwoQueries(int numOfWindowsBetweenTwoQueries){
		Editor editor = this.sharedPref.edit();
		editor.putInt("numOfWindowsBetweenTwoQueries", numOfWindowsBetweenTwoQueries);
		editor.apply();
	}
	
	public boolean getCollectMode(){
		return sharedPref.getBoolean("collectMode", false);
	}
	
	public void setCollectMode(boolean isInCollectingMode){
		Editor editor = this.sharedPref.edit();
		editor.putBoolean("collectMode", isInCollectingMode);
		editor.apply();
	}

	public int getMaxFrameQueueSize() {
		return sharedPref.getInt("maxFrameQueueSize", 20);
	}

	public void setMaxFrameQueueSize(int maxFrameQueueSize) {
		Editor editor = this.sharedPref.edit();
		editor.putInt("maxFrameQueueSize", maxFrameQueueSize);
		editor.apply();	
	}

	public double getAlertThreshold() {
		return sharedPref.getFloat("alertThreshold", (float) 0.15);
	}

	public void setAlertThreshold(double alertThreshold) {
		Editor editor = this.sharedPref.edit();
		editor.putFloat("alertThreshold", (float)alertThreshold);
		editor.apply();	
	}

	public String getAlertType() {
		return sharedPref.getString("alertType", "SimpleAlerter");
	}

	public void setAlertType(String alertType) {
		Editor editor = this.sharedPref.edit();
		editor.putString("alertType", alertType);
		editor.apply();
	}

	public int getMinSamples() {
		return sharedPref.getInt("minSamples", 50);
	}

	public void setMinSamples(int minSamples) {
		Editor editor = this.sharedPref.edit();
		editor.putInt("minSamples", minSamples);
		editor.apply();	
	}

	public int getLearningModeDuration() {
		return sharedPref.getInt("learningModeDuration", 2);
	}

	public void setLearningModeDuration(int learningModeDuration) {
		Editor editor = this.sharedPref.edit();
		editor.putInt("learningModeDuration", learningModeDuration);
		editor.apply();	
	}

	public int getDurationBetweenAlerts() {
		return sharedPref.getInt("durationBetweenAlerts", 3);
	}

	public void setDurationBetweenAlerts(int durationBetweenAlerts) {
		Editor editor = this.sharedPref.edit();
		editor.putInt("durationBetweenAlerts", durationBetweenAlerts);
		editor.apply();	
	}

	public boolean getCameraMode() {
		return sharedPref.getBoolean("cameraMode", false);	// true = native, false = java
	}

	public void setCameraMode(boolean cameraMode) {
		Editor editor = this.sharedPref.edit();
		editor.putBoolean("cameraMode", cameraMode);
		editor.apply();	
	}
	
}
