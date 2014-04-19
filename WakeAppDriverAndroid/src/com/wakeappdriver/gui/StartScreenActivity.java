package com.wakeappdriver.gui;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;


import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.configuration.Enums.StartMode;
import com.wakeappdriver.framework.services.GoService;
import com.wakeappdriver.R;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class StartScreenActivity extends Activity {
	private static final String TAG = "WAD";
	private StartMode startMode;
	private Intent mGoServiceIntent;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				Log.i(TAG, "OpenCV loaded successfully");
			} break;
			default:
			{
				super.onManagerConnected(status);
			} break;
			}
		}
	};
	Thread t = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_screen);			
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
		ConfigurationParameters.init(this);
		//startMode = ConfigurationParameters.getStartMode();
		//startMode = StartMode.SERVICE;
		startMode = StartMode.ACTIVITY;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.start_screen, menu);
		return true;
	}
	
	
	public void toMonitoring(View view){
		Log.d(TAG, "entering, start mode: " + startMode.name());
		Intent intent;

		switch(startMode){
		
		case ACTIVITY:
			intent = new Intent(this, GoActivity.class);
			startActivity(intent);
			finish();
			break;
		case DEBUG:
			intent = new Intent(this, DebugActivity.class);
			startActivity(intent);
			break;
		case SERVICE:
			Context context = getApplicationContext();
			mGoServiceIntent = new Intent(context, GoService.class);
			View v = this.getWindow().getDecorView();
			mGoServiceIntent.putExtra("frameWidth", v.getWidth());
			mGoServiceIntent.putExtra("frameHeight", v.getHeight());
			Log.d(TAG, "Calling " + mGoServiceIntent.getClass().getName() + " startService(..)");
			context.startService(mGoServiceIntent);
			
			// Start monitor activity
			intent = new Intent(this, MonitorActivity.class);
			startActivity(intent);
			// When the monitor stops it creates a new StartScreenActivity, so delete this one.
			finish();
			break;
		default:
		
		}
	}
	
	public void goDebug(View view) {
		Intent intent = new Intent(this, DebugActivity.class);
		startActivity(intent);
	}
	
	
	public void toSettings(View view){
		Log.d(TAG, "entering Settings");
		
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
	
	
	/**
	 * Display an exit message. The user can choose whether to exit the App or not
	 * by choosing the corresponding option. 
	 */
	public void onBackPressed() {
	    (new AlertDialog.Builder(this))
	            .setTitle("Confirm exit")
	            .setMessage("Do you want to exit WakeAppDriver?")
	            .setNegativeButton("Cancel", null)
	            .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
	                @Override
	                public void onClick(DialogInterface dialog, int which) {
	                    // Close all if needed
	                	//getApplicationContext().stopService(mGoServiceIntent);
	                    finish();
	                }

	            })
	            .show();
	}
	
	
	
	
	
}
