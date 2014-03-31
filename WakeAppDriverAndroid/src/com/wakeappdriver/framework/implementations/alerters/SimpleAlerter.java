package com.wakeappdriver.framework.implementations.alerters;

import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

import com.wakeappdriver.framework.interfaces.Alerter;

public class SimpleAlerter implements Alerter, OnInitListener{
	
	public static String TAG = "WAD";
	
	private TextToSpeech tts;
	
	public SimpleAlerter(Context context) {
		Log.d(TAG, Thread.currentThread().getName() + " :: SimpleAlerter has been created");
		tts = new TextToSpeech(context, this);
		tts.setLanguage(Locale.US);
	}
	
	@Override
	public void alert() {
		Log.i(TAG, Thread.currentThread().getName() + " :: ALERT");
		tts.speak("Hey, Wake Up, Driver!", TextToSpeech.QUEUE_FLUSH, null);
	}

	@Override
	public void onInit(int arg0) {
		tts.speak("The system is tracking you now.", TextToSpeech.QUEUE_FLUSH, null);
	}
	
	@Override
	public void destroy() {
		tts.shutdown();
	}

}
