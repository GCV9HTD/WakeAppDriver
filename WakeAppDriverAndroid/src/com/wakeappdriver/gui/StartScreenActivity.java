package com.wakeappdriver.gui;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;


import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.framework.services.GoService;
import com.wakeappdriver.R;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class StartScreenActivity extends Activity {
	private static final String TAG = "WAD";

	private AlertDialog mExitMessage;


	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {

			case LoaderCallbackInterface.SUCCESS:
				Log.i(TAG, "OpenCV loaded successfully");
				break;
			default:
				super.onManagerConnected(status);
				break;
			}
		};
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_screen);			
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
		ConfigurationParameters.init(this);
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
		mExitMessage.show();
	}
	

	public void toMonitoring(View view){
		Log.d(TAG, "toMonitoring start");
		Intent intent = new Intent(this, MonitorActivity.class);
		startActivity(intent);
		finish();
	}
	
	public void toCalibration(View view){
		Log.d(TAG, "toCalibration start");
		Intent intent = new Intent(this, CalibrationActivity.class);
		startActivity(intent);
		finish();
	}


	public void toSettings(View view){
		Log.d(TAG, "toSettings start");
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}


	private void setExitMessage() {
		Builder builder = new AlertDialog.Builder(this); 
		builder.setTitle(R.string.dialog_exit_app_title);
		builder.setMessage(R.string.dialog_exit_app_message);
		builder.setPositiveButton(R.string.dialog_exit_app_pos_button, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Do nothing but disappear the dialog box.
			}
		});
		builder.setNegativeButton(R.string.dialog_exit_app_neg_button, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Close GoService if needed
				final Context context = getApplicationContext();
				Intent intent = new Intent(context, GoService.class);
				context.stopService(intent);
				finish();
			}

		});
		
		mExitMessage = builder.create();
	}



}
