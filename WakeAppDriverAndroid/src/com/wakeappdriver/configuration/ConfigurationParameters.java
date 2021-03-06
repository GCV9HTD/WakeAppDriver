package com.wakeappdriver.configuration;

import org.opencv.core.Core.MinMaxLocResult;

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
	 */
	public static int getWindowSize() {
		int windowSize = Constants.DEFAULT_WINDOW_SIZE;
		try {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
			String pref_window_size = sharedPreferences.getString("window_size", "");
			windowSize = Integer.parseInt(pref_window_size);
		}
		catch(Exception e) { }
		return windowSize * 1000;

	}

	public static void setWindowSize(int windowSize) {
		Editor editor = sharedPref.edit();
		editor.putInt("windowSize", windowSize);
		editor.apply();
	}

	public static int getMaxFrameQueueSize() {
		try{
			return sharedPref.getInt("maxFrameQueueSize", 20);
		} catch (Exception e) {
			return 20;
		}
	}

	public static void setMaxFrameQueueSize(int maxFrameQueueSize) {
		Editor editor = sharedPref.edit();
		editor.putInt("maxFrameQueueSize", maxFrameQueueSize);
		editor.apply();	
	}

	public static double getAlertThreshold() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		float pref_sensitivity = sharedPreferences.getInt("sesitivity_scale", 500);
		return 0.5 + pref_sensitivity/2500;
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
		if(audioFileName.equals("hapoel_beer_sheva"))
			return R.raw.hapoel_beer_sheva;
			
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
		int pref_volume = sharedPreferences.getInt("volume", 15);
		return pref_volume;
	}

	public static int getMinSamples() {
		try{
			return sharedPref.getInt("minSamples", 50);
		} catch (Exception e) {
			return 50;
		}
	}

	public static void setMinSamples(int minSamples) {
		Editor editor = sharedPref.edit();
		editor.putInt("minSamples", minSamples);
		editor.apply();	
	}

	public static int getLearningModeDuration() {
		return sharedPref.getInt("learningModeDuration", 2);
	}

	/**
	 * this mode is used for learning during the begining of the drive.
	 * @param learningModeDuration - the number of windows that the system will learn the driver but won't raise alerts.
	 */
	public static void setLearningModeDuration(int learningModeDuration) {
		Editor editor = sharedPref.edit();
		editor.putInt("learningModeDuration", learningModeDuration);
		editor.apply();	
	}

	public static int getDurationBetweenAlerts() {
		return sharedPref.getInt("durationBetweenAlerts", 0);
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
		try{
			
			return sharedPref.getFloat("blinkLimit", (float) 0.4);
		} catch (Exception e) {
			return 0.4;
		}
	}

	public static void setBlinkLimit(double blinkLimit) {
		Editor editor = sharedPref.edit();
		editor.putFloat("blinkLimit", (float)blinkLimit);
		editor.apply();	
	}
	public static long getEmergencyCooldown() {
		return sharedPref.getLong("emergencyCooldown", (long) 10000);
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
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPreferences.getBoolean("switch_data_collector", false);
	}

	public static void setCollectMode(boolean isInCollectingMode){
		Editor editor = sharedPref.edit();
		editor.putBoolean("collectMode", isInCollectingMode);
		editor.apply();
	}

	public static int getDrowsinessLevel(){
		return sharedPref.getInt("drowsinessLevel", -1);
	}

	public static boolean toDisplayBar(){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPreferences.getBoolean("display_drowsiness_bar", true);
	}

	public static void setDrowsinessLevel(int drowsinessLevel){
		Editor editor = sharedPref.edit();
		editor.putInt("drowsinessLevel", drowsinessLevel);
		editor.apply();
	}

	public static String getDrowsinessPromptMethod(){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPreferences.getString("data_collector_mode", "gui");
	}

	public static void setDrowsinessPromptMethod(String drowsinessLevelMethod){
		Editor editor = sharedPref.edit();
		editor.putString("drowsinessLevelMethod", drowsinessLevelMethod);
		editor.apply();
	}

	public static boolean getDisplayBar(Context context){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getBoolean("display_drowsiness_bar", true);
	}


	public static int getNumOfWindowsBetweenTwoQueries(){
		//must be at least 2 to work properly
		return sharedPref.getInt("numOfWindowsBetweenTwoQueries", 6);
	}

	public static void setNumOfWindowsBetweenTwoQueries(int numOfWindowsBetweenTwoQueries){
		Editor editor = sharedPref.edit();
		editor.putInt("numOfWindowsBetweenTwoQueries", numOfWindowsBetweenTwoQueries);
		editor.apply();
	}
	
	
	public static boolean isImageRecorderOn(){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPreferences.getBoolean("switch_image_recorder", false);
	}
	
	public static boolean isAlertEnable(){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPreferences.getBoolean("check_box_enable_alert", true);
	}
	
	
	
	
	
}