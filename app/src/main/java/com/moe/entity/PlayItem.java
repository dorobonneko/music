package com.moe.entity;
import java.util.List;

public class PlayItem extends MusicItem
{
	private List<Song> playList;
	


	public void setPlayList(List<Song> playList)
	{
		this.playList = playList;
	}

	public List<Song> getPlayList()
	{
		return playList;
	}

	@Override
	public String getUrl()
	{
		if(playList!=null&&playList.size()>0)
		return playList.get(playList.size()-1).getUrl();
		return null;
	}
	
}
