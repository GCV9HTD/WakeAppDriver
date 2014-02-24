package com.wakeappdriver.classes;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Mat;

public class CapturedFrame implements CvCameraViewFrame{
	private long timestamp;
    private Mat rgba;
    private Mat gray;
    
	public CapturedFrame(long timestamp, Mat rgba, Mat gray){
		this.timestamp = timestamp;
		this.rgba = rgba;
		this.gray = gray;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	@Override
	public Mat rgba() {
		//return mCapture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
		return this.rgba;
	}
	@Override
	public Mat gray() {
		//return mCapture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);
		return this.gray;
	}

}
