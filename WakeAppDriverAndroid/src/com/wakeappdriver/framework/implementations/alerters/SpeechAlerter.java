package com.wakeappdriver.framework.implementations.alerters;

import java.util.Locale;

import com.wakeappdriver.framework.interfaces.Alerter;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;


public class SpeechAlerter implements Alerter, OnInitListener{
	
	public static String TAG = "WAD";
	
	private TextToSpeech tts;
	private String message;
	
	public SpeechAlerter(Context context, String message) {
		Log.d(TAG, Thread.currentThread().getName() + " :: speech alerter has been created");
		tts = new TextToSpeech(context, this);
		tts.setLanguage(Locale.US);
		this.message = message;
	}
	
	@Override
	public void alert() {
		Log.i(TAG, Thread.currentThread().getName() + " :: ALERT");
		tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
	}

	@Override
	public void onInit(int arg0) {
	}
	
	@Override
	public void destroy() {
		tts.shutdown();
	}

}
