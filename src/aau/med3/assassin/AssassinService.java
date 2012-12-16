package aau.med3.assassin;

import java.util.Timer;
import java.util.TimerTask;

import aau.med3.assassin.activities.DashboardActivity;
import aau.med3.assassin.events.BluetoothEvent;
import aau.med3.assassin.events.Event;
import aau.med3.assassin.events.EventDispatcher;
import aau.med3.assassin.events.EventListener;
import aau.med3.assassin.events.IEventDispatcher;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;


// TODO: Change scan interval to be dependent on when a scan finishes
public class AssassinService extends Service implements IEventDispatcher {
	
	
	private final static String TAG = "ASSASSIN_SERVICE";
	
	public final static String 	RESUMED = "serviceResumed",
									PAUSED = "servicePaused",
									SERVICE_CREATED = "serviceCreated";
	
	public BluetoothClient btClient;

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
				if(btClient != null){
					if(!btClient.isScanning())
						btClient.scan();
				}
				
				Log.d(TAG, "timer doing work");
			}
		}
	};
	
	private void createNotification(String title, String content, Intent resultIntent){
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
		.setDefaults(Notification.DEFAULT_VIBRATE)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle(title)
		.setContentText(content);
		
		
		
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(DashboardActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);
		
		
		notMan.notify(NOTIFICATION_ID, builder.build());
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
				
		
		notMan = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		btClient = new BluetoothClient(this, BluetoothAdapter.getDefaultAdapter());
		
		btClient.addEventListener(BluetoothEvent.DEVICE_FOUND, new DeviceFoundListener());
		btClient.addEventListener(BluetoothEvent.DISCOVERY_FINISHED, new DiscoveryFinishedListener());
		Globals.events.dispatchEvent(SERVICE_CREATED, null);
		Globals.assassinService = this;
		
		startScanning();
		Log.d(TAG, "AssassinService created!");
	}
	
	public Boolean isConnected(){
		StateTracker state = new StateTracker(this);
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
		
		if(Globals.user == null || Globals.user.loggedIn == false){
			return false;
		}
		
		return true;
	}

	
	public void startScanning(){
		
		btClient.register();
		btClient.addEventListener(BluetoothEvent.DISCOVERY_STARTED, new ServiceResumedListener());
		timer = new Timer("UpdateTimer");
		timer.schedule(new UpdateTask(), 100L, INTERVAL * 1000L);
//		timer.schedule(new UpdateTask(), 100L);
		Log.d(TAG, "Service scanning starting...");
		
		
	}
	
	public void pauseScanning(){
		if(timer != null){
			timer.cancel();
			timer.purge();
			timer = null;
		}
		if(btClient.isScanning()){
			btClient.addEventListener(BluetoothEvent.DISCOVERY_CANCELED, new ServicePausedListener());
			btClient.cancel();
		} else {
			dispatchEvent(PAUSED, null);
		}
		
		
		Log.d(TAG, "Service scanning pausing...");
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		timer.cancel();
		timer = null;
		
		if(btClient != null){
			if(btClient.isScanning()){
				btClient.cancel();
			}
			
			btClient.unregister();
			btClient = null;
		}

		Log.d(TAG, "AssassinService destroyed!");
	}
	
	//--------------------------------LISTENERS-----------------------------//
	private class DeviceFoundListener implements EventListener {

		@Override
		public void handle(Event e) {
			
			BluetoothDevice device = (BluetoothDevice) e.data;
			
			Log.d(TAG, "target: " + Globals.user.target_MAC + ", device found: " + device.getAddress());
			
			if(device.getAddress().equals(Globals.user.target_MAC)){
				createNotification("Target detected!", "Your target was detected in the area!", 
						new Intent(AssassinService.this, DashboardActivity.class));
				Log.d(TAG, "Target found!");
//				btClient.cancel();
			}
			
		}
		
	}
	
	private class DiscoveryFinishedListener implements EventListener {

		@Override
		public void handle(Event e) {
			// TODO Auto-generated method stub

		}

	}
	
	private class ServicePausedListener implements EventListener {

		@Override
		public void handle(Event e) {
			Log.d(TAG, "Service paused!");
			btClient.unregister();
			e.target.removeEventListener(BluetoothEvent.DISCOVERY_CANCELED, this);
			dispatchEvent(PAUSED, null);
		}
		
	}
	
	private class ServiceResumedListener implements EventListener {

		@Override
		public void handle(Event e) {
			Log.d(TAG, "Service resumed!");
			e.target.removeEventListener(BluetoothEvent.DISCOVERY_STARTED, this);

			dispatchEvent(RESUMED, null);
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

	@Override
	public void removeAllEventListeners() {
		dispatcher.removeAllEventListeners();
		
	}

}
