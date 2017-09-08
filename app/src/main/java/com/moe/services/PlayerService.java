package com.moe.services;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.content.Context;
import java.util.HashSet;
import android.app.NotificationManager;
import android.app.Notification;
import android.widget.RemoteViews;
import com.moe.Music.R;
import java.io.IOException;
import com.moe.media.MediaPlayer;
import android.media.AudioManager;
import java.util.ArrayList;
import com.moe.entity.MusicItem;
import android.widget.Toast;
import com.moe.entity.MainIndexItem;
import java.util.List;
import android.content.SharedPreferences;
import com.moe.database.MusicInfo;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import java.util.Random;
import com.moe.entity.PlayItem;
public class PlayerService extends Service implements android.media.MediaPlayer.OnPreparedListener,MediaPlayer.OnStateListener,AudioManager.OnAudioFocusChangeListener
{
	private static PlayerService service;
	private MediaPlayer mp;
	private Notification.Builder nb;
	private RemoteViews remote;
	private AudioManager am;
	private MusicItem mi;
	private List<? extends MainIndexItem> player_list;
	private SharedPreferences music;
	private MusicInfo music_info;
	private ControlReceiver broadcast;
	private boolean autoPlay;
	private int sleep=-1;

	public List<? extends MainIndexItem> getPlayList()
	{
		return player_list;
	}

	public void play(MainIndexItem get, List<? extends MainIndexItem> list)
	{
		player_list.clear();
		player_list.addAll(list);
		start((MusicItem)get);
	}
	public int getSleep()
	{
		return sleep;
	}
	public void setSleep(int sleep)
	{
		this.sleep = sleep;
		handler.removeMessages(0);
		if (sleep != -1)
			handler.sendEmptyMessageDelayed(0, sleep);
	}
	public static PlayerService getPlayerService()
	{
		return service;
	}
	public MediaPlayer getMediaPlayer()
	{
		return mp;
	}
	@Override
	public IBinder onBind(Intent p1)
	{
		return null;
	}
	public static void start(Context context)
	{
		try
		{context.startService(new Intent(context, PlayerService.class));
		}
		catch (Exception e)
		{
			Toast.makeText(context, "服务受限", Toast.LENGTH_SHORT).show();
		}
	}
	public static void stop(Context context)
	{
		try
		{context.stopService(new Intent(context, PlayerService.class));}
		catch (Exception e)
		{}
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		if (service == null)
		{
			service = this;
			if (broadcast == null)
			{
				IntentFilter filter=new IntentFilter();
				filter.addAction(Control.NEXT);
				filter.addAction(Control.PRE);
				filter.addAction(Control.PLAY);
				filter.addAction(Control.CLOSE);
				filter.addAction(Control.SHUFFLE);
				registerReceiver(broadcast = new ControlReceiver(), filter);
			}
			if (music_info == null)music_info = MusicInfo.getInstance(this);
			if (music == null)music = getSharedPreferences("music", 0);
			if (player_list == null)player_list = new ArrayList<>();
			if (mp == null)
			{mp = new MediaPlayer();
				mp.setOnStateListener(this);}
			if (am == null)am = (AudioManager)getSystemService(AUDIO_SERVICE);
			mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
			if (remote == null)createNotification();
			for (OnStateListener o: listener)
				o.onStart(this);
			int music_id=music.getInt("music_id", -1);
			if (music_id != -1)
			{
				player_list.clear();
				try
				{
					player_list.addAll(music_info.query(music.getString("type", null), music.getString("other", null), music.getString("data", null)));
					mi = new MusicItem();
					mi.setId(music_id);
					this.mi = (MusicItem)player_list.get(player_list.indexOf(mi));
					loadDataSource(mi.getUrl(), false);
				}
				catch (Exception e)
				{}
			}
		}
	}

