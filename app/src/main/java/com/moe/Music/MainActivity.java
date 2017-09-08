package com.moe.Music;
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.widget.DrawerLayout;
import android.content.res.Configuration;
import android.view.View;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.graphics.Palette;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.support.v4.widget.NestedScrollView;
import android.view.Menu;
import android.support.design.widget.NavigationView;
import com.moe.fragment.MainFragment;
import android.app.FragmentTransaction;
import java.util.ArrayList;
import java.util.HashMap;
import com.moe.fragment.SettingFragment;
import com.moe.fragment.DownloadFragment;
import com.moe.fragment.ScannerFragment;
import android.content.Intent;
import com.moe.services.PlayerService;
import com.moe.utils.StorageHelper;
import android.content.pm.PackageManager;
import android.widget.Toast;
import com.moe.fragment.PlayerFragment;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.app.Fragment;

public class MainActivity extends AppCompatActivity implements
NavigationView.OnNavigationItemSelectedListener
{
	private DrawerLayout drawerlayout;
	private NavigationView menu;
	private int checkedId=R.id.home;
	private Fragment current,player;
	private HashMap<String,Fragment> fragmentlist;
	private String tag;
	/*public void close(){
		getFragmentManager().beginTransaction().hide(player).commit();
	}*/
	public void open()
	{
		if(player==null)player=new PlayerFragment();
		FragmentTransaction ft=getFragmentManager().beginTransaction();
		if(player.isAdded())
			ft.show(player);
			else
			ft.add(R.id.main_player,player,"player");
			ft.commit();
		}
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
		fragmentlist=new HashMap<>();
        super.onCreate(savedInstanceState);
		if(savedInstanceState!=null){
			fragmentlist.put("home",getFragmentManager().findFragmentByTag("home"));
			fragmentlist.put("setting",getFragmentManager().findFragmentByTag("setting"));
			fragmentlist.put("scanner",getFragmentManager().findFragmentByTag("scanner"));
			fragmentlist.put("download",getFragmentManager().findFragmentByTag("download"));
			player=getFragmentManager().findFragmentByTag("player");
			tag=savedInstanceState.getString("tag");
			current=fragmentlist.get(tag);
		}
        setContentView(R.layout.main);
		drawerlayout=(DrawerLayout)findViewById(R.id.main_drawerlayout);
		menu=((NavigationView)findViewById(R.id.main_leftselectedView));
		menu.setNavigationItemSelectedListener(this);
		open(savedInstanceState==null?R.id.home:savedInstanceState.getInt("id"));
		StorageHelper.isGrantExternalRW(this);
    }

	


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()){
			case android.R.id.home:
		return false;
		case R.id.search:
				startActivity(new Intent(this,SearchActivity.class));
			break;
			}
			return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		if(requestCode==1){
			for(int result:grantResults)
			if(result==PackageManager.PERMISSION_DENIED){
				Toast.makeText(this,"权限不足，部分功能将无法使用",Toast.LENGTH_SHORT).show();
				break;
			}
		}else
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		
	}
	

	@Override
	public void onBackPressed()
	{
		if(player!=null&&!player.isHidden()){
			((MainFragment)current).refresh();
			getFragmentManager().beginTransaction().hide(player).commit();
			return;
		}
		if(drawerlayout.isDrawerOpen(Gravity.START)){
			drawerlayout.closeDrawer(Gravity.START);
			return;
			}
		if(current instanceof com.moe.fragment.Fragment){
			if(((com.moe.fragment.Fragment)current).onBackPressed())
				return;
		}
		if(checkedId!=R.id.home){
			open(R.id.home);
			return;
			}
		
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.search,menu);
		return true;
	}

	
	@Override
	public boolean onNavigationItemSelected(MenuItem p1)
	{
		open(p1.getItemId());
		return true;
	}
	private void open(int id){
		drawerlayout.closeDrawers();
		switch(id){
			case R.id.sleep:
				new AlertDialog.Builder(this).setItems(PlayerService.getPlayerService().getSleep()>0?R.array.sleep_cancel:R.array.sleep, new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							int sleep=-1;
							switch(p2){
								case 0:
									sleep=300000;
									break;
								case 1:
									sleep=600000;
									break;
								case 2:
									sleep=900000;
									break;
								case 3:
									sleep=1800000;
									break;
								case 4:
									sleep=2700000;
									break;
								case 5:
									sleep=3600000;
									break;
								case 6:
									sleep=-1;
									break;
							}
							PlayerService.getPlayerService().setSleep(sleep);
						}
					}).show();
				break;
			case R.id.home:
				show(id,"home",MainFragment.class);
				break;
			case R.id.setting:
				show(id,"setting",SettingFragment.class);
				break;
			case R.id.scanner:
				show(id,"scanner",ScannerFragment.class);
				break;
			case R.id.download:
				show(id,"download",DownloadFragment.class);
				break;
			case R.id.exit:
				PlayerService.stop(this);
				break;
		}
	}
	private void show(int id,String tag,Class name){
		checkedId=id;
		menu.setCheckedItem(checkedId);
		Fragment tmp=fragmentlist.get(tag);
		try
		{
			if (tmp == null || tmp.isDetached())tmp = (Fragment)name.newInstance();
			else if(!tmp.isHidden())return;
		}
		catch (InstantiationException e)
		{}
		catch (IllegalAccessException e)
		{}
		fragmentlist.put(tag, tmp);
		FragmentTransaction ft=getFragmentManager().beginTransaction();
		if(current!=null){ft.hide(current);
		//if(id!=R.id.home)ft.detach(current);
		}
		if(tmp.isAdded()){
			ft.show(tmp);
		}else{
			ft.add(R.id.main,tmp,tag);
		}
		ft.commit();
		current=tmp;
		this.tag=tag;
	}
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		outState.putInt("id",checkedId);
		outState.putString("tag",tag);
		super.onSaveInstanceState(outState);
	}


	
}
