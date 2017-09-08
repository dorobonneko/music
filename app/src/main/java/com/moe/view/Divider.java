package com.moe.view;
import android.support.v7.widget.RecyclerView;
import android.graphics.Rect;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;

public class Divider extends RecyclerView.ItemDecoration
{
	private int height=3;
	private Paint paint;
	public Divider(){
		this(0xffbdbdbd,3);
	}
	public Divider(int color,int height){
	this.height=height;
	paint=new Paint();
	paint.setColor(color);
	}


	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
	{
		// TODO: Implement this method
		super.getItemOffsets(outRect, view, parent, state);
		outRect.bottom=height;
	}

	@Override
	public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state)
	{
		int childCount = parent.getChildCount()-1;
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        for (int i = 0; i < childCount; i++) {
            View view = parent.getChildAt(i);
            float top = view.getBottom();
            float bottom = view.getBottom() + height;
            c.drawRect(left, top, right, bottom, paint);
        }

			}

	
	
}
