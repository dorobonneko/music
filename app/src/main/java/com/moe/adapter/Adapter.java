package com.moe.adapter;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.content.res.TypedArray;

public abstract class Adapter<T extends Adapter.ViewHolder> extends RecyclerView.Adapter<T>
{
	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
		private Adapter adapter;
		public ViewHolder(Adapter a,View v){
			super(v);
			adapter=a;
			TypedArray ta=v.getContext().obtainStyledAttributes(new int[]{android.support.v7.appcompat.R.attr.selectableItemBackground});
			v.setForeground(ta.getDrawable(0));
			v.setClickable(true);
			ta.recycle();
			v.setOnClickListener(this);
		}

		@Override
		public void onClick(View p1)
		{
			if(adapter.getOnItemClickListener()!=null)adapter.getOnItemClickListener().onItemClick(adapter,ViewHolder.this);
		}

		
	}
	public void setOnItemClickListener(OnItemClickListener o){
		oicl=o;
	}
	private OnItemClickListener getOnItemClickListener(){
		return oicl;
	}
	private OnItemClickListener oicl;
	public abstract interface OnItemClickListener{
		void onItemClick(Adapter adapter,ViewHolder vh);
	}
}
