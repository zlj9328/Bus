package com.zlj.busphone;


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;
import com.zlj.AndroidToolsClass.AndroidToolsClass;
import com.zlj.BaiDuMap.BaiDuMapHelper;
import com.zlj.busphone.MessageClss.LatLngMsg;
import com.zlj.busphone.MessageClss.NetPacket;
import com.zlj.busphone.MessageClss.RequestPacket;

public class MainActivity extends Activity {
	
	private MapView mMapView = null;
	private BaiDuMapHelper mBaiDuMap;
	private AndroidToolsClass mAndroidToolsClass;
	private long mExitTime = 0;
	private MessageClss msgc;

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext  
        SDKInitializer.initialize(getApplicationContext());  
        
        setContentView(R.layout.activity_main);
        
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiDuMap = new BaiDuMapHelper(getApplicationContext(), mMapView);
        mBaiDuMap.StartLocate();
        
        mAndroidToolsClass = new AndroidToolsClass(getApplicationContext());
        mAndroidToolsClass.CheckNetworkStatus();//联网监听
        
		msgc = new MessageClss();
		
		//创建线程
		DrawLineThread RMThread = new DrawLineThread();
		RMThread.start();
		
		//启动日志服务
		Intent  serviceIntent=new Intent();
		serviceIntent.setAction("com.zlj.busphone.MY_SERVICE");
		serviceIntent.setClass(MainActivity.this, LogService.class);
		startService(serviceIntent);
	
		writeFileData("busPhone.txt", "busPhone11111");
		System.out.println(readLine("busPhone.txt"));
    }
    
    public void writeFileData(String fileName, String message) {  
    
        try {  
        	
            /* 第一个参数，代表文件名称，注意这里的文件名称不能包括任何的/或者/这种分隔符，只能是文件名 
             *          该文件会被保存在/data/data/应用名称/files/chenzheng_java.txt */
            FileOutputStream outputStream = openFileOutput(fileName, Activity.MODE_PRIVATE);  
            outputStream.write(message.getBytes());
            outputStream.write("/r/t".getBytes());
            outputStream.flush();  
            outputStream.close();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } 
    }

    
    public String readLine(String fileName) {  
    	String content =null;
        try {  
            FileInputStream inputStream = this.openFileInput(fileName);  
            byte[] bytes = new byte[1024];  
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();  
            while (inputStream.read(bytes) != -1) {  
                arrayOutputStream.write(bytes, 0, bytes.length);  
            }  
            inputStream.close();  
            arrayOutputStream.close();  
            content = new String(arrayOutputStream.toByteArray());  
            //showTextView.setText(content);  
            
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return content;
  
    } 
    
   

    
    
    
    /**
     * 数据处理线程
     * @author zlj
     *
     */
    class DrawLineThread extends Thread
    {
    	int recmsglen = 0;
		byte[] recibuf = new byte[512];
		int pktLen = 0;
		int len;
		
		byte[] pktdata = new byte[512];
		
		RequestPacket RePkt = new RequestPacket();
		NetPacket pkt = new NetPacket();

		@Override
		public void run()
		{
			while(msgc.flag == 1)
			{
				try {
					//实例化socket
					msgc.socket = new Socket(msgc.severIP, msgc.severPort);
					msgc.flag = 0;
					System.out.println("连接服务器成功！");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					msgc.flag = 1;
					System.out.println("连接服务器失败！");
					try {
						sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			if(msgc.flag == 0)
			{
				try {//创建数据流
					msgc.writemsg = msgc.socket.getOutputStream();
					msgc.readmsg = msgc.socket.getInputStream();//接收
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
			}
			
			while(true)
			{
				if(msgc.flag == 0)
				{
					try {
						
						//发送请求
						msgc.writemsg.write(msgc.intToByte(RePkt.head));
						msgc.writemsg.write(RePkt.msg);
						msgc.writemsg.flush();
								
						//接收长度
						recmsglen = msgc.readmsg.read(recibuf);
						pktLen = msgc.bytesToIntBig(recibuf);
						len = pktLen;//站点个数
						
						//接收站点信息
						while(pktLen > 0)
						{
							recmsglen = msgc.readmsg.read(recibuf);
							System.arraycopy(recibuf, 0, pktdata, len-pktLen,  recmsglen);	
							pktLen -= recmsglen;
						}
						
						pkt.head = msgc.bytesToIntLittle(pktdata);
						
						//发送位置信息
						LatLngMsg llm = new LatLngMsg();
						llm.latlngmag = msgc.ChangeLatLngToInt(mBaiDuMap.mLatitude, mBaiDuMap.mLongitude);
						msgc.writemsg.write(msgc.intToByte(llm.len));
						msgc.writemsg.write(llm.head);
						msgc.writemsg.write(msgc.intToByte(llm.latlngmag[0]));
						msgc.writemsg.write(msgc.intToByte(llm.latlngmag[1]));
						msgc.writemsg.write(msgc.intToByte(llm.latlngmag[2]));
						msgc.writemsg.write(msgc.intToByte(llm.latlngmag[3]));
						msgc.writemsg.flush();
						
					//	if((Arrays.toString(pkt.data)).equals((Arrays.toString(pktdata))))//判断数据包是否和上次相同
						{
							pkt.data = pktdata;
							
							//解析数据包
							msgc.GetSite(len, pkt.data);
							msgc.GetSiteLatLng();
							
							//画线
							mBaiDuMap.dNum = msgc.SiteNum;
							mBaiDuMap.dSiteLatlLngList = msgc.SiteLatlLngList;
							mBaiDuMap.ShowLocationMark();
						}				    	
					} catch (Exception  e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					try {
						sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}		
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
                        mAndroidToolsClass.ToastShow("再按一次退出程序");
                        mExitTime = System.currentTimeMillis();

                } else {
                        finish();
                        mBaiDuMap.AllDestroy();
                        msgc.Close();
                        System.exit(0);
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }	
    


	@Override  
    protected void onDestroy() {  
        super.onDestroy();  
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理  
        mMapView.onDestroy();  
    }  
    @Override  
    protected void onResume() {  
        super.onResume();  
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理  
        mMapView.onResume();  
    }  
    @Override  
    protected void onPause() {  
        super.onPause();  
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理  
        mMapView.onPause();  
    }  

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
