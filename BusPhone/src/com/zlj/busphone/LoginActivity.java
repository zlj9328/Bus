package com.zlj.busphone;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity{
	
	private EditText username;
	private EditText password;
	private Button   login;
	private long mExitTime = 0;
	
	private String myname = "aa";
	private String mypassword = "123456";
	
	private SoundPool soundPool;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        
//        soundPool= new SoundPool(10,AudioManager.STREAM_SYSTEM,5);
//        soundPool.load(this,R.raw.PleaseStartPlease,1);
//        soundPool.play(1,1, 1, 0, 0, 1);
        
        username = (EditText)findViewById(R.id.username_edit);
        password = (EditText)findViewById(R.id.password_edit);
        login    = (Button)findViewById(R.id.login_button);
        
        login.setOnClickListener(new View.OnClickListener() {
			
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String namebuf;
			String passwordbuf;
			
			namebuf = username.getText().toString();
			passwordbuf = password.getText().toString();
			
			if(namebuf.equals(myname) && passwordbuf.endsWith(mypassword))
			{
        		Intent MainActivityIntent = new Intent(LoginActivity.this, MainActivity.class);
        		//下面两行代码启动一个新的Activity，同时关闭当前Activity。
        		LoginActivity.this.startActivity(MainActivityIntent);
        		LoginActivity.this.finish();
			}
			else
			{
				Toast.makeText(getApplicationContext(), "账号或密码错误，请重新输入", Toast.LENGTH_SHORT).show();
			}
			
		}
	});
        
    }
    
    /*
     * 函数名：onKeyDown
     * 参    数：keyCode，event
     * 返回值：boolean
     * 说    明：按两次返回键退出程序
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                		Toast.makeText(getApplicationContext(),"再按一次退出程序", Toast.LENGTH_SHORT).show();
                        
                        mExitTime = System.currentTimeMillis();

                } else {
                        finish();
                        System.exit(0);
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }	
}
