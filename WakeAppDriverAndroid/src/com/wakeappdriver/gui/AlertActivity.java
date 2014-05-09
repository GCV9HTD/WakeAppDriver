package com.wakeappdriver.gui;

import java.lang.reflect.Constructor;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.wakeappdriver.R;
import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.configuration.Enums.Action;
import com.wakeappdriver.framework.implementations.alerters.SimpleAlerter;
import com.wakeappdriver.framework.interfaces.Alerter;

public class AlertActivity extends ListenerActivity{
	private static final String TAG = "WAD";

	private MediaPlayer mPlayer;
	private Alerter alerter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Decide which activity layout to show, according to "display bar" setting
		boolean displayBar = ConfigurationParameters.toDisplayBar();
		if(displayBar)
			setContentView(R.layout.activity_monitor_with_bar);
		else
			setContentView(R.layout.activity_monitor);

		try {
			Class<?> clazz = Class.forName(ConfigurationParameters.getAlertType());
			Constructor<?> constructor = clazz.getConstructor(Context.class);
			alerter = (Alerter)(constructor.newInstance(this));
		} catch (Exception e) {
			alerter = new SimpleAlerter(this);
		}

		int audioFile = ConfigurationParameters.getAlert(getApplicationContext());
		mPlayer = MediaPlayer.create(this, audioFile);
		float volume = ConfigurationParameters.getVolume(getApplicationContext());
		mPlayer.setVolume(volume, volume);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}


	
	public void stopMonitoring(View view) {
		// kill detector and stuff according to sequence.
		
		
		// Go to startScreen activity
		Intent intent = new Intent(this, StartScreenActivity.class);
		startActivity(intent);
		// Disable the option to go back here (from activated screen)
		finish();

	}

	public void onAlert() {
		/**
		 * 
		 * maybe do something with alerter for example
		 * aleter.alert();
		 * 
		 */
		
		Log.i(TAG, "MonitorActivity: onAlert() has been called");
		mPlayer.start();
	}
	
	@Override
	public Action[] getActions() {
		return new Action[]{Action.WAD_ACTION_ALERT};
	}

	@Override
	public void onListenEvent(Intent intent) {
		/**
		 * implement all actions alert activity is registered to
		 * should cover all actions in getActions()
		 */
		switch(Action.toAction(intent.getAction())){

		case WAD_ACTION_ALERT:
			this.onAlert();
			break;
		default:
			break;

		}
	}


}
