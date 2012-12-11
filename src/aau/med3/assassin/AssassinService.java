package aau.med3.assassin;

import java.util.Timer;
import java.util.TimerTask;

import aau.med3.assassin.activities.GameActivity;
import aau.med3.assassin.events.EventHandler;
import android.app.Notification;
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


public class AssassinService extends Service {
	
	private final static String TAG = "ASSASSIN_SERVICE";
	private final static Integer REQUEST_ENABLE_BT = 1;
	
	public BluetoothScanner scanner;
	public BluetoothAdapter bta;
	
	private Timer timer;
	private final Integer INTERVAL = 20;
	private final Integer notID = 12;
	
		
	private class UpdateTask extends TimerTask {
		@Override
		public void run(){
			if(!isConnected()){
				Log.d(TAG, "Connectivity not enabled");
				return;
			}
			if(scanner != null){
				if(!scanner.isScanning())
					scanner.scan();
			}
			
			Log.d(TAG, "timer doing work");
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
		
		NotificationManager notMan = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notMan.notify(notID, builder.build());
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
				
		bta = BluetoothAdapter.getDefaultAdapter();

		scanner = new BluetoothScanner(this, bta);
		scanner.onDeviceFound = new DeviceFoundHandler();
		
		
		Globals.assassinService = this;
		
		startScanning();
		Log.d(TAG, "AssassinService created!");
	}
	
	private Boolean isConnected(){
		// Bluetooth
		if(bta.isEnabled() == false || bta.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
			createNotification("Enable Bluetooth", "Please enable bluetooth to continue", intent);
			return false;
		}
		ConnectivityManager cm = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo network = cm.getActiveNetworkInfo();
		if(network == null || network.isConnected() == false){
			Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
			
			createNotification("Enable Connectivity", "Please enable internet connectivity to continue", intent);
			return false;
		} else {
			
			return true;
		}
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

}
