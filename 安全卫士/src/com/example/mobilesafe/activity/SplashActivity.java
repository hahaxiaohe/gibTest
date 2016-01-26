package com.example.mobilesafe.activity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilesafe.R;
import com.example.mobilesafe.utils.StringUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

public class SplashActivity extends Activity {

	private String mVersionName;
	private int mVersionCode;
	private String mdescription;
	private String mDownloadUrl;
	private int mResponseCode;
	private static final String TAG = "SplashActivity";
	protected static final int NEW_VERSION_UPDATE = 0;
	protected static final int CHECKUPDATE_ERROR = 1;
	protected static final int NO_NEW_VERSION = 2;
	protected static final int CODE_URL_ERROR = 3;
	protected static final int CODE_NET_ERROR = 4;
	protected static final int CODE_JSON_ERROR = 5;
	private static final int INSTALLCODE = 6;
	private static final String AUTO_UPDATE = "auto_update";
	private static final int NO_CHECK_UPDATE = 7;
	private TextView tv_versionName;
	private String versionName;
	private int versionCode;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case NEW_VERSION_UPDATE:
				alertUpdate();
				break;
			case CODE_URL_ERROR:
			case CODE_NET_ERROR:
			case CODE_JSON_ERROR:
				AlertUser("获取新版本出错!");
				goHome();
				break;
			case NO_NEW_VERSION:
				AlertUser("目前使用的是最新版本!");
				goHome();
			case NO_CHECK_UPDATE:
				goHome();
				break;
			default:
				break;
			}
		};
	};
	private TextView tv_progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		initView();
		String versionName = getVersionName();
		tv_versionName.setText("版本号: " + versionName);
		//获取sharePrefernces文件
		SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
		//查看用户上一次的选择,是否选择自动更新
		boolean lastOpt = sp.getBoolean(AUTO_UPDATE, true);
		//如果选择自动更新 则联网查找是否有新版本
		if(lastOpt)
		{
			checkVerson();
		}
		//如果选择不自动更新, 则延时2秒发送消息,跳转主页面
		else
		{
			mHandler.sendEmptyMessageDelayed(NO_CHECK_UPDATE, 2000);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		goHome();
	}

	/**
	 * 检查服务器是否存在新版本,提示用户更新http://10.0.2.2:8080/360/update.json
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	private void checkVerson() {
		final long startTime = System.currentTimeMillis();
		// 启动子线程异步加载数据
		new Thread() {

			@Override
			public void run() {
				Message msg = Message.obtain();
				HttpURLConnection conn = null;
				try {
					// 本机地址用localhost, 但是如果用模拟器加载本机的地址时,可以用ip(10.0.2.2)来替换
					URL url = new URL("http://10.0.2.2:8080/360/update.json");
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");// 设置请求方法
					conn.setConnectTimeout(5000);// 设置连接超时
					conn.setReadTimeout(5000);// 设置响应超时, 连接上了,但服务器迟迟不给响应
					conn.connect();// 连接服务器

					int responseCode = conn.getResponseCode();// 获取响应码
					if (responseCode == 200) {
						InputStream inputStream = conn.getInputStream();
						String result = StringUtils.input2String(inputStream);
						// System.out.println("网络返回:" + result);

						// 解析json
						JSONObject jo = new JSONObject(result);
						mVersionName = jo.getString("versionName");
						mVersionCode = jo.getInt("versionCode");
						mdescription = jo.getString("description");
						mDownloadUrl = jo.getString("downloadUrl");
						// System.out.println("版本描述:" + mDesc);

						if (mVersionCode > getVersionCode()) {// 判断是否有更新
							// 服务器的VersionCode大于本地的VersionCode
							// 说明有更新, 弹出升级对话框
							msg.what = NEW_VERSION_UPDATE;
						} else {
							// 没有版本更新
							msg.what = NO_NEW_VERSION;
						}
					}
				} catch (MalformedURLException e) {
					// url错误的异常
					msg.what = CODE_URL_ERROR;
					e.printStackTrace();
				} catch (IOException e) {
					// 网络错误异常
					msg.what = CODE_NET_ERROR;
					e.printStackTrace();
				} catch (JSONException e) {
					// json解析失败
					msg.what = CODE_JSON_ERROR;
					e.printStackTrace();
				} finally {
					long endTime = System.currentTimeMillis();
					long timeUsed = endTime - startTime;// 访问网络花费的时间
					if (timeUsed < 2000) {
						// 强制休眠一段时间,保证闪屏页展示2秒钟
						try {
							Thread.sleep(2000 - timeUsed);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					mHandler.sendMessage(msg);
					if (conn != null) {
						conn.disconnect();// 关闭网络连接
					}
				}
			}
		}.start();
	}

	/**
	 * 如果有新版本,弹出提示框,提示用户更新
	 */
	private void alertUpdate() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("有新版本啦!!!");
		builder.setMessage(mdescription);
		builder.setPositiveButton("现在升级", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub 升级
				downloadNewVersion();
			}
		});
		builder.setNegativeButton("以后再说", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub 不升级
				goHome();
			}
		});
		builder.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub 按取消键
				goHome();
			}
		});
		builder.show();
	}

	/**
	 * 下载新版本的安装包mobile360_3.apk "http://10.0.2.2:8080/360/mobile360_3.apk"
	 */
	protected void downloadNewVersion() {

		//判断sdcard是否正常
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			HttpUtils http = new HttpUtils();
			final String target = Environment.getExternalStorageDirectory()
					+ "/update.apk";
			http.download(mDownloadUrl, target, new RequestCallBack<File>() {

				@Override
				public void onLoading(long total, long current,
						boolean isUploading) {
					super.onLoading(total, current, isUploading);
					tv_progress.setVisibility(View.VISIBLE);
					tv_progress.setText("下载进度 : " + (current * 100) / total);
				}

				@Override
				public void onFailure(HttpException arg0, String arg1) {
					AlertUser("下载失败");
					goHome();
				}

				@Override
				public void onSuccess(ResponseInfo<File> arg0) {
					AlertUser("下载成功" + target);
					File file = arg0.result;
					Uri uri = Uri.fromFile(file);
					installApk(uri);
				}
			});
			//提示找不到sdcard
		} else {
			AlertUser("没有找到sd卡");
			goHome();
		}
	}

	/**
	 * 跳转安装activity 安装新版本的apk
	 * 
	 * @param uri
	 */
	protected void installApk(Uri uri) {

		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		startActivityForResult(intent, INSTALLCODE);
	}

	/**
	 * 初始化控件
	 */
	public void initView() {
		tv_versionName = (TextView) findViewById(R.id.tv_versionName);
		tv_progress = (TextView) findViewById(R.id.tv_progress);
	}

	/**
	 * 获取软件当前的versionName
	 * 
	 * @return
	 */
	public String getVersionName() {
		PackageManager packageManager = getPackageManager();
		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(
					getPackageName(), 1);
			versionCode = packageInfo.versionCode;
			versionName = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			Log.w(TAG, e.toString());
		}
		return versionName;

	}

	/**
	 * 获取软件当前的versionCode
	 * 
	 * @return
	 */
	public int getVersionCode() {
		PackageManager packageManager = getPackageManager();
		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(
					getPackageName(), 1);
			int versionCode = packageInfo.versionCode;
			versionName = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			Log.w(TAG, e.toString());
		}
		return versionCode;

	}

	/**
	 * 提示用户
	 * 
	 * @param text
	 */
	public void AlertUser(final String text) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(SplashActivity.this, text, Toast.LENGTH_LONG)
						.show();
			}
		});
	}

	public void goHome() {
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		finish();
	}
}

