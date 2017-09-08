package com.moe.services;
import android.app.Service;
import android.os.IBinder;
import android.content.Intent;
import android.content.Context;
import com.moe.entity.PlayItem;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import com.moe.entity.DownloadItem;
import android.os.Environment;
import com.moe.download.Download;
import com.moe.database.DownloadDatabase;
import android.widget.Toast;

public class DownloadService extends Service
{
	private static List<DownloadItem> list=new ArrayList<>();
	private static DownloadService service=null;
	private static List<Download> download=new ArrayList<>();
	@Override
	public IBinder onBind(Intent p1)
	{
		return null;
	}
	private static void start(Context context){
		try{context.startService(new Intent(context,DownloadService.class));
		}catch(Exception e){
			Toast.makeText(context,"服务受限",Toast.LENGTH_SHORT).show();
		}
	}
	private static void stop(Context context){
		context.stopService(new Intent(context,DownloadService.class));
	}
	public static void addTask(DownloadItem di,Context context){
		DownloadDatabase dd=DownloadDatabase.getInstance(context);
		switch(di.getState()){
			case State.WAITING:
				list.remove(di);
				di.setState(State.UNKNOW);
				break;
			case State.LOADING:
				int index=download.indexOf(di);
				if(index!=-1)
				download.remove(index).close();
				di.setState(State.UNKNOW);
				break;
			default:
			list.add(di);
			di.setState(State.WAITING);
			if(service!=null)service.check();
			else
				start(context);
				break;
		}
		dd.updateState(di.getUrl(),di.getState());
	}
	public static void addTask(PlayItem pi,int index,Context context){
		DownloadItem di=new DownloadItem();
		di.setTitle(pi.getTitle()+"-"+pi.getArtist());
		di.setUrl(pi.getPlayList().get(index).getUrl());
		di.setDir(context.getSharedPreferences("setting",0).getString("path",Environment.getExternalStorageDirectory().getAbsolutePath()+"/MoeMusic")+"/"+di.getTitle()+".mp3");
		list.add(di);
		DownloadDatabase dd=DownloadDatabase.getInstance(context);
		dd.insert(di);
		dd.updateState(di.getUrl(),State.WAITING);
		di.setState(State.WAITING);
		if(service==null)
			start(context);
			else
		service.check();
		if(osl!=null)osl.onAdded(di);
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		service=this;
		check();
	}
	private void check(){
		if(list.size()==0)return;
		int size=download.size();
		for(;size<5;size++){
			try{
			Download d=new Download(list.remove(0),this);
			download.add(d);
			d.start();
			}catch(IndexOutOfBoundsException e){break;}
		}
		if(download.size()==0&&list.size()==0)
			stop(this);
	}
	@Override
	public void onDestroy()
	{
		service=null;
		super.onDestroy();
	}
	public static DownloadService getDownloadService(){
		return service;
	}
	public List<DownloadItem> getDownloadList(){
		return list;
	}
	public List<Download> getActivedList(){
		return download;
	}
	public void onItemEnd(Download down,boolean flag){
		if(osl!=null)osl.onRemoved(down.getDownloadItem(),flag);
		download.remove(down);
		check();
		if(flag){
			
		}
	}
	public abstract interface OnStateListener{
		void onAdded(DownloadItem di);
		void onRemoved(DownloadItem di,boolean flag);
	}
	public static void setOnStateListener(OnStateListener o){
		osl=o;
	}
	private static OnStateListener osl;
	public static class State{
		public final static int UNKNOW=0;
		public final static int WAITING=1;
		public final static int LOADING=2;
		public final static int SUCCESS=3;
		public final static int ERROR=4;
	}
}
