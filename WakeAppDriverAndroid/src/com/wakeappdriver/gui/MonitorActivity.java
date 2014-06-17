package com.wakeappdriver.gui;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.wakeappdriver.R;
import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.configuration.Constants;
import com.wakeappdriver.configuration.Enums.Action;
import com.wakeappdriver.framework.services.GoService;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class MonitorActivity extends ListenerActivity{

	private static final String TAG = "WAD";
	private static final String CLASS_NAME = "MonitorActivity";
	private static final int REQUEST_CODE = 1234;

	
	/** timestamp of last alert**/
	private long lastAlert = 0;
	
	/** Dialog box for alert */
	private Dialog mAlertDialog;
	private MediaPlayer mPlayer;
	private int mOriginalAudioStreamVolume;

	/** Dialog box for no-identify message */ 
	private Dialog mNoIdenDialog;
	/** Dialog box for warning that the monitor will stop if OK is pressed */
	private Dialog mStopMonitorDialog;
	
	private double mOldPrediction;
	private double mNewPrediction;
	Timer barTimer = new Timer();
	
	private String drowsinessPromptMethod;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mOldPrediction = 0;
		mNewPrediction = 0;
		setLeyout();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		startGoService();
		initAlertDialog();
		initAudio();
		initNoIdentDialog();
		initStopMonitorDialog();
		
		final Handler barHandler = new Handler();

		barTimer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				barHandler.post(new Runnable() {
					VerticalProgressBar progressBar = (VerticalProgressBar) findViewById(R.id.acd_id_progress_bar);
					TextView progressValueTextView = (TextView) findViewById(R.id.acd_id_progress_value);
					// Set maximal value of the bar as 150% of the current alert threshold.
					double max_bar_value = ConfigurationParameters.getAlertThreshold() * 1.5;
					@Override
					public void run() {

						// Update drowsiness bar:
						progressBar.setCurrentValue((int) (mOldPrediction * 10000 / max_bar_value));
						int drowsiness_percent = (int) (mOldPrediction * 100 / max_bar_value);
						// Drowsiness percent shell not be over 100, so make it 100 if it's above.
						drowsiness_percent = drowsiness_percent > 100 ? 100 : drowsiness_percent;
						progressValueTextView.setText(drowsiness_percent + "%");
						
						if((int)(mOldPrediction*100) < (int)(mNewPrediction*100))
							mOldPrediction += 0.01;
						else if((int)(mOldPrediction*100) > (int)(mNewPrediction*100))
							mOldPrediction -= 0.01;
					}
				});
			}
		}, Constants.BAR_UPDATE_INTERVAL, Constants.BAR_UPDATE_INTERVAL);
		
		this.drowsinessPromptMethod = ConfigurationParameters.getDrowsinessPromptMethod();
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}


	@Override
	public void onBackPressed() {
		Log.i(TAG, CLASS_NAME + ": onBackedPressed");
		mStopMonitorDialog.show();
	}

	

	/**
	 * Stops the monitoring process. 
	 */
	public void stopMonitoring(View view) {
		Log.d(TAG, CLASS_NAME + ": stopMonitoring start");
		// Stop GoService
		this.stopService(GoService.class);

		if(mPlayer.isPlaying()) {
			mPlayer.stop();
		}
		mPlayer.release();
		// Restore device's audio settings:
		final AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
		am.setStreamVolume(Constants.ALERT_STREAM, mOriginalAudioStreamVolume, 0);
		
		// Stop bar update
		barTimer.cancel();
		
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
			// Alert only if data collector is off or if alert is enable
			if(ConfigurationParameters.isAlertEnable() && !ConfigurationParameters.getCollectMode()) {
				onAlert();
			}
			else {
				Log.d(TAG, "Alert is disable so assume that alert message popped.");
			}
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
		long timestamp = System.currentTimeMillis();
		if(timestamp - lastAlert  > Constants.GLOBAL_ALERT_COOLDOWN && lastAlert != 0){
			Log.d(TAG, CLASS_NAME + ": Alert canceled, still in cooldown");	
			this.lastAlert = timestamp;
			return;
		}
		this.lastAlert = timestamp;
		
		Log.d(TAG, CLASS_NAME + ": starting alert");
		mAlertDialog.show();
		mPlayer.start();
	}
	
	/**
	 * Stops alerting.
	 * NOTE: This must be public for now, to enable the OK button to call it
	 * from xml file (#Asa).
	 */
	public void offAlert(View view) {
		Log.d(TAG, CLASS_NAME + ": shutting alert off");
		mAlertDialog.dismiss();
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
	 * Starts a Voice Recognition to get the drowsiness level and updating the Shared Preferences variable drowsinessLevel
	 */
	private void startVoiceRecognition(){

		MediaPlayer mPlayer = MediaPlayer.create(this, R.raw.tiredness);
		mPlayer.start();
		mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
						RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
				intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
						"WakeAppDriver Voice Recognition");
				startActivityForResult(intent, REQUEST_CODE);
				
				//starting voice activity, setting delay of few seconds to turn it off in case of error
				Handler handler=new Handler();
				Runnable r=new Runnable()
				{
				    public void run() 
				    {
				    	finishActivity(REQUEST_CODE); 			
				    }
				};
				handler.postDelayed(r, 8000);
				
			}
		});

	}
	
	/**
	 * get the result from the voice recognition activity and save it
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			boolean found = false;
			int drowsinessLevel = -1;
			for(String curr : matches) {
				if(!found){
					try{
						int temp = Integer.parseInt(curr);
						if(temp>=1 && temp<=10){
							drowsinessLevel = temp;
						}
						found = true;
					}
					catch(Exception e){
					}
				}
			}
			ConfigurationParameters.setDrowsinessLevel(drowsinessLevel);
		}
		else{
			//finish the voice recognition if error results
			this.finishActivity(REQUEST_CODE);
		}
	}
	
	

	/**
	 * When the driver's face & eyes cannot be identified.
	 */
	private void onNoIdent() {
		Log.d(TAG, CLASS_NAME + ": onNoIdent start");
		// First, stop the GoService
		this.stopService(GoService.class);
		
	    mNoIdenDialog.show();
	}


	private void initNoIdentDialog() {
		final Context context = getApplicationContext();
		Builder b = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom)); 
		b.setTitle(R.string.dialog_no_ident_title);
		b.setIcon(R.drawable.ic_calibration);
		b.setMessage(R.string.dialog_no_ident_message);
		b.setNegativeButton(R.string.dialog_no_ident_neg_button, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				Intent intent = new Intent(context, StartScreenActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				finish();
			}
		});
		b.setPositiveButton(R.string.dialog_no_ident_pos_button, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(context, CalibrationActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				finish();
			}

		});
		b.setCancelable(false);
		
		mNoIdenDialog = b.create();
	}
	
	/**
	 * Starts a Voice Recognition / GUI screen to get the drowsiness level and updating the Shared Preferences variable drowsinessLevel
	 */
	private void promptUserForDrowsiness() {
		if(this.drowsinessPromptMethod.equalsIgnoreCase("Voice")){
			startVoiceRecognition();
		}
		else if (this.drowsinessPromptMethod.equalsIgnoreCase("Gui")){
			getDrowsinessLevelFromGui();
		}
	}
	
	private void getDrowsinessLevelFromGui(){
		AlertDialog.Builder b = new Builder(this);
	    b.setTitle("Your Drowsiness Level");
	    String[] types = {"1", "2","3","4","5","6","7","8","9","10"};
	    b.setItems(types, new OnClickListener() {

	        @Override
	        public void onClick(DialogInterface dialog, int which) {

	            dialog.dismiss();
	            if(which >=0 && which <10){
	            	ConfigurationParameters.setDrowsinessLevel(which + 1);
	            }
	        }

	    });
	    final AlertDialog alertDialog = b.create();
	    b.show().getListView().setSelection(3);
		
	    //Remove the dialog after few seconds
	    Handler handler=new Handler();
		Runnable r=new Runnable()
		{
		    public void run() 
		    {
		    	alertDialog.dismiss();		
		    }
		};
		handler.postDelayed(r, 8000);
	}


	/**
	 * Updates the GUI drowsiness level.
	 * @param prediction a number between [0..1] where 0 is wake and 1 is drowsy.  
	 */
	private void onUpdatePrediction(double prediction) {
		if(prediction < 0){
			return;
		}
		mOldPrediction = mNewPrediction;
		mNewPrediction = prediction;
//		// Update drowsiness bar:
//		VerticalProgressBar progressBar = (VerticalProgressBar) findViewById(R.id.acd_id_progress_bar);
//		TextView progressValueTextView = (TextView) findViewById(R.id.acd_id_progress_value);
//		// Set maximal value of the bar as 150% of the current alert threshold.
//		double max_bar_value = ConfigurationParameters.getAlertThreshold() * 1.5;
//		progressBar.setCurrentValue((int) (prediction * 10000 / max_bar_value));
//		int drowsiness_percent = (int) (prediction * 100 / max_bar_value);
//		// Drowsiness percent shell not be over 100, so make it 100 if it's above.
//		drowsiness_percent = drowsiness_percent > 100 ? 100 : drowsiness_percent;
//		progressValueTextView.setText(drowsiness_percent + "%");
//		
//		Log.i(TAG, "#Asa MonitorActivity prediction = " + prediction + "   max_bar_val = " + max_bar_value +
//				"   percent = " + drowsiness_percent);
//		
	}
	
	
	private void startGoService() {
		Context context = getApplicationContext();
		Intent intent = new Intent(context, GoService.class);
		context.startService(intent);
	}
	
	
	/**
	 * Creates and prepares alert dialog box (you can call AlertDialog.show() after
	 * this method finished). 
	 */
	private void initAlertDialog() {
		mAlertDialog = new Dialog(this);
		mAlertDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		mAlertDialog.setContentView(getLayoutInflater().inflate(R.layout.alert_popup_message, null));
		mAlertDialog.setCanceledOnTouchOutside(false);
	}

	
	private void initStopMonitorDialog() {
		Builder b = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
		b.setTitle(R.string.dialog_stop_monitoring_title);
		b.setIcon(R.drawable.ic_warning_holo_light);
		b.setMessage(R.string.dialog_stop_monitoring_message);
		b.setNegativeButton(R.string.dialog_stop_monitoring_neg_button, null);
		b.setPositiveButton(R.string.dialog_stop_monitoring_pos_button, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				stopMonitoring(null);
			}
		});
		mStopMonitorDialog = b.create();
	}

	/**
	 * Sets the layout for this activity according to the settings.
	 */
	private void setLeyout() {
		// Decide which activity layout to show, according to "display bar" setting
		boolean displayBar = ConfigurationParameters.toDisplayBar();
		if(displayBar) {
			setContentView(R.layout.activity_monitor_with_bar);
			double rand_drowsiness = Math.random() * 0.01;
			onUpdatePrediction(rand_drowsiness);
		}
		else
			setContentView(R.layout.activity_monitor);
	}

	/**
	 * Creates and prepares the audio player for alerting.
	 * You can call MediaPlayer.start() only after you called this one.  
	 */
	private void initAudio() {
		final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		final int audioFile = ConfigurationParameters.getAlert(getApplicationContext());
		mPlayer = MediaPlayer.create(this, audioFile);
		mPlayer.setAudioStreamType(Constants.ALERT_STREAM);
		
		// Backup the device's audio settings:
		mOriginalAudioStreamVolume = am.getStreamVolume(Constants.ALERT_STREAM);
		// Set volume according to settings (preferences):
		int max_stream_volume = am.getStreamMaxVolume(Constants.ALERT_STREAM);
		float pref_volume = ConfigurationParameters.getVolume(getApplicationContext());
		am.setStreamVolume(Constants.ALERT_STREAM, (int) (max_stream_volume * 1000 / pref_volume), 0);
		
		mPlayer.setLooping(true);
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

}
