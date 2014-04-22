package com.wakeappdriver.framework.services;

import java.util.List;
import java.util.Map;

import com.wakeappdriver.configuration.Enums.Action;
import com.wakeappdriver.framework.IntentMessenger;
import com.wakeappdriver.framework.interfaces.IntentListener;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public abstract class ListenerService extends Service implements IntentListener{

	private IntentMessenger intentMessenger;
	@Override
	public void onCreate() {
		super.onCreate();

		this.intentMessenger = new IntentMessenger(this, this.getActions(), this);	
		intentMessenger.register();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();

		this.intentMessenger.unregister();
	}

	public final void forceStartActivity(String dst){
		if(dst != null){
			if(!isForeground(dst)){
				startActivity(dst);
			}
		}
	}
	private void startActivity(String packageName){
		Intent i = new Intent();
		String[]split = packageName.split("\\.");
		String pname = split[0] + "." + split[1];
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setComponent(new ComponentName(pname,packageName));
		this.startActivity(i);
	}
	private boolean isForeground(String packageName){
		ActivityManager manager = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
		List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(1); 

		ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
		if(componentInfo.getClassName().equals(packageName)){
			return true;
		}

		return false;
	}
}
