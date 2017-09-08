package com.moe.utils;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.io.IOException;

public class StringUtils
{
	public static InputStream getInputStream(String url){
	try
		{
			HttpURLConnection huc=(HttpURLConnection)new URL(url).openConnection();
			return huc.getInputStream();
		}
		catch (IOException e)
		{}
		return null;
	}
	public static String getString(String url){
		return getString("GET",url);
	}
	public static String getString(String method,String url){
		return getString(method,null,url);
	}
	public static String getString(String method,String referer,String url){
		try{
		HttpURLConnection huc=(HttpURLConnection)new URL(url).openConnection();
		huc.setRequestMethod(method);
		if(referer!=null)
			huc.setRequestProperty("Referer",referer);
		InputStream is=huc.getInputStream();
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		byte[] buffer=new byte[4096];
		int len=-1;
		while((len=is.read(buffer))!=-1){
			baos.write(buffer,0,len);
		}
		baos.flush();
		is.close();
		huc.disconnect();
		String data=baos.toString();
		baos.close();
		return data;
		}catch(Exception e){}
		return null;
	}
}
