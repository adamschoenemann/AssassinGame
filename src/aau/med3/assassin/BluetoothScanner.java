package aau.med3.assassin;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class BluetoothScanner {
	
	// Events
	public EventListener<Integer> onScanFinished;
	public EventListener<Integer> onScanStarted;
	public EventListener<BluetoothDevice> onDeviceFound;
	public EventListener<BluetoothDevice> onDeviceDisconnected;
	
	private static final String TAG = "BLUETOOTH_SCANNER";
	private BluetoothAdapter bta;
	private Context ctx;
	
	
	public BluetoothScanner(Context ctx, BluetoothAdapter bta){
//		this.ctx = ctx;
		this.bta = bta;
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		ctx.registerReceiver(receiver, filter);
	}
	
	private final BroadcastReceiver receiver = new BroadcastReceiver() {				
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, action + " broadcast received");
			
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if(onDeviceFound != null) onDeviceFound.onEvent(device);
			}
			
			if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
				
				if(onScanStarted != null) onScanStarted.onEvent(1);
			}
			
			if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				if(onScanFinished != null) onScanFinished.onEvent(1);
			}
			
			if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if(onDeviceDisconnected != null) onDeviceDisconnected.onEvent(device);
			}
			
		}
	};
	
	public void scan(){
		if(!bta.isDiscovering()){
			bta.startDiscovery();
		}
	}
	
	public void stopScan(){
		if(bta.isDiscovering()){
			bta.cancelDiscovery();
		}
	}

}
