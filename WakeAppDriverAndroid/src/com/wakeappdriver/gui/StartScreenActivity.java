package com.wakeappdriver.gui;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import com.wakeappdriver.services.GoService;

import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.enums.Enums.StartMode;
import com.wakeappdriver.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class StartScreenActivity extends Activity {
	private static final String TAG = "WAD";
	private StartMode startMode;

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
		startMode = ConfigurationParameters.getStartMode();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.start_screen, menu);
		return true;
	}
	public void toSettings(View view){
		Log.d(TAG, "entering, start mode: " + startMode.name());
		Intent intent;

		switch(startMode){
		
		case ACTIVITY:
			intent = new Intent(this, GoActivity.class);
			startActivity(intent);
			break;
		case DEBUG:
			intent = new Intent(this, DebugActivity.class);
			startActivity(intent);
			break;
		case SERVICE:
		default:
			Context context = getApplicationContext();
			intent = new Intent(context, GoService.class);
			View v = this.getWindow().getDecorView();
			intent.putExtra("frameWidth", v.getWidth());
			intent.putExtra("frameHeight", v.getHeight());
			context.startService(intent); 
			
			//start monitor activity
			intent = new Intent(this, MonitorActivity.class);
			startActivity(intent);
			break;
		
		}
	}
}
