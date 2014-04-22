package com.wakeappdriver.framework.implementations.analyzers;

import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import android.os.Environment;
import android.util.Log;

import com.wakeappdriver.configuration.Enums.OperationMode;
import com.wakeappdriver.framework.EmergencyHandler;
import com.wakeappdriver.framework.FrameQueue;
import com.wakeappdriver.framework.FrameQueueManager;
import com.wakeappdriver.framework.ResultQueue;
import com.wakeappdriver.framework.dto.CapturedFrame;
import com.wakeappdriver.framework.interfaces.FrameAnalyzer;

public class PercentCoveredFrameAnalyzer extends FrameAnalyzer {
    
	private static final String TAG = "WAD";
	
	private Mat mGray;
	private Mat mRgba;

	private CascadeClassifier mFaceDetector;
	private CascadeClassifier mRightEyeDetector;

	private Mat mZoomWindow;
	private Mat mZoomWindow2;
	private Rect eyearea_right;
	private Rect eyearea_left;
	private Rect eyeOnlyRect_right;
	private float mRelativeFaceSize = 0.2f;
	private int mAbsoluteFaceSize = 0;
	
	private double maxHeight = 0;
	private double minHeight = 100;
	
	private EmergencyHandler emergencyHandler = null;
	
	private String pathToWrite = Environment.getExternalStorageDirectory() + "/WakeAppDriver/";
	
	public PercentCoveredFrameAnalyzer(FrameQueueManager queueManager,
			FrameQueue frameQueue, ResultQueue resultQueue) {
		super(queueManager, frameQueue, resultQueue);
		Log.d(TAG, Thread.currentThread().getName() + " :: creating Percent-Covered frame analyzer ");
		this.mGray = new Mat();
		this.mRgba = new Mat();
		
		File folder = new File(pathToWrite);
		if(!folder.exists()) {
			folder.mkdirs();
		}
	}
	
	public PercentCoveredFrameAnalyzer(CascadeClassifier mFaceDetector, CascadeClassifier mRightEyeDetector) {
		super(null, null, null);
		Log.d(TAG, Thread.currentThread().getName() + " :: creating Percent-Covered frame analyzer ");
		this.mGray = new Mat();
		this.mRgba = new Mat();
		this.mFaceDetector = mFaceDetector;
		this.mRightEyeDetector = mRightEyeDetector;
	}

	public void setmFaceDetector(CascadeClassifier mFaceDetector) {
		this.mFaceDetector = mFaceDetector;
	}

	public void setmRightEyeDetector(CascadeClassifier mRightEyeDetector) {
		this.mRightEyeDetector = mRightEyeDetector;
	}
	
	public void setEmergencyHandler(EmergencyHandler emergencyHandler){
		this.emergencyHandler = emergencyHandler;
	}

	public Double analyze(CapturedFrame capturedFrame) {
		Log.d(TAG, Thread.currentThread().getName() + " :: analyzing!");

		Double result = null;
		mRgba = capturedFrame.rgba();
		mGray = capturedFrame.gray();
		
		if (mAbsoluteFaceSize == 0) {
			int height = mGray.rows();
			if (Math.round(height * mRelativeFaceSize) > 0) {
				mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
			}
		}
		
		Rect r = detectFace();
		if(r == null) {
//			Highgui.imwrite(pathToWrite + "no_face_detection_" + capturedFrame.getTimestamp() + ".jpg", capturedFrame.rgba());
			return null;
		}
		
		eyeOnlyRect_right = detectEye(r);
		if(eyeOnlyRect_right == null){
//			Highgui.imwrite(pathToWrite + "no_eye_detection_" + capturedFrame.getTimestamp() + ".jpg", capturedFrame.rgba());
			return null;
		}

		//images to display in zoom areas
		Mat toDisplayGray = mGray.submat(eyeOnlyRect_right);
		Mat toDisplayRBGA = mRgba.submat(eyeOnlyRect_right);

		//get binarization threshold
		double thresh = getThreshold(toDisplayGray);
		double maxval = 255; 

		//print threshold
		//Log.e("WAD", "THRESH " + thresh);

		//binarize image
		Imgproc.threshold(toDisplayGray, toDisplayGray , thresh , maxval ,Imgproc.THRESH_BINARY);

		//count black pixels in binarized image to get iris size
		result = getIrisSize(toDisplayGray,toDisplayRBGA, OperationMode.SERVICE_MODE);
		
		toDisplayGray.release();
		toDisplayRBGA.release();

//		Highgui.imwrite(pathToWrite + "detected_frame_" + capturedFrame.getTimestamp() + ".jpg", capturedFrame.rgba());
		
		if(emergencyHandler != null){
			emergencyHandler.check(result, capturedFrame.getTimestamp());
		}
		
		return result;
	}

