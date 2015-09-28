package com.zlj.busphone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class StartActivity extends Activity{
	private final int SPLASH_DISPLAY_LENGHT = 1000;	//延时6秒
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		
        //延迟指定时间再执行
        new Handler().postDelayed(new Runnable(){
        	public void run(){
        		Intent LoginActivityIntent = new Intent(StartActivity.this, LoginActivity.class);
        		//下面两行代码启动一个新的Activity，同时关闭当前Activity。
        		StartActivity.this.startActivity(LoginActivityIntent);
        		StartActivity.this.finish();
        	}
        }, SPLASH_DISPLAY_LENGHT);
	}
	
	
}
