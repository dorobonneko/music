package com.moe.entity;
import com.moe.download.Download;

public class DownloadItem
{
	private String title,dir,url;
	private long total;
	private int state;

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}

	public void setDir(String dir)
	{
		this.dir = dir;
	}

	public String getDir()
	{
		return dir;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getUrl()
	{
		return url;
	}

	public void setState(int current)
	{
		this.state = current;
	}

	public int getState()
	{
		return state;
	}

	public void setTotal(long total)
	{
		this.total = total;
	}

	public long getTotal()
	{
		return total;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof Download)
			return ((Download)obj).getDownloadItem().getUrl().equals(url);
		return ((DownloadItem)obj).getUrl().equals(url);
	}
	
	}
