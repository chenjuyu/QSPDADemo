package com.qs.service;

import java.io.UnsupportedEncodingException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.posapi.PosApi;
import android.util.Log;
import android.widget.Toast;

import com.qs.qs3505pdademo.R;
import com.qs.wiget.App;

/**
 * ɨ������� 
 * ����PDAɨ��ķ�����
 * @author wsl
 *
 */
public class ScanService extends Service {

	public static PosApi mApi = null;
    //GPIO��Դ�Ŀ���
	private static byte mGpioPower = 0x1E;// PB14
	private static byte mGpioTrig = 0x29;// PC9
    //���ںźͲ����ʵ�����
	private static int mCurSerialNo = 3; // usart3
	private static int mBaudrate = 4; // 9600
    //SCAN��������
	private ScanBroadcastReceiver scanBroadcastReceiver;
    //F3������
	private ScanBroadcastReceiver_F3 scanBroadcastReceiver_F3;
    //ɨ����������
	private MediaPlayer player;
	// ��Ƶ����
	private AudioManager audioManager = null; 


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
        //������ʵ����
		player = MediaPlayer.create(getApplicationContext(), R.raw.beep);
        //��Ƶ��ʼ��
		audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
		
		super.onCreate();

	}

	public static void init() {

		//mApi�ำֵ
		mApi = App.getInstance().getPosApi();
        //�ӳ�һ��򿪴��ڣ����Ϊ�˳�ʼ��ɨ��ͷ�������ӳ�һ��ִ�У����������ӳٴ�ӡ���ߴ�ӡ������������ע��
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//��ɨ�贮��
				openDevice();
			}
		}, 1000);
		
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
			String action = intent.getAction();
			if (action.equalsIgnoreCase(PosApi.ACTION_POS_COMM_STATUS)) {
				int cmdFlag = intent.getIntExtra(PosApi.KEY_CMD_FLAG, -1);
				byte[] buffer = intent
						.getByteArrayExtra(PosApi.KEY_CMD_DATA_BUFFER);
				switch (cmdFlag) {
			    // ����ɨ����Ϣ�Ĵ���
				case PosApi.POS_EXPAND_SERIAL3:
					//���Ϊ�գ�����
					if (buffer == null)
						return;
					//����ɨ��������ʾ�Ѿ�ɨ�赽��Ϣ
					player.start();
					try {
						//��ɨ��ͷ��������byte�ֽ�ת���ַ���
						String str = new String(buffer, "GBK");
						Log.e("ScanStr", "-----:" + str.trim());
						//׼��ͨ���㲥����ɨ����Ϣ������Ǽ��ɽ��Լ���Ŀ���˶οɺ���
						Intent intentBroadcast = new Intent();
						Intent intentBroadcast1 = new Intent();
						//���÷��͹㲥��action
						intentBroadcast.setAction("com.qs.scancode");
						intentBroadcast1.setAction("com.zkc.scancode");
						//����ɨ����Ϣ��intent��
						intentBroadcast.putExtra("code", App.prefixx+str.trim());
						intentBroadcast1.putExtra("code", App.prefixx+str.trim());
						//���͹㲥����Softkeyboard���߹ȸ�ƴ�����뷨��ע���н��ո���㲥�Ľ����������뷨���յ�������Ϣ���뵽��ǰ���ڽ���ı༭����
						if(App.isInput1){
						sendBroadcast(intentBroadcast);
						sendBroadcast(intentBroadcast1);
						}
						//���õ�ǰ�ͻ����ٴν���ɨ��
						isScan = false;
						//����ɨ��ͷ��ѹ��ʹɨ��ͷϨ��
						ScanService.mApi.gpioControl(mGpioTrig, 0, 1);
						//�Ƴ�ɨ��ͷϨ���߳�
						handler.removeCallbacks(run);
						// ����ָ����ģʽ������
						// ��һ���������������е�һ��Ԫ���ǵȴ��೤��ʱ��������𶯣�
						// ֮�󽫻��ǿ����͹ر��𶯵ĳ���ʱ�䣬��λΪ����
						// �ڶ����������ظ���ʱ��pattern�е��������������Ϊ-1���ʾ���ظ���
						// vibrator.vibrate(new long[]{1000,50,50,100,50},-1);
                        //��2��
						// vibrator.vibrate(2000);

					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
				buffer = null;
			}
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
//    //��ɨ��ͷ��ע����������һ�ε�ѹ��ɨ��ͷ������һ�ι���
//	public void openScan() {
//		//����ɨ��ͷ��ѹ
//		ScanService.mApi.gpioControl(mGpioTrig, 0, 0);
//		try {
//			//����100ms
//			Thread.sleep(100);
//		} catch (Exception e) {
//		}
//		//����ɨ��ͷ��ѹ
//		ScanService.mApi.gpioControl(mGpioTrig, 0, 1);
//	}

	//�򿪴���
	private static void openDevice() {
		// GPIO��������ʼ��
		mApi.gpioControl(mGpioPower, 0, 1);
        //ɨ�贮�ڳ�ʼ��
		mApi.extendSerialInit(mCurSerialNo, mBaudrate, 1, 1, 1, 1);
		
	}

   
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		//�ر������²㴮��
		mApi.closeDev();
		super.onDestroy();
	}

	boolean isScan = false;
    //SCAN�����ļ���
	class ScanBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(App.isScan1&&App.isModel1){
			if (!isScan) {
				//ɨ��ͷδ����ɨ��״̬
				//��ɨ��ͷ
				ScanService.mApi.gpioControl(mGpioTrig, 0, 0);
				isScan = true;
				handler.removeCallbacks(run);
				handler.postDelayed(run, 3000);
			} else {
				//ɨ��ͷ����ɨ��ͷ״̬���ȹص�ɨ��ͷ��
				ScanService.mApi.gpioControl(mGpioTrig, 0, 1);
				//��ɨ��ͷ
				ScanService.mApi.gpioControl(mGpioTrig, 0, 0);
				isScan = true;
				handler.removeCallbacks(run);
				handler.postDelayed(run, 3000);
			}
			}
		}
	}
	
	 //F3�����ļ���
	class ScanBroadcastReceiver_F3 extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			//�����������ڿ�
//			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
//					AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND
//							| AudioManager.FLAG_SHOW_UI);
			Toast.makeText(context, "F3���", 0).show();
		}
	}

	Handler handler = new Handler();
	Runnable run = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//��һ��ʱ�������ɨ��ͷ��ѹ���ص�ɨ���
			ScanService.mApi.gpioControl(mGpioTrig, 0, 1);
			isScan = false;
		}
	};

	 /**
     * ע��㲥������
     */
    private void registerListener() {
    	
    	//ע��ɨ����Ϣ�Ľ�����
        IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(PosApi.ACTION_POS_COMM_STATUS);
		registerReceiver(receiver_, mFilter);
		//SCAN��������ʱ��㲥�Ľ�����
		scanBroadcastReceiver = new ScanBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("ismart.intent.scandown");
		this.registerReceiver(scanBroadcastReceiver, intentFilter);
		//F3��������ʱ��㲥�Ľ�������F3��������5501�������ұߣ�Ҳ����ɨ��������Ǹ�С����
		scanBroadcastReceiver_F3 = new ScanBroadcastReceiver_F3();
		IntentFilter intentFilter_f3 = new IntentFilter();
		intentFilter_f3.addAction("ismart.intent.f3down");
		this.registerReceiver(scanBroadcastReceiver_F3, intentFilter_f3);
		
    }
    

}
