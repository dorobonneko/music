package com.moe.database;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.moe.entity.MusicItem;
import android.database.sqlite.SQLiteStatement;
import java.io.File;
import com.moe.entity.MainIndexItem;
import java.util.List;
import org.apache.http.conn.util.PublicSuffixListParser;
import java.util.ArrayList;
import android.database.Cursor;
import android.text.TextUtils;

public class MusicInfo extends Database
{
	private static MusicInfo mi;
	private SQLiteDatabase sql;
	protected MusicInfo(Context context){
		super(context);
		sql=getReadableDatabase();
	}
	public static MusicInfo getInstance(Context context){
		if(mi==null)mi=new MusicInfo(context);
		return mi;
	}
	public void insertPlayHistory(int id){
		SQLiteStatement state=sql.compileStatement("insert into playhistory values(?,?)");
		state.acquireReference();
		state.bindLong(1,id);
		state.bindLong(2,System.currentTimeMillis());
		try{state.executeInsert();}catch(Exception e){
			state.close();
			state.releaseReference();
			state=sql.compileStatement("update playhistory set time=? where id=?");
			state.acquireReference();
			state.bindLong(1,System.currentTimeMillis());
			state.bindLong(2,id);
			state.executeUpdateDelete();
		}
		state.close();
		state.releaseReference();
	}
	public void setFavourite(int id,boolean isfav){
		SQLiteStatement state=null;
		if(isfav){
			state=sql.compileStatement("insert into favourite values(?)");
			state.acquireReference();
			state.bindLong(1,id);
			state.executeInsert();
		}else{
			state=sql.compileStatement("delete from favourite where id=?");
			state.acquireReference();
			state.bindLong(1,id);
			state.executeUpdateDelete();
		}
		state.close();
		state.releaseReference();
	}
	public boolean isFavourite(int id){
		boolean flag=false;
		Cursor c=sql.query("favourite",new String[]{"id"},"id=?",new String[]{id+""},null,null,null);
		flag=c.getCount()==1;
		c.close();
		return flag;
	}
	public boolean isExists(String url){
		boolean flag=false;
		Cursor c=sql.query("musicinfo",new String[]{"id"},"url=?",new String[]{url},null,null,null);
		flag=c.getCount()==1;
		c.close();
		return flag;
	}
	public void clear(){
		SQLiteStatement state=sql.compileStatement("delete from musicinfo");
		state.acquireReference();
		state.executeUpdateDelete();
		state.close();
		state.releaseReference();
	}
	public void insert(MusicItem mi){
		SQLiteStatement state=sql.compileStatement("insert into musicinfo values(?,?,?,?,?,?,?,?,?,?)");
		state.acquireReference();
		state.bindLong(1,mi.getUrl().hashCode());
		state.bindString(2,mi.getUrl());
		state.bindString(3,mi.getTitle());
		state.bindString(4,mi.getArtist());
		state.bindString(5,mi.getAlbum());
		state.bindString(6,new File(mi.getUrl()).getParent());
		state.bindString(7,mi.getYear());
		state.bindLong(8,0);
		state.bindString(9,"");
		state.bindLong(10,new File(mi.getUrl()).lastModified());
		state.executeInsert();
		state.close();
		state.releaseReference();
		
	}
	public MusicItem getMusicItem(int id){
		MusicItem mi=null;
		Cursor cursor=sql.query("musicinfo",null,"id=?",new String[]{id+""},null,null,null);
		if(cursor.moveToNext()){
					mi=new MusicItem();
					mi.setTitle(cursor.getString(cursor.getColumnIndex("title")));
					mi.setAlbum(cursor.getString(cursor.getColumnIndex("album")));
					mi.setArtist(cursor.getString(cursor.getColumnIndex("artist")));
					mi.setUrl(cursor.getString(cursor.getColumnIndex("url")));
			}
			cursor.close();
				return mi;
	}
	public List<? extends MainIndexItem> queryFolder(String type){
		List<MainIndexItem> list=null;
		Cursor cursor=sql.query("musicinfo",new String[]{type,"count(*)"},null,null,type,null,type+" desc");
		list=loadFolder(cursor);
		cursor=null;
		return list;
	}
	
	public List<? extends MainIndexItem> query(String type,String other,String data){
		List<MainIndexItem> list=null;
		Cursor cursor=null;
		if(type==null)return null;
		switch(type){
			case Type.Favourite:
				cursor=sql.rawQuery("select musicinfo.title,musicinfo.album,musicinfo.artist,musicinfo.url from musicinfo,favourite where musicinfo.id=favourite.id order by musicinfo.title desc",null);
				list=loadMusic(cursor);
				break;
			case Type.PlayHistory:
				cursor=sql.rawQuery("select musicinfo.title,musicinfo.album,musicinfo.artist,musicinfo.url from musicinfo,playhistory where musicinfo.id=playhistory.id order by playhistory.time desc",null);
				list=loadMusic(cursor);
			break;
			case Type.Music:
				cursor=sql.query("musicinfo",null,other==null?null:other,data==null?null:new String[]{data},null,null,"title desc");
				list=loadMusic(cursor);
			break;
			case Type.AddHistory:
				cursor=sql.query("musicinfo",null,null,null,null,null,"time desc");
				list=loadMusic(cursor);
				break;
		}
		cursor.close();
		return list;
	}
	private List<MainIndexItem> loadFolder(Cursor cursor){
		List<MainIndexItem> list=new ArrayList<MainIndexItem>();
		while(cursor.moveToNext()){
			MainIndexItem mi=new MainIndexItem();
			mi.setTitle(cursor.getString(0));
			mi.setSize(cursor.getInt(1));
			list.add(mi);
		}
		return list;
	}
	private List<MainIndexItem> loadMusic(Cursor cursor){
		List<MainIndexItem> list=new ArrayList<MainIndexItem>();
		while(cursor.moveToNext()){
			MusicItem mi=new MusicItem();
			mi.setTitle(cursor.getString(cursor.getColumnIndex("title")));
			mi.setAlbum(cursor.getString(cursor.getColumnIndex("album")));
			mi.setArtist(cursor.getString(cursor.getColumnIndex("artist")));
			mi.setUrl(cursor.getString(cursor.getColumnIndex("url")));
			list.add(mi);
		}
		return list;
	}
	public static class Type{
		public final static String Music="",Artist="artist",Album="album",Folder="dir",Favourite="fav",PlayHistory="playhistory",AddHistory="addhistory";
	}
	public static class Other{
		
		public final static String getArtist="artist=?";

		public final static String getAlbum="album=?";

		public final static String getFolder="dir=?";
	}
}
