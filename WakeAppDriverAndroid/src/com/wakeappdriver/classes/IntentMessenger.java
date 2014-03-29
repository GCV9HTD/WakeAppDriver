package com.wakeappdriver.classes;

import java.util.Map;
import java.util.Map.Entry;
import com.wakeappdriver.enums.Enums.Action;
import com.wakeappdriver.interfaces.IntentHandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class IntentMessenger {
	private BroadcastReceiver broadcastReceiver;
	private Context context;
	private IntentFilter filter;
	public IntentMessenger (Context context, Action[] actions, final IntentHandler intentHandler){
		this.context = context;
		
		broadcastReceiver = new BroadcastReceiver(){ 
			@Override
			public void onReceive(Context context, Intent intent) {
				intentHandler.handleIntent(intent);
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
	public void send(Action action, Map<String,String> extra){
		Intent i = new Intent(action.name());

		if(extra != null){
			for(Entry<String,String> entry : extra.entrySet()){
				i.putExtra(entry.getKey(),entry.getValue());
			}
		}
		context.sendBroadcast(i);
	}
}
