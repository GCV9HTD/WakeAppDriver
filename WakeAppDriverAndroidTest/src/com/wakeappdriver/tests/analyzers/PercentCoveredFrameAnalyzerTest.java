package com.wakeappdriver.tests.analyzers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.content.Context;
import android.os.Environment;
import android.test.AndroidTestCase;

import com.wakeappdriver.framework.dto.CapturedFrame;
import com.wakeappdriver.framework.implementations.analyzers.PercentCoveredFrameAnalyzer;
import com.wakeappdriver.tests.AnalyzerTestResult;
import com.wakeappdriver.tests.AnalyzerTestResult.Result;
import com.wakeappdriver.tests.WakeAppDriverTestRunner;
//import android.test.InstrumentationTestCase;


public class PercentCoveredFrameAnalyzerTest extends AndroidTestCase {

	private static final String ANALYZER_NAME = "PercentCovered";
	private static final String TAG = "WAD - Test";
	private Context testContext;
	private Context targetContext;
	private CascadeClassifier mFaceDetector;
	private CascadeClassifier mRightEyeDetector;
	private PercentCoveredFrameAnalyzer analyzer;
	
	private Mat rgba;
	private Mat gray;
	private OutputStreamWriter resultsFileWriter;
	
	private List<String> exportedFiles;

	@Before
	public void setUp() throws Exception {
		targetContext = getContext();
		testContext = WakeAppDriverTestRunner.context;
        exportedFiles = new ArrayList<String>();
        mFaceDetector = new CascadeClassifier(getFile("lbpcascade_frontalface.xml"));
        mRightEyeDetector = new CascadeClassifier(getFile("haarcascade_righteye_2splits.xml"));
        analyzer = new PercentCoveredFrameAnalyzer(mFaceDetector, mRightEyeDetector);
        
		File externalDirectoryStorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		String output = new File(externalDirectoryStorage, "testResults.csv").getAbsolutePath();
		resultsFileWriter = new OutputStreamWriter(new FileOutputStream(output));
	}

	@After
	public void tearDown() throws Exception {
		for (String filePath : exportedFiles) {
			File file = new File(filePath);
			file.delete();
		}
	}
	
