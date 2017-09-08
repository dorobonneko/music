package com.moe.database;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class Database extends SQLiteOpenHelper
{
	protected Database(Context context){
		super(context.getApplicationContext(),"database",null,3);
	}

	@Override
	public void onCreate(SQLiteDatabase p1)
	{
		p1.execSQL("create table musicinfo(id INTEGER primary key,url TEXT UNIQUE,title TEXT,artist TEXT,album TEXT,dir TEXT,year TEXT,fav INTEGER,poster TEXT,time INTEGER)");
		p1.execSQL("create table playhistory(id INTEGER primary key,time INTEGER,FOREIGN KEY(id) REFERENCES musicinfo(id))");
		p1.execSQL("create table favourite(id INTEGER primary key,FOREIGN KEY(id) REFERENCES musicinfo(id))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase p1, int p2, int p3)
	{
	}


	
}
