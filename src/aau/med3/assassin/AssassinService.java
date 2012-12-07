package aau.med3.assassin;

import java.util.Timer;
import java.util.TimerTask;

import aau.med3.assassin.activities.GameActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

// TODO: Use BluetoothScanner instead
public class AssassinService extends Service {
	
	private final static String TAG = "ASSASSIN_SERVICE";
	private final static Integer REQUEST_ENABLE_BT = 1;
	private BluetoothAdapter bta;
	private Timer timer;
	private final Integer INTERVAL = 20;
	private final Integer notID = 12;
	
	// BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver receiver = new BroadcastReceiver() {				
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, action + " broadcast received");
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String name = device.getName();
				String MAC = device.getAddress().replace(':', '-');
				User user = Globals.user;
				Log.d(TAG, "target: " + user.target_MAC + ", device: " + MAC);
				if(user.target_MAC.equals(MAC)){
//					user.kill(MAC);
					createNotification();
					Log.d(TAG, "Target found!");
				}
				Log.d(TAG, "Device found: " + name + ", MAC: " + MAC);
			}
			
		}
	};
	
	private TimerTask updateTask = new TimerTask(){
		@Override
		public void run(){
			if(bta != null){
				bta.startDiscovery();
			}
			
			Log.d(TAG, "timer doing work");
		}
	};
	
	private void createNotification(){
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle("Target detected!")
		.setContentText("Your target was detected in the area!");
		
		Intent resultIntent = new Intent(this, GameActivity.class);
		
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
		if(bta != null){
			if(!bta.isEnabled()){
				Log.d(TAG, "Bluetooth not enabled, trying to enable");
				bta.enable();
			}
			
			
		} else {
			Log.d(TAG, "No bluetooth adapter on device!");
		}
		

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(receiver, filter);
		
		timer = new Timer("UpdateTimer");
		
		Log.d(TAG, "AssassinService created!");
	}
	
	public void startScanning(){
		timer.schedule(updateTask, 100L, INTERVAL * 1000L);
	}
	
	public void stopScanning(){
		timer.cancel();
		timer.purge();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		timer.cancel();
		timer = null;
		unregisterReceiver(receiver);
		if(bta != null){
			bta.disable();
		}
		if(Globals.user != null){
			Globals.user.save();
		}
		Log.d(TAG, "AssassinService destroyed!");
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
