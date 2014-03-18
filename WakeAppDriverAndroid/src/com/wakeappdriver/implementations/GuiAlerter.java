package com.wakeappdriver.implementations;
import java.util.List;

import com.wakeappdriver.classes.IntentMessenger;
import com.wakeappdriver.interfaces.Alerter;
import com.wakeappdriver.enums.Enums.*;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class GuiAlerter implements Alerter {
	
	private String alertActivity;
	private Context context;
	private IntentMessenger intentMessenger;
	private Alerter alerter;
	
	public GuiAlerter(Context context, String alertActivity, IntentMessenger intentMessenger, Alerter alerter){
		this.context = context;
		this.alertActivity = alertActivity;
		this.alerter = alerter;
		this.intentMessenger = intentMessenger;
	}
	
	public void alert(){
		alerter.alert();
		if(alertActivity != null){
			if(isForeground(alertActivity)){
				intentMessenger.send(Action.WAD_ACTION_ALERT, null);
			} else {
				startAlertActiviry(alertActivity);
			}
		}
	}
	private void startAlertActiviry(String packageName){
		Intent i = new Intent();
		String[]split = packageName.split("\\.");
		String pname = split[0] + "." + split[1];
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setComponent(new ComponentName(pname,packageName));
		i.setAction(Action.WAD_ACTION_ALERT.name());
		context.startActivity(i);
	}
	private boolean isForeground(String packageName){
		ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(1); 

		ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
		if(componentInfo.getClassName().equals(packageName)) return true;

		return false;
	}

	@Override
	public void destroy() {
		alerter.destroy();
	}
}
