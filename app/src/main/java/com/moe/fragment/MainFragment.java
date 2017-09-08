package com.moe.fragment;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import com.moe.Music.R;
import android.support.design.widget.NavigationView;
import android.support.v7.graphics.Palette;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import com.moe.Music.MainActivity;
import android.support.v7.app.ActionBar;
import android.graphics.drawable.BitmapDrawable;
import android.content.res.Configuration;
import com.moe.Music.SearchActivity;
import android.content.Intent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import com.moe.fragment.mainfragment.LocalMusicFragment;
import android.view.ViewTreeObserver;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import com.moe.services.PlayerService;
import com.moe.media.MediaPlayer;
import com.moe.entity.MusicItem;
import com.moe.database.MusicInfo;
import android.widget.TextView;
import android.widget.ImageView;
import com.moe.fragment.mainfragment.MusicListFragment;

public class MainFragment extends Fragment implements Palette.PaletteAsyncListener,View.OnClickListener,
NavigationView.OnNavigationItemSelectedListener,
AppBarLayout.OnOffsetChangedListener,
View.OnTouchListener,
PlayerService.OnStateListener,
PlayerService.OnPlayerStateListener
{
	private View bottom;
	private DrawerLayout drawerlayout;
	private ActionBarDrawerToggle abdt;
	private CollapsingToolbarLayout ctl;
	private Toolbar toolbar;
	private AppBarLayout appbar;
	private Fragment current;
	private MusicInfo music;
	private TextView title,artist;
	private ImageView play,next;

	public void refresh()
	{
		if(current instanceof MusicListFragment)
			((MusicListFragment)current).refresh();
	}
	public boolean onBackPressed()
	{
		if(current!=null){
			if(current.onBackPressed())return true;
			getChildFragmentManager().beginTransaction().hide(current).detach(current).remove(current).commit();
			current=null;
			appbar.setEnabled(true);
			ctl.setTitle("Music");
			appbar.setExpanded(true,true);
			return true;
		}
		return false;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.main_content,container,false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		title=(TextView)view.findViewById(R.id.main_content_title);
		artist=(TextView)view.findViewById(R.id.main_content_artist);
		play=(ImageView)view.findViewById(R.id.main_content_play);
		next=(ImageView)view.findViewById(R.id.main_content_next);
		play.setOnClickListener(this);
		next.setOnClickListener(this);
		bottom=view.findViewById(R.id.main_content_bottom);
		bottom.setOnTouchListener(this);
		appbar=(AppBarLayout)view.findViewById(R.id.main_content_appbarlayout);
		appbar.addOnOffsetChangedListener(this);
		view.findViewById(R.id.main_random).setOnClickListener(this);
		toolbar=(Toolbar)view.findViewById(R.id.main_toolbar);
		drawerlayout=(DrawerLayout)getActivity().findViewById(R.id.main_drawerlayout);
		((NavigationView)view.findViewById(R.id.main_contentselectedView)).setNavigationItemSelectedListener(this);
		((MainActivity)getActivity()).setSupportActionBar(toolbar);
		ctl=(CollapsingToolbarLayout)view.findViewById(R.id.main_collapsing);
		ctl.setTitle("MUSIC");
		//getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
		ActionBar ab=((MainActivity)getActivity()).getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		//ab.setDisplayShowTitleEnabled(false);
		//ab.setDisplayUseLogoEnabled(true);
		ab.setHomeButtonEnabled(false);
		ab.setHomeAsUpIndicator(R.drawable.ic_menu);
		//ab.setIcon(R.drawable.ic_menu);
		abdt=new ActionBarDrawerToggle(getActivity(),drawerlayout,toolbar,0,0);
		//abdt.setHomeAsUpIndicator(R.drawable.ic_menu);
		drawerlayout.addDrawerListener(abdt);
		Palette.generateAsync(((BitmapDrawable)getResources().getDrawable(R.drawable.head_bg)).getBitmap(),this);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		music=MusicInfo.getInstance(getActivity());
		super.onActivityCreated(savedInstanceState);
		abdt.syncState();
		if(savedInstanceState!=null){
			current=(Fragment)getChildFragmentManager().findFragmentByTag("current");
			ctl.setTitle(savedInstanceState.getString("title"));
			}
		PlayerService.addOnStateListener(this);
		PlayerService.start(getActivity());
	}
	
	@Override
	public void onClick(View p1)
	{
		switch(p1.getId()){
			case R.id.main_random:
				getActivity().getSharedPreferences("music",0).edit().putInt("mode",PlayerService.Mode.shuffle).putString("type",MusicInfo.Type.Music).putString("other",null).putString("data",null).commit();
				getActivity().sendBroadcast(new Intent(PlayerService.Control.SHUFFLE));
				break;
			case R.id.main_content_play:
				getActivity().sendBroadcast(new Intent(PlayerService.Control.PLAY));
				break;
			case R.id.main_content_next:
				getActivity().sendBroadcast(new Intent(PlayerService.Control.NEXT));
				break;
		}
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem p1)
	{
		appbar.setExpanded(false,true);
		appbar.setTag(p1);
		ctl.setTitle(p1.getTitle());
		return true;
	}

	@Override
	public void onGenerated(Palette p1)
	{
		Palette.Swatch ps=p1.getLightVibrantSwatch();
		ctl.setContentScrimColor(ps.getRgb());
		toolbar.setTitleTextColor(ps.getTitleTextColor());
		bottom.setBackgroundColor(ps.getRgb());
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		// TODO: Implement this method
		super.onConfigurationChanged(newConfig);
		abdt.onConfigurationChanged(newConfig);
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return abdt.onOptionsItemSelected(item);
		//return super.onOptionsItemSelected(item);
	}
	@Override
	public void onOffsetChanged(AppBarLayout p1, int p2)
	{
		if(p1.getTag()!=null&&-p2>=p1.getTotalScrollRange()){
			//关闭
			MenuItem mi=(MenuItem)p1.getTag();
			switch(mi.getItemId()){
				case R.id.music_local:
					current=new LocalMusicFragment();
					break;
				case R.id.music_favourite:
					current=new MusicListFragment();
					((MusicListFragment)current).setType(MusicInfo.Type.Favourite);
					break;
				case R.id.music_sheet:
					current=new MusicListFragment();
					((MusicListFragment)current).setType(MusicInfo.Type.Favourite);
					break;
				case R.id.music_playhistory:
					current=new MusicListFragment();
					((MusicListFragment)current).setType(MusicInfo.Type.PlayHistory);
					break;
				case R.id.music_addhistory:
					current=new MusicListFragment();
					((MusicListFragment)current).setType(MusicInfo.Type.AddHistory);
					break;
			}
			getChildFragmentManager().beginTransaction().add(R.id.main_content_float,current,"current").commit();
			appbar.setEnabled(false);
			p1.setTag(null);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		outState.putString("title",ctl.getTitle().toString());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroy()
	{
		PlayerService.removeOnStateListener(this);
		PlayerService.removeOnPlayerStateListener(this);
		
		super.onDestroy();
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
						if(Math.abs(oldx-p2.getRawX())<10&&p2.getRawY()<oldy)
							move=0;
							else
							move=1;
							onTouch(p1,p2);
						break;
					case 0:
						if(oldy-p2.getRawY()>40){
							((MainActivity)getActivity()).open();
							move=1;
						}
						break;
				}
				break;
			case p2.ACTION_UP:
				if(oldx==p2.getRawX()&&oldy==p2.getRawY())
					((MainActivity)getActivity()).open();
				
				break;
		}
		return true;
	}

	@Override
	public void onStart(PlayerService service)
	{
		PlayerService.addOnPlayerStateListener(this);
		/*Object mi=service.getCurrentMusicItem();
		MusicItem item=null;
		if(mi instanceof MusicItem){
			item=(MusicItem)mi;
		}else{
			item=music.getMusicItem((Integer)mi);
		}
		if(item!=null){
			title.setText(item.getTitle());
			artist.setText(item.getArtist());
			if(service.getMediaPlayer().isPlaying())
				play.setImageResource(R.drawable.ic_pause);
				else
				play.setImageResource(R.drawable.ic_play);
		}*/
		if(service.getMediaPlayer().isPlaying())
			play.setImageResource(R.drawable.ic_pause);
		else
			play.setImageResource(R.drawable.ic_play);
		MusicItem mi=service.getCurrentMusicItem();
		if(mi!=null)
			onPrepare(null,mi);
	}

	@Override
	public void onStop(PlayerService service)
	{
		getActivity().finish();
	}

	@Override
	public void onStart(MediaPlayer mp)
	{
		play.setImageResource(R.drawable.ic_pause);
	}

	@Override
	public void onPause(MediaPlayer mp)
	{
		play.setImageResource(R.drawable.ic_play);
	}

	@Override
	public void OnStop(MediaPlayer mp)
	{
		play.setImageResource(R.drawable.ic_play);
	}

	@Override
	public void onPrepare(MediaPlayer mp,MusicItem item)
	{
		title.setText(item.getTitle());
		artist.setText(item.getArtist());
	}




	

	
}
