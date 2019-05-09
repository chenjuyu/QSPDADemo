package com.qs.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * ���������㲥�����������ڿ�������ɨ�����
 * @author wsl
 *
 */
public class StartReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		//���յ����������Ĺ㲥
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
		    //����ɨ�����
			Intent newIntent = new Intent(context, ScanService.class);
	    	newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(newIntent);
			//������ӡ����
			Intent newIntent1 = new Intent(context, PrintService.class);
	    	newIntent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(newIntent1);
			
		}
	}

}
