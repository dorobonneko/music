package com.moe.fragment;
import android.view.View;
import android.os.Bundle;

public abstract class Fragment extends android.app.Fragment
{

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		view.setOnClickListener(null);
		super.onViewCreated(view, savedInstanceState);
	}
	
	public abstract boolean onBackPressed();
}
