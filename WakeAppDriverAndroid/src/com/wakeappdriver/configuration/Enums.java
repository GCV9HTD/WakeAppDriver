package com.wakeappdriver.configuration;

public class Enums {
	
	public enum FrameAnalyzerType {
		PERCENT_COVERED,
		HEAD_INCLINATION,
		YAWN_SIZE
	}
	
	public enum FrameQueueType {
		PERCENT_COVERED_QUEUE,
		HEAD_INCLINATION_QUEUE,
		YAWN_SIZE_QUEUE;
	}
	
	public enum IndicatorType {
		PERCLOS,
		BLINK_DURATION,
	}
	
	public enum OperationMode {
		VISUAL_MODE,
		SERVICE_MODE;
	}
	
	public enum StartMode {
		DEBUG,
		SERVICE,
		ACTIVITY;
	    public static StartMode toStartMode (String StartMode) {
	        try {
	            return valueOf(StartMode);
	        } catch (Exception ex) {
	            return SERVICE;
	        }
	    }
	}
	public enum Action {
		WAD_ACTION_UPDATE_PREDICITON,
		WAD_ACTION_ALERT,
		WAD_ACTION_NO_IDEN,
		WAD_ACTION_NONE,
		WAD_ACTION_PROMPT_USER;
		
	    public static Action toAction (String action) {
	        try {
	            return valueOf(action);
	        } catch (Exception ex) {
	            return WAD_ACTION_NONE;
	        }
	    }
	}
}
