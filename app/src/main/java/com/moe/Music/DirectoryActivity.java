package com.moe.Music;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;
import android.support.v7.app.ActionBar;
import com.moe.utils.StorageHelper;
import java.util.List;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ArrayList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import com.moe.adapter.FolderAdapter;
import com.moe.adapter.Adapter;
import com.moe.adapter.Adapter.ViewHolder;
import java.io.FileFilter;
import android.widget.TextView;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;

public class DirectoryActivity extends AppCompatActivity implements FileFilter,Comparator<File>,Adapter.OnItemClickListener,View.OnClickListener
{
	private RecyclerView list_view;
	private File current;
	private File[] index;
	private ArrayList<File> list=new ArrayList<>();
	private TextView index_message;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.folder_picker_view);
		getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);
		//getWindow().setLayout(getWindowManager().getDefaultDisplay().getWidth(),getWindowManager().getDefaultDisplay().getHeight());
		Toolbar toolbar=(Toolbar)findViewById(R.id.folder_picker_view_toolbar);
		findViewById(R.id.folder_picker_view_select).setOnClickListener(this);
		toolbar.setTitle("选择文件夹");
		setSupportActionBar(toolbar);
		ActionBar ab=getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		list_view=(RecyclerView)findViewById(R.id.folder_picker_view_list);
		list_view.setLayoutManager(new LinearLayoutManager(this));
		index_message=(TextView)findViewById(R.id.folder_picker_view_index);
		FolderAdapter fa=new FolderAdapter(list);
		list_view.setAdapter(fa);
		fa.setOnItemClickListener(this);
		String[] path=StorageHelper.getAllPath(this).toArray(new String[0]);
		index=new File[path.length];
		for(int i=0;i<path.length;i++)
			index[i]=new File(path[i]);
		loadList(index);
		index_message.setText("/");
	}
	private void loadList(String[] list){
		File[] tmp=new File[list.length];
		for(int i=0;i<list.length;i++)
		tmp[i]=new File(list[i]);
		loadList(tmp);
	}
	private void loadList(File[] list){
		Arrays.sort(list,this);
		this.list.clear();
		for(File f:list)
		this.list.add(f);
		list_view.getAdapter().notifyDataSetChanged();
	}
	private boolean loadDir(File file){
		//file=file.getParentFile();
		
		if(file.canRead()){
			index_message.setText(file.getAbsolutePath());
			current=file;
		loadList(file.listFiles(this));
		return true;
		}
		return false;
		
	}

	@Override
	public boolean accept(File p1)
	{
		// TODO: Implement this method
		return p1.isDirectory()&&!p1.getName().startsWith(".");
	}

	
	@Override
	public int compare(File p1, File p2)
	{
		return p1.getName().compareTo(p2.getName());
	}

	@Override
	public void onBackPressed()
	{
		if(current!=null){
			for(File f:index){
				if(f.equals(current)){
					loadList(index);
					current=null;
					index_message.setText("/");
					return;
				}
			}
		loadDir(current.getParentFile());
		}else
		super.onBackPressed();
	}

	@Override
	public void onItemClick(Adapter adapter, Adapter.ViewHolder vh)
	{
		loadDir(list.get(vh.getAdapterPosition()));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()){
			case android.R.id.home:
				onBackPressed();
				break;
			case R.id.home:
				current=null;
				index_message.setText("/");
				loadList(index);
				break;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.folder_home,menu);
		return true;
	}

	@Override
	public void onClick(View p1)
	{
		if(current==null)
			Toast.makeText(this,"无效的目录",Toast.LENGTH_SHORT).show();
			else{
				setResult(RESULT_OK,new Intent().setData(Uri.parse(current.getAbsolutePath())));
				finish();
			}
	}


	
	
}
