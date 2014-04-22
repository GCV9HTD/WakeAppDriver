package com.wakeappdriver.gui;

import java.util.Map;

import com.wakeappdriver.configuration.Enums.Action;
import com.wakeappdriver.framework.IntentMessenger;
import com.wakeappdriver.framework.interfaces.IntentListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public abstract class ListenerActivity extends Activity implements IntentListener{
	
	private IntentMessenger intentMessenger;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.intentMessenger = new IntentMessenger(this, this.getActions(), this);	
		intentMessenger.register();
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		
		this.intentMessenger.unregister();
	}
}
