package com.moe.entity;
import java.io.Serializable;

public class MainIndexItem implements Serializable
{
	private String title;
	private int size;


	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	public int getSize()
	{
		return size;
	}}
