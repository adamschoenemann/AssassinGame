package aau.med3.assassin;

import aau.med3.assassin.events.BluetoothEvent;
import aau.med3.assassin.events.EventDispatcher;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class BluetoothClient extends EventDispatcher {
	
	private final static String TAG = "BluetoothClient";
	
	private BluetoothAdapter bta;
	private Context ctx;
	private Boolean registered = false;
	private Boolean canceled = false;
	
	
	private final BroadcastReceiver receiver = new BroadcastReceiver() {				
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, action + " broadcast received");
			
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				dispatchEvent(BluetoothEvent.DEVICE_FOUND, device);
				
			}
			
			if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
				dispatchEvent(BluetoothEvent.DISCOVERY_STARTED, 1);
				
			}
			
			if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				if(canceled){
					dispatchEvent(BluetoothEvent.DISCOVERY_CANCELED, 1);
					canceled = false;
				}
				dispatchEvent(BluetoothEvent.DISCOVERY_FINISHED, 1);
				
			}
			
			if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				dispatchEvent(BluetoothEvent.DEVICE_DISCONNECTED, device);
			}
			
		}
	};

		
	public BluetoothClient(Context ctx, BluetoothAdapter bta){
		this.ctx = ctx;
		this.bta = bta;
	}
	
	public void register(){
		if(registered == false){
			IntentFilter filter = new IntentFilter();
			filter.addAction(BluetoothDevice.ACTION_FOUND);
			filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
			filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
			ctx.registerReceiver(receiver, filter);
			registered = true;
		}
	}
	
	public void unregister(){
		if(registered == true){
			try{
				ctx.unregisterReceiver(receiver);
				registered = false;
			} catch (IllegalArgumentException e){
				e.printStackTrace();
			}
		}
		
	}
	
	public Boolean scan(){
		return bta.startDiscovery();
	}
	
	public Boolean cancel(){
		canceled = true;
		return bta.cancelDiscovery();
	}
	
	public Boolean isScanning(){
		return bta.isDiscovering();
	}
	
	public Boolean isRegistered(){
		return registered;
	}
	
}
