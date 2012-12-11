package aau.med3.assassin;

import java.util.Timer;
import java.util.TimerTask;

import aau.med3.assassin.activities.GameActivity;
import aau.med3.assassin.events.Event;
import aau.med3.assassin.events.EventDispatcher;
import aau.med3.assassin.events.EventHandler;
import aau.med3.assassin.events.EventListener;
import aau.med3.assassin.events.IEventDispatcher;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;


public class AssassinService extends Service implements IEventDispatcher {
	
	private final static String TAG = "ASSASSIN_SERVICE";
	private final static Integer REQUEST_ENABLE_BT = 1;
	
	public BluetoothScanner scanner;
	public BluetoothAdapter bta;
	private EventDispatcher dispatcher = new EventDispatcher();
	
	private NotificationManager notMan;
	
	private Timer timer;
	private final Integer INTERVAL = 20;
	private final static Integer NOTIFICATION_ID = 12;
	public Boolean running = false;
		
	private class UpdateTask extends TimerTask {
		@Override
		public void run(){
			if(!isConnected()){
				
				if(running){
					
					Log.d(TAG, "connectivity disabled");
					running = false;
					dispatchEvent(Event.STATE_CHANGED, false);
				}
				
			}
			else {
				if(running == false){
					notMan.cancel(NOTIFICATION_ID);
					Log.d(TAG, "connectivity enabled");
					running = true;
					dispatchEvent(Event.STATE_CHANGED, true);
				}
				if(scanner != null){
					if(!scanner.isScanning())
						scanner.scan();
				}
				
				Log.d(TAG, "timer doing work");
			}
		}
	};
	
	private void createNotification(String title, String content, Intent resultIntent){
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
		//.setDefaults(Notification.DEFAULT_VIBRATE)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle(title)
		.setContentText(content);
		
		
		
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(GameActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);
		
		
		notMan.notify(NOTIFICATION_ID, builder.build());
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
				
		bta = BluetoothAdapter.getDefaultAdapter();
		notMan = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		scanner = new BluetoothScanner(this, bta);
		scanner.onDeviceFound = new DeviceFoundHandler();
		
		
		Globals.assassinService = this;
		
		startScanning();
		Log.d(TAG, "AssassinService created!");
	}
	
	public Boolean isConnected(){
		StateMachine state = new StateMachine(this);
		if(state.isBTEnabled() == false || state.isBTDiscoverable() == false){
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
			createNotification("Enable Bluetooth", "Please enable bluetooth to continue", intent);
			return false;
		}
			
		
		if(!state.isNetworkConnected()){
			Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
			createNotification("Enable Connectivity", "Please enable internet connectivity to continue", intent);
			return false;
		}		
		
		return true;
	}

	
	public void startScanning(){
		
		timer = new Timer("UpdateTimer");
		timer.schedule(new UpdateTask(), 100L, INTERVAL * 1000L);
		Log.d(TAG, "Service is scanning");
		scanner.register();
		
	}
	
	public void stopScanning(){
		if(timer != null){
			timer.cancel();
			timer.purge();
			timer = null;
		}
		scanner.stopScan();
		scanner.unregister();
		Log.d(TAG, "Service scanning paused");
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		timer.cancel();
		timer = null;
		
		if(scanner != null){
			if(scanner.isScanning()){
				scanner.stopScan();
			}
			scanner.getAdapter().disable();
			scanner.unregister();
			scanner = null;
		}
		if(Globals.user != null){
			Globals.user.save();
		}
		Log.d(TAG, "AssassinService destroyed!");
	}
	
	private class DeviceFoundHandler implements EventHandler<BluetoothDevice>{

		@Override
		public void onEvent(BluetoothDevice device) {
			
			String deviceMAC = device.getAddress();
			
			Log.d(TAG, "target: " + Globals.user.target_MAC + ", device: " + deviceMAC);
			if(deviceMAC.equals(Globals.user.target_MAC)){
				createNotification("Target detected!", "Your target was detected in the area!", new Intent(AssassinService.this, GameActivity.class));
				Log.d(TAG, "Target found!");
//				scanner.stopScan();
			}
			
		}
		
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addEventListener(String name, EventListener listener) {
		dispatcher.addEventListener(name, listener);
		
	}

	@Override
	public void dispatchEvent(String name, Object data) {
		dispatcher.dispatchEvent(name, data);
		
	}

	@Override
	public void removeEventListeners(String name) {
		dispatcher.removeEventListeners(name);
		
	}

	@Override
	public void removeEventListener(String name, EventListener listenerAddress) {
		dispatcher.removeEventListener(name, listenerAddress);
		
	}

	@Override
	public Boolean hasEventListener(String name, EventListener listener) {
		return dispatcher.hasEventListener(name, listener);
	}

}
