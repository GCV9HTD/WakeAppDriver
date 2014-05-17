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
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class StartScreenActivity extends Activity {
	private static final String TAG = "WAD";
	private StartMode startMode;
	private Context mContext;

	private AlertDialog mExitMessage;


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
		mContext = getApplicationContext();
		startMode = StartMode.SERVICE;

		setExitMessage();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}
	
	@Override
	public void onBackPressed() {
		// Display exit message
		setExitMessage();
		mExitMessage.show();
	}
	

	public void toMonitoring(View view){
		Log.d(TAG, "entering, start mode: " + startMode.name());
		Intent intent;

		switch(startMode){

		case DEBUG:
			intent = new Intent(this, DebugActivity.class);
			startActivity(intent);
			break;
		case SERVICE:
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


	private void setExitMessage() {
		Builder builder = new AlertDialog.Builder(this); 
		builder.setTitle("Confirm exit");
		builder.setMessage("Do you want to exit WakeAppDriver?");
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Do nothing but disappear the dialog box.
			}
		});
		builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Close GoService if needed
				Intent intent = new Intent(mContext, GoService.class);
				mContext.stopService(intent);
				finish();
			}

		});
		mExitMessage = builder.create();
	}



}
