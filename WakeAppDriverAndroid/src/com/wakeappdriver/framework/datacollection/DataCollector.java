package com.wakeappdriver.framework.datacollection;

import java.util.Collection;
import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.framework.interfaces.FileWriter;
import com.wakeappdriver.framework.interfaces.Indicator;
import android.os.Handler;

public class DataCollector {
//	private static final int REQUEST_CODE = 1234;
	public static final int VOICE_RECOGNITION_CODE = 5678;
	private FtpSender ftpSender;
	private FileWriter fileWriter;
	
	public DataCollector(String android_id){
		this.ftpSender = new FtpSender();
		this.fileWriter = new CsvFileWriter(android_id);
	}
	
	public boolean init(){
		
		boolean isDirectoryAlreadyExsists = this.fileWriter.createDirIfNotExists();
		if (isDirectoryAlreadyExsists){
			this.ftpSender.sendToServer(this.fileWriter.getOutDir());
		}
		return this.fileWriter.createNewFile();
	}
	
	public void destroy(){
		this.fileWriter.close();
	}
	
	

	
	public void logCurrWindow(Collection<Indicator> indicatorsValues, int windowNumber) {
		
		this.fileWriter.writeToFile(indicatorsValues, windowNumber,ConfigurationParameters.getDrowsinessLevel());
	}
	
}
