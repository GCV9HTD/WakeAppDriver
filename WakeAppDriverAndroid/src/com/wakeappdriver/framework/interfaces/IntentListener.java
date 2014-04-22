package com.wakeappdriver.framework.interfaces;

import android.content.Intent;

import com.wakeappdriver.configuration.Enums.Action;

public interface IntentListener {
	
	public Action[] getActions();
	public void onListenEvent(Intent intent);
}
