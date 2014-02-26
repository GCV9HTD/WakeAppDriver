package com.wakeappdriver.configuration;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.wakeappdriver.R;

public class ConfigurationParameters {
	
	private SharedPreferences sharedPref;

	private int windowSize;
	private int maxFrameQueueSize;
	private double alertThreshold;
	private String alertType;
	private int minSamples;
	private int learningModeDuration;
	private int durationBetweenAlerts;
	private boolean cameraMode;
	
	public ConfigurationParameters(Context context) {
		Resources res = context.getResources();
		this.sharedPref = context.getSharedPreferences(res.getString(R.string.awd_config_fname), Activity.MODE_PRIVATE);
		this.windowSize = sharedPref.getInt(res.getString(R.string.awd_config_window_sizw_key), 15000);
		this.maxFrameQueueSize = sharedPref.getInt("maxFrameQueueSize", 20);
		this.alertThreshold = sharedPref.getFloat(res.getString(R.string.awd_config_threshold_key), (float) 0.15);
		this.alertType = sharedPref.getString("alertType", "SimpleAlerter");
		this.minSamples = sharedPref.getInt("minSamples", 50);
		this.learningModeDuration = sharedPref.getInt("learningModeDuration", 2);
		this.durationBetweenAlerts = sharedPref.getInt("durationBetweenAlerts", 3);
		this.cameraMode = sharedPref.getBoolean("cameraMode", false);	// true = native, false = java
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public int getMaxFrameQueueSize() {
		return maxFrameQueueSize;
	}

	public void setMaxFrameQueueSize(int maxFrameQueueSize) {
		this.maxFrameQueueSize = maxFrameQueueSize;
	}

	public double getAlertThreshold() {
		return alertThreshold;
	}

	public void setAlertThreshold(double alertThreshold) {
		this.alertThreshold = alertThreshold;
	}

	public String getAlertType() {
		return alertType;
	}

	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}

	public int getMinSamples() {
		return minSamples;
	}

	public void setMinSamples(int minSamples) {
		this.minSamples = minSamples;
	}

	public int getLearningModeDuration() {
		return learningModeDuration;
	}

	public void setLearningModeDuration(int learningModeDuration) {
		this.learningModeDuration = learningModeDuration;
	}

	public int getDurationBetweenAlerts() {
		return durationBetweenAlerts;
	}

	public void setDurationBetweenAlerts(int durationBetweenAlerts) {
		this.durationBetweenAlerts = durationBetweenAlerts;
	}

	public boolean getCameraMode() {
		return cameraMode;
	}

	public void setCameraMode(boolean cameraMode) {
		this.cameraMode = cameraMode;
	}
	
}
