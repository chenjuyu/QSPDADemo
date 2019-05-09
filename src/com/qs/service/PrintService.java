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
		//��ʼ��
		init();
        //ע��㲥������
		registerListener();
		super.onCreate();

	}

	public void init() {

		//mApi�ำֵ
		mApi = App.getInstance().getPosApi();
     
		//��ӡ���и�ֵ
		mPrintQueue = new PrintQueue(this, ScanService.mApi);
				//��ӡ���г�ʼ��
				mPrintQueue.init();
				//��ӡ�������ü���
				mPrintQueue.setOnPrintListener(new OnPrintListener() {
					//��ӡ���
					@Override
					public void onFinish() {
						// TODO Auto-generated method stub
						//��ӡ���
						Toast.makeText(PrintService.this,
								"complete", Toast.LENGTH_SHORT)
								.show();
						//��ǰ�ɴ�ӡ
						isCanPrint=true;
					}
		            //��ӡʧ��
					@Override
					public void onFailed(int state) {
						// TODO Auto-generated method stub
						isCanPrint=true;
						switch (state) {
						case PosApi.ERR_POS_PRINT_NO_PAPER:
							// ��ӡȱֽ
							showTip("no_paper");
							break;
						case PosApi.ERR_POS_PRINT_FAILED:
							// ��ӡʧ��
							showTip("failed");
							break;
						case PosApi.ERR_POS_PRINT_VOLTAGE_LOW:
							// ��ѹ����
							showTip("voltate_low");
							break;
						case PosApi.ERR_POS_PRINT_VOLTAGE_HIGH:
							// ��ѹ����
							showTip("voltate_high");
							break;
						}
					}

					@Override
					public void onGetState(int arg0) {
						// TODO Auto-generated method stub
						
					}
		            //��ӡ����
					@Override
					public void onPrinterSetting(int state) {
						// TODO Auto-generated method stub
						isCanPrint=true;
						switch(state){
						case 0:
							Toast.makeText(PrintService.this, "������ֽ", Toast.LENGTH_SHORT).show();
							break;
						case 1:
							//ȱֽ
							Toast.makeText(PrintService.this, "no paper", Toast.LENGTH_SHORT).show();
							break;
						case 2:
							//��⵽�ڱ�
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
	 * ɨ��ͷɨ����Ϣ������
	 */
	BroadcastReceiver receiver_ = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			
		}
	};

	//���ַ���ת��GBK��ʽ�ַ���
	public String toGBK(String str) throws UnsupportedEncodingException {
		return this.changeCharset(str, "GBK");
	}

	/**
	 * �ַ�������ת����ʵ�ַ���
	 * 
	 * @param str
	 *            ��ת��������ַ���
	 * @param newCharset
	 *            Ŀ�����
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String changeCharset(String str, String newCharset)
			throws UnsupportedEncodingException {
		if (str != null) {
			// ��Ĭ���ַ���������ַ�����
			byte[] bs = str.getBytes(newCharset);
			// ���µ��ַ����������ַ���
			return new String(bs, newCharset);
		}
		return null;
	}
  
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		//�ر������²㴮��
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
     * ��ʾ��
     * @param msg ��ʾ����
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
     * ע��㲥������
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