	private Rect detectEye(Rect r) {
		eyearea_right = new Rect(r.x + r.width / 16,
				(int) (r.y + (r.height / 4.5)),
				(r.width - 2 * r.width / 16) / 2, (int) (r.height / 3.0));
		eyearea_left = new Rect(r.x + r.width / 16
				+ (r.width - 2 * r.width / 16) / 2,
				(int) (r.y + (r.height / 4.5)),
				(r.width - 2 * r.width / 16) / 2, (int) (r.height / 3.0));

		//detect eyes with classifier
		return getEyeRect(mRightEyeDetector, eyearea_right);
	}
	
	private Rect detectFace() {
		//detect faces
		MatOfRect faces = new MatOfRect();
		if (mFaceDetector != null){
			mFaceDetector.detectMultiScale(mGray, faces, 1.1, 2, 2 | Objdetect.CASCADE_FIND_BIGGEST_OBJECT, 
					new Size(mAbsoluteFaceSize, mAbsoluteFaceSize),
					new Size());
		}
		Rect[] facesArray = faces.toArray();
		faces.release();
		Log.d(TAG, Thread.currentThread().getName() + " :: num of faces: " + faces.size());
		
		//handle face area
		if(facesArray.length == 0) {
			return null;
		}
		return facesArray[0];
	}

	public Mat visualAnalyze(CapturedFrame capturedFrame) {
		Log.d(TAG, Thread.currentThread().getName() + " :: visual analyzing!");

		mRgba = capturedFrame.rgba();
		mGray = capturedFrame.gray();

		//create zoom areas
		CreateAuxiliaryMats();

		if (mAbsoluteFaceSize == 0) {
			int height = mGray.rows();
			if (Math.round(height * mRelativeFaceSize) > 0) {
				mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
			}
		}

		Rect r = detectFace();
		if(r == null) {
			mZoomWindow.release();
			mZoomWindow2.release();
			return mRgba;
		}
		Core.rectangle(mRgba, r.tl(), r.br(), new Scalar(0,255,0),4);

		eyeOnlyRect_right = detectEye(r);

		//draw eye areas
		Core.rectangle(mRgba, eyearea_left.tl(), eyearea_left.br(),
				new Scalar(255, 0, 0, 255), 2);
		Core.rectangle(mRgba, eyearea_right.tl(), eyearea_right.br(),
				new Scalar(255, 0, 0, 255), 2);

		if(eyeOnlyRect_right == null){
			mZoomWindow.release();
			mZoomWindow2.release();
			return mRgba;
		}

		//images to display in zoom areas
		Mat toDisplayGray = mGray.submat(eyeOnlyRect_right);
		Mat toDisplayRBGA = mRgba.submat(eyeOnlyRect_right);

		//get binarization threshold
		double thresh = getThreshold(toDisplayGray);
		double maxval = 255; 

		//print threshold
		//Log.e("WAD", "THRESH " + thresh);

		//binarize image
		Imgproc.threshold(toDisplayGray, toDisplayGray , thresh , maxval ,Imgproc.THRESH_BINARY);

		//count black pixels in binarized image to get iris size
		getIrisSize(toDisplayGray,toDisplayRBGA, OperationMode.VISUAL_MODE);

		//draw zoom areas
		displayMat(toDisplayGray,toDisplayRBGA);

		toDisplayGray.release();
		toDisplayRBGA.release();
		mZoomWindow.release();
		mZoomWindow2.release();
		
		return mRgba;
	}

