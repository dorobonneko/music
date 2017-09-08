package com.moe.app;
import android.app.Application;
import android.content.Intent;
import android.content.Context;
import com.moe.Music.ExceptionActivity;

public class Application extends Application implements Thread.UncaughtExceptionHandler
{
	private Context context;
	@Override
	public void uncaughtException(final Thread p1, Throwable p2)
	{
		
				if (p1.getName().equals("main"))
				{
					StringBuffer sb=new StringBuffer();
					sb.append(p2.getMessage());
					for (StackTraceElement ste:p2.getStackTrace())
						sb.append("\n").append(ste.toString());
					startActivity(new Intent(context, ExceptionActivity.class).putExtra(Intent.EXTRA_TEXT, sb.toString()));
					android.os.Process.killProcess(android.os.Process.myPid());
				}
		
	}


	@Override
	public void onCreate()
	{
		super.onCreate();
		context=this;
		Thread.currentThread().setDefaultUncaughtExceptionHandler(this);
	}

}
