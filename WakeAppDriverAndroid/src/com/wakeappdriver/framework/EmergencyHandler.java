package com.wakeappdriver.framework;

import android.util.Log;

import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.framework.tasks.DetectorTask;

public class EmergencyHandler {

	public static String TAG = "WAD";

	private DetectorTask detector;
	private long closedDuration;
	private int closedAlertLimit;
	private long closedStartTime;
	private double blinkLimit;
	private long lastAlert;
	private long emergencyCoolDown;
	boolean blinking;

	public EmergencyHandler(DetectorTask detector){
		this.detector = detector;
		this.closedAlertLimit = ConfigurationParameters.getClosedAlertLimit();
		this.blinkLimit = ConfigurationParameters.getBlinkLimit() - 0.1;
		this.blinking = false;
		this.closedDuration = 0;
		this.closedStartTime = 0;
		this.lastAlert = 0;
		this.emergencyCoolDown = ConfigurationParameters.getEmergencyCooldown();
	}

	public void check(Double percentCovered, long timestamp){
		if(percentCovered!=null)
		Log.i(TAG, "percent covered: " + percentCovered.doubleValue());
		if (!blinking) {
			if (percentCovered != null && !percentCovered.isInfinite() 
					&& !percentCovered.isNaN() && percentCovered <= blinkLimit) {
				blinking = true;
				closedStartTime = timestamp;
			}
			closedDuration = 0;
		}
		else {
			if (percentCovered == null || percentCovered.isInfinite() 
					|| percentCovered.isNaN() || percentCovered > blinkLimit) {	// stop blinking
				blinking = false;
			}
			closedDuration = timestamp - closedStartTime;
		}

		if(closedDuration > closedAlertLimit){
			
			if(timestamp - lastAlert > emergencyCoolDown ){
				this.detector.emergency();
				this.lastAlert = timestamp;
				Log.e(TAG, "emergency!, duration: " + closedDuration);
			} else {
				Log.e(TAG, "would have alerted, duration: " + closedDuration);
			}
		}
	}
}
