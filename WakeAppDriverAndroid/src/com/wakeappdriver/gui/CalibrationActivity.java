package com.wakeappdriver.gui;

import android.content.Intent;

import com.wakeappdriver.configuration.Enums.Action;

public class CalibrationActivity extends ListenerActivity{

	@Override
	public Action[] getActions() {
		return new Action[]{Action.WAD_ACTION_NO_IDEN};
	}

	@Override
	public void onListenEvent(Intent intent) {
		/**
		 * implement all actions calibration activity is registered to
		 * should cover all actions in getActions()
		 */
		switch(Action.toAction(intent.getAction())){

		case WAD_ACTION_NO_IDEN:
			this.onNoIden();
			break;
		default:
			break;

		}
	}
	
	private void onNoIden(){
		
	}

}
