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
				AlertUser("��ȡ�°汾����!");
				goHome();
				break;
			case NO_NEW_VERSION:
				AlertUser("Ŀǰʹ�õ������°汾!");
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
		tv_versionName.setText("�汾��: " + versionName);
		checkVerson();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		goHome();
	}

	/**
	 * ���������Ƿ�����°汾,��ʾ�û�����http://10.0.2.2:8080/360/update.json
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	private void checkVerson() {
		final long startTime = System.currentTimeMillis();
		// �������߳��첽��������
		new Thread() {

			@Override
			public void run() {
				Message msg = Message.obtain();
				HttpURLConnection conn = null;
				try {
					// ������ַ��localhost, ���������ģ�������ر����ĵ�ַʱ,������ip(10.0.2.2)���滻
					URL url = new URL("http://10.0.2.2:8080/360/update.json");
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");// �������󷽷�
					conn.setConnectTimeout(5000);// �������ӳ�ʱ
					conn.setReadTimeout(5000);// ������Ӧ��ʱ, ��������,���������ٳٲ�����Ӧ
					conn.connect();// ���ӷ�����

					int responseCode = conn.getResponseCode();// ��ȡ��Ӧ��
					if (responseCode == 200) {
						InputStream inputStream = conn.getInputStream();
						String result = StringUtils.input2String(inputStream);
						// System.out.println("���緵��:" + result);

						// ����json
						JSONObject jo = new JSONObject(result);
						mVersionName = jo.getString("versionName");
						mVersionCode = jo.getInt("versionCode");
						mdescription = jo.getString("description");
						mDownloadUrl = jo.getString("downloadUrl");
						// System.out.println("�汾����:" + mDesc);

						if (mVersionCode > getVersionCode()) {// �ж��Ƿ��и���
							// ��������VersionCode���ڱ��ص�VersionCode
							// ˵���и���, ���������Ի���
							msg.what = NEW_VERSION_UPDATE;
						} else {
							// û�а汾����
							msg.what = NO_NEW_VERSION;
						}
					}
				} catch (MalformedURLException e) {
					// url������쳣
					msg.what = CODE_URL_ERROR;
					e.printStackTrace();
				} catch (IOException e) {
					// ��������쳣
					msg.what = CODE_NET_ERROR;
					e.printStackTrace();
				} catch (JSONException e) {
					// json����ʧ��
					msg.what = CODE_JSON_ERROR;
					e.printStackTrace();
				} finally {
					long endTime = System.currentTimeMillis();
					long timeUsed = endTime - startTime;// �������绨�ѵ�ʱ��
					if (timeUsed < 2000) {
						// ǿ������һ��ʱ��,��֤����ҳչʾ2����
						try {
							Thread.sleep(2000 - timeUsed);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					mHandler.sendMessage(msg);
					if (conn != null) {
						conn.disconnect();// �ر���������
					}
				}
			}
		}.start();
	}

	/**
	 * ������°汾,������ʾ��,��ʾ�û�����
	 */
	private void alertUpdate() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("���°汾��!!!");
		builder.setMessage(mdescription);
		builder.setPositiveButton("��������", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub ����
				downloadNewVersion();
			}
		});
		builder.setNegativeButton("�Ժ���˵", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub ������
				goHome();
			}
		});
		builder.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub ��ȡ����
				goHome();
			}
		});
		builder.show();
	}

	/**
	 * �����°汾�İ�װ��mobile360_3.apk "http://10.0.2.2:8080/360/mobile360_3.apk"
	 */
	protected void downloadNewVersion() {

		//�ж�sdcard�Ƿ�����
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
					tv_progress.setText("���ؽ��� : " + (current * 100) / total);
				}

				@Override
				public void onFailure(HttpException arg0, String arg1) {
					AlertUser("����ʧ��");
					goHome();
				}

				@Override
				public void onSuccess(ResponseInfo<File> arg0) {
					AlertUser("���سɹ�" + target);
					File file = arg0.result;
					Uri uri = Uri.fromFile(file);
					installApk(uri);
				}
			});
			//��ʾ�Ҳ���sdcard
		} else {
			AlertUser("û���ҵ�sd��");
			goHome();
		}
	}

	/**
	 * ��ת��װactivity ��װ�°汾��apk
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
	 * ��ʼ���ؼ�
	 */
	public void initView() {
		tv_versionName = (TextView) findViewById(R.id.tv_versionName);
		tv_progress = (TextView) findViewById(R.id.tv_progress);
	}

	/**
	 * ��ȡ�����ǰ��versionName
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
	 * ��ȡ�����ǰ��versionCode
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
	 * ��ʾ�û�
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
