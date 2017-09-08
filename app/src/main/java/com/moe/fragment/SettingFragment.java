package com.moe.fragment;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import com.moe.Music.R;
import android.preference.ListPreference;
import android.preference.Preference;
import android.content.SharedPreferences;
import android.os.Build;
import com.moe.utils.StorageHelper;
import java.io.File;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.os.Environment;
import com.moe.Music.DirectoryActivity;
import android.view.View;
import android.widget.ListView;
import android.view.ViewGroup;
public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener,
Preference.OnPreferenceClickListener
{
	private SharedPreferences setting;
	private ListPreference listen_list;
	private Preference path;
	private String dir;
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName("setting");
		addPreferencesFromResource(R.xml.setting);
		setting=getPreferenceManager().getSharedPreferences();
		listen_list=(ListPreference)findPreference("setting_listen");
		listen_list.setOnPreferenceChangeListener(this);
		listen_list.setSummary(getResources().getStringArray(R.array.listen)[Integer.parseInt(setting.getString("setting_listen","0"))]);
		path=findPreference("setting_download_path");
		path.setOnPreferenceClickListener(this);
		path.setSummary(setting.getString("path",Environment.getExternalStorageDirectory().getAbsolutePath()+"/MoeMusic"));
	}

	@Override
	public boolean onPreferenceChange(Preference p1, Object p2)
	{
		switch(p1.getKey()){
			case "setting_listen":
					p1.setSummary(getResources().getStringArray(R.array.listen)[Integer.parseInt(p2.toString())]);
				break;
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference p1)
	{
		switch(p1.getKey()){
			case "setting_download_path":
				if(Build.VERSION.SDK_INT>=21&&setting.getString("sdcard",null)==null){
					for(String dir:StorageHelper.getAllPath(getActivity())){
						if(!new File(dir).canWrite()){
							this.dir=dir;
							checkPermission();
							return true;
						}
					}
				}
				startActivityForResult(new Intent(getActivity(),DirectoryActivity.class),3);
				break;
		}
		return true;
	}
	private void checkPermission(){
		new AlertDialog.Builder(getActivity()).setMessage("没有" + dir + "写入权限，请授权！").setNegativeButton("取消", null)
			.setPositiveButton("授权", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					try{startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),2);}catch(Exception e){
						Toast.makeText(getActivity(),"请先去（设置>应用管理>更多应用）启用文档",Toast.LENGTH_SHORT).show();
						}
				}
			}).show();
		
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch(requestCode){
			case 2:
			if(resultCode==Activity.RESULT_OK){
			if(data.getDataString().matches(".*?primary%3A$")){
				Toast.makeText(getActivity(),"请选择sd卡",Toast.LENGTH_SHORT).show();
			checkPermission();
				}else if(!data.getDataString().endsWith("%3A")){
				Toast.makeText(getActivity(),"请选择sd卡根目录",Toast.LENGTH_SHORT).show();
			checkPermission();
				}else{
				getContext().getContentResolver().takePersistableUriPermission(data.getData(),Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				setting.edit().putString("sdcard",data.getDataString()).commit();
				startActivityForResult(new Intent(getActivity(),DirectoryActivity.class),3);
			}
			}else{
				checkPermission();
			}
			break;
			case 3:
				if(resultCode==Activity.RESULT_OK){
					setting.edit().putString("path",data.getDataString()).commit();
					path.setSummary(data.getDataString());
				}
				break;
		}
		
	}
	
}
