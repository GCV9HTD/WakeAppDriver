package com.wakeappdriver.gui;

import com.wakeappdriver.R;
import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.configuration.Constants;
import com.wakeappdriver.configuration.Enums.Action;
import com.wakeappdriver.framework.services.GoService;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MonitorActivity extends ListenerActivity{

	private static final String TAG = "WAD";
	private static final String CLASS_NAME = "MonitorActivity";

	private Dialog alertDialog;
	private MediaPlayer mPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * see:
		 * https://github.com/mgrzechocinski/AndroidClipDrawableExample
		 */
		setLeyout();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		initAlertDialog();
		initAudiofile();
		
		Context context = getApplicationContext();
		Intent intent = new Intent(context, GoService.class);
		//View v = this.getWindow().getDecorView();
		//intent.putExtra("frameWidth", v.getWidth());
		//intent.putExtra("frameHeight", v.getHeight());
		Log.d(TAG, "Calling " + intent.getClass().getName() + " startService(..)");
		context.startService(intent);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}


	@Override
	public void onBackPressed() {
		/*
		 * Display a dialog (text message with OK/Cancel buttons)
		 * which inform the user that going back from this activity will stop
		 * the monitoring process.
		 */
		(new AlertDialog.Builder(this))
		.setTitle("NOTICE")
		.setMessage("The application will stop tracking you.")
		.setNegativeButton("Cancel", null)
		.setPositiveButton("Stop tracking me", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				stopMonitoring(null);
			}
		})
		.show();
	}
	
	private void stopService(Class<?> service) {
		try{
		Context context = this;
		Intent intent = new Intent(this, service);
		context.stopService(intent);
		}
		catch(Exception e) {
			Log.e(TAG, CLASS_NAME + ": stopService error: " + e.getStackTrace());
		}
	}

	/**
	 * Stops the monitoring process. 
	 */
	public void stopMonitoring(View view) {
		// Stop GoService
		this.stopService(GoService.class);

		if(mPlayer.isPlaying())
			mPlayer.stop();
		// Go to startScreen activity
		Intent intent = new Intent(this, StartScreenActivity.class);
		startActivity(intent);
		finish();
	}


	@Override
	public Action[] getActions() {
		return new Action[]{Action.WAD_ACTION_UPDATE_PREDICITON,
				Action.WAD_ACTION_PROMPT_USER,
				Action.WAD_ACTION_ALERT,
				Action.WAD_ACTION_NO_IDEN};
	}


	@Override
	public void onListenEvent(Intent intent) {
		/*
		 * Parameter intent includes an action and extra data.
		 * Here we must implement all actions that this activity is registered to,
		 * i.e. cover all actions in getActions()
		 * 
		 * NOTE: we can use the extra-data in order to pass data in addition to the
		 * action that should be performed. For example: sending the drowsiness level (double)
		 * as an extra data to the action UPDATE_PREDICTION.
		 */
		System.out.println(CLASS_NAME + ": got action " + intent.getAction());
		
		switch(Action.toAction(intent.getAction())){

		case WAD_ACTION_UPDATE_PREDICITON:
			this.onUpdatePrediction(intent.getDoubleExtra(Constants.UPDATE_PRED_KEY, -1 ));	
			break;
		case WAD_ACTION_PROMPT_USER:
			this.promptUserForDrowsiness();
			break;
		case WAD_ACTION_ALERT:
			onAlert();
			break;
		case WAD_ACTION_NO_IDEN:
			onNoIdent();
			break;
		default:
			break;

		}
	}


	/**
	 * Starts alerting.
	 */
	public void onAlert() {
		Log.d(TAG, CLASS_NAME + ": starting alert");
		alertDialog.show();
		mPlayer.start();
	}
	
	/**
	 * Stops alerting.
	 * NOTE: This must be public for now, to enable the OK button to call it
	 * from xml file (Asa).
	 */
	public void offAlert(View view) {
		Log.d(TAG, CLASS_NAME + ": shutting alert off");
		alertDialog.dismiss();
		/* We can't call mPlayer.play() right after we called mPlayer.stop().
		 * Alternatives:
		 *   1. pause() -> play()
		 *   2. stop() -> prepare() -> play()
		 * 
		 * For more information:
		 * http://developer.android.com/reference/android/media/MediaPlayer.html
		 */
		mPlayer.stop();
		try {
			mPlayer.prepare();
		}
		catch (Exception e) {
			Log.e(TAG, "offAlert: failed to prepare mPlayer");
			e.printStackTrace();
		}
	}

	/**
	 * When the driver's face & eyes cannot be identified.
	 */
	private void onNoIdent() {
		final Context context = getApplicationContext();
		// First, stop the GoService
		this.stopService(GoService.class);
		
		/*
		 * Display a message with 2 options: go to start screen or
		 * to calibration screen. 
		 */
	    (new AlertDialog.Builder(this))
	            .setTitle("Notice")
	            .setMessage("The system could not detect your face and eyes. Please calibrate your device " +
	            			"so your face can be seen.")
	            .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						Intent intent = new Intent(context, StartScreenActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(intent);
						finish();
					}
				})
	            .setPositiveButton("Calibrate", new DialogInterface.OnClickListener() {
	                @Override
	                public void onClick(DialogInterface dialog, int which) {
	                    // Close GoService if needed
	                	Intent intent = new Intent(context, CalibrationActivity.class);
	                	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                	context.startActivity(intent);
	                    finish();
	                }

	            })
	            .show();
		
	}
	
	private void promptUserForDrowsiness() {
		// TODO Auto-generated method stub

	}


	private void onUpdatePrediction(double prediction) {

		if(prediction < 0){
			return;
		}
		// TODO update drowsiness bar

	}
	
	/**
	 * Creates and prepares alert dialog box (you can call AlertDialog.show() after
	 * this method finished). 
	 */
	private void initAlertDialog() {
		alertDialog = new Dialog(this);
		alertDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		alertDialog.setContentView(getLayoutInflater().inflate(R.layout.alert_popup_message, null));
		alertDialog.setCanceledOnTouchOutside(false);
	}


	/**
	 * Sets the layout for this activity according to the settings.
	 */
	private void setLeyout() {
		// Decide which activity layout to show, according to "display bar" setting
		boolean displayBar = ConfigurationParameters.getDisplayBar(getApplicationContext());
		if(displayBar)
			setContentView(R.layout.activity_monitor_with_bar);
		else
			setContentView(R.layout.activity_monitor);
	}

	/**
	 * Creates and prepares the audio player for alerting.
	 * You can call MediaPlayer.start() after this method finished.  
	 */
	private void initAudiofile() {
		int audioFile = ConfigurationParameters.getAlert(getApplicationContext());
		mPlayer = MediaPlayer.create(this, audioFile);
		final int MAX_VOLUME = 1000;
		int soundVolume = ConfigurationParameters.getVolume(getApplicationContext());	// The volume to be sound
		final float volume = (float) (1 - (Math.log(MAX_VOLUME - soundVolume) / Math.log(MAX_VOLUME)));
		mPlayer.setVolume(volume, volume);
		mPlayer.setLooping(true);
	}

}
