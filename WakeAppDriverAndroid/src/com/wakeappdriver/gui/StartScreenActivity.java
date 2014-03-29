package com.wakeappdriver.gui;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import com.wakeappdriver.tasks.CameraTask;
import com.wakeappdriver.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class StartScreenActivity extends Activity {
	private static final String TAG = "WAD";
	private boolean DrawImage = true;

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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.start_screen, menu);
		return true;
	}
	public void toSettings(View view){
		Log.d(TAG, "entering");

		if(DrawImage){	
			Intent intent = new Intent(this, GoActivity.class);
			startActivity(intent);
		} else {

			if(t == null){

				Log.d(TAG, "started");

				View v = this.getWindow().getDecorView();
				int frameWidth = v.getWidth();
				int frameHeight = v.getHeight();
				//int frameWidth = 320;
				//int frameHeight = 240;
				
				CameraTask camera = new CameraTask(0, 1, null, frameWidth, frameHeight);
				t = new Thread(camera);
				t.run();
				Log.d(TAG, "height = " + frameHeight + " width = " + frameWidth);
			}
		}
	}

	public void goDebug(View view) {
		Intent intent = new Intent(this, DebugActivity.class);
		startActivity(intent);
	}
}
