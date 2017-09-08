package com.moe.widget;
import android.view.*;
import android.content.*;
import android.graphics.*;
import android.support.v4.view.*;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import com.moe.Music.R;
import android.content.res.TypedArray;

public class TabCursor extends View implements ViewPager.OnPageChangeListener
{
	private ViewPager vp;
	private Observer server;
	private int radius=8,cellspace=12;
	private Bitmap background;
	private Paint paint=new Paint();
	private int id;
	private int bgColor,high;
	public TabCursor(Context context,AttributeSet attrs){
		super(context,attrs);
		TypedArray ta=context.obtainStyledAttributes(attrs,R.styleable.tabcursor);
		id=ta.getResourceId(R.styleable.tabcursor_viewpager,-1);
		ta.recycle();
		ta=context.obtainStyledAttributes(new int[]{android.R.attr.colorControlNormal,android.R.attr.colorControlHighlight});
		bgColor=ta.getColor(0,0xff000000);
		high=ta.getColor(1,context.getResources().getColor(R.color.accent));
		ta.recycle();
		setWillNotDraw(false);
		paint.setAntiAlias(true);
		paint.setDither(true);
		server=new Observer();
		}

	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		if(vp==null&&id!=-1){
			vp=(ViewPager)getRootView().findViewById(id);
			if(vp.getAdapter()!=null)vp.getAdapter().registerDataSetObserver(server);
			vp.setOnPageChangeListener(this);
		}
	}

	

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int widthmode=MeasureSpec.getMode(widthMeasureSpec);
		int heightmode=MeasureSpec.getMode(heightMeasureSpec);
		int width=MeasureSpec.getSize(widthMeasureSpec);
		int height=MeasureSpec.getSize(heightMeasureSpec);
		switch(widthmode){
			case MeasureSpec.UNSPECIFIED://无限制
				break;
			case MeasureSpec.EXACTLY://固定尺寸
				break;
			case MeasureSpec.AT_MOST://可取的最大尺寸
				if(vp==null)
					width=0;
				else{
					int count=vp.getAdapter().getCount();
					width=count*radius*2+(count-1)*cellspace+2;
				}
			break;
		}
		switch(heightmode){
			case MeasureSpec.UNSPECIFIED://无限制
				break;
			case MeasureSpec.EXACTLY://固定尺寸
				break;
			case MeasureSpec.AT_MOST://可取的最大尺寸
				if(vp==null)
					height=0;
				else{
					height=radius*2+2;
				}
				break;
		}
		setMeasuredDimension(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY));
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		if(background==null)drawBackground();
		canvas.save();
		super.onDraw(canvas);
		canvas.restore();
		if(background!=null){
		canvas.drawBitmap(background,0,0,null);
		paint.setStyle(Paint.Style.FILL);
			float x=vp.getCurrentItem()*radius*3+cellspace+1;
			canvas.drawCircle(x,getHeight()/2,radius-1,paint);
		}
	}
	private void drawBackground(){
		if(background!=null)background.recycle();
		int count=vp.getAdapter().getCount();
		background=Bitmap.createBitmap(getWidth(),getHeight(),Bitmap.Config.ARGB_4444);
		Canvas canvas=new Canvas(background);
		paint.setStyle(Paint.Style.STROKE);//空心
		paint.setColor(bgColor);
		paint.setStrokeWidth(2f);//画笔宽度
		for(int i=0;i<count;i++){
			float x=i*radius*3+cellspace+1;
			canvas.drawCircle(x,getHeight()/2,radius,paint);
		}
		paint.setColor(high);
	}
	public void setUpViewPager(ViewPager vp){
		this.vp=vp;
		vp.setOnPageChangeListener(this);
		vp.getAdapter().registerDataSetObserver(server);
		drawBackground();
	}

	@Override
	protected void onDetachedFromWindow()
	{
		try{
		vp.getAdapter().unregisterDataSetObserver(server);
		}catch(Exception e){}
		vp=null;
		server=null;
		super.onDetachedFromWindow();
	}

	@Override
	public void onPageScrolled(int p1, float p2, int p3)
	{
		invalidate();
	}

	@Override
	public void onPageSelected(int p1)
	{
		
	}

	@Override
	public void onPageScrollStateChanged(int p1)
	{
		
	}
	
	class Observer extends DataSetObserver
	{

		@Override
		public void onChanged()
		{
			drawBackground();
			invalidate();
		}

		@Override
		public void onInvalidated()
		{
			drawBackground();
			invalidate();
		}
		
	}
}
