package com.wakeappdriver.classes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import android.util.Log;
import com.wakeappdriver.interfaces.FileWriter;
import com.wakeappdriver.interfaces.Indicator;

public class CsvFileWriter extends FileWriter {
	
	
	private OutputStreamWriter logFile;
	
	private static final String delimiter = ",";
	private static final String tupleDelimiter = "\r\n";
	public CsvFileWriter(String android_id) {
		super(android_id);
	}

	@Override
	public boolean createNewFile() {
		boolean ans = true;
		String filename =this.android_id + "_" + new SimpleDateFormat("yyyy-MM-dd hh-mm-ss").format(new Date()) + ".csv";
		File outFile=new File(outDir,filename);
		if (outFile.exists()) {
			outFile.delete();
		}
		try {
			logFile = new OutputStreamWriter(new FileOutputStream(outFile.getPath()));
		} catch (FileNotFoundException e) {
			Log.d(TAG, "Error: could not open log file " + outFile.getName() );
			ans = false;
		}
		return ans;

	}

	@Override
	public void writeToFile(Collection<Indicator> indicatorsValues,int windowNumber, int drowsinessAssumption) {
		String log = "";
		try {
			log += windowNumber + delimiter;
			for(Indicator currIndicator : indicatorsValues){
				log += currIndicator.getValue() + delimiter;
			}
			log += new SimpleDateFormat("HH:mm:ss.SSS").format(Calendar.getInstance().getTime()) + delimiter;
			log += drowsinessAssumption + tupleDelimiter;
			logFile.write(log);
			logFile.flush();
		}
		catch (IOException e) {
			Log.e(TAG, "File write failed: " + e.toString());
		} 

	}
	
	public void close(){
		try {
			logFile.close();
		} 
		catch (IOException e) {
			Log.e(TAG, "File close failed: " + e.toString());
		}
	}

}
