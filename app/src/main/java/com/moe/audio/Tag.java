package com.moe.audio;

public class Tag
{
	private String title="",artist="<unknow>",album="<unknown>",year="0000";
	private String comment;

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public String getComment()
	{
		return comment;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}

	public void setArtist(String artist)
	{
		this.artist = artist;
	}

	public String getArtist()
	{
		return artist;
	}

	public void setAlbum(String album)
	{
		this.album = album;
	}

	public String getAlbum()
	{
		return album;
	}

	public void setYear(String year)
	{
		this.year = year;
	}

	public String getYear()
	{
		return year;
	}}
