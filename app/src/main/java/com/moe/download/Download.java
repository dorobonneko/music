package com.moe.download;
import com.moe.entity.PlayItem;
import android.content.Context;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.io.InputStream;
import com.moe.entity.DownloadItem;
import java.io.IOException;
import java.io.File;
import android.content.SharedPreferences;
import android.support.v4.provider.DocumentFile;
import android.net.Uri;
import com.moe.utils.DocumentFileUtils;
import java.io.OutputStream;
import java.io.FileOutputStream;
import com.moe.database.DownloadDatabase;
import com.moe.services.DownloadService;
import android.os.Environment;

public class Download extends Thread
{
	private DownloadItem pi;
	private SharedPreferences setting;
	private DownloadService service;
	private DownloadDatabase dd;
	private HttpURLConnection huc=null;
	private InputStream is=null;
	private OutputStream os=null;
	public Download(DownloadItem pi,DownloadService context){
		this.pi=pi;
		setting=context.getSharedPreferences("setting",0);
		this.service=context;
		dd=DownloadDatabase.getInstance(context);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof DownloadItem)
			return pi.equals(obj);
		return super.equals(obj);
	}

	public DownloadItem getDownloadItem()
	{
		return pi;
	}
	public void close(){
		try
		{
			if (os != null)os.close();
		}
		catch (Exception e)
		{}
		try
		{
			if (is != null)is.close();
		}
		catch (Exception e)
		{}
		if (huc != null)huc.disconnect();
	}
	@Override
	public void run()
	{
		dd.updateState(pi.getUrl(),DownloadService.State.LOADING);
		pi.setState(DownloadService.State.LOADING);
		File file=new File(pi.getDir());
		try{
		huc=(HttpURLConnection)new URL(pi.getUrl()).openConnection();
		huc.setRequestProperty("Accept", "*/*");
		huc.setRequestProperty("Connection", "Keep-Alive");
		//request.addHeader("Icy-MetaData", "1");
		huc.setRequestProperty("Accept-Encoding","gzip");
		if(pi.getTotal()!=0)
		huc.setRequestProperty("Range", "bytes=" + file.length() + "-" + pi.getTotal());
		is=huc.getInputStream();
		if("gzip".equalsIgnoreCase(huc.getHeaderField("Content-Encoding")))
			is=new GZIPInputStream(is);
			if(pi.getTotal()==0){
				long length = huc.getHeaderFieldLong("Content-Length",0);
				if(length==0)
					length=huc.getContentLengthLong();
				pi.setTotal(length);
				//更新数据长度
				dd.updateTotal(pi.getUrl(),length);
			}
		if(file.getAbsolutePath().startsWith(Environment.getExternalStorageDirectory().getAbsolutePath())){
			File parent=file.getParentFile();
			if(!parent.exists())parent.mkdirs();
			os=new FileOutputStream(file,true);
		}else if(setting.getString("sdcard",null)!=null){
			os=service.getContentResolver().openOutputStream(DocumentFileUtils.getDocumentFilePath(DocumentFile.fromTreeUri(service,Uri.parse(setting.getString("sdcard",null))),file).getUri(),"wa");
		}else
			throw new IOException();
			switch(huc.getResponseCode()){
				case 200:
					is.skip(file.length());
					break;
				case 206:
					break;
				default:
				throw new IOException();
			}
			byte[] buffer=new byte[4096];
			int len=-1;
			while((len=is.read(buffer))!=-1){
				os.write(buffer,0,len);
			}
			os.flush();
			pi.setState(DownloadService.State.SUCCESS);
			dd.updateState(pi.getUrl(),DownloadService.State.SUCCESS);
			service.onItemEnd(this,true);
			}catch(Exception e){
			pi.setState(DownloadService.State.ERROR);
			dd.updateState(pi.getUrl(),DownloadService.State.ERROR);
			service.onItemEnd(this,false);
			}
		finally{
			try
			{
				if (os != null)os.close();
			}
			catch (IOException e)
			{}
			try
			{
				if (is != null)is.close();
			}
			catch (IOException e)
			{}
			if (huc != null)huc.disconnect();
		}
	}
	
}
