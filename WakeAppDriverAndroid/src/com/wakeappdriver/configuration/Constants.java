package com.wakeappdriver.configuration;

import android.media.AudioManager;

public class Constants {
	
	public static final String ALERT_ACTIVITY = "com.wakeappdriver.gui.AlertActivity";
	public static final String MONITOR_ACTIVITY = "com.wakeappdriver.gui.MonitorActivity";
	public static final String CALIB_ACTIVITY = "com.wakeappdriver.gui.CalibrationActivity";
	public static final String UPDATE_PRED_KEY = "Prediction";
	public static final int REQUEST_CODE = 1234;
	public static final int VOICE_RECOGNITION_CODE = 5678;
	
	/** Minimum face-and-eyes-detected frames required to approve that calibration succeeded */  
	public static final int MIN_CALIB_FRAMES = 10;
	/** Minimum "no detection" frames required to fail the calibration process */  
	public static final int MIN_NO_IDENT_CALIB_FRAMES = 10 * MIN_CALIB_FRAMES;
	/** Default window size in sec */
	public static final int DEFAULT_WINDOW_SIZE = 15;
	/** Minimum threshold for PERCLOS detection */
	public static final double MIN_PERCLOS_THRESHOLD = 0.05;
	
	
	
	//======================================================================================================
	//												G U I
	//======================================================================================================
	
	/** Alert stream (currently MUSIC stream) to AudioManager	*/
	public static final int ALERT_STREAM = AudioManager.STREAM_MUSIC;
	/** Interval to update monitoring bar in ms		*/
	public static final long BAR_UPDATE_INTERVAL = 200;
}
