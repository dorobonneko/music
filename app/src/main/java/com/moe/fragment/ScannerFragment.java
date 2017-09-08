package com.moe.fragment;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import com.moe.Music.R;
import java.io.File;
import com.moe.utils.StorageHelper;
import java.io.IOException;
import java.util.HashSet;
import android.widget.ProgressBar;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import java.util.ArrayList;
import com.moe.entity.MusicItem;
import com.moe.audio.AudioReader;
import com.moe.audio.Tag;
import com.moe.database.MusicInfo;
public class ScannerFragment extends Fragment implements View.OnClickListener
{
	private ArrayList<File> file=new ArrayList<>();
	private ProgressBar pb;
	private TextView message,current;
	private View scanner;
	private AudioReader ar=new AudioReader();
	private MusicInfo music;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.scanner_view,container,false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		current=(TextView)view.findViewById(R.id.scanner_view_currentPoint);
		message=(TextView)view.findViewById(R.id.scanner_view_message);
		pb=(ProgressBar)view.findViewById(R.id.scanner_view_progress);
		scanner=view.findViewById(R.id.scanner_view_scanner);
		scanner.setOnClickListener(this);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onHiddenChanged(boolean hidden)
	{
		if(hidden&&scanner.getVisibility()==scanner.VISIBLE)message.setText(null);
		super.onHiddenChanged(hidden);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		music=MusicInfo.getInstance(getActivity());
		super.onActivityCreated(savedInstanceState);
	}
	private void scanner(){
		for(String path:StorageHelper.getAllPath(getActivity()))
		loop(new File(path));
		handler.sendEmptyMessage(0);
		music.clear();
		for(int i=0;i<file.size();i++){
			ParseInfo(file.get(i));
			pb.setProgress(i);
		}
	}
	private void loop(File f){
		handler.obtainMessage(2,f.getAbsolutePath()).sendToTarget();
		if(f.isDirectory()&&!f.getName().matches("^[\\.].*")){
			if(!new File(f,".nomedia").exists())
			for(File dir:f.listFiles())
			loop(dir);
		}else if(f.getName().matches(".*[\\.](?i:mp3|m4a|ape|flac|wma|wav)$")){
			ScannerFragment.this.file.add(f);
			handler.sendEmptyMessage(1);
		}
	}
	private void ParseInfo(File f){
		handler.obtainMessage(3,f.getName()).sendToTarget();
		handler.obtainMessage(2,f.getAbsolutePath()).sendToTarget();
		try
		{
			ar.read(f);
			Tag tag=ar.getTag();
			MusicItem mi=new MusicItem();
			mi.setTitle(tag.getTitle());
			mi.setAlbum(tag.getAlbum());
			mi.setArtist(tag.getArtist());
			mi.setYear(tag.getYear());
			mi.setUrl(f.getAbsolutePath());
			music.insert(mi);
		}
		catch (IOException e)
		{}

		
	}
	@Override
	public void onClick(View p1)
	{
		file.clear();
		new Thread(){
			public void run(){
				scanner();
				handler.sendEmptyMessage(4);
			}
		}.start();
		p1.setVisibility(p1.INVISIBLE);
		pb.setVisibility(pb.VISIBLE);
	}

	Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what){
				case 0:
					pb.setMax(file.size());
					break;
				case 1:
					message.setText("已找到"+file.size()+"首歌曲");
					break;
				case 2:
					current.setText(msg.obj.toString());
					break;
				case 3:
					message.setText(msg.obj.toString());
					break;
				case 4:
					pb.setProgress(0);
					pb.setVisibility(pb.INVISIBLE);
					message.setText("扫描完成！共"+file.size()+"首歌曲");
					current.setText(null);
					scanner.setVisibility(scanner.VISIBLE);
					break;
					
			}
		}
		
	};

	@Override
	public boolean onBackPressed()
	{
		// TODO: Implement this method
		return false;
	}

	
}
