package aau.med3.assassin;

import aau.med3.assassin.events.BluetoothEvent;
import aau.med3.assassin.events.EventDispatcher;
import aau.med3.assassin.events.NetworkEvent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class FeatureDependencies extends EventDispatcher {
	
	public class Feature {
		public Boolean enabled;
		public Feature(){
			enabled = false;
		}
	}
	
	public class BluetoothFeature extends Feature {
		public Feature discoverable;
				
	}
	
	public Feature networkAccess = new Feature();
	public BluetoothFeature bluetooth = new BluetoothFeature();
	private Context ctx;
	
	private BroadcastReceiver receiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context ctx, Intent intent) {
			String action = intent.getAction();
			
			if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, 0);
				if(state == BluetoothAdapter.STATE_CONNECTED){
					bluetooth.enabled = true;
					dispatchEvent(BluetoothEvent.ENABLED, null);
				} else {
					bluetooth.enabled = false;
					dispatchEvent(BluetoothEvent.DISABLED, null);
				}
			}
			
			else if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
				int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0);
				if(scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
					bluetooth.discoverable.enabled = true;
					dispatchEvent(BluetoothEvent.DISCOVERABLE_ENABLED, null);
				} else {
					bluetooth.discoverable.enabled = false;
					dispatchEvent(BluetoothEvent.DISCOVERABLE_DISABLED, null);
				}
			}
			
			else if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
				Boolean noConnection = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, true);
				if(noConnection == true){
					networkAccess.enabled = false;
					dispatchEvent(NetworkEvent.CONNECTIVITY_DISABLED, null);
				} else {
					networkAccess.enabled = true;
					dispatchEvent(NetworkEvent.CONNECTIVITY_ENABLED, null);
				}
			}
			
		}
		
	};
	
	public FeatureDependencies(Context ctx){
		this.ctx = ctx;
		
		// Network access
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if(activeNetwork != null){
			if(activeNetwork.isConnected())
				networkAccess.enabled = true;
			else
				networkAccess.enabled = false;
		} else networkAccess.enabled = false;
		
		// Bluetooth access
		BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
		if(bta != null){
			if(bta.isEnabled()){
				bluetooth.enabled = true;
	
				if(bta.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
					bluetooth.discoverable.enabled = true;
				}
			}
		} else bluetooth.enabled = false;
			
			
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		ctx.registerReceiver(receiver, filter);
	}
	
	public Boolean isAllEnabled(){
		if(networkAccess.enabled && bluetooth.enabled && bluetooth.discoverable.enabled){
			return true;
		} else return false;
	}
}
