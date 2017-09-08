package com.moe.entity;
import java.io.Serializable;

public class MusicItem extends MainIndexItem 
{
	private String album,artist,year;
	private String url;
	private int id=-1;

	public void setId(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		if(id==-1)return url.hashCode();
		return id;
	}
	public void setAlbum(String album)
	{
		this.album = album;
	}

	public String getAlbum()
	{
		return album;
	}

	public void setArtist(String artist)
	{
		this.artist = artist;
	}

	public String getArtist()
	{
		return artist;
	}

	public void setYear(String year)
	{
		this.year = year;
	}

	public String getYear()
	{
		return year;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getUrl()
	{
		return url;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof MusicItem)
		return ((MusicItem)obj).getId()==getId();
		return false;
	}
	
	}