	@Test
	public void testSets() {
		List<String> sets = new ArrayList<String>();
		sets.add("Niv");
		
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(AnalyzerTestResult.getAnalyzerTestResultSubject());
			sb.append('\n');
			resultsFileWriter.write(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(String setName : sets) {
			testSetAnalyzer(setName);
		}
	}

	public void testSetAnalyzer(String setName) {
		
		List<AnalyzerTestResult> testResults = new ArrayList<AnalyzerTestResult>();
		
		List<String> frames = getFiles(setName + "/Initial-set");
		for(String frame : frames) {
			CapturedFrame capturedFrame = prepareFrame(frame);
			analyzer.analyze(capturedFrame);
		}
		
		frames = getFiles(setName + "/Open");
		for(String frame : frames) {
			CapturedFrame capturedFrame = prepareFrame(frame);
			Double result = analyzer.analyze(capturedFrame);
			
			AnalyzerTestResult testResult = new AnalyzerTestResult(ANALYZER_NAME, 
					setName, "Open", frame.substring(frame.lastIndexOf('/')+1), analyzer.getMaxHeight(), analyzer.getMinHeight(), result);
			if (result == null) {
				testResult.setDebugFileLocation(createDebugFrame(frame, capturedFrame));
				testResult.setResult(Result.FAIL);
			}
			else if (result < 0.5) {
				testResult.setResult(Result.FAIL);
			}
			else {
				testResult.setResult(Result.PASS);
			}
			testResults.add(testResult);

		}
		
		frames = getFiles(setName + "/Semi-open");
		for(String frame : frames) {
			CapturedFrame capturedFrame = prepareFrame(frame);
			Double result = analyzer.analyze(capturedFrame);
			
			AnalyzerTestResult testResult = new AnalyzerTestResult(ANALYZER_NAME, 
					setName, "Semi-open", frame.substring(frame.lastIndexOf('/')+1), analyzer.getMaxHeight(), analyzer.getMinHeight(), result);
			if (result == null) {
				testResult.setDebugFileLocation(createDebugFrame(frame, capturedFrame));
				testResult.setResult(Result.FAIL);
			}
			else if (result > 0.7) {
				testResult.setResult(Result.FAIL);
			}
			else {
				testResult.setResult(Result.PASS);
			}
			testResults.add(testResult);
		}
		
		frames = getFiles(setName + "/Close");
		for(String frame : frames) {
			CapturedFrame capturedFrame = prepareFrame(frame);
			Double result = analyzer.analyze(capturedFrame);
			
			AnalyzerTestResult testResult = new AnalyzerTestResult(ANALYZER_NAME, 
					setName, "Close", frame.substring(frame.lastIndexOf('/')+1), analyzer.getMaxHeight(), analyzer.getMinHeight(), result);
			if (result == null) {
				testResult.setDebugFileLocation(createDebugFrame(frame, capturedFrame));
				testResult.setResult(Result.FAIL);
			}
			else if (result > 0.2) {
				testResult.setResult(Result.FAIL);
			}
			else {
				testResult.setResult(Result.PASS);
			}
			testResults.add(testResult);
		}
		
//		frames = getFiles(setName + "/No-identification");
//		for(String frame : frames) {
//			CapturedFrame capturedFrame = prepareFrame(frame);
//			Double result = analyzer.analyze(capturedFrame);
//			
//			WakeAppDriverTestRunner.Log("No-identification pic: " + frame.substring(frame.lastIndexOf('/'))
//					+ ", Ratio: " + result);
//
//		}
		
		// release resources
		rgba.release();
		gray.release();
		
		try {
			StringBuilder sb = new StringBuilder();
			for(AnalyzerTestResult result : testResults) {
				sb.append(result.toString());
				sb.append("\n");
			}
			resultsFileWriter.write(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int pass = 0;
		int fail = 0;
		for(AnalyzerTestResult result : testResults) {
			if (result.getResult().equals(Result.PASS)) {
				pass++;
			}
			else {
				fail++;
			}
		}
		assertTrue("Success percentage: " + (double)pass / (pass+fail), (double)pass / (pass+fail) > 0.6);
	}
	

	private CapturedFrame prepareFrame(String frame) {
		rgba = Highgui.imread(frame);
		gray = Highgui.imread(frame, Highgui.IMREAD_GRAYSCALE);
		Long timestamp = Long.valueOf(frame.substring(frame.lastIndexOf('_')+1, frame.lastIndexOf('.')));
		CapturedFrame capturedFrame = new CapturedFrame(timestamp, rgba, gray);
		return capturedFrame;
	}

	/**
	 * creates a debug frame and save it to downloads directory in the smartphone
	 * @param frame
	 * @param capturedFrame
	 * @return debug frame path
	 */
	private String createDebugFrame(String frame, CapturedFrame capturedFrame) {
		Mat visualAnalyzedFrame = analyzer.visualAnalyze(capturedFrame);
		File externalDirectoryStorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		String output = new File(externalDirectoryStorage, "DEBUG_" + frame.substring(frame.lastIndexOf('/')+1)).getAbsolutePath();
		Highgui.imwrite(output, visualAnalyzedFrame);
		return output;
	}
	
	private List<String> getFiles(String path) {
		List<String> result = new ArrayList<String>();
		String[] files = null;
		try {
			files = testContext.getAssets().list(path);
		} catch (IOException e) {e.printStackTrace();}
		for (String file : files) {
			result.add(getFile(path + "/" + file));
		}
		return result;
	}
	
	private String getFile(String fileName) {
		 File f = new File(targetContext.getCacheDir() + "/" + fileName);
		 if (f.exists()) 
			 f.delete();
		 try {
		    InputStream is = testContext.getAssets().open(fileName);
		    int size = is.available();
		    byte[] buffer = new byte[size];
		    is.read(buffer);
		    is.close();

		    if(!f.getParentFile().exists())
		    	f.mkdirs();
		    FileOutputStream fos = new FileOutputStream(f);
		    fos.write(buffer);
		    fos.close();
		    
		  } catch (Exception e) { throw new RuntimeException(e); }

		  exportedFiles.add(f.getAbsolutePath());
		return f.getAbsolutePath();
	}

}
