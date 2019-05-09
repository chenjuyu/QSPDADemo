package com.qs.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 开机启动广播接收器，用于开机启动扫描服务
 * @author wsl
 *
 */
public class StartReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		//接收到开机启动的广播
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
		    //启动扫描服务
			Intent newIntent = new Intent(context, ScanService.class);
	    	newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(newIntent);
			//启动打印服务
			Intent newIntent1 = new Intent(context, PrintService.class);
	    	newIntent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(newIntent1);
			
		}
	}

}
