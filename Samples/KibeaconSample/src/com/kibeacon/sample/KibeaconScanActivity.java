package com.kibeacon.sample;

import java.util.ArrayList;

import com.kibeacon.sdk.IKibeaconMonitoringListener;
import com.kibeacon.sdk.KibeaconManager;
import com.kibeacon.sdk.device.Kibeacon;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class KibeaconScanActivity extends Activity implements OnItemClickListener, OnClickListener {
	
	private final int REQUEST_ENABLE_BT = 1000;
	
	private final int FINISH_MSG = 0;
	
	// Stops scanning after 10 seconds.
//    private static final long SCAN_PERIOD = 10000;
	
	private KibeaconManager mKibeaconManager;
	private KibeaconDeviceListAdapter mKibeaconDeviceListAdapter;
	private KibeaconConnectLayout mKibeaconConnectLayout;
	private Kibeacon mSelectedBeacon;
	private Handler mHandler;
	
	private ListView lv_device_list;
	private ImageButton ib_refresh;
	
	private boolean isFinish = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_scan);
		Log.d("KibeaconScanActivity", "onCreate");
		
		mKibeaconManager = KibeaconManager.getKibeaconManager(KibeaconScanActivity.this);
		
		// Use this check to determine whether BLE is supported on the device.
        // Then you can selectively disable BLE-related features.
		if (!mKibeaconManager.hasBluetoothLE()) {
			Toast.makeText(this, "Device does not have Bluetooth Low Energy", Toast.LENGTH_LONG).show();
			finish();
		}
		
		// Initializes list view adapter.
        mKibeaconDeviceListAdapter = new KibeaconDeviceListAdapter();
		lv_device_list = (ListView) findViewById(R.id.lv_device_list);
        lv_device_list.setAdapter(mKibeaconDeviceListAdapter);
        lv_device_list.setOnItemClickListener(KibeaconScanActivity.this);
        
        ib_refresh = (ImageButton) findViewById(R.id.ib_refresh);
        ib_refresh.setOnClickListener(KibeaconScanActivity.this);
        
		mHandler = new Handler(){
			public void handleMessage(android.os.Message msg) {
				switch(msg.what){
				case FINISH_MSG:
					isFinish = false;
					break;
				}
			}
		};
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		// Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
		if (!mKibeaconManager.isBluetoothEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			mKibeaconDeviceListAdapter.clear();
			scanLeDevice(true);
		}
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mKibeaconDeviceListAdapter.clear();
	}
	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if(mKibeaconConnectLayout != null && mKibeaconConnectLayout.isShown())
			mKibeaconConnectLayout.hideLayout();
		
		if(mKibeaconManager != null && mKibeaconManager.isStarted())
			mKibeaconManager.destroy();
	}
	
	
	@Override
	public void onBackPressed() {
		if(!isFinish){
			Toast.makeText(getApplicationContext(), R.string.back_message, Toast.LENGTH_SHORT).show();
			isFinish = true;
			mHandler.sendEmptyMessageDelayed(FINISH_MSG, 2000);
		}else{
			super.onBackPressed();
			finish();
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
			finish();
            return;
        }
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.ib_refresh:
			scanLeDevice(false);
			mKibeaconDeviceListAdapter.clear();
			scanLeDevice(true);
			break;
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		mSelectedBeacon = mKibeaconDeviceListAdapter.getDevice(position);
		if (mSelectedBeacon == null) return;
		
		openKibeaconConnectLayout(mSelectedBeacon);
		
	}
	
	public void scanLeDevice(boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
////                	setLoading(false);
//                    mKibeaconManager.stopMonitoring();
//                }
//            }, SCAN_PERIOD);
            
//            setLoading(true);
            mKibeaconManager.startMonitoring(mKibeaconMonitoringListener);
        } else {
//        	setLoading(false);
        	mKibeaconManager.stopMonitoring();
        }
    }
	
	public void openKibeaconConnectLayout(Kibeacon beacon){
		if(mKibeaconConnectLayout == null){
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			mKibeaconConnectLayout = new KibeaconConnectLayout(KibeaconScanActivity.this);
			addContentView(mKibeaconConnectLayout, layoutParams);
		}
		
		mKibeaconConnectLayout.showLayout(beacon);
		mKibeaconConnectLayout.bringToFront();
	}
	
	// Adapter for holding devices found through scanning.
    private class KibeaconDeviceListAdapter extends BaseAdapter {
        private ArrayList<Kibeacon> mKibeaconDevices;
        private LayoutInflater mInflator;

        public KibeaconDeviceListAdapter() {
            super();
            mKibeaconDevices = new ArrayList<Kibeacon>();
            mInflator = KibeaconScanActivity.this.getLayoutInflater();
        }

        public void addDevice(Kibeacon device) {
        	boolean isIncluded = false;
        	for (int i = 0; i < mKibeaconDevices.size(); i++) {
    			if (mKibeaconDevices.get(i) != null && mKibeaconDevices.get(i).getMacAddress().equals(device.getMacAddress())) {
    				mKibeaconDevices.set(i, device);
    				isIncluded = true;
    				break;
    			}
    		}
    		if(!isIncluded)
    			mKibeaconDevices.add(device);
        }

        public Kibeacon getDevice(int position) {
            return mKibeaconDevices.get(position);
        }

        public void clear() {
            mKibeaconDevices.clear();
        }

        @Override
        public int getCount() {
            return mKibeaconDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mKibeaconDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }
        
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.list_device_item, null);
                viewHolder = new ViewHolder();
                viewHolder.tv_address = (TextView) view.findViewById(R.id.tv_address);
                viewHolder.tv_name = (TextView) view.findViewById(R.id.tv_name);
                viewHolder.tv_uuid = (TextView) view.findViewById(R.id.tv_uuid);
                viewHolder.tv_battery = (TextView) view.findViewById(R.id.tv_battery);
                viewHolder.tv_rssi = (TextView) view.findViewById(R.id.tv_rssi);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            Kibeacon device = mKibeaconDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.tv_name.setText(deviceName);
            else
                viewHolder.tv_name.setText(R.string.unknown_device);
            viewHolder.tv_address.setText("Address : "+device.getMacAddress());
            viewHolder.tv_uuid.setText("UUID : "+device.getProximityUUID());
            viewHolder.tv_battery.setText("Battery Level : "+device.getBatteryPercent()+"%");
            viewHolder.tv_rssi.setText(""+device.getRssi());
            
            return view;
        }
    }
    
    static class ViewHolder {
        TextView tv_name;
        TextView tv_address;
        TextView tv_uuid;
        TextView tv_battery;
        TextView tv_rssi;
    }

	private IKibeaconMonitoringListener mKibeaconMonitoringListener = new IKibeaconMonitoringListener() {
		
		@Override
		public void onScanCallback(final Kibeacon device) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(mKibeaconConnectLayout == null || !mKibeaconConnectLayout.isShown()){
						mKibeaconDeviceListAdapter.addDevice(device);
						mKibeaconDeviceListAdapter.notifyDataSetChanged();
					}else{
						if(mSelectedBeacon != null && mSelectedBeacon.getMacAddress().equals(device.getMacAddress()))
							mKibeaconConnectLayout.setProximityInfo(device);
					}
				}
			});
		}
	};
	
}
