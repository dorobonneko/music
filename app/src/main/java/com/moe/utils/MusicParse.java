package com.moe.utils;
import org.json.JSONObject;
import com.moe.entity.PlayItem;
import java.util.List;
import org.json.JSONArray;
import java.util.ArrayList;
import com.moe.entity.Song;
import android.net.Uri;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.json.JSONException;
import android.text.TextUtils;
import android.text.StaticLayout;

public class MusicParse
{
	public static List<PlayItem> ting1(String json, String[] data)
	{
		JSONArray array;
		try
		{
			array = new JSONObject(json).getJSONArray("results");}
		catch (Exception e)
		{
			return null;
		}
		List<PlayItem> lpi=new ArrayList<>();
		for (int i=0;i < array.length();i++)
		{
			try
			{
				PlayItem pi=new PlayItem();
				JSONObject song_info=array.getJSONObject(i);
				pi.setId(song_info.getInt("song_id"));
				pi.setTitle(song_info.getString("song_name"));
				pi.setArtist(song_info.getString("singer_name"));
				List<Song> playlist=new ArrayList<>();
				Song song=new Song();
				song.setUrl(String.format(data[2],song_info.getString("song_path").replace("wma","mp3")));
				playlist.add(song);
				pi.setPlayList(playlist);
				lpi.add(pi);
			}
			catch (Exception e)
			{}
		}
		return lpi;
	}
	public static List<PlayItem> imusic(String json, String[] data)
	{
		JSONArray array;
		try
		{
			array = new JSONObject(json).getJSONArray("data");}
		catch (Exception e)
		{
			return null;
		}
		List<PlayItem> lpi=new ArrayList<>();
		for (int i=0;i < array.length();i++)
		{
			try
			{
				PlayItem pi=new PlayItem();
				JSONObject song_info=array.getJSONObject(i);
				pi.setId(song_info.getInt("id"));
				pi.setTitle(song_info.getString("name"));
				pi.setArtist(song_info.getString("player"));
				List<Song> playlist=new ArrayList<>();
				Song song=new Song();
				song.setUrl("http://3g.imusic.ic"+song_info.getString("href"));
				playlist.add(song);
				pi.setPlayList(playlist);
				lpi.add(pi);
			}
			catch (Exception e)
			{}
		}
		return lpi;
	}
	public static List<PlayItem> migu(String json, String[] data)
	{
		JSONArray array;
		try
		{
			array = new JSONObject(json).getJSONObject("data").getJSONArray("list");}
		catch (Exception e)
		{
			return null;
		}
		List<PlayItem> lpi=new ArrayList<>();
		for (int i=0;i < array.length();i++)
		{
			try
			{
				PlayItem pi=new PlayItem();
				JSONObject song_info=array.getJSONObject(i);
				pi.setId(song_info.getInt("song_id"));
				pi.setTitle(song_info.getString("song_name"));
				pi.setArtist(song_info.getString("artist_name"));
				JSONArray songlist=new JSONObject(StringUtils.getString(String.format(data[3], pi.getId()))).getJSONObject("data").getJSONArray("trackList");
				List<Song> playlist=new ArrayList<>();
				for (int j=0;j < songlist.length();j++)
				{
					JSONObject songitem=songlist.getJSONObject(j);
					Song song=new Song();
					song.setUrl(xiamiDecode(songitem.getString("location")));
					playlist.add(song);
				}
				pi.setPlayList(playlist);
				lpi.add(pi);
			}
			catch (Exception e)
			{}
		}
		return lpi;
	}
	public static List<PlayItem> baidu(String json, String[] data)
	{
		//json = json.substring(18, json.length() - 2);
		JSONArray array;
		try
		{
			array = new JSONObject(json).getJSONObject("result").getJSONObject("song_info").getJSONArray("song_list");}
		catch (Exception e)
		{
			return null;
		}
		List<PlayItem> lpi=new ArrayList<>();
		for (int i=0;i < array.length();i++)
		{
			try
			{
				PlayItem pi=new PlayItem();
				JSONObject song_info=array.getJSONObject(i);
				pi.setTitle(song_info.getString("title").replaceAll("<(|/)em>", ""));
				pi.setArtist(song_info.getString("author").replaceAll("<(|/)em>", ""));
				pi.setId(song_info.getInt("song_id"));
				List<Song> playlist=new ArrayList<>();
				try
				{
					JSONObject song_item=new JSONObject(StringUtils.getString(String.format(data[2], pi.getId()))).getJSONObject("bitrate");
					Song song=new Song();
					song.setBr(song_item.getInt("file_bitrate") * 1000);
					song.setUrl(song_item.getString("file_link"));
					playlist.add(song);
				}
				catch (Exception e)
				{}
				pi.setPlayList(playlist);
				lpi.add(pi);
			}
			catch (Exception e)
			{}
		}
		return lpi;
	}
	public static List<PlayItem> xiami(String json, String[] data)
	{
		JSONArray array;
		try
		{
			array = new JSONObject(json).getJSONObject("data").getJSONArray("songs");}
		catch (Exception e)
		{
			return null;
		}
		List<PlayItem> lpi=new ArrayList<>();
		for (int i=0;i < array.length();i++)
		{
			try
			{
				PlayItem pi=new PlayItem();
				JSONObject song_info=array.getJSONObject(i);
				pi.setId(song_info.getInt("song_id"));
				pi.setTitle(song_info.getString("song_name"));
				pi.setArtist(song_info.getString("artist_name"));
				JSONArray songlist=new JSONObject(StringUtils.getString(String.format(data[3], pi.getId()))).getJSONObject("data").getJSONArray("trackList");
				List<Song> playlist=new ArrayList<>();
				for (int j=0;j < songlist.length();j++)
				{
					JSONObject songitem=songlist.getJSONObject(j);
					Song song=new Song();
					song.setUrl(xiamiDecode(songitem.getString("location")));
					playlist.add(song);
				}
				pi.setPlayList(playlist);
				lpi.add(pi);
			}
			catch (Exception e)
			{}
		}
		return lpi;
	}
	public static String xiamiDecode(String s2)
	{
		int heigth = Integer.valueOf(s2.substring(0, 1));  
		String s3 = s2.substring(1);
		int width = s3.length() / heigth;
		int remainder = s3.length() % heigth;
		String[] urlSeparate = new String[heigth];

		for (int i = 0; i < urlSeparate.length; i++)
		{
			if (remainder > 0)
			{
				urlSeparate[i] = s3.substring(0, width + 1);
				remainder--;
				s3 = s3.substring(width + 1);
			}
			else
			{
				urlSeparate[i] = s3.substring(0, width);
				s3 = s3.substring(width);
			}

		}
		String location = "";
		for (int i = 0; i < urlSeparate[0].length(); i++)
		{
			for (int j = 0; j < urlSeparate.length; j++)
			{
				if (urlSeparate[j].length() < urlSeparate[0].length() && i == urlSeparate[0].length() - 1)
				{
					continue;
				}
				else
				{
					location += urlSeparate[j].substring(i, i + 1);
				}
			}
		}
		return Uri.decode(location).replaceAll("\\^", "0");
	}

