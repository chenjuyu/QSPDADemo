package com.qs.wiget;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.posapi.PosApi;
import android.util.Log;




public class App extends Application{
	

	private static String mCurDev1 = "";

	static App instance = null;
	//PosSDK mSDK = null;
	
	static PosApi mPosApi = null;
	
	public static boolean isScan1=false;
	public static boolean isModel1=false;
	public static boolean isInput1=false;
	
	public static String prefixx="";
	
	SharedPreferences sp;
	
	
	public App(){
		 super.onCreate();
		instance = this;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		//mDb = Database.getInstance(this);
		Log.v("hello", "APP onCreate~~");
		//mSDK = PosSDK.getInstance(this);
		mPosApi = PosApi.getInstance(this);
//		mPosApi.closeDev()
		init();
		
		sp = getSharedPreferences("sp_demo", Context.MODE_PRIVATE);
		isScan1= sp.getBoolean("isScan", true);
		isModel1= sp.getBoolean("isModel", true);
		isInput1= sp.getBoolean("isInput", true);
		
		prefixx=sp.getString("prefixx", "");
	}
	
	public static void init(){
			//根据型号进行初始化mPosApi类
			if (Build.MODEL.contains("LTE")||android.os.Build.DISPLAY.contains("3508")||
					android.os.Build.DISPLAY.contains("403")||
					android.os.Build.DISPLAY.contains("35S09")) {
				mPosApi.initPosDev("ima35s09");
				setCurDevice("ima35s09");
			} else if(Build.MODEL.contains("5501")){
				mPosApi.initPosDev("ima35s12");
				setCurDevice("ima35s12");
			}else{
				mPosApi.initPosDev(PosApi.PRODUCT_MODEL_IMA80M01);
				setCurDevice(PosApi.PRODUCT_MODEL_IMA80M01);
			}
			
		}
	
	
	public static  App getInstance(){
		if(instance==null){
			instance =new App();
		}
		return instance;
	}


	public String getCurDevice() {
		return mCurDev1;
	}

	public static  void setCurDevice(String mCurDev) {
		mCurDev1 = mCurDev;
	}

	public PosApi getPosApi(){
		return mPosApi;
	}
	
	
	
}
