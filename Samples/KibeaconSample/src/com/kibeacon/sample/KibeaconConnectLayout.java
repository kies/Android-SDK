package com.kibeacon.sample;

import com.kibeacon.sdk.device.Kibeacon;
import com.kibeacon.sdk.gatt.KibeaconGatt;
import com.kibeacon.sdk.gatt.KibeaconGattCharacteristic;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class KibeaconConnectLayout extends LinearLayout implements OnClickListener {
	
	private static final String TAG = KibeaconConnectLayout.class.getSimpleName();
	
	private static final int State_Connect = 0;
	private static final int State_Disconnect = 1;
	
	private ImageButton ib_back;
	private Button bt_connect;
	private TextView tv_name, tv_address, tv_uuid, tv_major, tv_minor, tv_battery;
	private TextView tv_rssi, tv_measured_power, tv_distance, tv_proximity;
	private TextView tv_state, tv_advertising_interval, tv_txpower;
	private TextView tv_manufactured_name, tv_hw_version, tv_sw_version;
	private TableLayout tl_proximity_info, tl_setting_info, tl_device_info;
	private ProgressBar progressBar;

	private KibeaconScanActivity mActivity;
	private Kibeacon mBeacons;
	private KibeaconGatt mKibeaconGatt;
	
	private KibeaconGatt.ConnectionCallback mConnectionCallback = new KibeaconGatt.ConnectionCallback() {
		
		@Override
		public void onConnected(
				final KibeaconGattCharacteristic characteristics) {
			// TODO Auto-generated method stub
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG ,"GATT BLE is connected.");
					setLoading(false);
					changeUI(State_Connect);
					
					if(characteristics != null){
						setSettingInfo(characteristics);
						setDeviceInfo(characteristics);
					}
				}
			});
		}
		
		@Override
		public void onError() {
			// TODO Auto-generated method stub
			Log.e(TAG ,"GATT BLE is connected.");
			Toast.makeText(getApplicationContext(), "Device is already connected.", Toast.LENGTH_SHORT).show();
			
			setLoading(false);
		}
		
		@Override
		public void onDisconnected() {
			// TODO Auto-generated method stub
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG ,"GATT BLE is disconnected.");
					mKibeaconGatt.close();
					
					changeUI(State_Disconnect);
					mActivity.scanLeDevice(true);
				}
			});
		}
	};
	
	public KibeaconConnectLayout(KibeaconScanActivity activity) {
		super(activity);
		this.mActivity = activity;
		
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li;
		li = (LayoutInflater)getContext().getSystemService(infService);
		li.inflate(R.layout.layout_device_detail, this, true);
		
		initLayout();
	}
	
	public void initLayout(){
		ib_back = (ImageButton) findViewById(R.id.ib_back);
		ib_back.setOnClickListener(KibeaconConnectLayout.this);
		
		bt_connect = (Button) findViewById(R.id.bt_connect);
		bt_connect.setOnClickListener(KibeaconConnectLayout.this);
		
		//device detail
		tv_name = (TextView) findViewById(R.id.tv_name);
		tv_address = (TextView) findViewById(R.id.tv_address);
		tv_uuid = (TextView) findViewById(R.id.tv_uuid);
		tv_major = (TextView) findViewById(R.id.tv_major);
		tv_minor = (TextView) findViewById(R.id.tv_minor);
		tv_battery = (TextView) findViewById(R.id.tv_battery);
		
		//proximity table
		tl_proximity_info = (TableLayout) findViewById(R.id.tl_proximity_info);
		tv_rssi = (TextView) findViewById(R.id.tv_rssi);
		tv_measured_power = (TextView) findViewById(R.id.tv_measured_power);
		tv_distance = (TextView) findViewById(R.id.tv_distance);
		tv_proximity = (TextView) findViewById(R.id.tv_proximity);
		
		//setting info table
		tl_setting_info = (TableLayout) findViewById(R.id.tl_setting_info);
		tv_state = (TextView) findViewById(R.id.tv_state);
		tv_advertising_interval = (TextView) findViewById(R.id.tv_advertising_interval);
		tv_txpower = (TextView) findViewById(R.id.tv_txpower);
		
		//device info table
		tl_device_info = (TableLayout) findViewById(R.id.tl_device_info);
		tv_manufactured_name = (TextView) findViewById(R.id.tv_manufactured_name);
		tv_hw_version = (TextView) findViewById(R.id.tv_hw_version);
		tv_sw_version = (TextView) findViewById(R.id.tv_sw_version);
		
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
	}
	
	public void showLayout(Kibeacon beacon){
		if(beacon == null) return;
		this.mBeacons = beacon;
		this.mKibeaconGatt = new KibeaconGatt(getApplicationContext(), this.mBeacons, this.mConnectionCallback);
		
		changeUI(State_Disconnect);
		setDeviceDetail(beacon);
		
		setVisibility(View.VISIBLE);
	}
	
	public void hideLayout(){
		if (mKibeaconGatt != null && mKibeaconGatt.isConnected()){
			mKibeaconGatt.disconnect();
		}
		
		if(isShown())
			setVisibility(View.GONE);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ib_back:
			hideLayout();
			break;
		case R.id.bt_connect:
			if(mBeacons != null){
				if (mKibeaconGatt.isConnected()){
					Log.d(TAG, "isConnected");
					mKibeaconGatt.disconnect();
				}else{
					mActivity.scanLeDevice(false);
					setLoading(true);
					mKibeaconGatt.connect();
				}
			}
			break;
		}
	}
	
	public void setDeviceDetail(Kibeacon beacon){
		tv_name.setText(beacon.getName());
		tv_address.setText(beacon.getMacAddress());
		tv_uuid.setText(beacon.getProximityUUID());
		tv_major.setText(String.valueOf(beacon.getMajor()));
		tv_minor.setText(String.valueOf(beacon.getMinor()));
		tv_battery.setText(beacon.getBatteryPercent()+"%");
	}
	
	public void setProximityInfo(Kibeacon beacon){
		tv_rssi.setText(beacon.getRssi()+" dBm");
		tv_measured_power.setText(beacon.getMeasuredPower()+" dBm");
		tv_distance.setText(Double.toString(beacon.getDistance())+" m");
		
		switch (beacon.getProximityState()) {
		case Immediate:
			tv_proximity.setTextColor(getApplicationContext().getResources().getColor(R.color.green));
			break;
		case Near:
			tv_proximity.setTextColor(getApplicationContext().getResources().getColor(R.color.orange));
			break;
		case Far:
			tv_proximity.setTextColor(getApplicationContext().getResources().getColor(R.color.red));
			break;

		default:
			tv_proximity.setTextColor(getApplicationContext().getResources().getColor(R.color.black));
			break;
		}
		tv_proximity.setText(beacon.getProximityState().toString());
	}
	
	public void setSettingInfo(KibeaconGattCharacteristic characteristics){
		tv_state.setText("Connected");
		tv_advertising_interval.setText(String.valueOf(characteristics.getAdvertisingInterval())+" ms");
		tv_txpower.setText(String.valueOf(characteristics.getTxPower())+" dBm");
	}
	
	public void setDeviceInfo(KibeaconGattCharacteristic characteristics){
		tv_manufactured_name.setText(characteristics.getManufacturedName());
		tv_hw_version.setText(characteristics.getHardwareVersion());
		tv_sw_version.setText(characteristics.getSoftwareVersion());
	}
	
	private void changeUI(int state){
		switch (state) {
		case State_Connect:
			tl_proximity_info.setVisibility(View.GONE);
			tl_setting_info.setVisibility(View.VISIBLE);
			tl_device_info.setVisibility(View.VISIBLE);
			bt_connect.setText(getApplicationContext().getString(R.string.disconnect));
			break;
		case State_Disconnect:
			tl_proximity_info.setVisibility(View.VISIBLE);
			tl_setting_info.setVisibility(View.GONE);
			tl_device_info.setVisibility(View.GONE);
			bt_connect.setText(getApplicationContext().getString(R.string.connect));
			break;
		}
	}
	
	private void setLoading(boolean enable){
		if(enable){
			progressBar.setVisibility(View.VISIBLE);
		}else{
			progressBar.setVisibility(View.GONE);
		}
	}
	
	private Context getApplicationContext(){
		return mActivity.getApplicationContext();
	}
	
}
