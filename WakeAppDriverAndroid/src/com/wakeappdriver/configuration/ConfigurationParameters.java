package com.wakeappdriver.configuration;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.wakeappdriver.R;
import com.wakeappdriver.configuration.Enums.StartMode;

public class ConfigurationParameters {
	
	private static SharedPreferences sharedPref;
	private static Context mContext;
			
	public static void init(Context context){
		Resources res = context.getResources();
		sharedPref = context.getSharedPreferences(res.getString(R.string.awd_config_fname), Activity.MODE_PRIVATE);
		mContext = context;
	}
	
	/**
	 * Get window size in milisec. Default value in defined as Constants.DEFALT_WINDOW_SIZE.
	 * @return
	 */
	public static int getWindowSize() {
		int windowSize = Constants.DEFAULT_WINDOW_SIZE;
		try {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
			String pref_window_size = sharedPreferences.getString("window_size", "");
			System.out.println("#Asa pref_window_size = " + pref_window_size);
			windowSize = Integer.parseInt(pref_window_size);
		}
		catch(Exception e) {System.out.println("#Asa error parse");}
		return windowSize * 1000;
		
	}

	public static void setWindowSize(int windowSize) {
		Editor editor = sharedPref.edit();
		editor.putInt("windowSize", windowSize);
		editor.apply();
	}

	public static int getMaxFrameQueueSize() {
		return sharedPref.getInt("maxFrameQueueSize", 20);
	}

	public static void setMaxFrameQueueSize(int maxFrameQueueSize) {
		Editor editor = sharedPref.edit();
		editor.putInt("maxFrameQueueSize", maxFrameQueueSize);
		editor.apply();	
	}

	public static double getAlertThreshold() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		float pref_threshold = sharedPreferences.getInt("sesitivity_scale", 300);
		final int MAX_THRESHOLD = 1000;
		//final float threshold = (float) (1 - (Math.log(MAX_THRESHOLD - pref_threshold) / Math.log(MAX_THRESHOLD)));
		// Sensitivity scale is between [0..1] so value 0.95 means high sensitivity, which is low threshold.
		return 1 - (pref_threshold / MAX_THRESHOLD);
	}

	public static void setAlertThreshold(double alertThreshold) {
		Editor editor = sharedPref.edit();
		editor.putFloat("alertThreshold", (float)alertThreshold);
		editor.apply();	
	}

	public static String getAlertType() {
		return sharedPref.getString("alertType", "SimpleAlerter");
	}
	
	public static int getAlert(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String audioFileName = sharedPreferences.getString("alert_type", "ship_bell");
		return getAudioFile(audioFileName);
	}

	private static int getAudioFile(String audioFileName) {
		if(audioFileName.equals("ship_bell"))
			return R.raw.ship_bell;
		if(audioFileName.equals("speaking_voice_wake_up_call"))
			return R.raw.speaking_voice_wake_up_call;
		if(audioFileName.equals("car_horn"))
			return R.raw.car_horn;
		// Add more alerts here.
	return 0;
}

	public static void setAlertType(String alertType) {
		Editor editor = sharedPref.edit();
		editor.putString("alertType", alertType);
		editor.apply();
	}
	
	public static float getVolume(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		int pref_volume = sharedPreferences.getInt("volume", 1);
		/*
		 * SeekBar values are not linear, so the volume won't be consistent. It means that values
		 * below 0.5 will behave differently then values above 0.5.
		 * We make a transformation so the volume will behave "smoothly" as a linear scale. 
		 */
		final int MAX_VOLUME = 1000;
		final float volume = (float) (1 - (Math.log(MAX_VOLUME - pref_volume) / Math.log(MAX_VOLUME)));
		return volume;
	}

	public static int getMinSamples() {
		return sharedPref.getInt("minSamples", 50);
	}

	public static void setMinSamples(int minSamples) {
		Editor editor = sharedPref.edit();
		editor.putInt("minSamples", minSamples);
		editor.apply();	
	}

	public static int getLearningModeDuration() {
		return sharedPref.getInt("learningModeDuration", 2);
	}

	public static void setLearningModeDuration(int learningModeDuration) {
		Editor editor = sharedPref.edit();
		editor.putInt("learningModeDuration", learningModeDuration);
		editor.apply();	
	}

	public static int getDurationBetweenAlerts() {
		return sharedPref.getInt("durationBetweenAlerts", 3);
	}

	public static void setDurationBetweenAlerts(int durationBetweenAlerts) {
		Editor editor = sharedPref.edit();
		editor.putInt("durationBetweenAlerts", durationBetweenAlerts);
		editor.apply();	
	}

	public static boolean getCameraMode() {
		return sharedPref.getBoolean("cameraMode", false);	// true = native, false = java
	}

	public static void setCameraMode(boolean cameraMode) {
		Editor editor = sharedPref.edit();
		editor.putBoolean("cameraMode", cameraMode);
		editor.apply();	
	}

	public static int getClosedAlertLimit() {
		return sharedPref.getInt("closedAlertLimit", 1000);
	}
	
	public static void setClosedAlertLimit(int closedAlertLimit) {
		Editor editor = sharedPref.edit();
		editor.putInt("closedAlertLimit", closedAlertLimit);
		editor.apply();		
	}
	
	public static double getBlinkLimit() {
		return sharedPref.getFloat("blinkLimit", (float) 0.4);
	}
	
	public static void setBlinkLimit(double blinkLimit) {
		Editor editor = sharedPref.edit();
		editor.putFloat("blinkLimit", (float)blinkLimit);
		editor.apply();	
	}
	public static long getEmergencyCooldown() {
		return sharedPref.getLong("emergencyCooldown", (long) 2000);
	}
	
	public static void setEmergencyCooldown(long emergencyCooldown) {
		Editor editor = sharedPref.edit();
		editor.putLong("emergencyCooldown", emergencyCooldown);
		editor.apply();	
	}
	public static StartMode getStartMode() {
		String modeName = sharedPref.getString("StartMode", StartMode.ACTIVITY.name());
		return StartMode.toStartMode(modeName);
	}
	
	public static void setStartMode(StartMode startMode) {
		Editor editor = sharedPref.edit();
		editor.putString("StartMode", startMode.name());
		editor.apply();		
	}

	public static boolean getCollectMode(){
		return sharedPref.getBoolean("collectMode", false);
	}
	
	public static void setCollectMode(boolean isInCollectingMode){
		Editor editor = sharedPref.edit();
		editor.putBoolean("collectMode", isInCollectingMode);
		editor.apply();
	}

	public static int getDrowsinessAssumption(){
		return sharedPref.getInt("drowsinessAssumption", -1);
	}
	
	public static boolean toDisplayBar(){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPreferences.getBoolean("display_drowsiness_bar", true);
	}
	
	public static void setDrosinessAssumption(int drowsinessAssumption){
		Editor editor = sharedPref.edit();
		editor.putInt("drowsinessAssumption", drowsinessAssumption);
		editor.apply();
	}
	
	public static int getNumOfWindowsBetweenTwoQueries(){
		//must be at least 2 to work properly
		return sharedPref.getInt("numOfWindowsBetweenTwoQueries", 2);
	}
	
	public static void setNumOfWindowsBetweenTwoQueries(int numOfWindowsBetweenTwoQueries){
		Editor editor = sharedPref.edit();
		editor.putInt("numOfWindowsBetweenTwoQueries", numOfWindowsBetweenTwoQueries);
		editor.apply();
	}
}