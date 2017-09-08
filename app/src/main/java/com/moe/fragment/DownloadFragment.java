package com.moe.fragment;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Bundle;
import com.moe.Music.R;
import android.support.design.widget.TabLayout;
import java.util.List;
import android.support.v7.widget.RecyclerView;
import java.util.ArrayList;
import android.support.v4.view.ViewPager;
import com.moe.adapter.ViewPagerAdapter;
import com.moe.entity.DownloadItem;
import com.moe.adapter.DownloadAdapter;
import android.support.v7.widget.LinearLayoutManager;
import com.moe.database.DownloadDatabase;
import com.moe.services.DownloadService;
import android.os.Handler;
import android.os.Message;
import com.moe.download.Download;
import com.moe.view.Divider;
import com.moe.adapter.Adapter.ViewHolder;
import com.moe.adapter.Adapter;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;
public class DownloadFragment extends Fragment implements DownloadService.OnStateListener,View.OnClickListener,
Adapter.OnItemClickListener,
DownloadAdapter.OnItemLongClickListener
{
	private List<DownloadItem> loading_selected,success_selected;
	private List<DownloadItem> loading,success;
	private DownloadAdapter loading_adapter,success_adapter;
	private List<RecyclerView> list;
	private DownloadDatabase dd;
	private View delete,cancel;
	private ViewPager vp;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		list=new ArrayList<>();
		list.add(new RecyclerView(container.getContext()));
		list.add(new RecyclerView(container.getContext()));
		list.get(0).setTag("下载中");
		list.get(1).setTag("已完成");
		list.get(0).addItemDecoration(new Divider());
		list.get(1).addItemDecoration(new Divider());
		return inflater.inflate(R.layout.download_view,container,false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		TabLayout tab=(TabLayout)view.findViewById(R.id.local_music_view_tablayout);
		vp=(ViewPager)view.findViewById(R.id.local_music_view_viewpager);
		tab.setupWithViewPager(vp);
		vp.setAdapter(new ViewPagerAdapter(list));
		list.get(0).setLayoutManager(new LinearLayoutManager(getActivity()));
		list.get(1).setLayoutManager(new LinearLayoutManager(getActivity()));
		list.get(0).setAdapter(loading_adapter=new DownloadAdapter(loading=new ArrayList<>(),loading_selected=new ArrayList<>()));
		list.get(1).setAdapter(success_adapter=new DownloadAdapter(success=new ArrayList<>(),success_selected=new ArrayList<>()));
		delete=view.findViewById(R.id.download_view_delete);
		delete.setOnClickListener(this);
		cancel=view.findViewById(R.id.download_view_cancel);
		cancel.setOnClickListener(this);
		loading_adapter.setOnItemClickListener(this);
		loading_adapter.setOnItemLongClickListener(this);
		success_adapter.setOnItemClickListener(this);
		success_adapter.setOnItemLongClickListener(this);
		((DefaultItemAnimator)list.get(0).getItemAnimator()).setSupportsChangeAnimations(false);
		((DefaultItemAnimator)list.get(1).getItemAnimator()).setSupportsChangeAnimations(false);
		list.get(0).setNestedScrollingEnabled(false);
		list.get(1).setNestedScrollingEnabled(false);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		dd=DownloadDatabase.getInstance(getActivity());
		super.onActivityCreated(savedInstanceState);
		loading.addAll(dd.query(false));
		loading_adapter.notifyDataSetChanged();
		success.addAll(dd.query(true));
		success_adapter.notifyDataSetChanged();
		DownloadService.setOnStateListener(this);
		onHiddenChanged(false);
	}

	@Override
	public void onAdded(DownloadItem di)
	{
		if(!loading.contains(di))
		{	
		loading.add(di);
		loading_adapter.notifyItemInserted(loading.size()-1);
		}
	}

	@Override
	public void onRemoved(final DownloadItem di,boolean flag)
	{
		if(flag){
			getView().post(new Runnable(){

					@Override
					public void run()
					{
						int index=loading.indexOf(di);
						if(index!=-1){
							loading.remove(index);
							loading_adapter.notifyItemRemoved(index);
						}
						success.add(di);
						success_adapter.notifyItemInserted(success.size()-1);
						
					}
				});
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden)
	{
		if(!hidden){
			DownloadService service=DownloadService.getDownloadService();
			if(service!=null){
				for(DownloadItem di:service.getDownloadList()){
					int index=loading.indexOf(di);
					if(index!=-1){
						loading.remove(index);
						loading_adapter.notifyItemRemoved(index);
						loading.add(index,di);
						loading_adapter.notifyItemInserted(index);
					}
				}
				for(Download down:service.getActivedList()){
					int index=loading.indexOf(down.getDownloadItem());
					if(index!=-1){
						loading.remove(index);
						loading_adapter.notifyItemRemoved(index);
						loading.add(index,down.getDownloadItem());
						loading_adapter.notifyItemInserted(index);
					}
				}
			}
		//if(loading.size()>0)
			handler.sendEmptyMessage(0);
			}else
			handler.removeMessages(0);
	}

	private Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what){
				case 0:
					loading_adapter.notifyDataSetChanged();
					//if(loading.size()>0)
						sendEmptyMessageDelayed(0,1000);
					break;
				case 1:
					break;
			}
		}
	
	};

	@Override
	public void onClick(View p1)
	{
		switch(p1.getId()){
			case R.id.download_view_delete:
				switch(vp.getCurrentItem()){
					case 0:
						if(loading_selected.size()==0){
							Toast.makeText(getActivity(),"没有任务被选中",Toast.LENGTH_SHORT).show();
							return;
						}
						break;
					case 1:
						if(success_selected.size()==0){
							Toast.makeText(getActivity(),"没有任务被选中",Toast.LENGTH_SHORT).show();
							return;
						}
						break;
				}
				new AlertDialog.Builder(getActivity()).setMessage("确定删除已选任务？")
					.setNeutralButton("确定", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							switch(vp.getCurrentItem()){
								case 0:
									for(DownloadItem url:loading_selected){
										int index=loading.indexOf(url);
										if(index!=-1){
											dd.delete(loading.remove(index),false);
											loading_adapter.notifyItemRemoved(index);
										}
									}
									
									break;
								case 1:
									for(DownloadItem url:success_selected){
										int index=success.indexOf(url);
										if(index!=-1){
											dd.delete(success.remove(index),false);
											success_adapter.notifyItemRemoved(index);
										}
									}
									break;
							}
						}
					})
					.setNegativeButton("和文件一起", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							switch(vp.getCurrentItem()){
								case 0:
									for(DownloadItem url:loading_selected){
										int index=loading.indexOf(url);
										if(index!=-1){
											dd.delete(loading.remove(index),true);
											loading_adapter.notifyItemRemoved(index);
										}
									}

									break;
								case 1:
									for(DownloadItem url:success_selected){
										int index=success.indexOf(url);
										if(index!=-1){
											dd.delete(success.remove(index),true);
											success_adapter.notifyItemRemoved(index);
										}
									}
									break;
							}
						}
					})
				.setPositiveButton("取消",null).show();
				break;
			case R.id.download_view_cancel:
				onBackPressed();
				break;
		}
	}

	@Override
	public boolean onItemLongClick(DownloadAdapter adapter, Adapter.ViewHolder vh)
	{
		if(delete.getVisibility()==delete.INVISIBLE){
			delete.setVisibility(delete.VISIBLE);
			cancel.setVisibility(delete.VISIBLE);
			switch(vh.getItemViewType()){
				case 0:
					loading_selected.add(loading.get(vh.getAdapterPosition()));
					loading_adapter.notifyItemChanged(vh.getAdapterPosition());
					break;
				case 1:
					success_selected.add(success.get(vh.getAdapterPosition()));
					success_adapter.notifyItemChanged(vh.getAdapterPosition());
					break;
			}
		}
		return true;
	}

	@Override
	public void onItemClick(Adapter adapter, Adapter.ViewHolder vh)
	{
		if(delete.getVisibility()==delete.VISIBLE){
			DownloadItem url=null;
			switch(vh.getItemViewType()){
				case 0:
					url=loading.get(vh.getAdapterPosition());
					if(loading_selected.contains(url))
						loading_selected.remove(url);
						else
						loading_selected.add(url);
					loading_adapter.notifyItemChanged(vh.getAdapterPosition());
					break;
				case 1:
					url=success.get(vh.getAdapterPosition());
					if(success_selected.contains(url))
						success_selected.remove(url);
						else
						success_selected.add(url);
					success_adapter.notifyItemChanged(vh.getAdapterPosition());
					break;
			}
		}else{
			switch(vh.getItemViewType()){
				case 0:
			DownloadService.addTask(loading.get(vh.getAdapterPosition()),getActivity());
			break;
			}
		}
	}

	@Override
	public boolean onBackPressed()
	{
		if(delete.getVisibility()==delete.VISIBLE){
			delete.setVisibility(delete.INVISIBLE);
			cancel.setVisibility(cancel.INVISIBLE);
			loading_selected.clear();
			success_selected.clear();
			loading_adapter.notifyDataSetChanged();
			success_adapter.notifyDataSetChanged();
			return true;
		}
		return false;
	}




	
}
