package com.wakeappdriver.framework.implementations.intenthandlers;

import android.content.Intent;
import com.wakeappdriver.configuration.Enums;
import com.wakeappdriver.framework.interfaces.AlertActivity;
import com.wakeappdriver.framework.interfaces.IntentHandler;

public class ActivityIntentHandler implements IntentHandler {

	private AlertActivity mAlertActivity;
	
	public ActivityIntentHandler(AlertActivity activity) {
		mAlertActivity = activity;
	}
	
	
	@Override
	public void handleIntent(Intent intent) {
		Enums.Action action = Enums.Action.toAction(intent.getAction());
		switch (action) {
		case WAD_ACTION_ALERT:
			mAlertActivity.onAlert();
			break;

		default:
			break;
		}
		
	}

}
