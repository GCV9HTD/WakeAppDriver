package com.wakeappdriver.tests;

public class AnalyzerTestResult {
	
	public enum Result {
		PASS, FAIL
	}
	
	private String analyzer;	// tested analyzer
	private String setName;		// the name of the set (Shahar, Niv, etc.)
	private String setType;		// the type of the picture (close, open, etc.)
	private String fileName;	// the name of the file
	private double maxHeight;		// the height of the most opened-eye in the set
	private double minHeight;		// the height of the most-closed-eye in the set
	private Double ratio;		// the result of the analyzer
	private Result result;		// the frame's test result
	private String debugFileLocation;	// location of debug file created for this frame
	
	public AnalyzerTestResult(String analyzer, String setName, String setType,
			String fileName, double maxHeight, double minHeight, Double ratio) {
		super();
		this.analyzer = analyzer;
		this.setName = setName;
		this.setType = setType;
		this.fileName = fileName;
		this.maxHeight = maxHeight;
		this.minHeight = minHeight;
		this.ratio = ratio;
	}

	public void setDebugFileLocation(String debugFileLocation) {
		this.debugFileLocation = debugFileLocation;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public Result getResult() {
		return result;
	}

	@Override
	public String toString() {
		return analyzer + ", "
				+ setName + ", " + setType + ", " + fileName
				+ ", " + maxHeight + ", " + minHeight
				+ ", " + ratio + ", " + result + ", " + debugFileLocation;
	}
	
	public static String getAnalyzerTestResultSubject(){
		return "Analyzer Name, Set, Type, File, Max Height, Min Height, Ratio, Result, Debug File";
	}
	
	
	
}
