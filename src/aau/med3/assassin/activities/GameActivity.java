package aau.med3.assassin.activities;

import java.util.Timer;
import java.util.TimerTask;

import aau.med3.assassin.BluetoothScanner;
import aau.med3.assassin.EventListener;
import aau.med3.assassin.Globals;
import aau.med3.assassin.R;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

// TODO: Implement distance checking
public class GameActivity extends Activity {
	
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int REQUEST_ENABLE_DISCOVERABILITY = 2;
	
	private Boolean initialScanSuccess,
					finalScanSuccess;
	
	private Boolean killInProgress = false;
	
	private final long KILL_DURATION = 8000;
	private Timer timer;
	private final TimerTask timerTask = new TimerTask(){

		@Override
		public void run() {
			if(killInProgress == true){
				scanner.scan();
			}
			Log.d(Globals.DEBUG, String.valueOf(KILL_DURATION) + " ms passed");
		}
		
	};
	
	private BluetoothAdapter bta;
	private BluetoothScanner scanner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		// Show the Up button in the action bar.
		setupActionBar();

		// Bluetooth setup
		bta = BluetoothAdapter.getDefaultAdapter();
		if(bta != null){

			if(bta.isEnabled() == false){
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
			// Enable discoverability
			if(bta.getState() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
				Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
				startActivityForResult(discoverableIntent, REQUEST_ENABLE_DISCOVERABILITY);
			}
			
			scanner = new BluetoothScanner(this, bta);
			scanner.onDeviceFound = new DeviceFoundHandler();
			scanner.onScanFinished = new ScanFinishedHandler();
			
		} else {
			// Device does not support bluetooth
			Log.d(Globals.DEBUG, "Device does not support bluetooth");
		}

	}
	
	public void btn_kill_onclick(View view){
		initialScanSuccess = false;
		finalScanSuccess = false;
		scanner.scan();
		
	}
	
	private class DeviceFoundHandler implements EventListener<BluetoothDevice> {
		@Override
		public void onEvent(BluetoothDevice device) {
			String deviceMAC = device.getAddress().replace(':', '-');
			if(deviceMAC.equals(Globals.user.target_MAC)){
				if(initialScanSuccess == true && killInProgress == true){
					finalScanSuccess = true;
					scanner.stopScan();
					Globals.user.kill(deviceMAC);
					killSucessful();
				}
				if(initialScanSuccess == false){
					initialScanSuccess = true;
					scanner.stopScan();
					killInProgress = true;
					scanner.onDeviceDisconnected = new DeviceDisconnectedHandler();
					timer = new Timer("KillTimer");
					timer.schedule(timerTask, KILL_DURATION);
				}
			}
			
		}
	}
	
	private class DeviceDisconnectedHandler implements EventListener<BluetoothDevice> {

		@Override
		public void onEvent(BluetoothDevice data) {
			if(killInProgress == true){
				abortKill();
			}
			
		}
		
	}
	
	private class ScanFinishedHandler implements EventListener<Integer> {

		@Override
		public void onEvent(Integer data) {
			if(initialScanSuccess == false){
				abortKill();
				return;
			}
			if(finalScanSuccess == false){
				abortKill();
				return;
			}
			
		}
		
	}
	
	public void abortKill(){
		initialScanSuccess = false;
		killInProgress = false;
		finalScanSuccess = false;
		if(timer != null) timer.cancel();
		
		Log.d(Globals.DEBUG, "Kill failed!");
		Toast.makeText(getApplicationContext(), "Kill failed!", Toast.LENGTH_SHORT).show();
	}
	
	public void killSucessful(){
		Context context = getApplicationContext();
		CharSequence text = "Kill succesful!";
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
	@Override
	protected void onStop(){
		// TODO: Clean up
		// Maybe we should be in onPause
		// Means that we should set up stuff in onResume
		// Specifically, the bluetooth scanner should stop listening for broadcasts
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		switch(requestCode){
			case REQUEST_ENABLE_BT:
				
				break;
			case REQUEST_ENABLE_DISCOVERABILITY:
				
				break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_game, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	
}
