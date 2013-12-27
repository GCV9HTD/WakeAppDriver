package com.wakeappdriver.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import com.wakeappdriver.classes.FrameAnalyzer;
import com.wakeappdriver.stubs.StubFrameAnalyzer;
import com.wakeappdriver.wakeappdriver.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.WindowManager;

public class TrackerActivity extends Activity implements CvCameraViewListener2 {
	
    private CameraBridgeViewBase	mOpenCvCameraView;
    private FrameAnalyzer			closedEyeAnalyzer;
    private File                   mCascadeFile;
	private CascadeClassifier mJavaDetector;

	
    // Initialize OpenCV on the smartphone
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				try {
					Log.i("NIV", "OpenCV loaded successfully");

					// load cascade file from application resources
					InputStream is = getResources().openRawResource(R.raw.haarcascade_eye);
					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
					mCascadeFile = new File(cascadeDir, "haarcascade_eye.xml");
					FileOutputStream os;

					os = new FileOutputStream(mCascadeFile);


					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					is.close();
					os.close();
					
                    mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                    if (mJavaDetector.empty()) {
                        Log.e("NIV", "Failed to load cascade classifier");
                        mJavaDetector = null;
                    } else
                        Log.i("NIV", "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
                    
                    cascadeDir.delete();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				mOpenCvCameraView.enableView();

			} break;
			default:
			{
				super.onManagerConnected(status);
			} break;
			}
		}
	};

	
	// Starting the activity
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_tracker);
		
		this.closedEyeAnalyzer = new StubFrameAnalyzer();
		
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_tracker);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
	}

	// Opening the options menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tracker, menu);
		return true;
	}
	
	// ****** OpenCV Camera functions ******
	
	 @Override
	 public void onPause()
	 {
	     super.onPause();
	     if (mOpenCvCameraView != null)
	         mOpenCvCameraView.disableView();
	 }
	 
	 public void onDestroy() {
	     super.onDestroy();
	     if (mOpenCvCameraView != null)
	         mOpenCvCameraView.disableView();
	 }

	@Override
	public void onCameraViewStarted(int width, int height) {
	
	}

	@Override
	public void onCameraViewStopped() {
		System.exit(1);
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		System.out.println("1");
		Log.i("NIV", "********************************** HERE I AM *************************");
		//this.closedEyeAnalyzer.analyze(inputFrame.rgba());
		
		Mat frame = inputFrame.rgba();
		
        MatOfRect faces = new MatOfRect();
        
        if (mJavaDetector != null)
            mJavaDetector.detectMultiScale(frame, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    new Size(5, 5), new Size(100,100));

        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
            Core.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);

        return frame;

	}
	
    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_7, this, mLoaderCallback);
    }

}
