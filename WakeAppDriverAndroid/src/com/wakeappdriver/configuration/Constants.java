package com.wakeappdriver.configuration;

public class Constants {
	
	public static final String ALERT_ACTIVITY = "com.wakeappdriver.gui.AlertActivity";
	public static final String MONITOR_ACTIVITY = "com.wakeappdriver.gui.MonitorActivity";
	public static final String CALIB_ACTIVITY = "com.wakeappdriver.gui.CalibrationActivity";
	public static final String UPDATE_PRED_KEY = "Prediction";
	public static final int REQUEST_CODE = 1234;
	public static final int VOICE_RECOGNITION_CODE = 5678;
	
	/** Minimum face-and-eyes-detected frames required to approve that calibration succeeded */  
	public static final int MIN_CALIB_FRAMES = 20;
	/** Minimum "no detection" frames required to fail the calibration process */  
	public static final int MIN_NO_IDENT_CALIB_FRAMES = 4 * MIN_CALIB_FRAMES;
	/** Window size in milisec */
	public static final int WINDOW_SIZE = 6000;

}
