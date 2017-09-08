package com.moe.adapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import com.moe.Music.R;
import android.content.res.TypedArray;
public class SiteListAdapter extends Adapter<SiteListAdapter.ViewHolder>
{
	private int[] data=new int[]{R.drawable.wangyi,R.drawable.kugou,R.drawable.qq,R.drawable.wusing,R.drawable.baidu,R.drawable.kuwo,R.drawable.xiami,R.drawable.ting1};
	@Override
	public SiteListAdapter.ViewHolder onCreateViewHolder(ViewGroup p1, int p2)
	{
		return new ViewHolder(new ImageButton(p1.getContext()));
	}

	@Override
	public void onBindViewHolder(SiteListAdapter.ViewHolder p1, int p2)
	{
		p1.img.setImageResource(data[p2]);
		p1.itemView.setId(data[p2]);
	}

	@Override
	public int getItemCount()
	{
		return data.length;
	}
	
	public class ViewHolder extends Adapter.ViewHolder{
		ImageButton img;
		public ViewHolder(View v){
			super(SiteListAdapter.this,v);
			img=(ImageButton)v;
			img.setBackground(null);
			TypedArray ta=v.getContext().obtainStyledAttributes(new int[]{android.support.v7.appcompat.R.attr.selectableItemBackgroundBorderless});
			v.setForeground(ta.getDrawable(0));
			ta.recycle();
			img.setScaleType(ImageButton.ScaleType.CENTER_INSIDE);
		}
	}
}
