package aau.med3.assassin;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AssassinService extends Service {
	
	private final static String TAG = "ASSASSIN_SERVICE";
	private final static Integer REQUEST_ENABLE_BT = 1;
	private BluetoothAdapter bta;
	private Timer timer;
	private TimerTask updateTask = new TimerTask(){
		@Override
		public void run(){
			Log.d(TAG, "timer doing work");
		}
	};
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		timer = new Timer("UpdateTimer");
		timer.schedule(updateTask, 1000L, 60 * 1000L);

		bta = BluetoothAdapter.getDefaultAdapter();
		if(bta != null){
			if(!bta.isEnabled()){
				Log.d(TAG, "Bluetooth not enabled, trying to enable");
				bta.enable();
				
			}
			
			
		} else {
			Log.d(TAG, "No bluetooth adapter on device!");
		}
		
		Log.d(TAG, "AssassinService created!");
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		timer.cancel();
		timer = null;
		Log.d(TAG, "AssassinService destroyed!");
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