	public MusicItem getCurrentMusicItem()
	{
		return mi;
	}
	public long getCurrentPosition()
	{
		if (mp.isPlaying() || !(mp.getCurrentPosition() < 1))
			return mp.getCurrentPosition();
		else
			return music.getLong("music_position", -1);
	}
	public void seekTo(int time)
	{
		mp.seekTo(time);
		if (!mp.isPlaying())
			music.edit().putLong("music_position", time).commit();
	}
	public void start(MusicItem mi)
	{
		music.edit().putLong("music_position", -1).commit();
		if (mi != null)
		{
			if (mi.equals(this.mi))
			{
				if (mp.isPlaying())
				{
					mp.pause();
					am.abandonAudioFocus(this);
				}
				else
					mp.start();
			}
			else
			{
				if (mi instanceof PlayItem)
					this.mi = mi;
				else
				{
					player_list.clear();
					player_list.addAll(music_info.query(music.getString("type", null), music.getString("other", null), music.getString("data", null)));
					this.mi = (MusicItem)player_list.get(player_list.indexOf(mi));

				}
				loadDataSource(mi.getUrl(), true);
			}
		}
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{

		return START_REDELIVER_INTENT;
	}
	public void loadDataSource(String path, boolean autoPlay)
	{
		this.autoPlay = autoPlay;
		for (OnPlayerStateListener o:playerListener)
			o.onPrepare(mp, mi);
		remote.setTextViewText(R.id.notify_title, mi.getTitle());
		remote.setTextViewText(R.id.notify_artist, mi.getArtist());
		startForeground(nb.hashCode(), nb.build());
		mp.reset();
		mp.setOnPreparedListener(this);
		try
		{
			mp.setDataSource(path);
			mp.prepareAsync();
		}
		catch (Exception e)
		{
			Toast.makeText(this, "資源不可用", Toast.LENGTH_SHORT).show();
			playerNext();
		}

	}
	private void createNotification()
	{
		remote = new RemoteViews(getPackageName(), R.layout.notify_view);
		remote.setOnClickPendingIntent(R.id.notify_pre, PendingIntent.getBroadcast(this, 1, new Intent(Control.PRE), PendingIntent.FLAG_UPDATE_CURRENT));
		remote.setOnClickPendingIntent(R.id.notify_next, PendingIntent.getBroadcast(this, 1, new Intent(Control.NEXT), PendingIntent.FLAG_UPDATE_CURRENT));
		remote.setOnClickPendingIntent(R.id.notify_play, PendingIntent.getBroadcast(this, 1, new Intent(Control.PLAY), PendingIntent.FLAG_UPDATE_CURRENT));
		remote.setOnClickPendingIntent(R.id.notify_close, PendingIntent.getBroadcast(this, 1, new Intent(Control.CLOSE), PendingIntent.FLAG_UPDATE_CURRENT));
		nb = new Notification.Builder(this)
			.setSmallIcon(R.drawable.music)
			//.setTicker("music")
			.setContent(remote)
			.setOngoing(true);
		//.setContentTitle("").setContentText("");
		startForeground(nb.hashCode(), nb.build());
		//nm.notify(33,nb.build());
	}

	@Override
	public void onPrepared(android.media.MediaPlayer p1)
	{
		if (autoPlay)
			p1.start();
		long time=music.getLong("music_position", -1);
		if (time != -1)p1.seekTo((int)time);
		music_info.insertPlayHistory(mi.getId());
	}

	@Override
	public void onStart(MediaPlayer mp)
	{
		for (OnPlayerStateListener o:playerListener)
			o.onStart(mp);
		remote.setImageViewResource(R.id.notify_play, R.drawable.ic_pause);
		startForeground(nb.hashCode(), nb.build());
		music.edit().putInt("music_id", mi.getId()).commit();
		am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
	}

	@Override
	public void onPause(MediaPlayer mp)
	{
		for (OnPlayerStateListener o:playerListener)
			o.onPause(mp);
		remote.setImageViewResource(R.id.notify_play, R.drawable.ic_play);
		startForeground(nb.hashCode(), nb.build());

	}

	@Override
	public void onStop(MediaPlayer mp)
	{
		for (OnPlayerStateListener o:playerListener)
			o.OnStop(mp);
		remote.setImageViewResource(R.id.notify_play, R.drawable.ic_play);
		startForeground(nb.hashCode(), nb.build());
		music.edit().putLong("music_position", -1).commit();
		playerNext();
	}
	private void playerPre()
	{
		if (player_list.size() == 0)return;
		int index=-1;
		switch (music.getInt("mode", Mode.repeat))
		{
			case Mode.repeat:
				index = player_list.indexOf(mi);
				if (index == -1)return;
				index--;
				if (index == -1)index = player_list.size() - 1;
				break;
			case Mode.repeatOnce:
				index = player_list.indexOf(mi);
				break;
			case Mode.shuffle:
				index = new Random().nextInt(player_list.size());
				break;
		}
		mi = (MusicItem)player_list.get(index);
		loadDataSource(mi.getUrl(), true);
	}
	private void playerNext()
	{
		if (player_list.size() == 0)return;
		int index=-1;
		switch (music.getInt("mode", Mode.repeat))
		{
			case Mode.repeat:
				index = player_list.indexOf(mi);
				if (index == -1)return;
				index++;
				if (index == player_list.size())index = 0;
				break;
			case Mode.repeatOnce:
				index = player_list.indexOf(mi);
				break;
			case Mode.shuffle:
				index = new Random().nextInt(player_list.size());
				break;
		}
		mi = (MusicItem)player_list.get(index);
		loadDataSource(mi.getUrl(), true);
	}
	@Override
	public void onDestroy()
	{
		service = null;
		if (mp.isPlaying())
			mp.pause();
		music.edit().putLong("music_position", mp.getCurrentPosition()).commit();
		am.abandonAudioFocus(this);
		mp.release();
		stopForeground(true);
		for (OnStateListener o: listener)
			o.onStop(this);
		unregisterReceiver(broadcast);
		super.onDestroy();

	}

	@Override
	public void onAudioFocusChange(int p1)
	{
		switch (p1)
		{
			case AudioManager.AUDIOFOCUS_GAIN:
				mp.start();
				break;
			case AudioManager.AUDIOFOCUS_LOSS:
				mp.pause();
				break;
		}
	}


	private Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case 0:
					stopSelf();
					break;
			}
		}

	};
	public static void addOnStateListener(OnStateListener o)
	{
		if (!listener.contains(o))
			listener.add(o);
		if (service != null)o.onStart(service);
	}
	public static void removeOnStateListener(OnStateListener o)
	{
		listener.remove(o);
	}
	private static HashSet<OnStateListener> listener=new HashSet<>();
	public abstract interface OnStateListener
	{
		void onStart(PlayerService service);
		void onStop(PlayerService service);
	}
	public static void addOnPlayerStateListener(OnPlayerStateListener o)
	{
		if (!playerListener.contains(o))
			playerListener.add(o);
	}
	public static void removeOnPlayerStateListener(OnPlayerStateListener o)
	{
		playerListener.remove(o);
	}
	public abstract interface OnPlayerStateListener
	{
		void onPrepare(MediaPlayer mp, MusicItem mi);
		void onStart(MediaPlayer mp);
		void onPause(MediaPlayer mp);
		void OnStop(MediaPlayer mp);
	}
	private static ArrayList<OnPlayerStateListener> playerListener=new ArrayList<>();
	private class ControlReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context p1, Intent p2)
		{
			switch (p2.getAction())
			{
				case Control.NEXT:
					music.edit().putLong("music_position", -1).commit();
					playerNext();
					break;
				case Control.CLOSE:
					stopSelf();
					break;
				case Control.PLAY:
					if (mp.isPlaying())
					{
						mp.pause();
						am.abandonAudioFocus(PlayerService.this);
					}
					else
					{
						try
						{mp.start();
							long time=music.getLong("music_position", -1);
							if (time != -1)
								mp.seekTo((int)time);
							music.edit().putLong("music_position", -1).commit();
						}
						catch (Exception e)
						{
							onStop(mp);
						}
					}
					break;
				case Control.PRE:
					music.edit().putLong("music_position", -1).commit();
					playerPre();
					break;
				case Control.SHUFFLE:
					music.edit().putLong("music_position", -1).commit();
					player_list.clear();
					player_list.addAll(music_info.query(music.getString("type", null), music.getString("other", null), music.getString("data", null)));
					playerNext();
					break;
			}
		}


	}
	public static class Mode
	{
		public final static int repeat=0;
		public final static int repeatOnce=1;
		public final static int shuffle=2;
	}
	public static class Control
	{
		public final static String SHUFFLE="COM.MOE.PLAY_SHUFFLE";
		public final static String NEXT="COM.MOE.PLAY_NEXT";
		public final static String PRE="COM.MOE.PLAY_PRE";
		public final static String PLAY="COM.MOE.PLAY_PLAY";
		public final static String CLOSE="COM.MOE.PLAY_CLOSE";
	}
}
