package com.kibeacon.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class KibeaconMainActivity extends Activity {
	
	private TextView tv_app_version;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tv_app_version = (TextView) findViewById(R.id.tv_app_version);
		tv_app_version.setText(getVersionName(KibeaconMainActivity.this));
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				startActivity();
				finish();
			}
		}, 2000);
	}
	
	private void startActivity(){
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.setClass(KibeaconMainActivity.this , KibeaconScanActivity.class);
		startActivity(intent);
	}
	
	public static String getVersionName(Context context) {
		try {
			PackageInfo pi= context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pi.versionName;
		} catch (NameNotFoundException e) {
			return null;
		}
	}
}
