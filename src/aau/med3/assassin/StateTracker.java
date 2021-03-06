package aau.med3.assassin;

import aau.med3.assassin.events.EventDispatcher;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class StateTracker extends EventDispatcher {
	
	private Context ctx;
	
	public StateTracker(Context ctx){
		this.ctx = ctx;
	}
	
	public Boolean isBTEnabled(){
		BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
		if(bta != null){
			if(bta.isEnabled()){
				return true;
			}
		}
		return false;
	}
	
	public Boolean isBTDiscoverable(){
		BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
		if(bta != null){
			if(bta.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
				return true;
			}
		}
		return false;
	}
	
	public Boolean isNetworkConnected(){
		ConnectivityManager manager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo network = manager.getActiveNetworkInfo();
		if(network != null){
			if(network.isConnected()){
				return true;
			}
		}
		return false;
		
	}
	
	public Boolean isUserActive(){
		User user = Globals.user;
		if(user != null && user.loggedIn && user.alive)
			return true;
		
		return false;
	}
}
