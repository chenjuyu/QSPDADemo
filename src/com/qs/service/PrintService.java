package com.qs.service;

import java.io.UnsupportedEncodingException;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.posapi.PosApi;
import android.posapi.PrintQueue;
import android.posapi.PrintQueue.OnPrintListener;
import android.widget.Toast;

import com.qs.wiget.App;

public class  PrintService extends Service{

	public static PosApi mApi = null;
    
	private ScanBroadcastReceiver scanBroadcastReceiver_text;
    
	private ScanBroadcastReceiver_bitmap scanBroadcastReceiver_bitmap;
	
	private  PrintQueue mPrintQueue = null;
	
	boolean isCanPrint=true;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		//初始化
		init();
        //注册广播接收器
		registerListener();
		super.onCreate();

	}

	public void init() {

		//mApi类赋值
		mApi = App.getInstance().getPosApi();
     
		//打印队列赋值
		mPrintQueue = new PrintQueue(this, ScanService.mApi);
				//打印队列初始化
				mPrintQueue.init();
				//打印队列设置监听
				mPrintQueue.setOnPrintListener(new OnPrintListener() {
					//打印完成
					@Override
					public void onFinish() {
						// TODO Auto-generated method stub
						//打印完成
						Toast.makeText(PrintService.this,
								"complete", Toast.LENGTH_SHORT)
								.show();
						//当前可打印
						isCanPrint=true;
					}
		            //打印失败
					@Override
					public void onFailed(int state) {
						// TODO Auto-generated method stub
						isCanPrint=true;
						switch (state) {
						case PosApi.ERR_POS_PRINT_NO_PAPER:
							// 打印缺纸
							showTip("no_paper");
							break;
						case PosApi.ERR_POS_PRINT_FAILED:
							// 打印失败
							showTip("failed");
							break;
						case PosApi.ERR_POS_PRINT_VOLTAGE_LOW:
							// 电压过低
							showTip("voltate_low");
							break;
						case PosApi.ERR_POS_PRINT_VOLTAGE_HIGH:
							// 电压过高
							showTip("voltate_high");
							break;
						}
					}

					@Override
					public void onGetState(int arg0) {
						// TODO Auto-generated method stub
						
					}
		            //打印设置
					@Override
					public void onPrinterSetting(int state) {
						// TODO Auto-generated method stub
						isCanPrint=true;
						switch(state){
						case 0:
							Toast.makeText(PrintService.this, "持续有纸", Toast.LENGTH_SHORT).show();
							break;
						case 1:
							//缺纸
							Toast.makeText(PrintService.this, "no paper", Toast.LENGTH_SHORT).show();
							break;
						case 2:
							//检测到黑标
							Toast.makeText(PrintService.this, "label", Toast.LENGTH_SHORT).show();
							break;
						}
					}
				});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 扫描头扫描信息接收器
	 */
	BroadcastReceiver receiver_ = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			
		}
	};

	//将字符串转成GBK格式字符串
	public String toGBK(String str) throws UnsupportedEncodingException {
		return this.changeCharset(str, "GBK");
	}

	/**
	 * 字符串编码转换的实现方法
	 * 
	 * @param str
	 *            待转换编码的字符串
	 * @param newCharset
	 *            目标编码
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String changeCharset(String str, String newCharset)
			throws UnsupportedEncodingException {
		if (str != null) {
			// 用默认字符编码解码字符串。
			byte[] bs = str.getBytes(newCharset);
			// 用新的字符编码生成字符串
			return new String(bs, newCharset);
		}
		return null;
	}
  
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		//关闭整个下层串口
		mApi.closeDev();
		super.onDestroy();
	}

	boolean isScan = false;
	class ScanBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
		
			String str=intent.getExtras().getString("text");;
			
			try {
				mPrintQueue.addText(50,str.getBytes("GBK"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mPrintQueue.printStart();
		}
	}
	
	 
	class ScanBroadcastReceiver_bitmap extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
//			if(!isCanPrint) return;
//			bitmap
			Bitmap mBitmap=intent.getParcelableExtra("bitmap");
			
			byte[] printData = bitmap2PrinterBytes(mBitmap);
			mPrintQueue.addBmp(50, 30, mBitmap.getWidth(),
					mBitmap.getHeight(), printData);
			
			mPrintQueue.printStart();
			
		}
	}

	public  static  byte[] bitmap2PrinterBytes (Bitmap bitmap){
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		//Log.v("hello", "height?:"+height);
		int startX = 0;
		int startY = 0;
		int offset = 0;
		int scansize = width;
		int writeNo = 0;
		int rgb=0;
		int colorValue = 0;
		int[] rgbArray = new int[offset + (height - startY) * scansize
		                         + (width - startX)];
		bitmap.getPixels(rgbArray, offset, scansize, startX, startY,
				width, height);

		int iCount = (height % 8);
		if (iCount > 0) {
			iCount = (height / 8) + 1;
		} else {
			iCount = (height / 8);
		}

		byte [] mData = new byte[iCount*width];

		//Log.v("hello", "myiCount?:"+iCoun t);
		for (int l = 0; l <= iCount - 1; l++) {
			//Log.v("hello", "iCount?:"+l);
			//Log.d("hello", "l?:"+l);
			for (int i = 0; i < width; i++) {
				int rowBegin = l * 8;
				//Log.v("hello", "width?:"+i);
				int tmpValue = 0;
				int leftPos = 7;
				int newheight = ((l + 1) * 8 - 1);
				//Log.v("hello", "newheight?:"+newheight);
				for (int j = rowBegin; j <=newheight; j++) {
					//Log.v("hello", "width?:"+i+"  rowBegin?:"+j);
					if (j >= height) {
						colorValue = 0;
					} else {
						rgb = rgbArray[offset + (j - startY)* scansize + (i - startX)];
						if (rgb == -1) {
							colorValue = 0;
						} else {
							colorValue = 1;
						}
					}
					//Log.d("hello", "rgbArray?:"+(offset + (j - startY)
					//		* scansize + (i - startX)));
					//Log.d("hello", "colorValue?:"+colorValue);
					tmpValue = (tmpValue + (colorValue << leftPos));
					leftPos = leftPos - 1;					

				}
				mData[writeNo]=(byte) tmpValue;
				writeNo++;
			}
		}

		return mData;
	}
	
    /**
     * 提示框
     * @param msg 提示内容
     */
	private void showTip(String msg) {
		new AlertDialog.Builder(this)
				.setTitle("tips")
				.setMessage(msg)
				.setNegativeButton(("close"),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						}).show();
	}

	 /**
     * 注册广播接收器
     */
    private void registerListener() {
    	
    	
        IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(PosApi.ACTION_POS_COMM_STATUS);
		registerReceiver(receiver_, mFilter);
		
		scanBroadcastReceiver_text = new ScanBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("COM.QSPDA.PRINTTEXT");
		this.registerReceiver(scanBroadcastReceiver_text, intentFilter);
		
		scanBroadcastReceiver_bitmap = new ScanBroadcastReceiver_bitmap();
		IntentFilter intentFilter_1 = new IntentFilter();
		intentFilter_1.addAction("COM.QSPDA.PRINTBITMAP");
		this.registerReceiver(scanBroadcastReceiver_bitmap, intentFilter_1);
		
    }
    
}
