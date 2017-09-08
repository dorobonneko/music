package com.moe.fragment.mainfragment;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import com.moe.Music.R;
import com.moe.fragment.Fragment;
import com.moe.entity.MainIndexItem;
import java.util.ArrayList;
import java.util.List;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import com.moe.adapter.MusicAdapter;
import com.moe.database.MusicInfo;
import com.moe.adapter.Adapter;
import com.moe.adapter.Adapter.ViewHolder;
import com.moe.services.PlayerService;
import com.moe.entity.MusicItem;
import android.content.SharedPreferences;
import com.moe.media.MediaPlayer;
import com.moe.view.Divider;
public class MusicListFragment extends Fragment implements Adapter.OnItemClickListener,PlayerService.OnPlayerStateListener
{
	private String type;
	private List<? extends MainIndexItem> list;
	private MusicAdapter ma;
	private MusicInfo info;
	private SharedPreferences player;
	public void refresh()
	{
		if(getView().getTag(R.id.key)==null){
		list.clear();
		list.addAll(info.query(type,null,null));
		ma.notifyDataSetChanged();
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		list=new ArrayList<>();
		return inflater.inflate(R.layout.recyclerview,container,false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		RecyclerView rv=(RecyclerView)view;
		rv.setLayoutManager(new LinearLayoutManager(getActivity()));
		rv.setAdapter(ma=new MusicAdapter(list));
		rv.addItemDecoration(new Divider(getResources().getColor(R.color.primary_light),3));
		ma.setOnItemClickListener(this);
		super.onViewCreated(view, savedInstanceState);
	}
	
	public void setType(String type){
		this.type=type;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		if(savedInstanceState!=null)
			type=savedInstanceState.getString("type");
			info=MusicInfo.getInstance(getActivity());
		player=getActivity().getSharedPreferences("music",0);
		super.onActivityCreated(savedInstanceState);
		PlayerService.addOnPlayerStateListener(this);
		onPrepare(null,PlayerService.getPlayerService().getCurrentMusicItem());
		refresh();
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		outState.putString("type",type);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public boolean onBackPressed()
	{
		if(getView().getTag(R.id.key)!=null){
			getView().setTag(R.id.key,null);
			refresh();
			return true;
		}
		return false;
	}

	@Override
	public void onItemClick(Adapter adapter, Adapter.ViewHolder vh)
	{
		switch(vh.getItemViewType()){
			case 0:
				if(getView().getTag(R.id.key)!=null)
				player.edit().putString("type",type).putString("other",MusicInfo.Other.getFolder).putString("data",getView().getTag(R.id.key).toString()).commit();
				else
				player.edit().putString("type",type).putString("other",null).putString("data",null).commit();
				PlayerService.getPlayerService().start((MusicItem)list.get(vh.getAdapterPosition()));
				break;
			case 1:
				getView().setTag(R.id.key,list.get(vh.getAdapterPosition()).getTitle());
				list.clear();
				list.addAll(info.query(MusicInfo.Type.Music,MusicInfo.Other.getFolder,getView().getTag(R.id.key).toString()));
				ma.notifyDataSetChanged();
				break;
		}
	}

	@Override
	public void onPrepare(MediaPlayer mp, MusicItem mi)
	{
		if(mi!=null){
		ma.setItemSelected(mi.getId());
			ma.notifyDataSetChanged();
	}
	}

	@Override
	public void onStart(MediaPlayer mp)
	{
	}

	@Override
	public void onPause(MediaPlayer mp)
	{
		// TODO: Implement this method
	}

	@Override
	public void OnStop(MediaPlayer mp)
	{
		// TODO: Implement this method
	}

	@Override
	public void onDestroy()
	{
		PlayerService.removeOnPlayerStateListener(this);
		super.onDestroy();
	}
}