	public static List<PlayItem> kuwo(String json, String[] data)
	{
		JSONArray array;
		try
		{
			array = new JSONObject(json).getJSONArray("abslist");}
		catch (Exception e)
		{
			return null;
		}
		List<PlayItem> lpi=new ArrayList<>();
		for (int i=0;i < array.length();i++)
		{
			try
			{
				JSONObject song_info=array.getJSONObject(i);
				PlayItem pi=new PlayItem();
				pi.setId(song_info.getString("MUSICRID").hashCode());
				pi.setTitle(song_info.getString("SONGNAME"));
				pi.setArtist(song_info.getString("ARTIST"));
				List<Song> playlist=new ArrayList<>();
				String song_item=StringUtils.getString(String.format(data[2], song_info.getString("MUSICRID")));
				String mp3path=song_item.substring(song_item.indexOf("<mp3path>") + 9, song_item.indexOf("</mp3path>"));
				String mp3dl=song_item.substring(song_item.indexOf("<mp3dl>") + 7, song_item.indexOf("</mp3dl>"));
				String aacpath=song_item.substring(song_item.indexOf("<aacpath>") + 9, song_item.indexOf("</aacpath>"));
				String aacdl=song_item.substring(song_item.indexOf("<aacdl>") + 7, song_item.indexOf("</aacdl>"));
				Song song=new Song();
				song.setBr(64000);
				song.setUrl(String.format(data[3], mp3dl, mp3path));
				playlist.add(song);
				song=new Song();
				song.setUrl(String.format(data[3], aacdl, aacpath));
				playlist.add(song);
				pi.setPlayList(playlist);
				lpi.add(pi);
			}
			catch (Exception e)
			{}
		}
		return lpi;
	}
	public static List<PlayItem> sing5(String json, String[] data)
	{
		JSONArray array;
		try
		{
			array = new JSONObject(json).getJSONArray("list");}
		catch (Exception e)
		{
			return null;
		}
		Pattern pattern=Pattern.compile("<audio.*?>");
		List<PlayItem> lpi=new ArrayList<>();
		for (int i=0;i < array.length();i++)
		{
			try
			{
				PlayItem pi=new PlayItem();
				JSONObject song_info=array.getJSONObject(i);
				pi.setTitle(song_info.getString("songName").replaceAll("<.*?>", ""));
				pi.setArtist(song_info.getString("singer"));
				pi.setId(song_info.getInt("songId"));
				List<Song> playlist=new ArrayList<>();
				String type=song_info.getString("typeEname");
				String s=StringUtils.getString(String.format(data[2], type, pi.getId()));
				Matcher matcher=pattern.matcher(s);
				if (matcher.find())
				{
					String url=matcher.group();
					url = url.substring(url.indexOf("\"") + 1);
					url = url.substring(0, url.indexOf("\""));
					Song song=new Song();
					song.setUrl(url);
					playlist.add(song);
				}
				pi.setPlayList(playlist);
				lpi.add(pi);
			}
			catch (Exception e)
			{}
		}
		return lpi;
	}
	public static List<PlayItem> qq(String json, String[] data)
	{
		JSONArray array;
		try
		{
			array = new JSONObject(json).getJSONObject("data").getJSONObject("song").getJSONArray("list");}
		catch (Exception e)
		{
			return null;
		}
		List<PlayItem> lpi=new ArrayList<>();
		for (int i=0;i < array.length();i++)
		{
			try
			{
				PlayItem pi=new PlayItem();
				JSONObject song_info=array.getJSONObject(i);
				pi.setId(song_info.getInt("songid"));
				pi.setTitle(song_info.getString("songname"));
				JSONArray artist=song_info.getJSONArray("singer");//歌手数据
				StringBuilder sb=new StringBuilder();
				for (int n=0;n < artist.length();n++)
				{
					sb.append(artist.getJSONObject(n).getString("name")).append("&");//歌手
				}
				sb.deleteCharAt(sb.length() - 1);
				pi.setArtist(sb.toString());
				//String s=StringUtils.getString(data[0],String.format(data[4],song_info.getString("songmid")),String.format(data[3],pi.getId()));
				List<Song> playlist=new ArrayList<>();
				for(int n=2;n<data.length;n++){
				Song song=new Song();
				if(n==2)
				song.setBr(64000*(data.length-n));
				else
				song.setBr(64000*(data.length-n-1));
				song.setUrl(String.format(data[n], song_info.getString("songmid")));
				playlist.add(song);
				}
				pi.setPlayList(playlist);
				lpi.add(pi);
			}
			catch (JSONException j)
			{}
		}
		return lpi;
	}
	public static List<PlayItem> kugou(String json, String[] data) throws JSONException
	{
		JSONArray array;
		try
		{
			array = new JSONObject(json).getJSONObject("data").getJSONArray("info");}
		catch (Exception e)
		{
			return null;
		}
		List<PlayItem> lpi=new ArrayList<>();
		for (int i=0;i < array.length();i++)
		{
			PlayItem pi=new PlayItem();
			JSONObject song_info=array.getJSONObject(i);
			String hash320=song_info.getString("320hash");
			String sqhash=song_info.getString("sqhash");
			String hash=song_info.getString("hash");
			pi.setArtist(song_info.getString("singername"));
			pi.setTitle(song_info.getString("songname"));
			pi.setId(song_info.getInt("audio_id"));
			List<Song> playList=new ArrayList<>();
			if (!TextUtils.isEmpty(hash320))
			{
				Song song=new Song();
				song.setBr(320000);
				song.setUrl(new JSONObject(StringUtils.getString(String.format(data[2], hash320))).getString("url"));
				playList.add(song);
			}
			if (!TextUtils.isEmpty(sqhash))
			{
				Song song=new Song();
				song.setBr(192000);
				song.setUrl(new JSONObject(StringUtils.getString(String.format(data[2], sqhash))).getString("url"));
				playList.add(song);
			}
			if (!TextUtils.isEmpty(hash))
			{
				Song song=new Song();
				song.setBr(128000);
				song.setUrl(new JSONObject(StringUtils.getString(String.format(data[2], hash))).getString("url"));
				playList.add(song);
			}
			pi.setPlayList(playList);
			lpi.add(pi);
		}
		return lpi;
	}
	public static List<PlayItem> wangyi(String json, String[] data) throws JSONException
	{
		JSONArray array=null;
		try
		{
			array = new JSONObject(json).getJSONObject("result").getJSONArray("songs");
		}
		catch (Exception e)
		{
			return null;
		}
		List<PlayItem> list=new ArrayList<>();
		for (int i=0;i < array.length();i++)
		{
			PlayItem pi=new PlayItem();
			JSONObject jo=array.getJSONObject(i);
			pi.setTitle(jo.getString("name"));//标题
			pi.setId(jo.getInt("id"));//歌曲id
			JSONArray artist=jo.getJSONArray("ar");//歌手数据
			StringBuilder sb=new StringBuilder();
			for (int n=0;n < artist.length();n++)
			{
				sb.append(artist.getJSONObject(n).getString("name")).append("&");//歌手
			}
			sb.deleteCharAt(sb.length() - 1);
			pi.setArtist(sb.toString());
			jo.getJSONObject("al");//专辑
			List<Song> playList=new ArrayList<>();
			for (int position=4;position < data.length;position++)
			{
				try
				{

					JSONArray songs=new JSONObject(StringUtils.getString(data[0], String.format(data[3], pi.getId(), data[position]))).getJSONArray("data");
					for (int j=0;j < songs.length();j++)
					{
						JSONObject song=songs.getJSONObject(j);
						Song song_item=new Song();
						song_item.setUrl(song.getString("url"));
						song_item.setBr(song.getInt("br"));
						if (song_item.getBr() == 0)break;
						int index=playList.indexOf(song_item);
						if (index == -1)
							playList.add(song_item);
					}
				}
				catch (Exception e)
				{}
			}
			pi.setPlayList(playList);
			list.add(pi);
		}
		return list;
	}
}
