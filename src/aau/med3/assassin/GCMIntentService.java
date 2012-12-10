package aau.med3.assassin;

import com.google.android.gcm.GCMBaseIntentService;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

// TODO: Implement these methods to provide GCM communication
public class GCMIntentService extends GCMBaseIntentService {

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void onError(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onRegistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

}
