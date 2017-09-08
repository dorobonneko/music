package com.moe.fragment;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import com.moe.Music.R;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import com.moe.Music.MainActivity;
import android.view.MotionEvent;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import com.moe.services.PlayerService;
import com.moe.media.MediaPlayer;
import com.moe.entity.MusicItem;
import android.os.Handler;
import android.os.Message;
import android.widget.SeekBar;
import android.content.SharedPreferences;
import com.moe.database.MusicInfo;
import android.support.v4.view.ViewPager;
import java.util.ArrayList;
import java.util.List;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import com.moe.adapter.MusicAdapter;
import com.moe.entity.MainIndexItem;
import com.moe.view.Divider;
import com.moe.adapter.ViewPagerAdapter;
import com.moe.adapter.Adapter;
import com.moe.adapter.Adapter.ViewHolder;
import com.moe.widget.LinePicture;
public class PlayerFragment extends Fragment implements View.OnTouchListener,View.OnClickListener,PlayerService.OnPlayerStateListener,SeekBar.OnSeekBarChangeListener,Adapter.OnItemClickListener
{
	private LinePicture lp;
	private TextView title,artist,currentTime,totalTime;
	private ImageView toggle,pre,play,next,fav;
	private SeekBar progress;
	private SharedPreferences music;
	private MusicInfo info;
	private MusicAdapter ma;
	private List<MainIndexItem> music_data;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.player_view,container,false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		ViewPager vp=(ViewPager)view.findViewById(R.id.player_view_viewpager);
		List<View> list=new ArrayList<>();
		RecyclerView rv=new RecyclerView(getActivity());
		rv.setLayoutManager(new LinearLayoutManager(getActivity()));
		rv.addItemDecoration(new Divider(getResources().getColor(R.color.divider),3));
		rv.setAdapter(ma=new MusicAdapter(music_data=new ArrayList<>()));
		list.add(lp=new LinePicture(getActivity()));
		list.add(rv);
		vp.setAdapter(new ViewPagerAdapter(list));
		ma.setOnItemClickListener(this);
		progress=(SeekBar)view.findViewById(R.id.player_view_progress);
		title=(TextView)view.findViewById(R.id.player_view_title);
		artist=(TextView)view.findViewById(R.id.player_view_artist);
		currentTime=(TextView)view.findViewById(R.id.player_view_currenttime);
		totalTime=(TextView)view.findViewById(R.id.player_view_totaltime);
		toggle=(ImageView)view.findViewById(R.id.player_view_toggle);
		pre=(ImageView)view.findViewById(R.id.player_view_pre);
		play=(ImageView)view.findViewById(R.id.player_view_play);
		next=(ImageView)view.findViewById(R.id.player_view_next);
		fav=(ImageView)view.findViewById(R.id.player_view_favourite);
		toggle.setOnClickListener(this);
		pre.setOnClickListener(this);
		play.setOnClickListener(this);
		next.setOnClickListener(this);
		fav.setOnClickListener(this);
		progress.setOnSeekBarChangeListener(this);
		view.setPadding(0,getResources().getDimensionPixelSize(getResources().getIdentifier("status_bar_height", "dimen", "android")),0,0);
		view.setOnTouchListener(this);
		view.findViewById(R.id.player_view_exit).setOnClickListener(this);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		info=MusicInfo.getInstance(getActivity());
		music=getActivity().getSharedPreferences("music",0);
		super.onActivityCreated(savedInstanceState);
		PlayerService.addOnPlayerStateListener(this);
		MusicItem mi=PlayerService.getPlayerService().getCurrentMusicItem();
		if(mi!=null)onPrepare(null,mi);
		if(PlayerService.getPlayerService().getMediaPlayer().isPlaying())
			onStart(PlayerService.getPlayerService().getMediaPlayer());
		totalTime.setText(format(PlayerService.getPlayerService().getMediaPlayer().getDuration()));
		progress.setMax(PlayerService.getPlayerService().getMediaPlayer().getDuration());
		progress.setProgress((int)PlayerService.getPlayerService().getCurrentPosition());
		onHiddenChanged(false);
	}
	
	@Override
	public Animator onCreateAnimator(int transit, boolean enter, int nextAnim)
	{
		ObjectAnimator anime=null; 
		if(enter)
		anime=ObjectAnimator.ofFloat(getView(),"Y",new float[]{getActivity().getWindowManager().getDefaultDisplay().getHeight(),0});
		else
		anime=ObjectAnimator.ofFloat(getView(),"Y",new float[]{0,getActivity().getWindowManager().getDefaultDisplay().getHeight()});
		anime.setDuration(350);
		return anime;
	}
	private float oldx,oldy;
	private byte move=-1;
	@Override
	public boolean onTouch(View p1, MotionEvent p2)
	{
		switch(p2.getAction()){
			case p2.ACTION_DOWN:
				oldx=p2.getRawX();
				oldy=p2.getRawY();
				move=-1;
				break;
			case p2.ACTION_MOVE:
				switch(move){
					case -1:
						if(Math.abs(oldx-p2.getRawX())<15&&p2.getRawY()>oldy)
							move=0;
						else
							move=1;
						onTouch(p1,p2);
						break;
					case 0:
						if(p2.getRawY()-oldy>60){
							getActivity().onBackPressed();
							move=1;
						}
						break;
				}
				break;
		}
		return true;
	}

	@Override
	public void onClick(View p1)
	{
		switch(p1.getId()){
			case R.id.player_view_exit:
			getActivity().onBackPressed();
			break;
			case R.id.player_view_pre:
				getActivity().sendBroadcast(new Intent(PlayerService.Control.PRE));
				break;
			case R.id.player_view_play:
				getActivity().sendBroadcast(new Intent(PlayerService.Control.PLAY));
				break;
			case R.id.player_view_next:
				getActivity().sendBroadcast(new Intent(PlayerService.Control.NEXT));
				break;
			case R.id.player_view_toggle:
				int mode=music.getInt("mode",PlayerService.Mode.repeat)+1;
				if(mode>PlayerService.Mode.shuffle)mode=PlayerService.Mode.repeat;
				music.edit().putInt("mode",mode).commit();
				onHiddenChanged(false);
				break;
			case R.id.player_view_favourite:
				MusicItem mi=PlayerService.getPlayerService().getCurrentMusicItem();
				if(mi!=null){
				info.setFavourite(mi.getId(),!info.isFavourite(mi.getId()));
				onHiddenChanged(false);
				}
				break;
		}
	}

	@Override
	public void onStart(MediaPlayer mp)
	{
		play.setImageResource(R.drawable.ic_pause);
		progress.setMax(mp.getDuration());
		totalTime.setText(format(mp.getDuration()));
		handler.sendEmptyMessage(0);
		if(!isHidden())
		lp.start();
	}

	@Override
	public void onPause(MediaPlayer mp)
	{
		play.setImageResource(R.drawable.ic_play);
		handler.removeMessages(0);
		lp.stop();
	}

	@Override
	public void OnStop(MediaPlayer mp)
	{
		play.setImageResource(R.drawable.ic_play);
		handler.removeMessages(0);
		lp.stop();
	}

	@Override
	public void onPrepare(MediaPlayer mp,MusicItem item)
	{
		title.setText(item.getTitle());
		artist.setText(item.getArtist());
		ma.setItemSelected(item.getId());
		ma.notifyDataSetChanged();
	}

	@Override
	public void onDestroy()
	{
		PlayerService.removeOnPlayerStateListener(this);
		super.onDestroy();
	}
	private String format(long time){
		if(time<=0)return "00:00";
		time=time/1000;
		long s=time%60;
		long m=time/60;
		return (m<10?"0"+m:""+m)+":"+(s<10?"0"+s:""+s);
	}

	private Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what){
				case 0:
					//currentTime.setText(format(PlayerService.getPlayerService().getCurrentPosition()));
					progress.setProgress((int)PlayerService.getPlayerService().getCurrentPosition());
					if(PlayerService.getPlayerService().getMediaPlayer().isPlaying())
					sendEmptyMessageDelayed(0,1000);
					break;
			}
		}
		
	};

	@Override
	public void onStartTrackingTouch(SeekBar p1)
	{
		handler.removeMessages(0);
	}

	@Override
	public void onProgressChanged(SeekBar p1, int p2, boolean p3)
	{
		currentTime.setText(format(p2));
	}

	@Override
	public void onStopTrackingTouch(SeekBar p1)
	{
		MediaPlayer mp=PlayerService.getPlayerService().getMediaPlayer();
		PlayerService.getPlayerService().seekTo(p1.getProgress());
		if(mp.isPlaying())
			handler.sendEmptyMessage(0);
	}

	@Override
	public void onHiddenChanged(boolean hidden)
	{
		if(!hidden){
			music_data.clear();
			music_data.addAll(PlayerService.getPlayerService().getPlayList());
			MusicItem mi=PlayerService.getPlayerService().getCurrentMusicItem();
			if(mi!=null)ma.setItemSelected(mi.getId());
			ma.notifyDataSetChanged();
			switch(music.getInt("mode",PlayerService.Mode.repeat)){
				case PlayerService.Mode.repeat:
					toggle.setImageResource(R.drawable.ic_repeat);
					break;
				case PlayerService.Mode.repeatOnce:
					toggle.setImageResource(R.drawable.ic_repeat_once);
					break;
				case PlayerService.Mode.shuffle:
					toggle.setImageResource(R.drawable.ic_shuffle);
					break;
			}
			if(mi!=null&&info.isFavourite(mi.getId()))
				fav.setImageResource(R.drawable.ic_heart);
				else
				fav.setImageResource(R.drawable.ic_heart_outline);
				MediaPlayer mp=PlayerService.getPlayerService().getMediaPlayer();
				if(mp!=null&&mp.isPlaying()){
					lp.start();
				handler.sendEmptyMessage(0);
				}
		}else{
			handler.removeMessages(0);
			lp.stop();
		}
	}

	@Override
	public boolean onBackPressed()
	{
		// TODO: Implement this method
		return false;
	}

	@Override
	public void onItemClick(Adapter adapter, Adapter.ViewHolder vh)
	{
		PlayerService.getPlayerService().start((MusicItem)music_data.get(vh.getAdapterPosition()));
	}





	
	
}
