package aau.med3.assassin.activities;

import java.util.Timer;
import java.util.TimerTask;

import aau.med3.assassin.AssassinService;
import aau.med3.assassin.BluetoothScanner;
import aau.med3.assassin.Globals;
import aau.med3.assassin.R;
import aau.med3.assassin.events.EventHandler;
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
	
	private View statusView;
	private View actionView;
	
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int REQUEST_ENABLE_DISCOVERABILITY = 2;
	public final static String TAG = "GAME_ACTIVITY";
	

	private final long KILL_DURATION = 8000;
	
	private Timer timer;
		
	private BluetoothAdapter bta;
	private BluetoothScanner scanner;
	
	private class KillTask extends TimerTask {

		@Override
		public void run() {
			scanner.onDeviceDisconnected = null;
			scanner.onDeviceFound = new FinalDeviceFoundHandler();
			scanner.onScanFinished = new UnsuccesfulScanHandler();
			scanner.scan();
			Log.d(TAG, String.valueOf(KILL_DURATION) + " ms passed");
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		// Show the Up button in the action bar.
		setupActionBar();
		
		statusView = findViewById(R.id.kill_status);
		actionView = findViewById(R.id.kill_layout);
		
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
			
			
			
		} else {
			// Device does not support bluetooth
			Log.d(TAG, "Device does not support bluetooth");
		}

	}
	
	@Override
	protected void onResume(){
		super.onResume();

//		scanner.register();
	}
	
	@Override
	protected void onPause(){
		super.onPause();

//		scanner.unregister();
	}
	
	private void initKill(){
		
		scanner.onDeviceFound = new InitialDeviceFoundHandler();
		scanner.onScanFinished = new UnsuccesfulScanHandler();
		
		scanner.scan();
		
		statusView.setVisibility(View.VISIBLE);
		actionView.setVisibility(View.GONE);
	}
	
	public void btn_kill_onclick(View view){
//		if(Globals.featureDependencies.isAllEnabled() == false){
//			Intent intent = new Intent(this, FeaturesActivity.class);
//			startActivity(intent);
//		}
		
		scanner.register();
		if(Globals.assassinService.scanner.isScanning()){
			
			scanner.onScanFinished = new EventHandler<Integer>() {
				@Override
				public void onEvent(Integer data) {
					initKill();
					
				}				
			};
			Globals.assassinService.stopScanning();
		} else {
			Globals.assassinService.stopScanning();
			initKill();
		}
		

	}
		
	private class InitialDeviceFoundHandler implements EventHandler<BluetoothDevice>{

		@Override
		public void onEvent(BluetoothDevice device) {
			String deviceMAC = device.getAddress();
			if(deviceMAC.equals(Globals.user.target_MAC)){
				Log.d(TAG, "GameActivity found target. Kill should be initialized");
				
				scanner.onDeviceFound = null;
				scanner.onScanFinished = new InitialSuccesfulScanHandler();
				scanner.stopScan();
			}
			
		}
		
	}
	
	private class InitialSuccesfulScanHandler implements EventHandler<Integer>{

		@Override
		public void onEvent(Integer data) {
			scanner.onDeviceDisconnected = new DeviceDisconnectedHandler();
			timer = new Timer("KillTimer");
			timer.schedule(new KillTask(), KILL_DURATION);
		}
		
	}
	
	private class UnsuccesfulScanHandler implements EventHandler<Integer>{

		@Override
		public void onEvent(Integer data) {
			abortKill();
			
		}
		
	}
	
	private class FinalDeviceFoundHandler implements EventHandler<BluetoothDevice>{

		@Override
		public void onEvent(BluetoothDevice device) {
			String deviceMAC = device.getAddress();
			if(deviceMAC.equals(Globals.user.target_MAC)){
				Log.d(TAG, "GameActivity found target. Kill should be initialized");
				
				scanner.onDeviceFound = null;
				scanner.onScanFinished = new FinalSuccesfulScanHandler();
				scanner.stopScan();
			}
			
		}
		
	}
	
	private class FinalSuccesfulScanHandler implements EventHandler<Integer>{

		@Override
		public void onEvent(Integer data) {
			killSucessful();
		}
		
	}
	
	private class DeviceDisconnectedHandler implements EventHandler<BluetoothDevice> {

		@Override
		public void onEvent(BluetoothDevice device) {
			
			String deviceMAC = device.getAddress();
			if(deviceMAC.equals(Globals.user.target_MAC)){
				
				Log.d(TAG, "Target device disconnected during kill process");
				abortKill();
				
			}
			
			
		}
		
	}
	
	
	public void abortKill(){
		scanner.unregister();

		if(timer != null){
			timer.cancel();
			timer.purge();
			
			timer = null;
		}
		
		Log.d(TAG, "Kill failed!");
		Thread.dumpStack();
		
		if(Globals.assassinService != null){
			AssassinService service = Globals.assassinService;
			service.startScanning();
			
		}
		
		statusView.setVisibility(View.GONE);
		actionView.setVisibility(View.VISIBLE);
		Toast.makeText(getApplicationContext(), "Kill failed!", Toast.LENGTH_SHORT).show();
	}
	
	public void killSucessful(){

		Globals.user.kill(Globals.user.target_MAC);
		scanner.unregister();

		if(timer != null){
			timer.cancel();
			timer.purge();
			
			timer = null;
		}
		
		Log.d(TAG, "Kill succesful!");
		
		Context context = getApplicationContext();
		CharSequence text = "Kill succesful!";
		
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
		statusView.setVisibility(View.GONE);
		actionView.setVisibility(View.VISIBLE);
		
		if(Globals.assassinService != null){
			AssassinService service = Globals.assassinService;
			service.startScanning();
			
		}
	}
	
	@Override
	protected void onStop(){
		super.onStop();
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
