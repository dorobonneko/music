package com.moe.Music;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.view.View;
import android.support.v7.widget.RecyclerView;
import com.moe.adapter.SiteListAdapter;
import android.support.v7.widget.LinearLayoutManager;
import com.moe.adapter.Adapter;
import com.moe.adapter.Adapter.ViewHolder;
import android.widget.ImageView;
import android.content.SharedPreferences;
import android.widget.EditText;
import java.net.URL;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import android.net.Uri;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import com.moe.entity.PlayItem;
import java.util.ArrayList;
import com.moe.entity.Song;
import java.util.List;
import android.os.Handler;
import android.os.Message;
import com.moe.adapter.MusicAdapter;
import com.moe.entity.MainIndexItem;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.Toast;
import com.moe.services.PlayerService;
import com.moe.entity.MusicItem;
import com.moe.media.MediaPlayer;
import com.moe.view.Divider;
import com.moe.utils.StringUtils;
import android.text.TextUtils;
import org.jsoup.nodes.XmlDeclaration;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParserException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.moe.utils.MusicParse;

public class SearchActivity extends Activity implements View.OnClickListener,Adapter.OnItemClickListener,PlayerService.OnPlayerStateListener
{
	private RecyclerView sitelist;
	private ImageView site;
	private SharedPreferences searchpreference;
	private EditText key;
	private MusicAdapter ma;
	private List<? extends MainIndexItem> list;
	private SwipeRefreshLayout refresh;
	private boolean canLoadMore;
	private String search_key;
	private int page=1;
	private int spage=0;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		searchpreference = getSharedPreferences("search", 0);
		super.onCreate(savedInstanceState);
		//Display display=getWindowManager().getDefaultDisplay();
		//setContentView(LayoutInflater.from(this).inflate(R.layout.search_view,null),new ViewGroup.LayoutParams(display.getWidth(),display.getHeight()));
		setContentView(R.layout.search_view);
		getWindow().setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT);
		sitelist = (RecyclerView)findViewById(R.id.search_view_sitelist);
		refresh = (SwipeRefreshLayout)findViewById(R.id.search_view_refresh);
		findViewById(R.id.search_view_search).setOnClickListener(this);
		key = (EditText)findViewById(R.id.search_view_key);
		site = (ImageView)findViewById(R.id.search_view_siteselet);
		site.setOnClickListener(this);
		sitelist.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
		SiteListAdapter sla=new SiteListAdapter();
		sitelist.setAdapter(sla);
		sla.setOnItemClickListener(this);
		site.setImageResource(searchpreference.getInt("site", R.drawable.wangyi));
		RecyclerView content=(RecyclerView)findViewById(R.id.search_view_resultlist);
		content.setLayoutManager(new LinearLayoutManager(this));
		content.setAdapter(ma = new MusicAdapter(list = new ArrayList<>()));
		ma.setOnItemClickListener(this);
		content.addOnScrollListener(new Scroll());
		content.addItemDecoration(new Divider());
		refresh.setEnabled(false);
		PlayerService.getPlayerService().addOnPlayerStateListener(this);
		MusicItem mi=PlayerService.getPlayerService().getCurrentMusicItem();
		if (mi != null)ma.setItemSelected(mi.getId());
	}

	@Override
	public void onClick(View p1)
	{
		switch (p1.getId())
		{
			case R.id.search_view_siteselet:
				if (sitelist.getVisibility() == sitelist.VISIBLE)
					sitelist.setVisibility(sitelist.GONE);
				else
					sitelist.setVisibility(sitelist.VISIBLE);
				break;
			case R.id.search_view_search:
				if (!refresh.isRefreshing() && key.getText().toString().trim().length() > 0)
				{
					page = 1;
					spage = 0;
					canLoadMore = true;
					list.clear();
					ma.notifyDataSetChanged();
					search_key = key.getText().toString().trim();
					loadMore();
				}
				break;
		}
	}

	@Override
	public void onItemClick(Adapter adapter, Adapter.ViewHolder vh)
	{
		if (adapter instanceof SiteListAdapter)
		{
			site.setImageResource(vh.itemView.getId());
			sitelist.setVisibility(sitelist.GONE);
			searchpreference.edit().putInt("site", vh.itemView.getId()).commit();
			list.clear();
			ma.notifyDataSetChanged();
		}
		else
		{
			if(PlayerService.getPlayerService()!=null)
			PlayerService.getPlayerService().play(list.get(vh.getAdapterPosition()), list);
		}
	}


	private void search(String key, int mode) throws  JSONException
	{
		String[] data = null;
		String json=null;
		Object list=null;
		switch (mode)
		{
			case R.drawable.wangyi:
				data = getResources().getStringArray(R.array.wangyi);
				json = StringUtils.getString(data[0], String.format(data[1], this.list.size(), key));
				list = MusicParse.wangyi(json, data);
				break;
			case R.drawable.kugou:
				data = getResources().getStringArray(R.array.kugou);
				json = StringUtils.getString(String.format(data[1], key, page));
				page++;
				list = MusicParse.kugou(json, data);

				break;
			case R.drawable.qq:
				data = getResources().getStringArray(R.array.qq);
				json = StringUtils.getString(String.format(data[1], key, page));
				page++;
				list = MusicParse.qq(json, data);
				break;
			case R.drawable.kuwo:
				data = getResources().getStringArray(R.array.kuwo);
				json = StringUtils.getString(String.format(data[1], key, spage));
				spage++;
				list = MusicParse.kuwo(json, data);
				break;
			case R.drawable.xiami:
				data = getResources().getStringArray(R.array.xiami);
				json = StringUtils.getString(data[0], data[2], String.format(data[1], key, page));
				page++;
				list = MusicParse.xiami(json, data);
				break;
			case R.drawable.baidu:
				data = getResources().getStringArray(R.array.baidu);
				json = StringUtils.getString(data[0], String.format(data[1], key, page));
				page++;
				list = MusicParse.baidu(json, data);
				break;
			case R.drawable.wusing:
				data = getResources().getStringArray(R.array.sing5);
				json = StringUtils.getString(data[0], String.format(data[1], key, page));
				page++;
				list = MusicParse.sing5(json, data);
				break;
			/*case R.drawable.migu:
				data = getResources().getStringArray(R.array.migu);
				json = StringUtils.getString(data[0], String.format(data[1], key, page));
				page++;
				list = migu(json, data);
				break;
			case R.drawable.imusic:
				data = getResources().getStringArray(R.array.imusic);
				key=Uri.encode(key);
				json = StringUtils.getString(data[0], String.format(data[2],key),String.format(data[1], key, page));
				page++;
				list = imusic(json, data);
				break;*/
			case R.drawable.ting1:
				data = getResources().getStringArray(R.array.ting1);
				json = StringUtils.getString(data[0], String.format(data[2],key),String.format(data[1], key, page));
				page++;
				list = MusicParse.ting1(json, data);
				break;
		}
		handler.obtainMessage(0, list).sendToTarget();
	}
	
	private void loadMore()
	{
		if (refresh.isRefreshing())return;
		refresh.setRefreshing(true);
		new Thread(){
			public void run()
			{
				try
				{
					search(Uri.encode(search_key), searchpreference.getInt("site", R.drawable.wangyi));
				}
				catch (Exception e)
				{handler.sendEmptyMessage(1);}
			}
		}.start();
	}
	Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case 0:
					refresh.setRefreshing(false);
					List<PlayItem> li=(List<PlayItem>)msg.obj;
					if (li == null)
					{
						canLoadMore = false;
						return;
					}
					list.addAll(li);
					ma.notifyDataSetChanged();
					break;
				case 1:
					refresh.setRefreshing(false);
					Toast.makeText(SearchActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					break;
				case 2:
					refresh.setRefreshing(false);
					canLoadMore = false;
					Toast.makeText(SearchActivity.this, "加载结束", Toast.LENGTH_SHORT).show();
					break;
			}
		}

	};
	class Scroll extends RecyclerView.OnScrollListener
	{

		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy)
		{
			if (canLoadMore && !recyclerView.canScrollVertically(1))
			{
				loadMore();
			}
		}

	}

	@Override
	public void onStart(MediaPlayer mp)
	{
		// TODO: Implement this method
	}

	@Override
	public void onPrepare(MediaPlayer mp, MusicItem mi)
	{
		ma.setItemSelected(mi.getId());
		ma.notifyDataSetChanged();
		// TODO: Implement this method
	}

	@Override
	public void OnStop(MediaPlayer mp)
	{
		// TODO: Implement this method
	}

	@Override
	public void onPause(MediaPlayer mp)
	{
		// TODO: Implement this method
	}

	@Override
	protected void onDestroy()
	{
		PlayerService.removeOnPlayerStateListener(this);
		super.onDestroy();
	}





}
