package com.moe.entity;

public class Song
{
	private int br;
	private String url;
	private long size;

	public void setSize(long size)
	{
		this.size = size;
	}

	public long getSize()
	{
		return size;
	}

	public void setBr(int br)
	{
		this.br = br;
	}

	public int getBr()
	{
		return br;
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
		return ((Song)obj).getBr()==getBr();
	}
	
	}
