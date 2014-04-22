package com.wakeappdriver.framework;

import com.wakeappdriver.configuration.Enums.Action;
import com.wakeappdriver.framework.interfaces.IntentListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class IntentMessenger {
	private BroadcastReceiver broadcastReceiver;
	private Context context;
	private IntentFilter filter;
	public IntentMessenger (Context context, Action[] actions, final IntentListener listener){
		this.context = context;
		
		broadcastReceiver = new BroadcastReceiver(){ 
			@Override
			public void onReceive(Context context, Intent intent) {
				listener.onListenEvent(intent);
			}
		};
		filter = new IntentFilter();
		for(Action action : actions){
			filter.addAction(action.name());
		}
	}
	
	public void register(){
		context.registerReceiver(broadcastReceiver, filter);

	}
	
	public void unregister(){
		context.unregisterReceiver(broadcastReceiver);

	}
}
