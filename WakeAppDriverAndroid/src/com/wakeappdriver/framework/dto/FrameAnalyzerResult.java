package com.wakeappdriver.framework.dto;

public class FrameAnalyzerResult {
	private Double value;
	private long timestamp;
	
	public FrameAnalyzerResult(Double value, long timestamp){
		this.value = value;
		this.timestamp = timestamp;
	}

	public Double getValue() {
		return value;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
}
