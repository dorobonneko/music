package com.moe.adapter;
import android.view.View;
import java.util.List;
import java.io.File;
import android.view.ViewGroup;
import com.moe.Music.R;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.content.res.TypedArray;
public class FolderAdapter extends Adapter<FolderAdapter.ViewHolder>
{

	
	private List<File> list;
	public FolderAdapter(List<File> list){
		this.list=list;
	}
	@Override
	public FolderAdapter.ViewHolder onCreateViewHolder(ViewGroup p1, int p2)
	{
		return new ViewHolder(LayoutInflater.from(p1.getContext()).inflate(R.layout.folder_item_view,p1,false));
	}

	@Override
	public void onBindViewHolder(FolderAdapter.ViewHolder p1, int p2)
	{
		p1.name.setText(list.get(p2).getName());
	}

	@Override
	public int getItemCount()
	{
		return list.size();
	}
	public class ViewHolder extends Adapter.ViewHolder{
		TextView name;
		public ViewHolder(View v){
			super(FolderAdapter.this,v);
			TypedArray ta=v.getContext().obtainStyledAttributes(new int[]{android.R.attr.listPreferredItemHeightSmall});
			v.getLayoutParams().height=ta.getDimensionPixelSize(0,100);
			ta.recycle();
			name=(TextView)v.findViewById(R.id.folder_item_view_name);
		}
	}
}
