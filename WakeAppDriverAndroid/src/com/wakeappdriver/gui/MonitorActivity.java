package com.wakeappdriver.gui;

import com.wakeappdriver.R;
import com.wakeappdriver.classes.IntentMessenger;
import com.wakeappdriver.enums.Enums.Action;
import com.wakeappdriver.implementations.ActivityIntentHandler;
import com.wakeappdriver.interfaces.IntentHandler;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MonitorActivity extends Activity {

	private IntentMessenger intentMessenger;
	private IntentHandler intentHandler;
	private Action[] actions = {Action.WAD_ACTION_ALERT};
	@Override
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitor);
		
		this.intentHandler = new ActivityIntentHandler();
		this.intentMessenger = new IntentMessenger(this, actions, this.intentHandler );	
		intentMessenger.register();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.monitor, menu);
		return true;
	}

}