	private double getIrisSize(Mat toDisplayGray, Mat toDisplayRgba, OperationMode mode) {

		int midCol = toDisplayGray.cols()/2;

		//last black pixel indices in edges (left right top bottom)
		int top = 0;
		int down = 0;

		//count black pixels from top to bottom in the middle column
		for(int i = 0; i < toDisplayGray.rows(); i++){
			//if value is 0 -> black pixel
			if(toDisplayGray.get(i,midCol)[0] == 0){
				if(down == 0){
					down = i;
					top = i;
				} 
				else {
					top = i;
				}
			}
		}
		
		double height = top - down;
		
		if (height > maxHeight)
			maxHeight = height;
		
		if (height < minHeight && height > 4)	// 4 is a temporary absolute min-limit
			minHeight = height;

		double ratio = (height-minHeight) / (maxHeight-minHeight);
		Log.d(TAG, Thread.currentThread().getName() + " :: Height = " + height);
		Log.d(TAG, Thread.currentThread().getName() + " :: maxHeight = " + maxHeight);
		Log.d(TAG, Thread.currentThread().getName() + " :: minHeight = " + minHeight);
		Log.d(TAG, Thread.currentThread().getName() + " :: Ratio = " + ratio);

		if(mode.equals(OperationMode.VISUAL_MODE)){ // no blink, draw red line
			Core.line(toDisplayRgba, new Point(midCol,down), new Point(midCol,top), new Scalar(255,0,0));
		}
		
		return ratio;
	}

	private double getThreshold(Mat toDisplayGray) {
		//works good in daylight (indoors)
		//double thresh    = 75;

		//threshhold for low-light situtations (afternoon indoors)
		//double thresh    = 30;

		//attempt to make a robust threshold, we should find a better
		//formula that fits all light situations
		double mean = Core.mean(toDisplayGray).val[0];
		double thresh    = mean*(0.75);
		return thresh;
	}

	private void displayMat(Mat toDisplayGray, Mat toDisplayRgba) {

		//convert grayscale image to RBGA in order to display in zoom window
		Mat toDisplayTmp = new Mat(toDisplayGray.rows(),toDisplayGray.cols(),mRgba.type());
		Imgproc.cvtColor(toDisplayGray, toDisplayTmp, Imgproc.COLOR_GRAY2RGBA);
		Imgproc.resize(toDisplayTmp, mZoomWindow,mZoomWindow.size());
//		mZoomWindow.release();
//		toDisplayTmp.copyTo(mZoomWindow);
		Imgproc.resize(toDisplayRgba, mZoomWindow2,mZoomWindow2.size());

		toDisplayTmp.release();
	}
	
	private Rect getEyeRect(CascadeClassifier clasificator, Rect area){
		Rect eye_only_rectangle = null;
		try{
			Mat mROI = mGray.submat(area);
			MatOfRect eyes = new MatOfRect();
			clasificator.detectMultiScale(mROI, eyes, 1.15, 2,
					Objdetect.CASCADE_SCALE_IMAGE | Objdetect.CASCADE_FIND_BIGGEST_OBJECT, new Size(10, 10),
					new Size());
			
			if(eyes.toArray().length == 0) {
				return null;
			}
			Rect e = eyes.toArray()[0];
			e.x = area.x + e.x;
			e.y = area.y + e.y;
			eye_only_rectangle = new Rect((int) e.tl().x,
					(int) (e.tl().y + e.height * 0.4), (int) e.width,
					(int) (e.height * 0.6));
			eyes.release();
			mROI.release();
		}
		catch(Exception e){
		}
		return eye_only_rectangle;
	}
	
	private void CreateAuxiliaryMats() {
		if (mGray.empty())
			return;

		int rows = mGray.rows();
		int cols = mGray.cols();

		//if (mZoomWindow == null) {
		mZoomWindow = mRgba.submat(rows / 2 + rows / 10, rows, cols / 2
				+ cols / 10, cols);
		mZoomWindow2 = mRgba.submat(0, rows / 2 - rows / 10, cols / 2
				+ cols / 10, cols);
		//}
	}

	public double getMaxHeight() {
		return maxHeight;
	}

	public void setMaxHeight(double maxHeight) {
		this.maxHeight = maxHeight;
	}

	public double getMinHeight() {
		return minHeight;
	}

	public void setMinHeight(double minHeight) {
		this.minHeight = minHeight;
	}
	
}
