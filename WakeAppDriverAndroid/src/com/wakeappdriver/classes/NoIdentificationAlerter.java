package com.wakeappdriver.classes;

import java.util.Locale;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

import com.wakeappdriver.interfaces.Alerter;

public class NoIdentificationAlerter implements Alerter, OnInitListener{
	
	public static String TAG = "WAD";
	
	private TextToSpeech tts;
	
	public NoIdentificationAlerter(Activity currentActivity) {
		Log.d(TAG, Thread.currentThread().getName() + " :: NoIdentificationAlerter has been created");
		tts = new TextToSpeech(currentActivity, this);
		tts.setLanguage(Locale.US);
	}
	
	@Override
	public void alert() {
		Log.i(TAG, Thread.currentThread().getName() + " :: ALERT");
		tts.speak("The system couldn't track you. Please set the smartphone again.", TextToSpeech.QUEUE_FLUSH, null);
	}

	@Override
	public void onInit(int arg0) {
	}
	
	@Override
	public void destroy() {
		tts.shutdown();
	}

}
