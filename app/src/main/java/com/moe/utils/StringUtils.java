package com.moe.utils;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.io.IOException;
import android.text.TextUtils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtils
{

	public static String md5(String hash)
	{
	if (TextUtils.isEmpty(hash)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(hash.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
	}
	public static String getString(InputStream input) throws IOException{
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		byte[] buffer=new byte[4096];
		int len=-1;
		while((len=input.read(buffer))!=-1){
			baos.write(buffer,0,len);
		}
		baos.flush();
		String data=baos.toString();
		baos.close();
		return data;
	}
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
		String data=getString(is);
		is.close();
		huc.disconnect();
		return data;
		}catch(Exception e){}
		return null;
	}
}
