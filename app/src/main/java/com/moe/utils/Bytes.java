package com.moe.utils;

public class Bytes
{

	public static int getInt(byte[] b){
		if(b.length>4)return -1;
		int value=0;
		for(int i=0;i<b.length;i++){
			value=(value<<8)|(b[i]&0xff);
		}
		return value;
	}
	public static void reverse(byte[] b){
		for(int i=0;i<b.length/2;i++){
			int end=b.length-i-1;
			b[i]=(byte)((b[i]^b[end])&0xff);
			b[end]=(byte)((b[i]^b[end])&0xff);
			b[i]=(byte)((b[i]^b[end])&0xff);
		}
	}
	public static long getLong(byte[] b){
		if(b.length>8)return -1;
		long value=0;
		for(int i=0;i<b.length;i++){
			value=(value<<8)|(b[i]&0xff);
		}
		return value;
	}
	public static void left(byte[] b,int size){
		for(int i=0;i<size;i++)
		for(int n=0;n<b.length;n++){
			if(n+1==b.length)
				b[n]=0;
				else
			b[n]=b[n+1];
		}
	}
}
