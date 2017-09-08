package com.moe.audio;
import java.io.File;
import android.os.Handler;
import android.os.Message;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import android.os.Looper;
import java.io.RandomAccessFile;
import java.util.Arrays;
import com.moe.utils.Bytes;
import java.io.ByteArrayOutputStream;
import android.text.TextUtils;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.zip.InflaterInputStream;
import java.io.ByteArrayInputStream;
import java.util.zip.InflaterOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class AudioReader
{
	private boolean isAsync;
	private Handler handler;
	private Tag tag;
	public void read(File f) throws FileNotFoundException, IOException{
		reset();
		parse(f);
	}
	public Tag getTag(){
		return tag;
	}
	
	public void readAsync(final File f){
		reset();
		if(handler==null){
			Looper.prepare();
			handler=new Handler(){

				@Override
				public void handleMessage(Message msg)
				{
					if(opl!=null)opl.onPrepared(AudioReader.this);
				}

			};
			Looper.loop();
			}
		new Thread(){
			public void run(){
				try
				{
					parse(f);
				}
				catch (Exception e)
				{
					handler.sendEmptyMessage(0);
				}
			}
		}.start();
	}
	private void parse(File f) throws FileNotFoundException, IOException{
		tag=new Tag();
		RandomAccessFile fis=new RandomAccessFile(f,"r");
		switch(fis.read()){
			case 'f'://flac
			flac(fis);
				break;
			case 'R'://wav
			wav(fis);
				break;
			case 'I'://mp3
			id3v2(fis);
				break;
			case 0x30://wma
			wma(fis);
				break;
			case 'M'://ape
			ape(fis);
				break;
			default:
			id3v1(fis);
			break;
		}
		fis.close();
		if(TextUtils.isEmpty(tag.getTitle().trim()))
			tag.setTitle(f.getName().substring(0,f.getName().lastIndexOf(".")));
	}
	public void reset(){
		isAsync=false;
		tag=null;
	}
	
	public void setOnPreparedListener(OnPreparedListener o){
		opl=o;
	}
	private OnPreparedListener opl;
	public abstract interface OnPreparedListener{
		void onPrepared(AudioReader ar);
	}
	private void flac(RandomAccessFile is) throws IOException{
		is.skipBytes(3);
		byte flag=0;
		while((flag=(byte)(is.read()&0xff))!=-1){
			byte[] b=new byte[3];
			is.read(b);
			byte[] data=data=new byte[Bytes.getInt(b)];
			is.read(data,0,data.length);
			switch(flag&0x7f){
				//case 0://信息
				//	break;
				case 6://专辑封面
					break;
				case 4:
					ByteArrayInputStream bais=new ByteArrayInputStream(data);
					byte[] size=new byte[4];
					bais.read(size);
					Bytes.reverse(size);
					int length=Bytes.getInt(size);
					bais.skip(4+length);
					for(int len=8+length;len<data.length;){
						bais.read(size);
						Bytes.reverse(size);
						length=Bytes.getInt(size);
						byte[] item=new byte[length];
						bais.read(item);
						String item_content=new String(item);
						if(item_content.indexOf("=")!=-1){
						String[] split=item_content.split("=");
						switch(split[0]){
							case "TITLE":
							case "title":
								tag.setTitle(split[1]);
								break;
							case "ARTIST":
							case "artist":
								tag.setArtist(split[1]);
								break;
							case "ALBUM":
							case "album":
								tag.setAlbum(split[1]);
								break;
							case "ALBUM ARTIST":
								break;
						}
						}
						//bais.skip(4);
						len+=4+length;
					}
					
					bais.close();
					
					break;
				case 0:
				case 1:
				case 2:
				case 3:
				case 5:
				case 127:
					default://7-126
					break;
			}
			//is.skip(2);
			if(((flag&0x80)>>7)==1)break;
		}
	}
	private void id3v2(RandomAccessFile is) throws IOException{
		is.skipBytes(2);
		int version=is.read();//2.2 2.3 2.4
		is.skipBytes(2);//副版本号和flags跳过
		byte[] size=new byte[4];
		is.read(size);
		int length=0;
		if(version==2)
			length=Bytes.getInt(size);
		else
			length=(size[0]&0x7F)<<21 |(size[1]&0x7F)<<14 |(size[2]&0x7F)<<7 |(size[3]&0x7F);
		for(int len=0;len<length;){
			if(version==2){
				byte[] id=new byte[3];
				byte[] valuesize=new byte[3];
				is.read(id);
				is.read(valuesize);
				byte[] data=new byte[Bytes.getInt(valuesize)];
				is.read(data);
				String charset="gb18030";
				int skipSize=1;
				if(data.length>0){
					switch(data[0]&0xff){
						case 0:
							charset="gb18030";
							//Bytes.left(data,1);
							//skip=false;
							break;
						case 1:
							if(((data[1]&0xff)==0xff&&(data[2]&0xff)==0xfe)||((data[1]&0xff)==0xfe&&(data[2]&0xff)==0xff)){
								charset="unicode";
							}else
								charset="utf-16";
							//Bytes.left(data,1);
							break;
						case 2:
							charset="utf-16be";
							//Bytes.left(data,1);
							break;
						case 3:
							charset="utf-8";
							//Bytes.left(data,1);
							break;
						default:
							skipSize=0;
							break;
					}
					if(data.length>8&&(data[5]&0xff)==0xff&&(data[7]&0xff)==0xfe){
						skipSize=8;
						charset="utf-16le";
					}
				switch(new String(id)){
					case "TT2":
						tag.setTitle(new String(data,skipSize,data.length-skipSize,charset));
						break;
					case "TAL":
						tag.setAlbum(new String(data,skipSize,data.length-skipSize,charset));
						break;
					case "TP1":
						tag.setArtist(new String(data,skipSize,data.length-skipSize,charset));
						break;
					case "TYE":
						tag.setYear(new String(data,skipSize,data.length-skipSize,charset));
						break;
				}
				len+=6+data.length;
				}
			}else{
			byte[] flag=new byte[2];
			byte[] id=new byte[4];
			is.read(id);
			is.read(size);
			is.read(flag);
			byte[] data=new byte[(size[0]&0xff)*0x100000000 +(size[1]&0xff)*0x10000 +(size[2]&0xff)*0x100 +(size[3]&0xff)];
			is.read(data);
			String charset="gb18030";
			int skipSize=1;
			if(data.length>0){
				switch(data[0]&0xff){
					case 0:
						charset="gb18030";
						//Bytes.left(data,1);
						//skip=false;
						break;
					case 1:
						if(((data[1]&0xff)==0xff&&(data[2]&0xff)==0xfe)){
							charset="unicode";
							}else
							charset="utf-16";
						//Bytes.left(data,1);
						break;
					case 2:
						charset="utf-16be";
						//Bytes.left(data,1);
						break;
					case 3:
						charset="utf-8";
						//Bytes.left(data,1);
						break;
					default:
					skipSize=0;
					break;
				}
				if(data.length>8&&(data[5]&0xff)==0xff&&(data[7]&0xff)==0xfe){
					skipSize=8;
					charset="utf-16le";
				}
				
			}
			switch(new String(id)){
				case "TIT2"://标题
					tag.setTitle(new String(data,skipSize,data.length-skipSize,charset));
					break;
				case "TPE1"://作者
					tag.setArtist(new String(data,skipSize,data.length-skipSize,charset));
					break;
				case "TALB"://专辑
					tag.setAlbum(new String(data,skipSize,data.length-skipSize,charset));
					break;
				case "TYER"://年代
					tag.setYear(new String(data,skipSize,data.length-skipSize,charset));
					break;
				case "APIC"://专辑封面
					break;
				case "COMM":
					tag.setComment(new String(data,skipSize,data.length-skipSize,charset));
					break;
				//case "TXXX":
				//	String da=new String(data);
				//	break;
			}
			len=len+10+data.length;
			}
		}
	}
	private void id3v1(RandomAccessFile is) throws IOException{
		is.seek(is.length()-128);
		byte[] header=new byte[3];
		is.read(header);
		if(header[0]=='T'&&header[1]=='A'&&header[2]=='G'){
			byte[] title=new byte[30];
			byte[] artist=new byte[30];
			byte[] album=new byte[30];
			byte[] year=new byte[4];
			byte[] comment=new byte[30];
			is.read(title);
			is.read(artist);
			is.read(album);
			is.read(year);
			is.read(comment);
			int genre=is.read();//流派
			tag.setTitle(new String(title,"gb18030").trim());
			tag.setArtist(new String(artist,"gb18030").trim());
			tag.setAlbum(new String(album,"gb18030").trim());
			tag.setYear(new String(year,"gb18030").trim());
			tag.setComment(new String(comment,"gb18030").trim());
		}else{
			
		}
	}
	private void ape(RandomAccessFile is) throws IOException{
		/*is.skipBytes(3);
		byte[] version=new byte[2];
		is.read(version);
		if((((0|(version[0]&0xff))<<8)|version[1]&0xff)>3980){
			byte[] padding=new byte[2];
			byte[] descriptor_length=new byte[2];
			byte[] header_length=new byte[4];
			byte[] seek_table_length=new byte[4];
			byte[] wave_header_length=new byte[4];
			byte[] audio_data_length=new byte[4];
			byte[] audio_data_length_high=new byte[4];
			byte[] wave_tail_length=new byte[4];
			byte[] md5=new byte[16];
			byte[] compression_level=new byte[2];
			byte[] format_flags=new byte[2];
			byte[] blocks_per_frame=new byte[4];
			byte[] final_frame_blocks=new byte[4];
			byte[] total_frames=new byte[4];
			byte[] bits_per_samples=new byte[2];
			byte[] channels_num=new byte[2];
			byte[] sample_rate=new byte[4];
		}else{
			byte[] compression_level=new byte[2];
			byte[] format_flags=new byte[2];
			byte[] channels_num=new byte[2];
			byte[] sample_rate=new byte[4];
			byte[] wave_header_length=new byte[4];
			byte[] wave_tail_length=new byte[4];
			byte[] total_frames=new byte[4];
			byte[] final_frame_blocks=new byte[4];
			
			
		}*/
		long length=is.length();
		is.seek(length=length-32);
		byte[] tag=new byte[32];
		is.read(tag);
		int[] data=null;
		try{
		data=apeParseTag(tag);
		}catch(NullPointerException e){
			is.seek(length=length-128);
			is.read(tag);
			try{
				data=apeParseTag(tag);
				}catch(NullPointerException n){
					//没有信息
				}
		}
		if(data!=null){
			is.seek(length=length-data[0]+32);
			for(int len=0;len<data[1];len++){
				byte[] size=new byte[4];
				is.read(size);
				Bytes.reverse(size);
				byte[] value=new byte[Bytes.getInt(size)];
				is.skipBytes(4);
				StringBuffer ab=new StringBuffer();
				int key=0;
				while((key=is.read())!=0){
					ab.append((char)key);
				}
				is.read(value);
				switch(ab.toString()){
					case "ALBUM":
						this.tag.setAlbum(new String(value));
						break;
					case "ARTIST":
						this.tag.setArtist(new String(value));
						break;
					case "TITLE":
						this.tag.setTitle(new String(value));
						break;
					case "Comment":
						this.tag.setComment(new String(value));
						break;
					case "Track":
						break;
					default:
					break;
				}
			}
		}
	}
	private int[] apeParseTag(byte[] data){
		byte[] header=new byte[]{'A','P','E','T','A','G','E','X'};
		byte[] head=new byte[8];
		System.arraycopy(data,0,head,0,8);
		if(!Arrays.equals(head,header))
			throw new NullPointerException();
			byte[] size=new byte[4];
			byte[] count=new byte[4];
			System.arraycopy(data,12,size,0,4);
			System.arraycopy(data,16,count,0,4);
			Bytes.reverse(size);
			Bytes.reverse(count);
			return new int[]{Bytes.getInt(size),Bytes.getInt(count)};
	}
	private void wma(RandomAccessFile is) throws IOException{
		is.skipBytes(15);
		byte[] size=new byte[8];
		is.read(size);
		is.skipBytes(6);
		Bytes.reverse(size);
		long length=Bytes.getLong(size);
		
		for(long i=30;i<length;){
			byte[] header=new byte[16];
			byte[] header_size=new byte[8];
			is.read(header);
			is.read(header_size);
			//is.skipBytes(8);
			//i+=8;
			Bytes.reverse(header_size);
			long frame_size=Bytes.getLong(header_size);
			if(Arrays.equals(header,new byte[]{0x33,0x26,(byte)0xB2,0x75,(byte)0x8E,0x66,(byte)0xCF,0x11,(byte)0xA6,(byte)0xD9,0x00,(byte)0xAA,0x00,0x62,(byte)0xCE,0x6C})){
				//标准帧
				byte[] title_size=new byte[2];
				byte[] artist_size=new byte[2];
				byte[] permission_size=new byte[2];
				byte[] comment_size=new byte[2];
				byte[] other_size=new byte[2];
				is.read(title_size);
				Bytes.reverse(title_size);
				is.read(artist_size);
				Bytes.reverse(artist_size);
				is.read(permission_size);
				Bytes.reverse(permission_size);
				is.read(comment_size);
				Bytes.reverse(comment_size);
				is.read(other_size);
				Bytes.reverse(other_size);
				byte[] title=new byte[Bytes.getInt(title_size)];
				is.read(title);
				tag.setTitle(new String(title));
				byte[] artist=new byte[Bytes.getInt(artist_size)];
				is.read(artist);
				tag.setArtist(new String(artist));
				byte[] permission=new byte[Bytes.getInt(permission_size)];
				is.read(permission);
				byte[] comment=new byte[Bytes.getInt(comment_size)];
				is.read(comment);
				tag.setComment(new String(comment));
				byte[] other=new byte[Bytes.getInt(other_size)];
				is.read(other);
				}else if(Arrays.equals(header,new byte[]{0x40,(byte)0xA4,(byte)0xD0,(byte)0xD2,0x07,(byte)0xE3,(byte)0xD2,0x11,(byte)0x97,(byte)0xF0,0x00,(byte)0xA0,(byte)0xC9,0x5E,(byte)0xA8,0x50})){
				//扩展帧
				byte[] count=new byte[2];
				is.read(count);
				Bytes.reverse(count);
				int num=Bytes.getInt(count);
				for(int n=0;n<num;n++){
				byte[] key_size=new byte[2];
				is.read(key_size);
				Bytes.reverse(key_size);
				byte[] key=new byte[Bytes.getInt(key_size)];
				is.read(key);
				is.skipBytes(2);
				byte[] value_size=new byte[2];
				is.read(value_size);
				Bytes.reverse(value_size);
				byte[] value=new byte[Bytes.getInt(value_size)];
				is.read(value);
				switch(new String(key)){
					case "WM/AlbumTitle":
						tag.setAlbum(new String(value));
						break;
				}
				}
			}
			is.seek(i=i+frame_size);
			
		}
	}
	private void wav(RandomAccessFile is) throws IOException{
		is.skipBytes(3);
		long skip_length=4;
		byte[] file_size=new byte[4];
		skip_length+=is.read(file_size);
		is.skipBytes(8);
		skip_length+=8;
		byte[] audio_size=new byte[4];
		skip_length+=is.read(audio_size);//格式长度
		Bytes.reverse(audio_size);
		int format_length=Bytes.getInt(audio_size);
		skip_length+=format_length;
		is.skipBytes(format_length);
		skip_length+=is.read(audio_size);//块标识
		String list=new String(audio_size);
		if(list.equals("LIST")){
			skip_length+=is.read(audio_size);//数据块长度
			Bytes.reverse(audio_size);
			format_length=Bytes.getInt(audio_size);
			skip_length+=format_length;
			//
			byte[] block=new byte[format_length];
			is.read(block);
			ByteArrayInputStream bais=new ByteArrayInputStream(block);
			bais.skip(4);
			for(int len=4;len<block.length;){
				byte[] key=new byte[4];
				len+=bais.read(key);
				String key_=new String(key);
				len+=bais.read(key);
				Bytes.reverse(key);
				int value_len=Bytes.getInt(key);
				if(value_len%2==1)value_len+=1;
				byte[] value=new byte[value_len];
				len+=bais.read(value);
				switch(key_){
					case "IART":
						tag.setArtist(new String(value));
						break;
					case "INAM":
						tag.setTitle(new String(value));
						break;
					default:
					break;
				}
				
			}
			bais.close();
			
		}
		
		
	}
}
