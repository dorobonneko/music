package com.moe.adapter;
import com.moe.entity.DownloadItem;
import java.util.List;
import android.view.View;
import android.widget.TextView;
import android.view.ViewGroup;
import com.moe.Music.R;
import android.support.v7.widget.RecyclerView;
import android.widget.ProgressBar;
import android.widget.ImageView;
import com.moe.services.DownloadService;
import android.view.LayoutInflater;
import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
public class DownloadAdapter extends Adapter
{
	private List<DownloadItem> selected;
	private List<DownloadItem> list;
	private DecimalFormat format=new DecimalFormat("0.00");
	public DownloadAdapter(List<DownloadItem> ldi,List<DownloadItem> selected){
		list=ldi;
		this.selected=selected;
	}
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup p1, int p2)
	{
		LayoutInflater inflater=LayoutInflater.from(p1.getContext());
		return p2==1?new ViewHolder2(inflater.inflate(R.layout.download_success_view,p1,false)):new ViewHolder(inflater.inflate(R.layout.download_item_view,p1,false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder p1, int p2)
	{
		DownloadItem di=list.get(p2);
		switch(p1.getItemViewType()){
			case 0:
				ViewHolder vh=(ViewHolder)p1;
				vh.title.setText(di.getTitle());
				switch(di.getState()){
					case DownloadService.State.WAITING:
					case DownloadService.State.LOADING:
						vh.state.setImageResource(R.drawable.ic_pause);
						break;
					default:
					vh.state.setImageResource(R.drawable.ic_play);
					break;
					}
				File file=new File(di.getDir());
				long length=file.length();
				try{
				vh.progress.setProgress((int)(((double)file.length())/di.getTotal()*vh.progress.getMax()));
				vh.size.setText(format.format(file.length()/1024.0/1024)+"M/"+format.format(di.getTotal()/1024.0/1024)+"M");
				}catch(Exception e){}
				break;
			case 1:
				((ViewHolder2)p1).title.setText(di.getTitle());
				((ViewHolder2)p1).size.setText(format.format(di.getTotal()/1024.0/1024)+"M");
				break;
		}
		if(selected.contains(di))
			p1.itemView.setBackgroundColor(p1.itemView.getResources().getColor(R.color.selected));
			else
			p1.itemView.setBackground(null);
	}

	@Override
	public int getItemCount()
	{
		return list.size();
	}

	@Override
	public int getItemViewType(int position)
	{
		return list.get(position).getState()==DownloadService.State.SUCCESS?1:0;
	}
	
	public class ViewHolder extends Adapter.ViewHolder implements View.OnLongClickListener{
		private TextView title,size;
		private ProgressBar progress;
		private ImageView state;
		public ViewHolder(View v){
			super(DownloadAdapter.this,v);
			size=(TextView)v.findViewById(R.id.download_item_view_size);
			title=(TextView)v.findViewById(R.id.download_item_view_title);
			progress=(ProgressBar)v.findViewById(R.id.download_item_view_progress);
			state=(ImageView)v.findViewById(R.id.download_item_view_state);
			progress.setMax(100);
			v.setOnLongClickListener(this);
		}

		@Override
		public boolean onLongClick(View p1)
		{
			if(oilcl!=null)return oilcl.onItemLongClick(DownloadAdapter.this,this);
			return false;
		}

		
	}
	public class ViewHolder2 extends Adapter.ViewHolder implements View.OnLongClickListener{
		private TextView title,size;
		public ViewHolder2(View v){
			super(DownloadAdapter.this,v);
			size=(TextView)v.findViewById(R.id.download_success_view_size);
			title=(TextView)v.findViewById(R.id.download_success_view_title);
			v.setOnLongClickListener(this);
		}
		@Override
		public boolean onLongClick(View p1)
		{
			if(oilcl!=null)return oilcl.onItemLongClick(DownloadAdapter.this,this);
			return false;
		}
	}
	public abstract interface OnItemLongClickListener{
		boolean onItemLongClick(DownloadAdapter adapter,Adapter.ViewHolder vh);
	}
	public void setOnItemLongClickListener(OnItemLongClickListener o){
		oilcl=o;
	}
	private OnItemLongClickListener oilcl;
}
