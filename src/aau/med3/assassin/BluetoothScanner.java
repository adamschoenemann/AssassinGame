package aau.med3.assassin;

import aau.med3.assassin.activities.GameActivity;
import aau.med3.assassin.events.BluetoothEvent;
import aau.med3.assassin.events.EventDispatcher;
import aau.med3.assassin.events.EventHandler;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

// TODO: Change from eventhandler system to proper eventlistener
public class BluetoothScanner extends EventDispatcher {
	
	
	
	// Legacy
	public EventHandler<Integer> onScanFinished;
	public EventHandler<Integer> onScanStarted;
	public EventHandler<BluetoothDevice> onDeviceFound;
	public EventHandler<BluetoothDevice> onDeviceDisconnected;
	
	private static final String TAG = "BLUETOOTH_SCANNER";
	private BluetoothAdapter bta;
	private Boolean registered = false;
	private Context ctx;
	
	private final BroadcastReceiver receiver = new BroadcastReceiver() {				
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, action + " broadcast received");
			
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				dispatchEvent(BluetoothEvent.DEVICE_FOUND, device);
				if(onDeviceFound != null) onDeviceFound.onEvent(device);
			}
			
			if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
				dispatchEvent(BluetoothEvent.DISCOVERY_STARTED, 1);
				if(onScanStarted != null) onScanStarted.onEvent(1);
			}
			
			if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				dispatchEvent(BluetoothEvent.DISCOVERY_FINISHED, 1);
				if(onScanFinished != null) onScanFinished.onEvent(1);
			}
			
			if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				dispatchEvent(BluetoothEvent.DEVICE_DISCONNECTED, device);
				if(onDeviceDisconnected != null) onDeviceDisconnected.onEvent(device);
			}
			
		}
	};
	
	public BluetoothScanner(Context ctx, BluetoothAdapter bta){
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
	
	public boolean isScanning(){
		return bta.isDiscovering();
	}
	
	public BluetoothAdapter getAdapter(){
		return bta;
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
	
	public Boolean isRegistered(){
		return registered;
	}


}
