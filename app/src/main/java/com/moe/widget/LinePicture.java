package com.moe.widget;
import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import java.util.Random;
import android.graphics.Paint;
import android.content.res.TypedArray;
import com.moe.Music.R;

public class LinePicture extends View
{
	private boolean start;
	private Thread thread;
	private int width=20;
	private Random random=new Random();
	private Paint paint=new Paint();
	public LinePicture(Context context){
		super(context);
		setWillNotDraw(false);
		TypedArray ta=context.obtainStyledAttributes(new int[]{android.R.attr.colorControlHighlight});
		paint.setColor(ta.getColor(1,context.getResources().getColor(R.color.accent)));
		ta.recycle();
	}
	public void start(){
		start=true;
	}
	public void stop(){
		start=false;
		invalidate();
	}

	@Override
	protected void onAttachedToWindow()
	{
		// TODO: Implement this method
		super.onAttachedToWindow();
		thread=new Thread(){
			public void run(){
				while(!isInterrupted()){
					if(start)
						postInvalidate();
					try
					{
						sleep(160);
					}
					catch (InterruptedException e)
					{}
				}
			}
		};
		thread.start();
	}

	@Override
	protected void onDetachedFromWindow()
	{
		// TODO: Implement this method
		super.onDetachedFromWindow();
		thread.interrupt();
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		if(start)
		for(int i=0;i<getWidth();i+=width*2){
			
			canvas.drawRect(i+width,random.nextInt(getHeight()/2)+getHeight()/2,i+width*2,getHeight(),paint);
			
		}
	}
	
}
