package com.wakeappdriver.framework.datacollection;

import java.io.File;
import java.io.FileInputStream;


import android.util.Log;

public class FtpSender {
	private final String ftpStorePath  = "/public_html/WakeAppDriver/";
	private static final String TAG = "WAD";
	public void sendToServer(File directoryToSend){
		WadFTPClient ftpclient = new WadFTPClient();
		boolean status = ftpclient.ftpConnect();
		if (status == true) {
			for(File currFile : directoryToSend.listFiles()){
				try{
					FileInputStream fis = new FileInputStream(currFile);
					status = ftpclient.storeFile(ftpStorePath + currFile.getName(), fis);
					fis.close();
					if (status == true){
						currFile.delete();
					}
				}
				catch(Exception e){
					Log.d(TAG, "Error: could not send file " + currFile.getName() );
				}
			}
			ftpclient.ftpDisconnect();
		}
		else{
			Log.d(TAG, "Couldn't connect to the ftp server");
		}
	}
}
