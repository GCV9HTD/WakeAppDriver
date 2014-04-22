package com.wakeappdriver.gui;

import com.wakeappdriver.R;
import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.configuration.Constants;
import com.wakeappdriver.configuration.Enums.Action;
import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MonitorActivity extends ListenerActivity{

	private static final String TAG = "WAD";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Decide which activity layout to show, according to "display bar" setting
		boolean displayBar = ConfigurationParameters.getDisplayBar(getApplicationContext());
		if(displayBar)
			setContentView(R.layout.activity_monitor_with_bar);
		else
			setContentView(R.layout.activity_monitor);

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


	@Override
	public Action[] getActions() {
		return new Action[]{Action.WAD_ACTION_UPDATE_PREDICITON,
							Action.WAD_ACTION_PROMPT_USER};
	}


	@Override
	public void onListenEvent(Intent intent) {
		/**
		 * implement all actions monitor activity is registered to
		 * should cover all actions in getActions()
		 */
		switch(Action.toAction(intent.getAction())){

		case WAD_ACTION_UPDATE_PREDICITON:
			this.onUpdatePrediction(intent.getDoubleExtra(Constants.UPDATE_PRED_KEY, -1 ));	
			break;
		case WAD_ACTION_PROMPT_USER:
			this.promptUserForDrowsiness();
		default:
			break;
			
		}
	}


	private void promptUserForDrowsiness() {
		// TODO Auto-generated method stub
		
	}


	private void onUpdatePrediction(double prediction) {
		
		if(prediction < 0){
			return;
		}
		// TODO
		/**
		 * 
		 * implement here what to do in case of prediction update
		 * 
		 */
		
	}

}
