package com.wakeappdriver.interfaces;

import java.io.File;
import java.util.Collection;

import android.os.Environment;

public abstract class FileWriter {
	protected String android_id;
	protected final String collectFolder = "WakeAppDriver";
	protected static final String TAG = "WAD";
	protected File outDir;
	public FileWriter(String android_id){
		this.android_id = android_id;
		File externalDirectoryStorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		this.outDir=new File(externalDirectoryStorage,collectFolder);
	}
	public boolean createDirIfNotExists(){
		boolean directoryExists = false;
		if (!outDir.exists()){
			outDir.mkdir();
		}
		else{
			directoryExists = true;
		}
		return directoryExists;
	}
	
	public File getOutDir(){
		return this.outDir;
	}
	public abstract boolean createNewFile();
	public abstract void writeToFile(Collection<Indicator> indicatorsValues, int windowNumber, int drowsinessAssumption);
	public abstract void close();
}
