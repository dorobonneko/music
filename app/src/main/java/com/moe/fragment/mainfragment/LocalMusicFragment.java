package com.moe.fragment.mainfragment;
import com.moe.fragment.Fragment;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import com.moe.Music.R;
import android.support.v7.widget.RecyclerView;
import java.util.List;
import java.util.ArrayList;
import android.widget.TableLayout;
import android.support.v4.view.ViewPager;
import android.support.design.widget.TabLayout;
import com.moe.adapter.ViewPagerAdapter;
import com.moe.entity.MainIndexItem;
import com.moe.database.MusicInfo;
import com.moe.adapter.MusicAdapter;
import android.support.v7.widget.LinearLayoutManager;
import com.moe.adapter.Adapter;
import com.moe.adapter.Adapter.ViewHolder;
import com.moe.services.PlayerService;
import com.moe.entity.MusicItem;
import android.content.SharedPreferences;
import com.moe.media.MediaPlayer;
import com.moe.view.Divider;
public class LocalMusicFragment extends Fragment implements ViewPager.OnPageChangeListener,Adapter.OnItemClickListener,PlayerService.OnPlayerStateListener
{
private SharedPreferences player;
private ViewPager viewpager;
private MusicInfo music;
private List<List<? extends MainIndexItem>> data;
private List<RecyclerView> list;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		list=new ArrayList<>();
		data=new ArrayList<>();
		
		for(int i=0;i<4;i++){
			RecyclerView rv=new RecyclerView(getActivity());
			List<MainIndexItem> lm=new ArrayList<>();
			rv.setLayoutManager(new LinearLayoutManager(getActivity()));
			MusicAdapter adapter=new MusicAdapter(lm);
			rv.setAdapter(adapter);
			list.add(rv);
			data.add(lm);
			adapter.setOnItemClickListener(this);
			//rv.setNestedScrollingEnabled(false);
			rv.addItemDecoration(new Divider(getResources().getColor(R.color.primary_light),3));
			}
		list.get(0).setTag("歌曲");
		list.get(1).setTag("歌手");
		list.get(2).setTag("专辑");
		list.get(3).setTag("文件夹");
		return inflater.inflate(R.layout.local_music_view,container,false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		TabLayout tab=(TabLayout)view.findViewById(R.id.local_music_view_tablayout);
		viewpager=(ViewPager)view.findViewById(R.id.local_music_view_viewpager);
		tab.setupWithViewPager(viewpager,true);
		viewpager.setAdapter(new ViewPagerAdapter(list));
		viewpager.setOnPageChangeListener(this);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		player=getActivity().getSharedPreferences("music",0);
		music=MusicInfo.getInstance(getActivity());
		super.onActivityCreated(savedInstanceState);
		PlayerService.addOnPlayerStateListener(this);
	}
	
	@Override
	public boolean onBackPressed()
	{
		if(list.get(viewpager.getCurrentItem()).getTag(R.id.key)!=null){
			list.get(viewpager.getCurrentItem()).setTag(R.id.key,null);
			load(viewpager.getCurrentItem());
			list.get(viewpager.getCurrentItem()).getAdapter().notifyDataSetChanged();
			return true;
		}
		return false;
	}
	@Override
	public void onPageScrolled(int p1, float p2, int p3)
	{
		if(data.get(p1).size()==0){
			MusicItem mi=PlayerService.getPlayerService().getCurrentMusicItem();
			load(p1);
			if(mi!=null)
			((MusicAdapter)list.get(p1).getAdapter()).setItemSelected(mi.getId());
			list.get(p1).getAdapter().notifyDataSetChanged();
			if(p1==0)
				list.get(p1).scrollToPosition(data.get(p1).indexOf(PlayerService.getPlayerService().getCurrentMusicItem()));
		}
	}
	private void load(int p1){
		data.get(p1).clear();
		switch(p1){
			case 0:
				data.get(p1).addAll(music.query(MusicInfo.Type.Music,null,null));
				break;
			case 1:
				data.get(p1).addAll(music.queryFolder(MusicInfo.Type.Artist));
				break;
			case 2:
				data.get(p1).addAll(music.queryFolder(MusicInfo.Type.Album));
				break;
			case 3:
				data.get(p1).addAll(music.queryFolder(MusicInfo.Type.Folder));
				break;
		}
	}
	@Override
	public void onPageSelected(int p1)
	{
		// TODO: Implement this method
	}

	@Override
	public void onPageScrollStateChanged(int p1)
	{
		// TODO: Implement this method
	}

	@Override
	public void onItemClick(Adapter adapter, Adapter.ViewHolder vh)
	{
		int current=viewpager.getCurrentItem();
		switch(current){
			case 0:
				player.edit().putString("type",MusicInfo.Type.Music).putString("other",null).putString("data",null).commit();
				PlayerService.getPlayerService().start((MusicItem)data.get(current).get(vh.getAdapterPosition()));
				break;
			case 1://歌手
				if(vh.getItemViewType()==1){
					//文件夹模式
					list.get(current).setTag(R.id.key,data.get(current).get(vh.getAdapterPosition()).getTitle());
					data.get(current).clear();
					data.get(current).addAll(music.query(MusicInfo.Type.Music,MusicInfo.Other.getArtist,list.get(current).getTag(R.id.key).toString()));
					list.get(current).getAdapter().notifyDataSetChanged();
				}else{
					player.edit().putString("type",MusicInfo.Type.Music).putString("other",MusicInfo.Other.getArtist).putString("data",list.get(current).getTag(R.id.key).toString()).commit();
					PlayerService.getPlayerService().start((MusicItem)data.get(1).get(vh.getAdapterPosition()));
				}
				break;
			case 2://专辑
				if(vh.getItemViewType()==1){
					//文件夹模式
					list.get(current).setTag(R.id.key,data.get(current).get(vh.getAdapterPosition()).getTitle());
					data.get(current).clear();
					data.get(current).addAll(music.query(MusicInfo.Type.Music,MusicInfo.Other.getAlbum,list.get(current).getTag(R.id.key).toString()));
					list.get(current).getAdapter().notifyDataSetChanged();
					
				}else{
					player.edit().putString("type",MusicInfo.Type.Music).putString("other",MusicInfo.Other.getAlbum).putString("data",list.get(current).getTag(R.id.key).toString()).commit();
					PlayerService.getPlayerService().start((MusicItem)data.get(current).get(vh.getAdapterPosition()));
				}
				break;
			case 3://文件夹
				if(vh.getItemViewType()==1){
					//文件夹模式
					list.get(current).setTag(R.id.key,data.get(current).get(vh.getAdapterPosition()).getTitle());
					data.get(current).clear();
					data.get(current).addAll(music.query(MusicInfo.Type.Music,MusicInfo.Other.getFolder,list.get(current).getTag(R.id.key).toString()));
					list.get(current).getAdapter().notifyDataSetChanged();
					
				}else{
					player.edit().putString("type",MusicInfo.Type.Music).putString("other",MusicInfo.Other.getFolder).putString("data",list.get(current).getTag(R.id.key).toString()).commit();
					PlayerService.getPlayerService().start((MusicItem)data.get(current).get(vh.getAdapterPosition()));
				}
				break;
		}
	}
	@Override
	public void onPrepare(MediaPlayer mp, MusicItem mi)
	{
		for(RecyclerView rv:list){
			MusicAdapter ma=(MusicAdapter)rv.getAdapter();
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
