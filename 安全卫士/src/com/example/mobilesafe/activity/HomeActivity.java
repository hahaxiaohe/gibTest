package com.example.mobilesafe.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.example.mobilesafe.R;

public class HomeActivity extends Activity {

	private GridView gv_home;
	private String[] titles;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		gv_home = (GridView) findViewById(R.id.gv_home);
		
		SimpleAdapter simpleAdapter = new SimpleAdapter(this, getData(), 
				R.layout.home_gridview_item, new String[]{"title","imgId"}, 
				new int[]{R.id.tv_home_title,R.id.iv_home_image});
		gv_home.setAdapter(simpleAdapter);
		//为九宫格菜单设置点击事件
		gv_home.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 8://点击设置中心,跳转设置页面activity
					startActivity(new Intent(HomeActivity.this,SettingActivity.class));
					
					break;

				default:
					break;
				}
			}
			
		});
	}

	protected int getPosition(String title) {
		for(int i = 0 ; i < titles.length ; i ++)
		{
			if(title.equals(titles[i]))
			{
				return i ;
			}
		}
		return -1;
	}

	private List<Map<String, Object>> getData() {
		titles = new String[]{"手机防盗","通讯卫士","软件管理","进程管理",
				"流量统计","手机杀毒","缓存清理","高级工具","设置中心"};
		int[]imgIds = new int[]{R.drawable.home_safe,R.drawable.home_callmsgsafe,
				R.drawable.home_apps,R.drawable.home_taskmanager,R.drawable.home_netmanager,
				R.drawable.home_trojan,R.drawable.home_sysoptimize,R.drawable.home_tools,
				R.drawable.home_settings};
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		for(int i = 0 ;i <titles.length ; i ++)
		{
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("title", titles[i]);
			map.put("imgId", imgIds[i]);
			list.add(map);
		}
		
		return list;
	}

}
