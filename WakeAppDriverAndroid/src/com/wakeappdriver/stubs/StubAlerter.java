package com.wakeappdriver.stubs;

import android.util.Log;

import com.wakeappdriver.interfaces.Alerter;

public class StubAlerter implements Alerter{
	public static String TAG = "AWD";
	@Override
	public void alert() {
		Log.e(TAG, "ALERT");
		
	}

}
