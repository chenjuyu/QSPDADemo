package com.qspda.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qs.qs3505pdademo.R;
import com.qs.service.PrintService;
import com.qs.service.ScanService;
import com.qs.wiget.App;

public class MainActivity extends Activity implements OnClickListener {

	private LinearLayout openScan, scanModel, input, pref;
	private TextView prefixx;
	private ImageView isScan,isModel,isInput;
	
	SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);

		init();

		// 启动扫描服务
		Intent newIntent = new Intent(this, ScanService.class);
		newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startService(newIntent);
		// 启动打印服务
		Intent newIntent1 = new Intent(this, PrintService.class);
		newIntent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startService(newIntent1);

	}

	private void init() {
		// TODO Auto-generated method stub
		openScan = (LinearLayout) findViewById(R.id.openScan);
		scanModel = (LinearLayout) findViewById(R.id.scanModel);
		input = (LinearLayout) findViewById(R.id.input);
		pref = (LinearLayout) findViewById(R.id.pref);
		
		prefixx=(TextView) findViewById(R.id.prefixx);
		
		isScan=(ImageView) findViewById(R.id.isScan);
		isModel=(ImageView) findViewById(R.id.isModel);
		isInput=(ImageView) findViewById(R.id.isInput);
		
		openScan.setOnClickListener(this);
		scanModel.setOnClickListener(this);
		input.setOnClickListener(this);
		pref.setOnClickListener(this);
		
		if(App.isScan1){
			isScan.setVisibility(View.VISIBLE);
		}else{
			isScan.setVisibility(View.INVISIBLE);
		}
		
		if(App.isModel1){
			isModel.setVisibility(View.VISIBLE);
		}else{
			isModel.setVisibility(View.INVISIBLE);
		}
		
		if(App.isInput1){
			isInput.setVisibility(View.VISIBLE);
		}else{
			isInput.setVisibility(View.INVISIBLE);
		}
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		// 启用扫描
		case R.id.openScan:
			
			if(App.isScan1){
				isModel.setVisibility(View.INVISIBLE);
				App.isScan1=false;
			}else{
				isModel.setVisibility(View.VISIBLE);
				App.isScan1=true;
			}

			break;
		// 触发模式
		case R.id.scanModel:
			if(App.isModel1){
				isModel.setVisibility(View.INVISIBLE);
				App.isModel1=false;
			}else{
				isModel.setVisibility(View.VISIBLE);
				App.isModel1=true;
			}
			break;
		// 前台输出
		case R.id.input:
			
			if(App.isInput1){
				isInput.setVisibility(View.INVISIBLE);
				App.isInput1=false;
			}else{
				isInput.setVisibility(View.VISIBLE);
				App.isInput1=true;
			}
			
			break;
		// 前缀
		case R.id.pref:
			alert_edit(v);
			break;

		default:
			break;
		}
	}

	
	 public void alert_edit(View view){
	        final EditText et = new EditText(this);
	        new AlertDialog.Builder(this).setTitle("请输入前缀")
//	                .setIcon(android.R.drawable.sym_def_app_icon)
	                .setView(et)
	                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialogInterface, int i) {
	                        //按下确定键后的事件
	                    	Toast.makeText(getApplicationContext(), et.getText().toString(),Toast.LENGTH_LONG).show();
	                    	App.prefixx=et.getText().toString();
	                    	prefixx.setText(et.getText().toString());
	                    	sp = getSharedPreferences("sp_demo", Context.MODE_PRIVATE);
	                    	sp.edit().putString("prefixx", et.getText().toString())
	                    	.commit();
	                    }
	                }).setNegativeButton("取消",null).show();
	    }
	 
	 @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
        	sp = getSharedPreferences("sp_demo", Context.MODE_PRIVATE);
        	sp.edit().putBoolean("isScan", App.isScan1)
        	.putBoolean("isModel", App.isModel1)
        	.putBoolean("isInput", App.isInput1)
        	.commit();
		super.onDestroy();
	}
}
