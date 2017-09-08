package com.moe.adapter;
import android.view.ViewGroup;
import android.view.View;
import com.moe.entity.MainIndexItem;
import java.util.List;
import com.moe.entity.MusicItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;
import com.moe.Music.R;
import android.content.res.TypedArray;
import android.support.v7.app.AlertDialog;
import com.moe.entity.PlayItem;
import com.moe.entity.Song;
import android.content.DialogInterface;
import com.moe.services.DownloadService;
public class MusicAdapter extends Adapter<MusicAdapter.ViewHolder>
{
	private int selected=-1;
	private List<? extends MainIndexItem> list;
	public MusicAdapter(List<? extends MainIndexItem> list){
		this.list=list;
	}

	public void setItemSelected(int indexOf)
	{
		selected=indexOf;
	}
	@Override
	public MusicAdapter.ViewHolder onCreateViewHolder(ViewGroup p1, int p2)
	{
		View v=LayoutInflater.from(p1.getContext()).inflate(R.layout.music_item_view,p1,false);
		return p2==1?new ViewHolder2(v):new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(MusicAdapter.ViewHolder p1, int p2)
	{
		switch(p1.getItemViewType()){
			case 2:
			case 0:
				MusicItem mi=(MusicItem)list.get(p2);
				p1.title.setText(mi.getTitle());
				p1.summary.setText(mi.getArtist());
				if(selected==mi.getId())
					p1.selected.setBackgroundColor(p1.selected.getResources().getColor(R.color.primary));
					else
					p1.selected.setBackgroundColor(0x00000000);
				break;
			case 1:
				p1.title.setText(list.get(p2).getTitle());
				p1.summary.setText("共"+list.get(p2).getSize()+"首歌曲");
				break;
		}
	}

	@Override
	public int getItemCount()
	{
		return list.size();
	}

	@Override
	public int getItemViewType(int position)
	{
		return list.get(position) instanceof PlayItem?2:list.get(position) instanceof MusicItem?0:1;
	}
	
	public class ViewHolder extends Adapter.ViewHolder{
		ImageView icon,more;
		TextView title,summary;
		View selected;
		public ViewHolder(View v){
			super(MusicAdapter.this,v);
			//TypedArray ta=v.getContext().obtainStyledAttributes(new int[android.R.attr.listPreferredItemHeightSmall]);
			//v.getLayoutParams().height=
			//ta.recycle();
			icon=(ImageView)v.findViewById(R.id.music_item_view_icon);
			more=(ImageView)v.findViewById(R.id.music_item_view_more);
			title=(TextView)v.findViewById(R.id.music_item_view_title);
			summary=(TextView)v.findViewById(R.id.music_item_view_summary);
			selected=v.findViewById(R.id.music_item_view_selected);
			more.setOnClickListener(this);
		}

		@Override
		public void onClick(final View view)
		{
			if(view.getId()==R.id.music_item_view_more){
				if(getItemViewType()==2){
				AlertDialog.Builder ab=new AlertDialog.Builder(view.getContext());
				final PlayItem pi=(PlayItem)list.get(getAdapterPosition());
				List<Song> song=pi.getPlayList();
				String[] items=new String[song.size()];
				for(int i=0;i<song.size();i++)
				items[i]=(song.get(i).getBr()/1000)+"k";
					ab.setItems(items, new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								DownloadService.addTask(pi,p2,view.getContext());
							}
						});
				ab.show();
				}else
				new AlertDialog.Builder(view.getContext()).setMessage(((MusicItem)list.get(getAdapterPosition())).getUrl()).show();
			}else
			super.onClick(view);
		}
		
	}
	public class ViewHolder2 extends ViewHolder{
		public ViewHolder2(View v){
			super(v);
			icon.setVisibility(icon.GONE);
			more.setVisibility(icon.GONE);
		}
	}
}
