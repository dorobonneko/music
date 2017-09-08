package com.moe.media;

public class MediaPlayer extends android.media.MediaPlayer implements android.media.MediaPlayer.OnCompletionListener,android.media.MediaPlayer.OnErrorListener
{
	public MediaPlayer(){
		super();
		setOnCompletionListener(this);
		setOnErrorListener(this);
	}
	@Override
	public void start() throws IllegalStateException
	{
		super.start();
		if(osl!=null)osl.onStart(this);
	}

	@Override
	public void pause() throws IllegalStateException
	{
		super.pause();
		if(osl!=null)osl.onPause(this);
	}

	@Override
	public void onCompletion(android.media.MediaPlayer p1)
	{
		if(osl!=null)osl.onStop(this);
	}

	@Override
	public boolean onError(android.media.MediaPlayer p1, int p2, int p3)
	{
		if(osl!=null)osl.onStop(this);
		return true;
	}

	
	public void setOnStateListener(OnStateListener o){
		osl=o;
	}
	private OnStateListener osl;
	public abstract interface OnStateListener{
		void onStart(MediaPlayer mp);
		void onPause(MediaPlayer mp);
		void onStop(MediaPlayer mp);
	}
}
