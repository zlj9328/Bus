package com.zlj.busphone;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MessageClss {

	public Socket socket;
	public String severIP = "192.168.22.1";// IP121.41.36.196
	public int severPort = 8888;// 端口
	
	public List<Integer> RecSiteDataList = new ArrayList<Integer>();//站点标号
	public List<Double> SiteLatlLngList = new ArrayList<Double>();//站点经纬度
	public int SiteNum;
	
	public OutputStream writemsg = null;
	public InputStream readmsg = null;
	public int flag = 1;

	//站点坐标
	public static final Double[] SiteLatLng = {	30.321927, 120.344380, 30.322044, 120.348378,
												30.322137, 120.353103, 30.319971, 120.371554,
												30.311218, 120.355403, 30.316331, 120.354567,
												30.311795, 120.346860, 30.321233, 120.347444,
												30.320992, 120.349734, 30.322067, 120.351594,
												30.319370, 120.347075, 30.317188, 120.349636
	}; 
	
	/**
	 * 坐标信息
	 * @author zlj
	 *
	 */
	public static class LatLngMsg
	{
		public byte[] head = {3,0,0,0};
		public int len = 20;
		public int[] latlngmag = new int[4];
	}

	/**
	 * 接收数据包
	 * @author zlj
	 *
	 */
	public static class NetPacket {
		
		public int head;
		public byte[] data;
	}
	
	/**
	 * 请求数据包
	 * @author zlj
	 *
	 */
	public static class RequestPacket
	{
		public int head = 4;
		public byte[] msg = {4,0,0,0};
	}
	
	/**
	 * 接收数据包解析，解析为实际地址对应的标号
	 * @param len
	 * @param data
	 */
	void GetSite(int len, byte[] data)
	{
		int i,j,k;
		int dat;
		byte[] buf = new byte[4];
		
		RecSiteDataList.clear();
		for(i=0,k=0; i<len/4; i++)
		{	//四个byte为一个int
			for(j=0; j<4; j++,k++)
			{
				buf[j] = data[k];
			}
			dat = bytesToIntLittle(buf);
			if(i>0)//去掉包头信息，List第一个数为站点的长度
			{
				RecSiteDataList.add(dat);
			}
		}
	}
	
	/**
	 * 得到站点经纬度
	 */
	void GetSiteLatLng()
	{
		int len,num;
		int i;
		SiteLatlLngList.clear();
		
		if(!RecSiteDataList.isEmpty())
		{
			len = RecSiteDataList.size() - 1;
			SiteNum = RecSiteDataList.get(0);//得到站点数
			
			for(i=0; i<len; i++)
			{
				num = RecSiteDataList.get(i+1);
				//添加坐标
				SiteLatlLngList.add(SiteLatLng[num*2]);
				SiteLatlLngList.add(SiteLatLng[num*2 + 1]);
			}
		}
	}
	
	
	void SendMsg(byte[] msg, int len)
	{
		try {
			writemsg.write(msg);
			writemsg.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	/**
	 * 将坐标转换为int，发送到服务器
	 * @param lat
	 * @param lng
	 * @return
	 */
	int[] ChangeLatLngToInt(double lat, double lng)
	{
		int[] data = new int[4];
		
		data[0] = (int)(lat * 100);//发送经度高四位
		data[1] = (int)((lat * 100 - data[0]) * 10000);//经度低四位
		
		data[2] = (int)(lng * 100);//纬度高四位
		data[3] = (int)((lng * 100 - data[2]) * 10000);//纬度低四位
		return data;
	}
	

	void Close() {
		if (flag == 0) {
			try {
				writemsg.close();
				readmsg.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 基于位移的int转化成byte[]
	 * 
	 * @param int number
	 * @return byte[]
	 */

	public byte[] intToByte(int number) {
		byte[] abyte = new byte[4];
		// "&" 与（AND），对两个整型操作数中对应位执行布尔代数，两个位都为1时输出1，否则0。
		abyte[3] = (byte) (0xff & number);
		// ">>"右移位，若为正数则高位补0，若为负数则高位补1
		abyte[2] = (byte) ((0xff00 & number) >> 8);
		abyte[1] = (byte) ((0xff0000 & number) >> 16);
		abyte[0] = (byte) ((0xff000000 & number) >> 24);
		return abyte;
	}

	/**
	 * 基于位移的 byte[]转化成int,大端
	 * 
	 * @param byte[] bytes
	 * @return int number
	 */

	public int bytesToIntBig(byte[] bytes) {
		int number = bytes[3] & 0xFF;
		// "|="按位或赋值。
		number |= ((bytes[2] << 8) & 0xFF00);
		number |= ((bytes[1] << 16) & 0xFF0000);
		number |= ((bytes[0] << 24) & 0xFF000000);
		return number;
	}
	
	/**
	 * 基于位移的 byte[]转化成int,小端
	 * @param bytes
	 * @return
	 */
	public int bytesToIntLittle(byte[] bytes) {
		int number = bytes[0] & 0xFF;
		// "|="按位或赋值。
		number |= ((bytes[1] << 8) & 0xFF00);
		number |= ((bytes[2] << 16) & 0xFF0000);
		number |= ((bytes[3] << 24) & 0xFF000000);
		return number;
	}
}
