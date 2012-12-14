package aau.med3.assassin;


import aau.med3.assassin.events.BluetoothEvent;
import aau.med3.assassin.events.Event;
import aau.med3.assassin.events.EventDispatcher;
import aau.med3.assassin.events.EventListener;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

public class KillAction extends EventDispatcher {
	
	// Events
	public final static String 	BOOTSTRAP_FAILED = "killBootstrapFailed",
									TARGET_NOT_FOUND = "targetNotFound",
									SUCCESS = "killSuccess",
									FINISHED = "killFinished";
	
	public final static String TAG = "KillAction";
	
	private Context ctx;
	private String targetMAC;
	private BluetoothClient btClient;
	private AssassinService service;
	
	public KillAction(Context ctx, String targetMAC){
		this.ctx = ctx;
		this.targetMAC = targetMAC;
		this.btClient = new BluetoothClient(ctx, BluetoothAdapter.getDefaultAdapter());
		
		service = Globals.assassinService;
	}
	
	public Boolean bootstrap(){
		StateTracker sm = new StateTracker(ctx);
		Boolean allGood = true;
		
		if(!sm.isBTDiscoverable())
			allGood = false;
		if(!sm.isNetworkConnected())
			allGood = false;
		if(Globals.user == null || Globals.user.loggedIn == false || Globals.user.alive == false)
			allGood = false;
		if(service == null || service.running == false)
			allGood = false;
		
		return allGood;
	}
	
	public void pauseService(){
		
	}
	
	public void initKill(){
		Log.d(TAG, "Attempting to kill");
		btClient.register();
		btClient.addEventListener(BluetoothEvent.DISCOVERY_FINISHED, new TargetNotFoundHandler());
		
		btClient.addEventListener(BluetoothEvent.DEVICE_FOUND, new EventListener() {
			
			@Override
			public void handle(Event e) {
				BluetoothDevice device = (BluetoothDevice) e.data;
				if(device.getAddress().equals(targetMAC)){
					Log.d(TAG, "Target found!");
					btClient.removeEventListener(BluetoothEvent.DISCOVERY_FINISHED, new TargetNotFoundHandler());
					btClient.addEventListener(BluetoothEvent.DISCOVERY_CANCELED, new TargetFoundHandler());
					btClient.cancel();
				}
				
			}
		});
		
		
		
		btClient.scan();
	}
	
	public void finish(String result){
		btClient.unregister();
		dispatchEvent(FINISHED, result);
	}
	
	private class TargetFoundHandler implements EventListener {

		@Override
		public void handle(Event e) {
			finish(SUCCESS);
			
		}
		
	}
	
	private class TargetNotFoundHandler implements EventListener {

		@Override
		public void handle(Event e) {
			finish(TARGET_NOT_FOUND);
			
		}
		
	}
}
