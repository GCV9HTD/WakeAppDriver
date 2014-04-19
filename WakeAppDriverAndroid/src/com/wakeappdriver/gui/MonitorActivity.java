package com.wakeappdriver.gui;

import com.wakeappdriver.R;
import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.configuration.Enums.Action;
import com.wakeappdriver.framework.IntentMessenger;
import com.wakeappdriver.framework.implementations.intenthandlers.ActivityIntentHandler;
import com.wakeappdriver.framework.interfaces.AlertActivity;
import com.wakeappdriver.framework.interfaces.IntentHandler;
import com.wakeappdriver.framework.services.GoService;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

public class MonitorActivity extends Activity implements AlertActivity{

	private static final String TAG = "WAD";
	private IntentMessenger intentMessenger;
	private IntentHandler intentHandler;
	private Action[] actions = {Action.WAD_ACTION_ALERT};
	private MediaPlayer mPlayer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Decide which activity layout to show, according to "display bar" setting
		boolean displayBar = ConfigurationParameters.getDisplayBar(getApplicationContext());
		if(displayBar)
			setContentView(R.layout.activity_monitor_with_bar);
		else
			setContentView(R.layout.activity_monitor);

		// Register to intentMessenger
		this.intentHandler = new ActivityIntentHandler(this);
		this.intentMessenger = new IntentMessenger(this, actions, this.intentHandler );	
		intentMessenger.register();


		int audioFile = ConfigurationParameters.getAlert(getApplicationContext());
		mPlayer = MediaPlayer.create(this, audioFile);
		/* Set alert volume:
		 * The MediaPLayer.setVolume() function gets a volume between 0 to 1.
		 * In order to transform our "real" volume (between 0 to 1000, from the settings)
		 * we make some logarithmic transformation into [0..1].
		 */
		final int MAX_VOLUME = 1000;
		int soundVolume = ConfigurationParameters.getVolume(getApplicationContext());	// The volume to be sound
		final float volume = (float) (1 - (Math.log(MAX_VOLUME - soundVolume) / Math.log(MAX_VOLUME)));
		mPlayer.setVolume(volume, volume);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	@Override
	public void onAlert() {
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Log.i(TAG, "MonitorActivity: onAlert() has been called");
				mPlayer.start();
			}
		});
		
	}
	
	
	public void stopMonitoring(View view) {
		// kill detector and stuff according to sequence.
		
		
		// Go to startScreen activity
		Intent intent = new Intent(this, StartScreenActivity.class);
		startActivity(intent);
		// Disable the option to go back here (from activated screen)
		finish();

	}

}
