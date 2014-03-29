package com.wakeappdriver.stubs;

import android.util.Log;

import com.wakeappdriver.interfaces.Alerter;

public class StubAlerter implements Alerter {
	

	public static String TAG = "WAD";
	
	@Override
	public void alert() {
		Log.e(TAG, "ALERT");
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

}
