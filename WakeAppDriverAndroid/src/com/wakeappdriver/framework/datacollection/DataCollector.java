package com.wakeappdriver.framework.datacollection;

import java.util.Collection;
import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.framework.interfaces.FileWriter;
import com.wakeappdriver.framework.interfaces.Indicator;
import com.wakeappdriver.gui.GoActivity;
import android.os.Handler;

public class DataCollector {
	
	private Handler uiHandler;
	private FtpSender ftpSender;
	private FileWriter fileWriter;
	public DataCollector(Handler uiHandler, FtpSender ftpSender, FileWriter fileWriter){
		this.uiHandler = uiHandler;
		this.ftpSender = ftpSender;
		this.fileWriter = fileWriter;
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
		if(windowNumber + 1 ==ConfigurationParameters.getNumOfWindowsBetweenTwoQueries()){
			triggerVoiceRecognition();
		}
		
		int drowsinessAssumption = -1;
		if(windowNumber == 0){
			drowsinessAssumption  = ConfigurationParameters.getDrowsinessAssumption();
		}
		this.fileWriter.writeToFile(indicatorsValues, windowNumber,drowsinessAssumption);
	}
	
	private void triggerVoiceRecognition(){
		uiHandler.sendEmptyMessage(GoActivity.VOICE_RECOGNITION_CODE);		
	}
}
